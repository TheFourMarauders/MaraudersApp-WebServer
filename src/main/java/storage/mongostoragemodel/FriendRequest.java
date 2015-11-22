package storage.mongostoragemodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import util.TimeStamp;

import java.time.ZonedDateTime;

/**
 * A representation of a FriendRequest corresponding to storage schema
 *
 * @author Joe and Matt
 * @version 1.0
 */
public class FriendRequest {
    private String senderUsername;
    private ZonedDateTime timeStamp;

    @JsonCreator
    public FriendRequest(
            @JsonProperty("senderUsername") String senderUsername,
            @JsonProperty("timeStamp") ZonedDateTime timeStamp) {
        this.senderUsername = senderUsername;
        this.timeStamp = timeStamp;
    }

    public FriendRequest(String senderUsername) {
        this(senderUsername, TimeStamp.getCurrentTimeUTC());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof FriendRequest)) return false;

        FriendRequest that = (FriendRequest) o;

        return !(senderUsername != null ? !senderUsername.equals(that.senderUsername) : that.senderUsername != null);

    }

    @Override
    public int hashCode() {
        return senderUsername != null ? senderUsername.hashCode() : 0;
    }

    public String getSenderUsername() {
        return senderUsername;
    }
}
