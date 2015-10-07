package storage;

import static com.mongodb.client.model.Filters.eq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import controller.AuthConfig;
import controller.HTTPException;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import controller.DatabaseConfig;
import storage.datatypes.GroupInfo;
import storage.datatypes.LocationInfo;
import storage.datatypes.UserInfo;
import storage.mongostoragemodel.FriendRequest;
import storage.mongostoragemodel.Group;
import storage.mongostoragemodel.User;
import util.GUIDGenerator;
import util.TimeStamp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by Matthew on 9/7/2015.
 */
public class MongoDBStorageService implements StorageService{
    private MongoClient mongoClient;
    private MongoDatabase database;

    private DatabaseConfig dbConfig;
    private AuthConfig authConfig;

    private static final String USER_COLLECTION = "users";
    private static final String GROUP_COLLECTION = "groups";

    private ObjectMapper mapper;

    public MongoDBStorageService(DatabaseConfig dbConfig, AuthConfig authConfig){
        this.dbConfig = dbConfig;
        this.authConfig = authConfig;
        mongoClient = new MongoClient(dbConfig.getURL());
        database = mongoClient.getDatabase(dbConfig.getDbName());
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
    }

    @Override
    public byte[] getHashedPassword(String username) {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document user = coll.find(eq("_id", username)).first();
        if (user == null) {
            return null;
        }

        return Base64.getDecoder().decode(user.getString("hashedPassword"));
    }

    @Override
    public synchronized void createUser(String username, String password, String firstName, String lastName)
            throws StorageException, HTTPException {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document user = coll.find(eq("_id", username)).first();
        if (user != null) {
            throw new UserAlreadyExistsException();
        }

        String encodedHashPass = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(authConfig.getHashAlgo());
            byte[] hashedPassword = digest.digest(password.getBytes(authConfig.getEncoding()));
            encodedHashPass = new String(Base64.getEncoder().encode(hashedPassword), "UTF8");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new HTTPException("Critical server error :(", 500);
        }

        User u = new User(username, encodedHashPass, firstName, lastName);
        String userJson = null;
        try {
            userJson = mapper.writeValueAsString(u);
        } catch (JsonProcessingException e) {
            throw new StorageException("Bad User serialization\n" + e.getMessage());
        }

        Document doc = Document.parse(userJson);
        try {
            coll.insertOne(doc);
        } catch (MongoException e) {
            throw new StorageException("Unable to insert into database \n" + e.getMessage());
        }
    }

    @Override
    public void insertFriendRequest(String senderUsername, String targetUsername) throws HTTPException {
        User sender = getUserFromDB(senderUsername);
        User target = getUserFromDB(targetUsername);

        if (sender.getFriendRequests().contains(new FriendRequest(targetUsername))) {
            createFriendship(senderUsername, targetUsername);
            throw new HTTPException("You already have a friend request from: " + targetUsername
                    + " We have added them as a friend for you :)", 201);
        } else if (target.getFriendRequests().contains(new FriendRequest(senderUsername))) {
            throw new HTTPException("I'm a teapot, and you've already sent a request to this person. Be patient :)", 409);
        }

        FriendRequest fr = new FriendRequest(senderUsername, TimeStamp.getCurrentTimeUTC());
        target.addFriendRequest(fr);

        updateUser(target);
    }

    @Override
    public boolean areUsersFriends(String username1, String username2) throws HTTPException {
        User user1 = getUserFromDB(username1);
        return user1.isFriendsWith(username2);
    }

    @Override
    public Set<UserInfo> getFriendRequestsFor(String username) throws HTTPException {
        User user = getUserFromDB(username);
        Set<UserInfo> requests = new HashSet<UserInfo>();
        boolean dirty = false;

        for (FriendRequest fr : user.getFriendRequests()) {
            try {
                User requester = getUserFromDB(fr.getSenderUsername());
                UserInfo requesterInfo =
                        new UserInfo(requester.get_id(), requester.getFirstName(), requester.getLastName());
                requests.add(requesterInfo);
            } catch (NoSuchUserException e) {
                user.removeFriendRequest(fr);
                dirty = true;
            }
        }
        if (dirty) {
            updateUser(user);
        }

        return requests;
    }

    @Override
    public Set<UserInfo> getFriendsFor(String username) throws HTTPException {
        User u = getUserFromDB(username);
        Set<String> friendIds = u.getFriends();
        Set<UserInfo> infos = new HashSet<>(friendIds.size());
        for (String uid : friendIds) {
            User friend = getUserFromDB(uid);
            infos.add(new UserInfo(friend));
        }
        return infos;
    }

    @Override
    public void createFriendship(String frAcceptor, String frSender) throws HTTPException {
        User acceptor = getUserFromDB(frAcceptor);
        User sender = null;
        sender = getUserFromDB(frSender);

        if (!(acceptor.getFriendRequests().contains(new FriendRequest(frSender, null)))) {
            throw new FriendshipException("No outstanding friend request for users: " + frAcceptor + "<--" + frSender);
        }
        acceptor.removeFriendRequest(frSender);

        acceptor.addFriend(frSender);
        sender.addFriend(frAcceptor);

        updateUser(acceptor);
        updateUser(sender);
    }

    @Override
    public void removeFriend(String removerUsername, String removeeUsername) throws HTTPException {
        User remover = getUserFromDB(removerUsername);
        User removee = null;
        try {
            removee = getUserFromDB(removeeUsername);
        } catch (NoSuchUserException e) {
            return; // muahaha
        }

        remover.removeFriend(removeeUsername);
        removee.removeFriend(removerUsername);

        updateUser(remover);
        updateUser(removee);
    }

    @Override
    public void addLocationsToUser(String username, List<LocationInfo> locations) throws HTTPException {
        User u = getUserFromDB(username);
        u.addLocations(locations);
        updateUser(u);
    }

    @Override
    public List<LocationInfo> getLocationsForUser(String username, ZonedDateTime start, ZonedDateTime end) throws HTTPException {
        User u = getUserFromDB(username);

        return u.getLocationHistory(start, end);
    }

    @Override
    public String createGroup(String username, String groupName) throws HTTPException {
        String groupId = GUIDGenerator.generateGUID();
        Group g = new Group(groupId, groupName);
        g.addMember(username);

        MongoCollection<Document> coll = database.getCollection(GROUP_COLLECTION);
        String json = null;
        try {
            json = mapper.writeValueAsString(g);
        } catch (JsonProcessingException e) {
            throw new StorageException("Bad Group serialization\n" + e.getMessage());
        }

        Document groupDoc = Document.parse(json);
        try {
            coll.insertOne(groupDoc);
        } catch (MongoException e) {
            throw new StorageException("Unable to add to database\n" + e.getMessage());
        }
        User u = getUserFromDB(username);
        u.addGroup(groupId);
        updateUser(u);
        return groupId;
    }

    @Override
    public Set<GroupInfo> getGroupsForUser(String username) throws HTTPException {
        User u = getUserFromDB(username);
        Set<String> groupIds = u.getGroupIds();
        Set<GroupInfo> groupInfos = new HashSet<>(groupIds.size());
        for (String id : groupIds) {
            Group g = getGroupFromDB(id);
            groupInfos.add(new GroupInfo(g));
        }
        return groupInfos;
    }

    @Override
    public GroupInfo getGroupById(String id) throws HTTPException {
        Group g = getGroupFromDB(id);
        return new GroupInfo(g);
    }

    @Override
    public boolean isUserInGroup(String username, String groupId) throws HTTPException {
        Group g = getGroupFromDB(groupId);
        return g.getMembers().contains(username);
    }

    @Override
    public void addUserToGroup(String username, String groupId) throws HTTPException {
        Group g = getGroupFromDB(groupId);
        g.addMember(username);

        User u = getUserFromDB(username);
        u.addGroup(groupId);

        updateUser(u);
        updateGroup(g);
    }

    @Override
    public void removeUserFromGroup(String username, String groupId) throws HTTPException {
        Group g = getGroupFromDB(groupId);
        if (!g.getMembers().contains(username)) {
            throw new HTTPException("User not in group", 200);
        }
        g.removeMember(username);

        User u = getUserFromDB(username);
        u.removeGroup(groupId);

        updateUser(u);
        updateGroup(g);
    }


    private Group getGroupFromDB(String id) throws HTTPException {
        MongoCollection<Document> coll = database.getCollection(GROUP_COLLECTION);
        Document groupDoc = coll.find(eq("_id", id)).first();
        if (groupDoc == null) {
            throw new HTTPException("No such group", 400);
        }
        Group group = null;
        try {
            group = mapper.readValue(groupDoc.toJson(), Group.class);
        } catch (IOException e) {
            throw new StorageException("Bad Group Serialization\n" + e.getMessage());
        }

        return group;
    }

    private void updateGroup(Group g) throws StorageException {
        MongoCollection<Document> coll = database.getCollection(GROUP_COLLECTION);
        String json = null;
        try {
            json = mapper.writeValueAsString(g);
        } catch (JsonProcessingException e) {
            throw new StorageException("Bad Group serialization\n" + e.getMessage());
        }

        Document updatedGroup = Document.parse(json);
        try {
            coll.replaceOne(eq("_id", g.get_id()), updatedGroup);
        } catch (MongoException e) {
            throw new StorageException("Unable to add to database\n" + e.getMessage());
        }
    }

    private User getUserFromDB(String username) throws HTTPException {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document userDoc = coll.find(eq("_id", username)).first();
        if (userDoc == null) {
            throw new NoSuchUserException("User not found");
        }
        User user = null;
        try {
            user = mapper.readValue(userDoc.toJson(), User.class);
        } catch (IOException e) {
            throw new StorageException("Bad User Serialization\n" + e.getMessage());
        }

        return user;
    }

    private void updateUser(User u) throws StorageException {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        String json = null;
        try {
            json = mapper.writeValueAsString(u);
        } catch (JsonProcessingException e) {
            throw new StorageException("Bad User serialization\n" + e.getMessage());
        }

        Document updatedUser = Document.parse(json);
        try {
            coll.replaceOne(eq("_id", u.get_id()), updatedUser);
        } catch (MongoException e) {
            throw new StorageException("Unable to add to database\n" + e.getMessage());
        }
    }
}
