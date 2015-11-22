package storage.datatypes;

import controller.HTTPException;
import storage.mongostoragemodel.Location;
import util.TimeStamp;

import java.time.ZonedDateTime;

/**
 * An inter-service representation of a Location
 *
 * @author Joe and Matt
 * @version 1.0
 */
public class LocationInfo implements Comparable<LocationInfo> {

    private double latitude;
    private double longitude;
    private ZonedDateTime time;

    public LocationInfo(double latitude, double longitude, ZonedDateTime time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public LocationInfo(Location l) throws HTTPException {
        this(l.getLatitude(), l.getLongitude(), TimeStamp.getTimeObject(l.getTime()));
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
    public int compareTo(LocationInfo locationInfo) {
        return time.compareTo(locationInfo.time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationInfo that = (LocationInfo) o;

        return time.equals(that.time);

    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }
}
