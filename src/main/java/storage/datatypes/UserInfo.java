package storage.datatypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import storage.mongostoragemodel.User;

/**
 * An inter-service representation of a User
 *
 * @author Joe and Matt
 * @version 1.0
 */
public class UserInfo {

    private String username, firstName, lastName;

    public UserInfo( String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserInfo(User u) {
        this(u.get_id(), u.getFirstName(), u.getLastName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        return !(username != null ? !username.equals(userInfo.username) : userInfo.username != null);

    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
