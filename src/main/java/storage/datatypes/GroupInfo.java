package storage.datatypes;

import storage.mongostoragemodel.Group;

import java.util.Set;

/**
 * Created by joe on 10/5/15.
 */
public class GroupInfo {

    private String groupId;
    private String groupName;
    private Set<String> members;

    public GroupInfo(String groupId, String groupName, Set<String> members) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.members = members;
    }

    public GroupInfo(Group g) {
        this(g.get_id(), g.getName(), g.getMembers());
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public Set<String> getMembers() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupInfo groupInfo = (GroupInfo) o;

        return groupId.equals(groupInfo.groupId);

    }

    @Override
    public int hashCode() {
        return groupId.hashCode();
    }
}
