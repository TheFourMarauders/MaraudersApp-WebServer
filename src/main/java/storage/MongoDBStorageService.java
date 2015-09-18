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
import controller.ServiceController;
import storage.mongostoragemodel.FriendRequest;
import storage.mongostoragemodel.User;
import util.TimeStamp;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
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

    public MongoDBStorageService(DatabaseConfig dbConfig, AuthConfig authConfig){
        this.dbConfig = dbConfig;
        this.authConfig = authConfig;
        mongoClient = new MongoClient(dbConfig.getURL());
        database = mongoClient.getDatabase(dbConfig.getDbName());
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
        ObjectMapper mapper = new ObjectMapper();
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
        return u.getFriends();
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

        acceptor.addFriend(sender);
        sender.addFriend(acceptor);

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

        remover.removeFriend(removee);
        removee.removeFriend(remover);

        updateUser(remover);
        updateUser(removee);
    }

    private User getUserFromDB(String username) throws HTTPException {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document userDoc = coll.find(eq("_id", username)).first();
        if (userDoc == null) {
            throw new NoSuchUserException("User not found");
        }

        ObjectMapper mapper = new ObjectMapper();
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
        ObjectMapper mapper = new ObjectMapper();
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
