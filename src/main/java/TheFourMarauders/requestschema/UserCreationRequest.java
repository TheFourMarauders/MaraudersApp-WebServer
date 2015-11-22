package TheFourMarauders.requestschema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class representing everything needed for creating a user
 *  @author Matt and Joe
 */
public class UserCreationRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    @JsonCreator
    public UserCreationRequest (
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }


    @Override
    public String toString() {
        return "UserCreationRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof UserCreationRequest)) return false;

        UserCreationRequest user = (UserCreationRequest) o;

        return user.username.equals(username);
    }

    @Override
    public int hashCode() {
        int result = getUsername().hashCode();
        return result;
    }
}
