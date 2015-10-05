package util;

import controller.HTTPException;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Created by Joe on 9/9/2015.
 */
public class TimeStamp {

    public static ZonedDateTime getCurrentTimeUTC() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return now;
    }

    public static ZonedDateTime getTimeObject(String timeString) throws HTTPException {
        try {
            return ZonedDateTime.parse(timeString);
        } catch (DateTimeParseException e) {
            throw new HTTPException("Bad datetime string", 400);
        }
    }
}
