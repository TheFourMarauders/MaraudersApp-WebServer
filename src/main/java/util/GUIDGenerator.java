package util;

/**
 * This class generates guids
 */
public class GUIDGenerator {
    /**
     * generates a string representation of a guidW
     * @return guid, a string representation of a guid
     */
    public synchronized static String generateGUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
