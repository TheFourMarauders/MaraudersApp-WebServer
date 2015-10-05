package storage;

import controller.AuthConfig;
import controller.HTTPException;
import storage.datatypes.GroupInfo;
import storage.datatypes.LocationInfo;
import storage.datatypes.UserInfo;
import storage.mongostoragemodel.FriendRequest;
import storage.mongostoragemodel.User;
import util.TimeStamp;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Created by Joe on 9/16/2015.
 */
public class MemoryStorageService implements StorageService {

    private Map<String, User> users;
    private AuthConfig authConfig;

    public MemoryStorageService(AuthConfig authConfig) {
        this.authConfig = authConfig;
        users = new HashMap<>();
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
        User sender = users.get(senderUsername);
        User target = users.get(targetUsername);

        if (sender == null || target == null) {
            throw new NoSuchUserException((sender==null) ? senderUsername : targetUsername);
        }

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
    public boolean areUsersFriends(String username1, String username2) throws StorageException {
        User user1 = users.get(username1);
        return user1.isFriendsWith(username2);
    }

    @Override
    public Set<UserInfo> getFriendRequestsFor(String username) throws HTTPException {
        User user = users.get(username);
        if (user == null) {
            throw new NoSuchUserException(username);
        }
        Set<UserInfo> requests = new HashSet<UserInfo>();

        for (FriendRequest fr : user.getFriendRequests()) {
            try {
                User requester = users.get(fr.getSenderUsername());
                if (requester == null) throw new NoSuchUserException(fr.getSenderUsername());
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
        User u = users.get(username);
        if (u == null) throw new NoSuchUserException(username);
        Set<String> friendIds = u.getFriends();
        Set<UserInfo> infos = new HashSet<>(friendIds.size());
        for (String uid : friendIds) {
            User friend = users.get(uid);
            infos.add(new UserInfo(friend));
        }
        return infos;
    }

    @Override
    public void createFriendship(String frAcceptor, String frSender) throws HTTPException {
        User acceptor = users.get(frAcceptor);
        if (acceptor == null) throw new NoSuchUserException(frAcceptor);
        User sender = null;
        try {
            sender = users.get(frSender);
            if (sender == null) throw new NoSuchUserException(frSender);
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
        User remover = users.get(removerUsername);
        if (remover == null) throw new NoSuchUserException(removerUsername);
        User removee = null;
        try {
            removee = users.get(removeeUsername);
            if (removee == null) throw new NoSuchUserException(removeeUsername);
        } catch (NoSuchUserException e) {
            return; // muahaha
        }

        remover.removeFriend(removeeUsername);
        removee.removeFriend(removerUsername);
    }

    @Override
    public void addLocationsToUser(String username, List<LocationInfo> locations) throws HTTPException {

    }

    @Override
    public List<LocationInfo> getLocationsForUser(String username, ZonedDateTime before, ZonedDateTime after) throws HTTPException {
        return null;
    }

    @Override
    public String createGroup(String username, String groupName) throws HTTPException {
        return null;
    }

    @Override
    public Set<GroupInfo> getGroupsForUser(String username) throws HTTPException {
        return null;
    }

    @Override
    public GroupInfo getGroupById(String id) throws HTTPException {
        return null;
    }

    @Override
    public boolean isUserInGroup(String username, String groupId) throws HTTPException {
        return false;
    }

    @Override
    public void addUserToGroup(String username, String groupId) throws HTTPException {

    }

    @Override
    public void removeUserFromGroup(String username, String groupId) throws HTTPException {

    }
}
