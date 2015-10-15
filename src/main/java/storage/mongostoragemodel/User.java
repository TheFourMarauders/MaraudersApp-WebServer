package storage.mongostoragemodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import controller.HTTPException;
import storage.datatypes.LocationInfo;
import storage.datatypes.UserInfo;

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
    private Set<String> friends;

    @JsonProperty("friendRequest")
    private Set<FriendRequest> friendRequests;

    @JsonProperty("groups")
    private Set<String> groupIds;

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
        this.groupIds = new HashSet<>();
    }

    public List<LocationInfo> getLocationHistory(ZonedDateTime start, ZonedDateTime end) throws HTTPException {
        List<LocationInfo> infos = new ArrayList<>(locationHistory.size());
        for (Location l : locationHistory) {
            infos.add(new LocationInfo(l));
        }
        List<LocationInfo> list = infos.stream()
                .filter(l -> l.getTime().isBefore(end) && l.getTime().isAfter(start))
                .collect(Collectors.toCollection(ArrayList<LocationInfo>::new));
        Collections.sort(list);
        return list;
    }

    public void addGroup(String groupId) {
        groupIds.add(groupId);
    }

    public void removeGroup(String groupId) {
        groupIds.remove(groupId);
    }

    public Set<String> getGroupIds() {
        return new HashSet<>(groupIds);
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

    public Set<String> getFriends() {
        return new HashSet<String>(friends);
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

    public void addFriend(String u) {
        friends.add(u);
    }

    public void removeFriend(String u) {
        friends.remove(u);
    }

    public boolean isFriendsWith(String u) {
        return friends.contains(u);
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
