package storage;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import controller.DatabaseConfig;
import controller.ServiceController;


/**
 * Created by Matthew on 9/7/2015.
 */
public class MongoDBStorageService implements  StorageService{
    private static StorageService instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

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
        return new byte[0];
    }
}
