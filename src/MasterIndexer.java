import DatabaseManagement.DBManager;

import java.util.*;


public class MasterIndexer {

    DBManager dbmanager = new DBManager();
    // Number of documents in database from crawler.
    int crawlerDocsSize;

    // Vector of spam urls
//    Vector<String> spamUrls = new Vector<String>();

    // Table used to store words in a document after performing indexer operations
    Hashtable<String, List<org.bson.Document>> words_table = new Hashtable<String, List<org.bson.Document>>();

    int number_threads;
    MasterIndexer(int numThreads){
        this.number_threads=numThreads;
        List<org.bson.Document> crawlerDocs = dbmanager.retrieveCrawlerDocuments();
        crawlerDocsSize =  crawlerDocs.size();
        // Calling WebIndexer
    }





}







