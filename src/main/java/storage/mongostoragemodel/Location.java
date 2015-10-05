package storage.mongostoragemodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import storage.datatypes.LocationInfo;

import java.time.ZonedDateTime;

/**
 * Created by Joe on 9/7/2015.
 */
public class Location implements Comparable<Location> {

    private double latitude;
    private double longitude;
    private ZonedDateTime time;

    @JsonCreator
    public Location(
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude,
            @JsonProperty("time") ZonedDateTime time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public Location(LocationInfo l) {
        this(l.getLatitude(), l.getLongitude(), l.getTime());
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ZonedDateTime getTime() {
        return time;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return time.equals(location.time);

    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    @Override
    public int compareTo(Location location) {
        return this.getTime().compareTo(location.getTime());
    }
}
