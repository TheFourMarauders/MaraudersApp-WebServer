package util;

/**
 * Created by joe on 10/5/15.
 */
public class GUIDGenerator {
    public synchronized static String generateGUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
