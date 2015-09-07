package storage.mongostoragemodel;

/**
 * Created by Joe on 9/7/2015.
 */
public class User {
    private String _id;
    private String hashedPassword;
    private String firstName;
    private String lastName;

    public User(String _id, String hashedPassword, String firstName, String lastName) {
        this._id = _id;
        this.hashedPassword = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String get_id() {
        return _id;
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
}
