package storage.mongostoragemodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import util.TimeStamp;

import java.time.ZonedDateTime;

/**
 * Created by Joe on 9/9/2015.
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
