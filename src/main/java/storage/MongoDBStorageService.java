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
import storage.mongostoragemodel.FriendRequest;
import storage.mongostoragemodel.User;
import util.TimeStamp;

import java.io.IOException;
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
    public synchronized void createUser(String username, String hashedPassword, String firstName, String lastName)
            throws StorageException {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document user = coll.find(eq("_id", username)).first();
        if (user != null) {
            throw new StorageException("Username already exists");
        }
        User u = new User(username, hashedPassword, firstName, lastName);
        ObjectMapper mapper = new ObjectMapper();
        String userJson = null;
        try {
            userJson = mapper.writeValueAsString(u);
        } catch (JsonProcessingException e) {
            throw new StorageException("Bad User serialization\n" + e.getMessage());
        }

        Document doc = Document.parse(userJson);
        try {
            coll.insertOne(doc);
        } catch (MongoException e) {
            throw new StorageException("Unable to insert into database \n" + e.getMessage());
        }
    }

    @Override
    public void insertFriendRequest(String senderUsername, String targetUsername) throws StorageException {
        User target = getUserFromDB(targetUsername);

        FriendRequest fr = new FriendRequest(senderUsername, TimeStamp.getCurrentTimeUTC());
        target.addFriendRequest(fr);

        updateUser(target);
    }

    @Override
    public boolean areUsersFriends(String username1, String username2) throws StorageException {
        User user1 = getUserFromDB(username1);
        return user1.isFriendsWith(username2);
    }

    private User getUserFromDB(String username) throws StorageException {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        Document userDoc = coll.find(eq("_id", username)).first();
        if (userDoc == null) {
            throw new StorageException("User not found");
        }

        ObjectMapper mapper = new ObjectMapper();
        User user = null;
        try {
            user = mapper.readValue(userDoc.toJson(), User.class);
        } catch (IOException e) {
            throw new StorageException("Bad User Serialization\n" + e.getMessage());
        }

        return user;
    }

    private void updateUser(User u) throws StorageException {
        MongoCollection<Document> coll = database.getCollection(USER_COLLECTION);
        String json = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(u);
        } catch (JsonProcessingException e) {
            throw new StorageException("Bad User serialization\n" + e.getMessage());
        }

        Document updatedUser = Document.parse(json);
        try {
            coll.replaceOne(eq("_id", u.get_id()), updatedUser);
        } catch (MongoException e) {
            throw new StorageException("Unable to add to database\n" + e.getMessage());
        }
    }
}
