package TheFourMarauders;

import TheFourMarauders.requestschema.GroupSchema;
import TheFourMarauders.requestschema.UserCreationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static spark.Spark.stop;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static ObjectMapper mapper;

    @Before
    public void init() throws InterruptedException {
        String[] params = new String[]{"test"};
        WebServer.main(params);
        mapper = new ObjectMapper();
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
        HttpResponse<String> res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        // should return HTTP status OK - meaning the system successfully created the user
        assertEquals(200, res.getStatus());

        res = Unirest.get("http://localhost:8080/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
        // ensure that the system created the user by trying to access the friends list
        assertEquals(200, res.getStatus());

        /*
         * Sad Path 1: username is already in use - responds with 409 Conflict
         */
        res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        assertEquals(409, res.getStatus());

        /*
         * Sad Path 2: empty password field - returns 400 Bad Request
         */
        res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson2)
                .asString();
        assertEquals(400, res.getStatus());

    }

    @Test
    public void addFriendTest() throws UnirestException {
        initUsers();

        /*
         * Happy path: authorized user sends friend request to valid username - response 200 OK
         */
        HttpResponse<String> res =
                Unirest.put("http://localhost:8080/api/services/user/jrossi/send-friend-request/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        // authorized user can check friend requests, and see the newly sent one
        res = Unirest.get("http://localhost:8080/api/services/user/mgruchacz/incoming-friend-requests")
                .basicAuth("mgruchacz", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertNotNull(res.getBody());
        assertEquals("[{\"username\":\"jrossi\",\"firstName\":\"Joe\",\"lastName\":\"Rossi\"}]",
                res.getBody());

        // authorized user can accept friend requests
        res = Unirest.put("http://localhost:8080/api/services/user/mgruchacz/accept-friend/jrossi")
                .basicAuth("mgruchacz", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        // and now the two users are friends :)
        // here jrossi accesses his friends, and sees mgruchacz added
        res = Unirest.get("http://localhost:8080/api/services/user/jrossi/friends")
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
            Unirest.put("http://localhost:8080/api/services/user/jrossi/send-friend-request/gruchinator")
                    .basicAuth("jrossi", "pass")
                    .asString();
        assertEquals(400, res.getStatus());

        /*
         * Sad Path2: Unauthorized user attempts to do anything... - response 401 Unauthorized
         */
        res =
                Unirest.put("http://localhost:8080/api/services/user/jrossi/send-friend-request/gruchinator")
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
                Unirest.delete("http://localhost:8080/api/services/user/jrossi/delete-friend/mgruchacz")
                        .basicAuth("jrossi", "pass")
                        .asString();
        assertEquals(200, res.getStatus());

        // now when jrossi accesses friends, there are none
        res = Unirest.get("http://localhost:8080/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());
        assertEquals("[]", res.getBody());

        // same for mgruchacz :'(
        res = Unirest.get("http://localhost:8080/api/services/user/mgruchacz/friends")
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
                .post("http://localhost:8080/api/services/group/create?groupname=testgroup")
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
                .get("http://localhost:8080/api/services/user/jrossi/groups")
                .basicAuth("jrossi", "pass")
                .asString();
        String res2String = res2.getBody();
        res2String = res2String.substring(1, res2String.length() - 1);
        assertEquals(expectedGroup, res2String);
        assertEquals(expected, mapper.readValue(res2String, GroupSchema.class));

        // assert that the group exists and contains jrossi as user

        res2 = Unirest
                .get("http://localhost:8080/api/services/group/" + res.getBody())
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(expectedGroup, res2.getBody());
        assertEquals(expected, mapper.readValue(res2.getBody(), GroupSchema.class));

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
        HttpResponse<String> res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson2)
                .asString();
    }

    private void initFriends() throws UnirestException {
        initUsers();
        Unirest.put("http://localhost:8080/api/services/user/jrossi/send-friend-request/mgruchacz")
                        .basicAuth("jrossi", "pass")
                        .asString();
        Unirest.get("http://localhost:8080/api/services/user/mgruchacz/incoming-friend-requests")
                .basicAuth("mgruchacz", "pass")
                .asString();
        Unirest.put("http://localhost:8080/api/services/user/mgruchacz/accept-friend/jrossi")
                .basicAuth("mgruchacz", "pass")
                .asString();
        Unirest.get("http://localhost:8080/api/services/user/jrossi/friends")
                .basicAuth("jrossi", "pass")
                .asString();
    }

    @After
    public void exit() {
        stop();
        System.gc();
    }
}
