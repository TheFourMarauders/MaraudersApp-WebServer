package TheFourMarauders;

import authentication.AuthenticationService;

import static spark.Spark.get;
import static spark.Spark.before;
import static spark.Spark.post;
import static spark.Spark.halt;
import static spark.Spark.put;
import static spark.Spark.delete;

/**
 * Main Class File, Executed when starting server
 *
 */
public class WebServer
{

    private static AuthenticationService authService;
    public static void main( String[] args )
    {
        //Load config
        spark.Spark.port(8080);
        //spark.Spark.threadPool(Runtime.getRuntime().availableProcessors());

        //Authentication it is done through before
        before("/api/services", (req, res) -> {
            halt(401, "Thou shall not pass, invalid credentials");
        });


        post("/api/create-user", (req, res) -> {
            //create user
            return null;
        });

        put("/api/services/user/:username/send-friend-request/:targetusername", (req, res) -> {
            //add friend request to target user specified in json
            return null;
        });

        get("/api/services/user/:username/incoming-friend-requests", (req, res) -> {
            return null;
        });

        put("/api/services/user/:username/accept-friend/:targetfriend", (req, res) -> {
            return null;
        });
        delete("/api/services/user/:username/delete-friend/:targetfriend", (req, res) -> {
            return null;
        });


    }
}
