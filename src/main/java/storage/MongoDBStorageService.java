package storage;

import static com.mongodb.client.model.Filters.eq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import controller.DatabaseConfig;
import controller.ServiceController;
import storage.mongostoragemodel.User;

import java.util.Base64;


/**
 * Created by Matthew on 9/7/2015.
 */
public class MongoDBStorageService implements StorageService{
    private static StorageService instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private static final String USER_COLLECTION = "users";
    private static final String GROUP_COLLECTION = "groups";

    public static StorageService getInstance() {
        if(instance == null)
        {
            instance = new MongoDBStorageService();
        }
        return instance;
    }

    private MongoDBStorageService(){
        DatabaseConfig dbConfig = ServiceController.getInstance().getDbConfig();
        mongoClient = new MongoClient(dbConfig.getURL());
        database = mongoClient.getDatabase(dbConfig.getDbName());
    }

    @Override
    public byte[] getHashedPassword(String username) {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document user = coll.find(eq("_id", username)).first();
        if (user == null) {
            return null;
        }

        return Base64.getDecoder().decode(user.getString("HashedPassword"));
    }

    @Override
    public synchronized boolean createUser(String username, String hashedPassword, String firstName, String lastName) {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document user = coll.find(eq("_id", username)).first();
        if (user != null) {
            return false;
        }
        User u = new User(username, hashedPassword, firstName, lastName);
        ObjectMapper mapper = new ObjectMapper();
        String userJson = null;
        try {
            userJson = mapper.writeValueAsString(u);
        } catch (JsonProcessingException e) {
            return false;
        }

        Document doc = Document.parse(userJson);
        try {
            coll.insertOne(doc);
        } catch (MongoException e) {
            return false;
        }
        return true;
    }
}
