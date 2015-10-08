package TheFourMarauders;

import TheFourMarauders.requestschema.GroupSchema;
import TheFourMarauders.requestschema.LocationSchema;
import TheFourMarauders.requestschema.UserCreationRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static spark.Spark.stop;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static ObjectMapper mapper;

    private static int port = 8080;
    private static int nextPort = 8081;
    private static String baseurl = "http://localhost:";

    @Before
    public void init() throws InterruptedException {
        String[] params = new String[]{"services.json", Integer.valueOf(port).toString()};
        WebServer.main(params);
        mapper = new ObjectMapper();
        nextPort++;
    }

    @Test
    public void creationTest() throws UnirestException {
        String creationJson = "";
        String creationJson2 = "";
        UserCreationRequest ucr = new UserCreationRequest("jrossi", "pass", "Joe", "Rossi");
        UserCreationRequest ucr2 = new UserCreationRequest("mgruchacz", "", "Matt", "Gruchacz");
        try {
            creationJson = mapper.writeValueAsString(ucr);
            creationJson2 = mapper.writeValueAsString(ucr2);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        /*
         * Happy Path: user created with unique username - responds 200 OK
         */
        HttpResponse<String> res = Unirest.post("http://localhost:" + port + "/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        // should return HTTP status OK - meaning the system successfully created the user
        assertEquals(200, res.getStatus());

        res = Unirest.get("http://localhost:" + port + "/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
        // ensure that the system created the user by trying to access the friends list
        assertEquals(200, res.getStatus());

        /*
         * Sad Path 1: username is already in use - responds with 409 Conflict
         */
        res = Unirest.post("http://localhost:" + port + "/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        assertEquals(409, res.getStatus());

        /*
         * Sad Path 2: empty password field - returns 400 Bad Request
         */
        res = Unirest.post("http://localhost:" + port + "/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson2)
                .asString();
        assertEquals(400, res.getStatus());

        //exit();

    }

    @Test
    public void addFriendTest() throws UnirestException {
        initUsers();

        /*
         * Happy path: authorized user sends friend request to valid username - response 200 OK
         */
        HttpResponse<String> res =
                Unirest.put("http://localhost:" + port + "/api/services/user/jrossi/send-friend-request/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        // authorized user can check friend requests, and see the newly sent one
        res = Unirest.get("http://localhost:" + port + "/api/services/user/mgruchacz/incoming-friend-requests")
                .basicAuth("mgruchacz", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertNotNull(res.getBody());
        assertEquals("[{\"username\":\"jrossi\",\"firstName\":\"Joe\",\"lastName\":\"Rossi\"}]",
                res.getBody());

        // authorized user can accept friend requests
        res = Unirest.put("http://localhost:" + port + "/api/services/user/mgruchacz/accept-friend/jrossi")
                .basicAuth("mgruchacz", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        // and now the two users are friends :)
        // here jrossi accesses his friends, and sees mgruchacz added
        res = Unirest.get("http://localhost:" + port + "/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertNotNull(res.getBody());
        assertEquals("[{\"username\":\"mgruchacz\",\"firstName\":\"Matt\",\"lastName\":\"Gruchacz\"}]",
                res.getBody());

        /*
         * Sad Path1: Authorized user sends friend request to invalid user - response 400 Bad Request
         */
        res =
            Unirest.put("http://localhost:" + port + "/api/services/user/jrossi/send-friend-request/gruchinator")
                    .basicAuth("jrossi", "pass")
                    .asString();
        assertEquals(400, res.getStatus());

        /*
         * Sad Path2: Unauthorized user attempts to do anything... - response 401 Unauthorized
         */
        res =
                Unirest.put("http://localhost:" + port + "/api/services/user/jrossi/send-friend-request/gruchinator")
                        .basicAuth("jrossi", "notpass")
                        .asString();
        assertEquals(401, res.getStatus());
    }

    @Test
    public void removeFriendTest() throws UnirestException {
        initFriends();
        /*
         * Happy Path: Authorized user removes a friend - response 200 OK
         */
        HttpResponse<String> res =
                Unirest.delete("http://localhost:" + port + "/api/services/user/jrossi/delete-friend/mgruchacz")
                        .basicAuth("jrossi", "pass")
                        .asString();
        assertEquals(200, res.getStatus());

        // now when jrossi accesses friends, there are none
        res = Unirest.get("http://localhost:" + port + "/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertEquals("[]", res.getBody());

        // same for mgruchacz :'(
        res = Unirest.get("http://localhost:" + port + "/api/services/user/mgruchacz/friends")
                .basicAuth("mgruchacz", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertEquals("[]", res.getBody());
    }

    @Test
    public void createGroupTest() throws UnirestException, IOException {
        initFriends();

        /*
         * Happy path: authorized user creates a group
         */
        HttpResponse<String> res = Unirest
                .post("http://localhost:" + port + "/api/services/group/create?groupname=testgroup")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertNotNull(res.getBody());


        Set<String> usernames = new HashSet<>();
        usernames.add("jrossi");
        GroupSchema expected = new GroupSchema(res.getBody(), "testgroup", usernames);
        String expectedGroup = mapper.writeValueAsString(expected);
        // assert that the jrossi account now contains the group id
        HttpResponse<String> res2 = Unirest
                .get("http://localhost:" + port + "/api/services/user/jrossi/groups")
                .basicAuth("jrossi", "pass")
                .asString();
        String res2String = res2.getBody();
        res2String = res2String.substring(1, res2String.length() - 1);
        assertEquals(expectedGroup, res2String);
        assertEquals(expected, mapper.readValue(res2String, GroupSchema.class));

        // assert that the group exists and contains jrossi as user

        res2 = Unirest
                .get("http://localhost:" + port + "/api/services/group/" + res.getBody())
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(expectedGroup, res2.getBody());
        assertEquals(expected, mapper.readValue(res2.getBody(), GroupSchema.class));

    }


    @Test
    public void testGPSCoordinatesForAndAuthenticatedUser()
            throws UnirestException, IOException {
        initUsers();

        LocationSchema loc1 = new LocationSchema(44, 444, ZonedDateTime.now().toString());
        LocationSchema loc2 = new LocationSchema(45, 445, ZonedDateTime.now().plusMinutes(1).toString());
        List<LocationSchema> list = new ArrayList<>(2);
        list.add(loc1);
        list.add(loc2);

        HttpResponse<String> res = Unirest
                .put("http://localhost:" + port + "/api/services/user/jrossi/locations")
                .basicAuth("jrossi", "pass")
                .body(mapper.writeValueAsString(list))
                .asString();
        assertEquals(200, res.getStatus());

        res = Unirest
                .get("http://localhost:" + port + "/api/services/user/jrossi/locations")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        JavaType jt = mapper.getTypeFactory().constructCollectionType(ArrayList.class, LocationSchema.class);
        List<LocationSchema> resList = mapper.readValue(res.getBody(), jt);
        assertEquals(list.size(), resList.size());
        assertEquals(loc1, resList.get(0));
        assertEquals(loc2, resList.get(1));

        res = Unirest
                .get("http://localhost:" + port + "/api/services/user/jrossi/locations")
                .basicAuth("mgruchacz", "pass")
                .asString();
        assertEquals(403, res.getStatus());

        beFriendsNow();

        res = Unirest
                .get("http://localhost:" + port + "/api/services/user/jrossi/locations")
                .basicAuth("mgruchacz", "pass")
                .asString();
        assertEquals(200, res.getStatus());
    }

    @Test
    public void testInvalidPutLocationMethodRequest() throws UnirestException, JsonProcessingException {
        initFriends();
        LocationSchema loc1 = new LocationSchema(44, 444, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        LocationSchema loc2 = new LocationSchema(45, 445, ZonedDateTime.now().plusMinutes(1)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME));
        List<LocationSchema> list = new ArrayList<>(2);
        list.add(loc1);
        list.add(loc2);

        HttpResponse<String> res = Unirest
                .put("http://localhost:" + port + "/api/services/user/jrossi/locations")
                .basicAuth("jrossi", "pass")
                .body(mapper.writeValueAsString(list))
                .asString();
        assertEquals(400, res.getStatus());
    }

    @Test
    public void groupManagementTest() throws UnirestException, IOException {
        initUsers();
        String guid = createGroup();

        HttpResponse<String> res = Unirest
                .put("http://localhost:" + port + "/api/services/group/" + guid + "/user/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(403, res.getStatus());

        beFriendsNow();

        res = Unirest
                .put("http://localhost:" + port + "/api/services/group/" + guid + "/user/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        res = Unirest
                .put("http://localhost:" + port + "/api/services/group/" + guid.replaceAll("-", "%") + "/user/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(400, res.getStatus());

        res = Unirest
                .put("http://localhost:" + port + "/api/services/group/" + guid + "/user/csimpkins")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(403, res.getStatus());

        res = Unirest
                .delete("http://localhost:" + port + "/api/services/group/" + guid + "/user/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        res = Unirest
                .get("http://localhost:" + port + "/api/services/group/" + guid)
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertTrue(mapper.readValue(res.getBody(), GroupSchema.class).getMembers().contains("jrossi"));
        assertEquals(1, mapper.readValue(res.getBody(), GroupSchema.class).getMembers().size());

        res = Unirest
                .delete("http://localhost:" + port + "/api/services/group/" + guid.replaceAll("-", "%") + "/user/jrossi")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(400, res.getStatus());
    }

    private void initUsers() throws UnirestException {
        String creationJson = "";
        String creationJson2 = "";
        UserCreationRequest ucr = new UserCreationRequest("jrossi", "pass", "Joe", "Rossi");
        UserCreationRequest ucr2 = new UserCreationRequest("mgruchacz", "pass", "Matt", "Gruchacz");
        try {
            creationJson = mapper.writeValueAsString(ucr);
            creationJson2 = mapper.writeValueAsString(ucr2);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        HttpResponse<String> res = Unirest.post("http://localhost:" + port + "/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        Unirest.post("http://localhost:" + port + "/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson2)
                .asString();
    }

    private void initFriends() throws UnirestException {
        initUsers();
        Unirest.put("http://localhost:" + port + "/api/services/user/jrossi/send-friend-request/mgruchacz")
                        .basicAuth("jrossi", "pass")
                        .asString();
        Unirest.get("http://localhost:" + port + "/api/services/user/mgruchacz/incoming-friend-requests")
                .basicAuth("mgruchacz", "pass")
                .asString();
        Unirest.put("http://localhost:" + port + "/api/services/user/mgruchacz/accept-friend/jrossi")
                .basicAuth("mgruchacz", "pass")
                .asString();
        Unirest.get("http://localhost:" + port + "/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
    }

    private void beFriendsNow() throws UnirestException {
        Unirest.put("http://localhost:" + port + "/api/services/user/jrossi/send-friend-request/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        Unirest.get("http://localhost:" + port + "/api/services/user/mgruchacz/incoming-friend-requests")
                .basicAuth("mgruchacz", "pass")
                .asString();
        Unirest.put("http://localhost:" + port + "/api/services/user/mgruchacz/accept-friend/jrossi")
                .basicAuth("mgruchacz", "pass")
                .asString();
        Unirest.get("http://localhost:" + port + "/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
    }

    private String createGroup() throws UnirestException {
        HttpResponse<String> res = Unirest
                .post("http://localhost:" + port + "/api/services/group/create?groupname=testgroup")
                .basicAuth("jrossi", "pass")
                .asString();
        return res.getBody();
    }



    @After
    public void exit() {
        stop();
        port = nextPort;
        System.gc();
    }
}
