package TheFourMarauders;

import TheFourMarauders.requestschema.UserCreationRequest;
import authentication.AuthenticationException;
import authentication.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.HTTPException;
import controller.ServiceController;

import javax.xml.ws.Service;

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
        before("/api/services/*", (req, res) -> {
            try {
                ServiceController.getInstance().authenticate(req.headers("Authorization"));
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
        });


        post("/api/create-user", (req, res) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                UserCreationRequest r = mapper.readValue(req.body(), UserCreationRequest.class);
                ServiceController.getInstance()
                        .createUser(r.getUsername(), r.getPassword(), r.getFirstName(), r.getLastName());
                return "Successfully created user: " + r.getUsername() + "\n Welcome to MaraudersApp!";
            } catch (JsonProcessingException e) {
                halt(400, "Invalid request schema");
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            return "This should never happen :) We promise...";
        });

        put("/api/services/user/:username/send-friend-request/:targetusername", (req, res) -> {
            //add friend request to target user specified in json
            try {
                String sender = req.params(":username");
                String target = req.params(":targetusername");
                String authtoken = req.headers("Authorization");
                ServiceController.getInstance().validate(authtoken, sender);

                ServiceController.getInstance().sendFriendRequest(sender, target);
                return "Successfully sent friend request to: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            return "This should never happen :) We promise...";
        });

        get("/api/services/user/:username/incoming-friend-requests", (req, res) -> {
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");
                ServiceController.getInstance().validate(authtoken, user);

                ObjectMapper mapper = new ObjectMapper();
                String friendReqs = mapper.writeValueAsString(
                        ServiceController.getInstance().getFriendRequestsFor(user));
                res.type("application/json");
                res.status(200);
                return friendReqs;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            return "This should never happen :) We promise...";
        });

        put("/api/services/user/:username/accept-friend/:targetfriend", (req, res) -> {
            try {
                String user = req.params(":username");
                String target = req.params(":targetfriend");
                String authtoken = req.headers("Authorization");
                ServiceController.getInstance().validate(authtoken, user);

                ServiceController.getInstance().acceptFriendRequest(user, target);
                return "Successfully added friend: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            return "This should never happen :) We promise...";
        });
        delete("/api/services/user/:username/delete-friend/:targetfriend", (req, res) -> {
            try {
                String user = req.params(":username");
                String target = req.params(":targetfriend");
                String authtoken = req.headers("Authorization");
                ServiceController.getInstance().validate(authtoken, user);

                ServiceController.getInstance().removeFriend(user, target);
                return "Successfully deleted friend: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            return "This should never happen :) We promise...";
        });


        get("/api/services/user/:username/friends", (req, res) -> {
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");
                ServiceController.getInstance().validate(authtoken, user);

                res.type("application/json");
                res.status(200);
                return new ObjectMapper().writeValueAsString(ServiceController.getInstance().getFriends(user));
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            return "This should never happen :) We promise...";
        });
    }
}
