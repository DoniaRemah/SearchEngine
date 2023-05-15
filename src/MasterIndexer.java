import DatabaseManagement.DBManager;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicInteger;
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
    MasterIndexer(int numThreads) throws InterruptedException {
        this.number_threads=numThreads;
        List<org.bson.Document> crawlerDocs = dbmanager.retrieveCrawlerDocuments();
        crawlerDocsSize =  crawlerDocs.size();

        // Using AtomicInteger to find the actual docs without spams
        // Automatically synchronizes
        AtomicInteger actualCrawlerDocsSize = new AtomicInteger(crawlerDocsSize);

        // Calling Indexer
        Runnable indexer = new Indexer(actualCrawlerDocsSize,crawlerDocs,number_threads,words_table);
        runThreads(number_threads,indexer);

        //// CALCULATING IDF-TF
        for (String word: words_table.keySet()){
           Double IDF = (double) ( actualCrawlerDocsSize.intValue()/ words_table.get(word).size());
           int index=0;
           for (Document wordDoc: words_table.get(word)) {
               Double IDFTF = IDF*Integer.parseInt((String) wordDoc.get("TF"));
               words_table.get(word).get(index).append("IDF_TF", IDFTF);
               index++;
           }
        }

        dbmanager.insertIndexerDocs(words_table);

    }


    public static void runThreads(int numberOfThreads, Runnable indexer) throws InterruptedException {
        Vector<Thread> threads = new Vector<Thread>();

        // Running threads
        for (int i = 0; i < numberOfThreads; i++) {
            Thread myindexer = new Thread(indexer);
            myindexer.setName(Integer.toString(i));
            threads.add(myindexer);
            myindexer.start();
        }

        // Waiting for all threads to finish
        for (int i = 0; i < numberOfThreads; i++) {
            threads.get(i).join();
        }
    }





}







