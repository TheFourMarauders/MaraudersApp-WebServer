package storage.mongostoragemodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import storage.UserInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Joe on 9/7/2015.
 */
public class User {
    private String _id;
    private String hashedPassword;
    private String firstName;
    private String lastName;

    @JsonProperty("friends")
    private Set<UserInfo> friends;

    @JsonProperty("friendRequest")
    private Set<FriendRequest> friendRequests;

    @JsonCreator
    public User(
            @JsonProperty("_id") String _id,
            @JsonProperty("hashedPassword") String hashedPassword,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName) {
        this._id = _id;
        this.hashedPassword = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;

        this.friends = new HashSet<>();
        this.friendRequests = new HashSet<>();
    }

    public String get_id() {
        return _id;
    }

    public Set<FriendRequest> getFriendRequests() {
        return new HashSet<FriendRequest>(friendRequests);
    }

    public Set<UserInfo> getFriends() {
        return new HashSet<UserInfo>(friends);
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void addFriend(User u) {
        friends.add(new UserInfo(u.get_id(), u.getFirstName(), u.getLastName()));
    }

    public void removeFriend(User u) {
        friends.remove(new UserInfo(u.get_id(), u.getFirstName(), u.getLastName()));
    }

    public boolean isFriendsWith(User u) {
        return friends.contains(u);
    }

    public boolean isFriendsWith(String username) {
        return isFriendsWith(new User(username, "", "", ""));
    }

    public void addFriendRequest(FriendRequest fr) {
        if (!(friendRequests.contains(fr))) {
            friendRequests.add(fr);
        }
    }

    public void removeFriendRequest(FriendRequest fr) {
        friendRequests.remove(fr);
    }

    public void removeFriendRequest(String frSender) {
        friendRequests.remove(new FriendRequest(frSender, null));
    }

    @Override
    public String toString() {
        return "User{" +
                "_id='" + _id + '\'' +
                ", hashedPassword='" + hashedPassword + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", friends=" + friends +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;

        User user = (User) o;

        return user._id.equals(_id);
    }

    @Override
    public int hashCode() {
        int result = get_id().hashCode();
        return result;
    }
}
