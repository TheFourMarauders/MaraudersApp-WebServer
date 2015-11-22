package util;

import controller.HTTPException;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * TimeStam= is a utility class tht is responsible for parsing and generating different time elements
 */
public class TimeStamp {

    /**
     * Gets the current time in UTC
     * @return ZondedDAteTime - time/date in UTC
     */
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
