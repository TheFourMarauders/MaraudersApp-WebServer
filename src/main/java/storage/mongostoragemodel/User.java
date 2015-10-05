package storage.mongostoragemodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import storage.LocationInfo;
import storage.UserInfo;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Joe on 9/7/2015.
 */
public class User {
    private String _id;
    private String hashedPassword;
    private String firstName;
    private String lastName;

    @JsonProperty("locationHistory")
    private SortedSet<Location> locationHistory;

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

        this.locationHistory = new TreeSet<>();
        this.friends = new HashSet<>();
        this.friendRequests = new HashSet<>();
    }

    public List<LocationInfo> getLocationHistory(ZonedDateTime start, ZonedDateTime end) {
        List<LocationInfo> list = locationHistory.stream()
                .filter(l -> l.getTime().isBefore(end) && l.getTime().isAfter(start))
                .map(l -> new LocationInfo(l))
                .collect(Collectors.toCollection(ArrayList<LocationInfo>::new));
        Collections.sort(list);
        return list;
    }

    public void addLocations(List<LocationInfo> locations) {
        locations.stream().forEach(l -> locationHistory.add(new Location(l)));
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

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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
