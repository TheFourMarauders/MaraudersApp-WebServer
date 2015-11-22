package thefourmarauders.requestschema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Schema representing a group
 */
public class GroupSchema {
    private String _id;
    private String name;
    private Set<String> members;

    @JsonCreator
    public GroupSchema(@JsonProperty("_id") String _id,
                       @JsonProperty("name") String name,
                       @JsonProperty("members") Set<String> members) {
        this._id = _id;
        this.name = name;
        this.members = members;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupSchema)) return false;

        GroupSchema that = (GroupSchema) o;

        if (!get_id().equals(that.get_id())) return false;
        if (!getName().equals(that.getName())) return false;
        return getMembers().equals(that.getMembers());

    }

    @Override
    public int hashCode() {
        int result = get_id().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getMembers().hashCode();
        return result;
    }
}
