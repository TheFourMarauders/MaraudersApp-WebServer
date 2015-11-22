package storage.mongostoragemodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * A representation of Group corresponding to a storage schema
 *
 * @author Joe and Matt
 * @version 1.0
 */
public class Group {
    private String _id;

    private String name;

    @JsonProperty("members")
    private Set<String> members;

    @JsonCreator
    public Group(@JsonProperty("_id") String _id, @JsonProperty("name") String name) {
        this._id = _id;
        this.name = name;
        members = new HashSet<>();
    }

    public boolean addMember(String uid) {
        return members.add(uid);
    }

    public void removeMember(String uid) {
        members.remove(uid);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getMembers() {
        return new HashSet<>(members);
    }
}
