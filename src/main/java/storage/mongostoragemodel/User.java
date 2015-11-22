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
 * A representation of a User corresponding to a storage schema
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

    /**
     *
     * @param start the time of the oldest location desired
     * @param end   the time of the newest location desired
     * @return  the locations of this user between start and end, sorted chronologically
     * @throws HTTPException
     */
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

    /**
     * Give this user reference to Group by groupId. This is part of a two-step
     * process of adding user to a group. The group must also have reference
     * to this user.
     *
     * @param groupId the id of the group this user is being added to
     */
    public void addGroup(String groupId) {
        groupIds.add(groupId);
    }

    /**
     * Remove this user's reference to group
     *
     * @param groupId   the group user is being removed from
     */
    public void removeGroup(String groupId) {
        groupIds.remove(groupId);
    }

    /**
     * Get the set of groupIds that this user has reference to (user is in)
     *
     * @return
     */
    public Set<String> getGroupIds() {
        return new HashSet<>(groupIds);
    }

    /**
     * Associate some new locations with this user
     * @param locations
     */
    public void addLocations(List<LocationInfo> locations) {
        locations.stream().forEach(l -> locationHistory.add(new Location(l)));
    }

    /**
     *
     * @return the unique identifier of this user, which is its username
     */
    public String get_id() {
        return _id;
    }

    /**
     *
     * @return  the outstanding friend requests sent to this user
     */
    public Set<FriendRequest> getFriendRequests() {
        return new HashSet<FriendRequest>(friendRequests);
    }

    /**
     *
     * @return  the usernames of this user's friends
     */
    public Set<String> getFriends() {
        return new HashSet<String>(friends);
    }

    /**
     *
     * @return  The stored hash of this user's password
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    /**
     * Give this user reference to a new friend's username
     *
     * @param u the username of the friend we're adding
     */
    public void addFriend(String u) {
        friends.add(u);
    }

    /**
     *
     * @param u the username of the friend to remove
     */
    public void removeFriend(String u) {
        friends.remove(u);
    }

    /**
     *
     * @param u the username whose friendship we are unsure about
     * @return  whether or not this user is friends with u
     */
    public boolean isFriendsWith(String u) {
        return friends.contains(u);
    }

    /**
     * Puts a new friend request in this user's collection
     * @param fr    the new friend request
     */
    public void addFriendRequest(FriendRequest fr) {
        if (!(friendRequests.contains(fr))) {
            friendRequests.add(fr);
        }
    }

    /**
     * Delete a friend request, either if user requests it deleted, or accepts it
     *
     * @param fr    the friend request to remove
     */
    public void removeFriendRequest(FriendRequest fr) {
        friendRequests.remove(fr);
    }

    /**
     * Delete a friend request by username instead of FriendRequest object
     *
     * @param frSender  the username of the sender of the request deleting
     */
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
