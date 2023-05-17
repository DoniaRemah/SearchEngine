package DatabaseManagement;

import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class DBManager {

    MongoClient mongoClient;
    MongoDatabase database;
    String connectionString = "mongodb+srv://abouelhadidola:8aWAvyLwc824XSm8@searchengine.uwkyb5b.mongodb.net/?retryWrites=true&w=majority";

    public DBManager() {

        // Connecting to database
        try{

            mongoClient = new MongoClient(new MongoClientURI(connectionString));
            database = mongoClient.getDatabase("SearchEngine");

        }
        catch (Exception e){
            // Connection Failed
            System.out.println(e.toString());
        }
        System.out.println("Database Connected Successfully");
    }

    ////////////////////////////////////////////////////////// CRAWLER REQUESTS

    /**
     *  Get all Documents from Crawler Collection
     */
    public List<Document> retrieveCrawlerDocuments() {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        FindIterable<Document> iterDoc = collection.find();
        MongoCursor<Document> it = iterDoc.iterator();
        List<Document> list = new ArrayList<>();

        while (it.hasNext()) {
            list.add(it.next());
        }
        it.close();
        System.out.println("Crawler Documents Retrieved successfully");
        return list;
    }

    /**
     * // Insert a crawler document into the webcrawler collection
     */
    public void insertCrawlerDocument(String htmlDocString, String URL, String title,List<String> PointsTo) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document document = new Document("URL", URL).append("Title", title).append("Content", htmlDocString).append("PointsT0", PointsTo);
        // Inserting document into the collection
        collection.insertOne(document);
        System.out.println("Crawler Document Inserted successfully" + URL);

    }

    /**
     * Delete a crawler document from the webcrawler collection
     */
    public void deleteCrawlerDocument(String URL) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document Filter = new Document("URL", URL);
        collection.deleteOne(Filter);
        System.out.println("Crawler Document Deleted successfully"+ URL);
    }

    public void UpdateCrawlerDocument(String URL, String htmlDocument) {

        MongoCollection<Document> collection = database.getCollection("WebCrawler");
        Document Updated = new Document("URL", URL).append("HTMLDoc", htmlDocument);
        collection.findOneAndReplace(new Document("URL", URL), Updated);
        System.out.println("Crawler Updated successfully"+ URL);
    }


    ///////////////////////////////////////////////// INDEXER REQUESTS

    /**
     * Insert final hashtable into indexer collection
     */
    public void insertIndexerDocs(Hashtable<String, List<Document>> IndexerTable) {

        MongoCollection<Document> collection = database.getCollection("Indexer");
        List<Document> bulkUpdates = new ArrayList<>();
        List<Document> docsListToBeInserted = new ArrayList<Document>();

        // Looping over every word.
        for (String word : IndexerTable.keySet()) {

            Document found = collection.find(new Document("Word", word)).first();

            // Word is new.
            if (found == null) {
                // Variable for holding a document for each word to be inserted in the database.
                Document docToBeInserted = new Document("Word", word);

                // List of Documents per word, each document contains data of url that contains the word.
                List<Document> wordProperty = new ArrayList<Document>();

                // Looping over every document associated with the word.
                for (int i = 0; i < IndexerTable.get(word).size(); i++) {

                    // i-> index of every document associated with the word.
                    Document wordDocData = IndexerTable.get(word).get(i);
                    Document WordDocProperties = new Document();

                    // Extracting data to be inserted in each associated documented.
                    String url = String.valueOf(wordDocData.get("URL"));
                    String title = String.valueOf(wordDocData.get("Title"));
                    String IDF_TF = String.valueOf(wordDocData.get("IDF_TF"));


                    WordDocProperties.append("URL", url);
                    WordDocProperties.append("Title", title);
                    WordDocProperties.append("IDF_TF", IDF_TF);

                    // Adding each document for a single word.
                    wordProperty.add(WordDocProperties);
                }

                // Appending the associated documents to each word.
                docToBeInserted.append("FoundInDocs", wordProperty);

                // Adding the final document containing word and associated documents in a list to be inserted in db later
                docsListToBeInserted.add(docToBeInserted);
            }
            else
            {
                // Looping over every document associated with the word.
                for (int i = 0; i < IndexerTable.get(word).size(); i++) {

                        // i-> index of every document associated with the word.
                        Document wordDocData = IndexerTable.get(word).get(i);
                        Document WordDocProperties = new Document();

                        // Extracting data to be inserted in each associated documented.
                        String url = String.valueOf(wordDocData.get("URL"));
                        String title = String.valueOf(wordDocData.get("Title"));
                        String IDF_TF = String.valueOf(wordDocData.get("IDF_TF"));


                        // CHECKING THAT URL WASN'T ADDED BEFORE

                        // Retrieving a certain url associated with the word already existing in database
//                        Document query = new Document("Word", word).append("FoundInDocs.URL", url);
//                        Document projection = new Document("FoundInDocs", 1);
//                        Document result = collection.find(query).projection(projection).first();

                        // URL Hasn't been added before.
//                        if (result == null) {
//
//                            // Appending new url document associated to the word in db
////                            WordDocProperties.append("URL", url);
////                            WordDocProperties.append("Title", title);
////                            WordDocProperties.append("IDF_TF", IDF_TF);
//
//
//                            // Adding the update operation to the bulk updates list
//                            collection.updateOne(
//                                    new Document("Word", word),
//                                    new Document("$push", new Document("FoundInDocs", WordDocProperties))
//                            );
//
//                        }

                    WordDocProperties.append("URL", url);
                    WordDocProperties.append("Title", title);
                    WordDocProperties.append("IDF_TF", IDF_TF);

                    Document filter = new Document("Word", word)
                            .append("FoundInDocs", new Document("$not", new Document("$elemMatch",
                                    new Document("URL", url))));

                    Document update = new Document("$push", new Document("FoundInDocs", WordDocProperties));

                    bulkUpdates.add(new Document("$set", update).append("$filter", filter));
                }
            }
        }

        if (!bulkUpdates.isEmpty()) {
            List<WriteModel<Document>> bulkOperations = new ArrayList<>();

            for (Document update : bulkUpdates) {
                Document filter = update.get("$filter", Document.class);
                Document updateData = update.get("$set", Document.class);
                UpdateOneModel<Document> updateModel = new UpdateOneModel<>(filter, updateData);
                bulkOperations.add(updateModel);
            }

            BulkWriteOptions options = new BulkWriteOptions().ordered(false);
            BulkWriteResult bulkWriteResult = collection.bulkWrite(bulkOperations, options);
//                    int modifiedCount = bulkWriteResult.getModifiedCount();

//                    System.out.println(bulkWriteResult.wasAcknowledged());
        }

        if (docsListToBeInserted.size() != 0){
            collection.insertMany(docsListToBeInserted);
        }
        System.out.println("Inserted IndexerDocuments into Indexer Collection");
    }

    ///////////////////////////////////////// RANKER REQUESTS

    /**
     * This function takes the hashmap of urls and its corresponding rank and adds them
     * to Crawler Collection.
     */
    public void insertRank(HashMap<String, Double> ranks){
        MongoCollection<Document> collection = database.getCollection("WebCrawler");

        for (String URL: ranks.keySet()){
            Document filter = new Document("URL", URL);
            Document update = new Document("$set", new Document("Rank", ranks.get(URL)));
            collection.updateOne(filter, update);
        }

    }




    
    //////////////////////////////////////// CLOSING

    /**
     * Close connection to the database
     */
    public void close() {

        mongoClient.close();
    }

}
