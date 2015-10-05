package TheFourMarauders;

import TheFourMarauders.requestschema.LocationRequest;
import TheFourMarauders.requestschema.UserCreationRequest;
import authentication.AuthenticationException;
import authentication.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.HTTPException;
import controller.ServiceController;
import controller.ServiceFactory;
import storage.LocationInfo;
import util.TimeStamp;

import javax.xml.ws.Service;

import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        ServiceController serviceController2 = null;
        if (args.length > 0 && args[0].equals("test")) {
            serviceController2 = new ServiceFactory().buildLocal();
        } else {
            serviceController2 = new ServiceFactory().build();
        }
        ServiceController serviceController = serviceController2;
        //Load config
        spark.Spark.port(8080);
        //spark.Spark.threadPool(Runtime.getRuntime().availableProcessors());

        //Authentication it is done through before
        before("/api/services/*", (req, res) -> {
            try {
                serviceController.authenticate(req.headers("Authorization"));
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
        });


        post("/api/create-user", (req, res) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                UserCreationRequest r = mapper.readValue(req.body(), UserCreationRequest.class);
                serviceController.createUser(r.getUsername(), r.getPassword(), r.getFirstName(), r.getLastName());
                return "Successfully created user: " + r.getUsername() + "\n Welcome to MaraudersApp!";
            } catch (JsonProcessingException e) {
                halt(400, "Invalid request schema");
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });

        put("/api/services/user/:username/send-friend-request/:targetusername", (req, res) -> {
            //add friend request to target user specified in json
            try {
                String sender = req.params(":username");
                String target = req.params(":targetusername");
                String authtoken = req.headers("Authorization");

                serviceController.sendFriendRequest(authtoken, sender, target);
                return "Successfully sent friend request to: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });

        get("/api/services/user/:username/incoming-friend-requests", (req, res) -> {
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");

                ObjectMapper mapper = new ObjectMapper();
                String friendReqs = mapper.writeValueAsString(
                        serviceController.getFriendRequestsFor(authtoken, user));
                res.type("application/json");
                res.status(200);
                return friendReqs;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });

        put("/api/services/user/:username/accept-friend/:targetfriend", (req, res) -> {
            try {
                String user = req.params(":username");
                String target = req.params(":targetfriend");
                String authtoken = req.headers("Authorization");

                serviceController.acceptFriendRequest(authtoken, user, target);
                return "Successfully added friend: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });

        delete("/api/services/user/:username/delete-friend/:targetfriend", (req, res) -> {
            try {
                String user = req.params(":username");
                String target = req.params(":targetfriend");
                String authtoken = req.headers("Authorization");

                serviceController.removeFriend(authtoken, user, target);
                return "Successfully deleted friend: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });


        get("/api/services/user/:username/friends", (req, res) -> {
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");

                res.type("application/json");
                res.status(200);
                return new ObjectMapper().writeValueAsString(serviceController.getFriends(authtoken, user));
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });

        // gps locations , before and after param
        // if ignored then obvious what happens

        // [{lat, long, time}]

        // /api/services/user/:username/location?start=10:01:00+01010&end=100101001
        get("/api/services/user/:username/location", (req, res) -> {
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");
                String startParam = req.queryParams("start");
                String endParam = req.queryParams("end");
                ZonedDateTime start = null;
                ZonedDateTime end = null;
                if (startParam != null) {
                    start = TimeStamp.getTimeObject(URLDecoder.decode(startParam, "UTF-8"));
                }
                if (endParam != null) {
                    end = TimeStamp.getTimeObject(URLDecoder.decode(endParam, "UTF-8"));
                }
                return new ObjectMapper().writeValueAsString(
                        serviceController.getLocationsFor(authtoken, user, start, end));
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            } catch (DateTimeParseException e) {
                halt(400, "Bad timestamp string");
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });


        //  [{lat, long, time}]
        put("/api/services/user/:username/location", (req, res) -> {
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");
                ObjectMapper mapper = new ObjectMapper();
                List<LocationRequest> list = null;
                JavaType jt = mapper.getTypeFactory().constructCollectionType(ArrayList.class, LocationRequest.class);
                list = mapper.readValue(req.body(), jt);
                List<LocationInfo> infoList = list.stream()
                        .map(l -> new LocationInfo(l))
                        .collect(Collectors.toCollection(ArrayList<LocationInfo>::new));
                serviceController
                        .putLocationsFor(authtoken, user, infoList);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(500);
            return "This should never happen :) We promise...";
        });

        // json name, description,   // names of groups not unique, tie guid
        // response , uuid
        //post("api/services/group/create");

        //get("api/services/group/:id/locations");

        //get("api/services/group/:id"); //general status

        //put("api/services/group/:id/user/:username");

        //delete("api/services/group/:id/user/:username");

    }
}
