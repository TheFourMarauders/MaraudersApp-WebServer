package util;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;

/**
 * Created by Joe on 9/9/2015.
 */
public class TimeStamp {

    public static ZonedDateTime getCurrentTimeUTC() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return now;
    }

    public static ZonedDateTime getTimeObject(String timeString) {
        return ZonedDateTime.parse(timeString);
    }
}
