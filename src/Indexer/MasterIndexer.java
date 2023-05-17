package Indexer;

import DatabaseManagement.DBManager;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;


// DELETE THOSEE
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

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
        this.number_threads = numThreads;

        //List<org.bson.Document> crawlerDocs = dbmanager.retrieveCrawlerDocuments();
        //crawlerDocsSize =  crawlerDocs.size();

        // TESTING
        crawlerDocsSize = 4;

        // Using AtomicInteger to find the actual docs without spams
        // Automatically synchronizes

        AtomicInteger actualCrawlerDocsSize = new AtomicInteger(crawlerDocsSize);

        // TODO FOR TESTING PURPOSES (DELETE WHEN CRAWLER IS FINISHED)

        List<org.bson.Document> crawlerDocs = new ArrayList<>();

        String [] urls = {
                "https://www.britannica.com/art/perfume",
                "https://deathnote.fandom.com/wiki/Pursuit",
                "https://www.mcgill.ca/oss/article/history/story-perfume",
                "https://www.recipegirl.com/how-to-make-iced-coffee/"
        };

        for(String strUrl : urls){
            StringBuilder htmlContent = new StringBuilder();
            try {
                // Specify the URL of the HTML page

                URL url = new URL(strUrl);

                // Open a connection to the URL
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                // Read the HTML content line by line
                htmlContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    htmlContent.append(line);
                }

                // Close the reader
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Document testdoc = new Document();
            testdoc.append("URL",strUrl).append("HTMLDoc",htmlContent.toString());

            crawlerDocs.add(testdoc);
        }



        ////////////////////////////////////////// END OF TEST

        if (number_threads > actualCrawlerDocsSize.intValue()){
            number_threads = actualCrawlerDocsSize.intValue();
        }

        // Calling Indexer
        Runnable indexer = new Indexer(actualCrawlerDocsSize,crawlerDocs,number_threads,words_table);

        runThreads(number_threads, indexer);

        //// CALCULATING IDF-TF
        for (String word : words_table.keySet()) {
            Double IDF = (double) (actualCrawlerDocsSize.intValue() / words_table.get(word).size());
            int index = 0;
            for (Document wordDoc : words_table.get(word)) {
                Double IDFTF = IDF * (Double) wordDoc.get("TF");
                words_table.get(word).get(index).append("IDF_TF", IDFTF);
                index++;
            }
        }

        dbmanager.insertIndexerDocs(words_table);
        dbmanager.close();
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







