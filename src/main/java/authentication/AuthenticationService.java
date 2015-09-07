package authentication;

import spark.Request;
/**
 * Created by Matthew on 9/7/2015.
 */
public interface AuthenticationService {
    boolean authenticate(Request request);
}
