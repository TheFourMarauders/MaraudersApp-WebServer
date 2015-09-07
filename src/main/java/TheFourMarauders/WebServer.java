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
        spark.Spark.threadPool(Runtime.getRuntime().availableProcessors());

        //Authentication it is done through before
        before("/api/services", (req, res) -> {
            if(!authService.authenticate((req))) {
                halt(401, "Thou shall not pass, invalid credentials");
            }
        });


        post("/api/create-user", (req, res) -> {
            //create user
        });

        put("/api/services/send-friend-request/:username", (req, res) -> {
            //add friend request to target user specified in json
        });

        get("/api/services/:username/incoming-friend-requests", (req, res) -> {

        });

        put("/api/services/:username/accept-friend/:targetfriend", (req, res) -> {

        });
        delete("/api/services/:username/delete-friend/:targetfriend", (req, res) -> {

        });



    }
}
