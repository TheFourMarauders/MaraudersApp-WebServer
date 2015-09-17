package TheFourMarauders;

import TheFourMarauders.requestschema.UserCreationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static spark.Spark.stop;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static ObjectMapper mapper;

    @Before
    public void init() {
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
        HttpResponse<String> res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        assertEquals(200, res.getStatus());

        // username conflict
        res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        assertEquals(409, res.getStatus());

        // empty password field
        res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson2)
                .asString();
        assertEquals(400, res.getStatus());

    }

    @Test
    public void addFriendTest() throws UnirestException {
        initUsers();
        HttpResponse<String> res =
                Unirest.put("http://localhost:8080/api/services/user/jrossi/send-friend-request/mgruchacz")
                .basicAuth("jrossi", "pass")
                .asString();
        assertEquals(200, res.getStatus());

        res =
            Unirest.put("http://localhost:8080/api/services/user/jrossi/send-friend-request/gruchinator")
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
        HttpResponse<String> res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson)
                .asString();
        res = Unirest.post("http://localhost:8080/api/create-user")
                .header("content-type", "application/json")
                .body(creationJson2)
                .asString();
    }

    @After
    public void exit() {
        stop();
    }
}
