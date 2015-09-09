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
            @JsonProperty("timeStamp") String senderUsername,
            @JsonProperty("timeStamp") ZonedDateTime timeStamp) {
        this.senderUsername = senderUsername;
        this.timeStamp = timeStamp;
    }
}
