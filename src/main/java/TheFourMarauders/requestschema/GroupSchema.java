package TheFourMarauders.requestschema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by joe on 10/5/15.
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
}
