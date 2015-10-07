package TheFourMarauders;

import TheFourMarauders.requestschema.GroupSchema;
import TheFourMarauders.requestschema.LocationSchema;
import TheFourMarauders.requestschema.UserCreationRequest;
import TheFourMarauders.requestschema.UserSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.HTTPException;
import controller.ServiceController;
import controller.ServiceFactory;
import storage.datatypes.GroupInfo;
import storage.datatypes.LocationInfo;
import storage.datatypes.UserInfo;
import util.TimeStamp;

import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

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
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
    }
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
            String response = "";
            try {
                UserCreationRequest r = mapper.readValue(req.body(), UserCreationRequest.class);
                serviceController.createUser(r.getUsername(), r.getPassword(), r.getFirstName(), r.getLastName());
                response =  "Successfully created user: " + r.getUsername() + "\n Welcome to MaraudersApp!";
            } catch (JsonProcessingException e) {
                halt(400, "Invalid request schema");
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return response;
        });

        put("/api/services/user/:username/send-friend-request/:targetusername", (req, res) -> {
            //add friend request to target user specified in json
            String response = "";
            try {
                String sender = req.params(":username");
                String target = req.params(":targetusername");
                String authtoken = req.headers("Authorization");

                serviceController.sendFriendRequest(authtoken, sender, target);
                return "Successfully sent friend request to: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return response;
        });

        get("/api/services/user/:username/incoming-friend-requests", (req, res) -> {
            String response = "";
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");

                String friendReqs = mapper.writeValueAsString(
                        serviceController.getFriendRequestsFor(authtoken, user));
                res.type("application/json");
                res.status(200);
                response = friendReqs;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return response;
        });

        put("/api/services/user/:username/accept-friend/:targetfriend", (req, res) -> {
            String response = "";
            try {
                String user = req.params(":username");
                String target = req.params(":targetfriend");
                String authtoken = req.headers("Authorization");

                serviceController.acceptFriendRequest(authtoken, user, target);
                response = "Successfully added friend: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return response;
        });

        delete("/api/services/user/:username/delete-friend/:targetfriend", (req, res) -> {
            String response = "";
            try {
                String user = req.params(":username");
                String target = req.params(":targetfriend");
                String authtoken = req.headers("Authorization");

                serviceController.deleteFriend(authtoken, user, target);
                response =  "Successfully deleted friend: " + target;
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return response;
        });


        get("/api/services/user/:username/friends", (req, res) -> {
            String friends = null;
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");

                Set<UserInfo> infos = serviceController.getFriends(authtoken, user);
                Set<UserSchema> userSchemas = new HashSet<>(infos.size());
                for (UserInfo info : infos) {
                    userSchemas.add(new UserSchema(info.getUsername(), info.getFirstName(), info.getLastName()));
                }
                res.type("application/json");
                friends = mapper.writeValueAsString(userSchemas);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            } catch (JsonProcessingException e) {
                halt(500, "Bad user schema storage :'(");
            }
            res.status(200);
            return friends;
        });

        // gps locations , before and after param
        // if ignored then obvious what happens

        // [{lat, long, time}]

        // /api/services/user/:username/location?start=10:01:00+01010&end=100101001
        get("/api/services/user/:username/location", (req, res) -> {
            String user = req.params(":username");
            String authtoken = req.headers("Authorization");
            String startParam = req.queryParams("start");
            String endParam = req.queryParams("end");
            ZonedDateTime start = null;
            ZonedDateTime end = null;
            String locations = null;
            try {
                if (startParam != null) {
                    start = TimeStamp.getTimeObject(URLDecoder.decode(startParam, "UTF-8"));
                }
                if (endParam != null) {
                    end = TimeStamp.getTimeObject(URLDecoder.decode(endParam, "UTF-8"));
                }
                List<LocationInfo> infoList = serviceController.getLocationsFor(authtoken, user, start, end);
                List<LocationSchema> schemaList = new ArrayList<>(infoList.size());
                for (LocationInfo loc : infoList) {
                    schemaList.add(new LocationSchema(loc.getLatitude(), loc.getLongitude(), loc.getTime().toString()));
                }
                res.type("application/json");
                locations = mapper.writeValueAsString(schemaList);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            } catch (DateTimeParseException e) {
                halt(400, "Bad timestamp string");
            }
            res.status(200);
            return locations;
        });


        //  [{lat, long, time}]
        put("/api/services/user/:username/location", (req, res) -> {
            String response = "";
            try {
                String user = req.params(":username");
                String authtoken = req.headers("Authorization");
                List<LocationSchema> list = null;
                JavaType jt = mapper.getTypeFactory().constructCollectionType(ArrayList.class, LocationSchema.class);
                list = mapper.readValue(req.body(), jt);
                List<LocationInfo> infoList = new ArrayList<>();
                for (LocationSchema r : list) {
                    infoList.add(
                            new LocationInfo(r.getLatitude(), r.getLongitude(), TimeStamp.getTimeObject(r.getTime())));
                }
                serviceController
                        .putLocationsFor(authtoken, user, infoList);
                response =  "Success";
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            } catch (JsonProcessingException e) {
                halt(400, "Invalid request schema, see api");
            }
            res.status(200);
            return response;
        });

        // json name, description,   // names of groups not unique, tie guid
        // response , uuid
        post("api/services/group/create", (req, res) -> {
            String groupId = null;
            try {
                String authtoken = req.headers("Authorization");
                String groupName = req.queryParams("groupname");
                if (groupName == null || groupName.isEmpty()) {
                    throw new HTTPException("Bad Request: missing group name", 400);
                }
                groupId = serviceController.createGroup(authtoken, groupName);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return groupId;
        });

        get("api/services/user/:username/groups", (req, res) -> {
            String groups = null;
            try {
                String authtoken = req.headers("Authorization");
                String username = req.params(":username");
                Set<GroupInfo> infos = serviceController.getGroups(authtoken, username);
                Set<GroupSchema> groupSchemaSet = new HashSet<>(infos.size());
                for (GroupInfo info : infos) {
                    groupSchemaSet.add(new GroupSchema(info.getGroupId(), info.getGroupName(), info.getMembers()));
                }
                groups = mapper.writeValueAsString(groupSchemaSet);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            } catch (JsonProcessingException e) {
                halt(500, "Bad group schema storage :'(");
            }
            res.status(200);
            return groups;
        });

        get("api/services/group/:id", (req, res) -> {
            String group = null;
            try {
                String authtoken = req.headers("Authorization");
                String groupId = req.params(":id");
                GroupInfo info = serviceController.getGroup(authtoken, groupId);
                GroupSchema groupSchema = new GroupSchema(info.getGroupId(),
                        info.getGroupName(), info.getMembers());
                group = mapper.writeValueAsString(groupSchema);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            } catch (JsonProcessingException e) {
                halt(500, "Bad group schema storage :'(");
            }
            res.status(200);
            return group;
        });

        put("api/services/group/:id/user/:username", (req, res) -> {
            String authtoken = req.headers("Authorization");
            String userToAdd = req.params(":username");
            String groupId = req.params(":id");
            try {
                serviceController.putUserInGroup(authtoken, userToAdd, groupId);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return "Successfully added user to group";
        });

        delete("api/services/group/:id/user/:username", (req, res) -> {
            String authtoken = req.headers("Authorization");
            String userToRemove = req.params(":username");
            String groupId = req.params(":id");
            try {
                serviceController.deleteUserFromGroup(authtoken, userToRemove, groupId);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            }
            res.status(200);
            return "Successfully deleted user from group";
        });

        get("api/services/group/:id/locations", (req, res) -> {
            String authtoken = req.headers("Authorization");
            String groupId = req.params(":id");
            String startParam = req.queryParams("start");
            String endParam = req.queryParams("end");
            ZonedDateTime start = null, end = null;
            String locations = "";
            try {
                if (startParam != null) {
                    start = TimeStamp.getTimeObject(URLDecoder.decode(startParam, "UTF-8"));
                }
                if (endParam != null) {
                    end = TimeStamp.getTimeObject(URLDecoder.decode(endParam, "UTF-8"));
                }
                Map<String, List<LocationInfo>> locationMap
                        = serviceController.getLocationsForGroup(authtoken, groupId, start, end);
                Map<String, List<LocationSchema>> locationResMap
                        = new HashMap<>();
                for (String uid : locationMap.keySet()) {
                    List<LocationInfo> locInfos = locationMap.get(uid);
                    List<LocationSchema> locSchemas = new ArrayList<>(locInfos.size());
                    for (LocationInfo l : locInfos) {
                        locSchemas.add(new LocationSchema(l.getLatitude(), l.getLongitude(), l.getTime().toString()));
                    }
                    locationResMap.put(uid, locSchemas);
                }
                locations = mapper.writeValueAsString(locationResMap);
            } catch (HTTPException e) {
                halt(e.getHttpErrorCode(), e.getMessage());
            } catch (JsonProcessingException e) {
                halt(500, "Location map serialization error :'(");
            }
            res.status(200);
            return locations;
        });
    }
}
