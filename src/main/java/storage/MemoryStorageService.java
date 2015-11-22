package storage;

import controller.AuthConfig;
import controller.HTTPException;
import storage.datatypes.GroupInfo;
import storage.datatypes.LocationInfo;
import storage.datatypes.UserInfo;
import storage.mongostoragemodel.FriendRequest;
import storage.mongostoragemodel.Group;
import storage.mongostoragemodel.User;
import util.GUIDGenerator;
import util.TimeStamp;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Implementation of StorageService that simply uses datastructures in memory, does not persist, use at risk
 * @author Matt & Jojo
 **/
public class MemoryStorageService implements StorageService {

    private Map<String, User> users;
    private Map<String, Group> groups;

    private AuthConfig authConfig;

    /**
     * constructor that takes in an authorization config
     * @param authConfig
     */
    public MemoryStorageService(AuthConfig authConfig) {
        this.authConfig = authConfig;
        users = new HashMap<>();
        groups = new HashMap<>();
    }

    @Override
    public byte[] getHashedPassword(String username) throws HTTPException {
        User u = users.get(username);
        if (u == null) {
            throw new NoSuchUserException(username);
        }
        return Base64.getDecoder().decode(u.getHashedPassword());
    }

    @Override
    public void createUser(String username, String password, String firstName, String lastName) throws HTTPException {
        if (users.get(username) != null) throw new UserAlreadyExistsException();
        String encodedHashPass = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(authConfig.getHashAlgo());
            byte[] hashedPassword = digest.digest(password.getBytes(authConfig.getEncoding()));
            encodedHashPass = new String(Base64.getEncoder().encode(hashedPassword), "UTF8");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new HTTPException("Critical server error :(", 500);
        }
        User u = new User(username, encodedHashPass, firstName, lastName);
        users.put(username, u);
    }

    @Override
    public void insertFriendRequest(String senderUsername, String targetUsername) throws HTTPException {
        User sender = getUser(senderUsername);
        User target = getUser(targetUsername);

        if (sender.getFriendRequests().contains(new FriendRequest(targetUsername))) {
            createFriendship(senderUsername, targetUsername);
            throw new HTTPException("You already have a friend request from: " + targetUsername
                    + " We have added them as a friend for you :)", 201);
        } else if (target.getFriendRequests().contains(new FriendRequest(senderUsername))) {
            throw new HTTPException("I'm a teapot, and you've already sent a request to this person. Be patient :)", 409);
        }

        FriendRequest fr = new FriendRequest(senderUsername, TimeStamp.getCurrentTimeUTC());
        target.addFriendRequest(fr);
    }

    @Override
    public boolean areUsersFriends(String username1, String username2) throws HTTPException {
        User user1 = getUser(username1);
        return user1.isFriendsWith(username2);
    }

    @Override
    public Set<UserInfo> getFriendRequestsFor(String username) throws HTTPException {
        User user = getUser(username);
        Set<UserInfo> requests = new HashSet<UserInfo>();

        for (FriendRequest fr : user.getFriendRequests()) {
            try {
                User requester = getUser(fr.getSenderUsername());
                UserInfo requesterInfo =
                        new UserInfo(requester.get_id(), requester.getFirstName(), requester.getLastName());
                requests.add(requesterInfo);
            } catch (NoSuchUserException e) {
                user.removeFriendRequest(fr);
            }
        }

        return requests;
    }

    @Override
    public Set<UserInfo> getFriendsFor(String username) throws HTTPException {
        User u = getUser(username);
        Set<String> friendIds = u.getFriends();
        Set<UserInfo> infos = new HashSet<>(friendIds.size());
        for (String uid : friendIds) {
            User friend = getUser(uid);
            infos.add(new UserInfo(friend));
        }
        return infos;
    }

    @Override
    public void createFriendship(String frAcceptor, String frSender) throws HTTPException {
        User acceptor = getUser(frAcceptor);
        User sender = null;
        try {
            sender = getUser(frSender);
        } catch (NoSuchUserException e) {
            return;
        }

        if (!(acceptor.getFriendRequests().contains(new FriendRequest(frSender, null)))) {
            throw new FriendshipException("No outstanding friend request for users: " + frAcceptor + "<--" + frSender);
        }
        acceptor.removeFriendRequest(frSender);

        acceptor.addFriend(frSender);
        sender.addFriend(frAcceptor);
    }

    @Override
    public void removeFriend(String removerUsername, String removeeUsername) throws HTTPException {
        User remover = getUser(removerUsername);
        User removee = null;
        try {
            removee = getUser(removeeUsername);
        } catch (NoSuchUserException e) {
            return; // muahaha
        }

        remover.removeFriend(removeeUsername);
        removee.removeFriend(removerUsername);
    }

    @Override
    public void addLocationsToUser(String username, List<LocationInfo> locations) throws HTTPException {
        User u = getUser(username);
        u.addLocations(locations);
    }

    @Override
    public List<LocationInfo> getLocationsForUser(String username, ZonedDateTime before, ZonedDateTime after) throws HTTPException {
        User u = getUser(username);
        return u.getLocationHistory(before, after);
    }

    @Override
    public String createGroup(String username, String groupName) throws HTTPException {
        User u = getUser(username);

        String id = GUIDGenerator.generateGUID();
        Group g = new Group(id, groupName);
        g.addMember(username);
        u.addGroup(id);
        groups.put(id, g);
        return id;
    }

    @Override
    public Set<GroupInfo> getGroupsForUser(String username) throws HTTPException {
        User u = getUser(username);
        Set<String> groupIds = u.getGroupIds();
        Set<GroupInfo> groups = new HashSet<>(groupIds.size());
        for (String id : groupIds) {
            groups.add(new GroupInfo(getGroup(id)));
        }
        return groups;
    }

    @Override
    public GroupInfo getGroupById(String id) throws HTTPException {
        return new GroupInfo(getGroup(id));
    }

    @Override
    public boolean isUserInGroup(String username, String groupId) throws HTTPException {
        return getGroup(groupId).getMembers().contains(username);
    }

    @Override
    public void addUserToGroup(String username, String groupId) throws HTTPException {
        Group g = getGroup(groupId);
        g.addMember(username);
    }

    @Override
    public void removeUserFromGroup(String username, String groupId) throws HTTPException {
        Group g = getGroup(groupId);
        g.removeMember(username);
    }

    private User getUser(String username) throws NoSuchUserException {
        User u = users.get(username);
        if (u == null) throw new NoSuchUserException(username);
        return u;
    }

    private Group getGroup(String id) throws HTTPException {
        Group g = groups.get(id);
        if (g == null) throw new HTTPException("Bad group_id: " + id, 400);
        return g;
    }
}
