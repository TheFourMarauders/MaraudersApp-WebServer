package TheFourMarauders.requestschema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import controller.HTTPException;
import util.TimeStamp;

import java.time.ZonedDateTime;

/**
 * Created by joe on 10/2/15.
 */
public class LocationSchema {

    private double latitude;
    private double longitude;
    private String time;

    @JsonCreator
    public LocationSchema(
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude,
            @JsonProperty("time") String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTime() {
        return time;
    }
}
