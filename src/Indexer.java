import DatabaseManagement.DBManager;
import com.mongodb.DB;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import javax.print.Doc;

import java.util.regex.Matcher;



public class Indexer implements Runnable{

    int crawlerDocsNum;
    List<org.bson.Document> crawlerDocs;

    int threadChunk;

    int numberOfThreads;
    int  threadID = Integer.parseInt(Thread.currentThread().getName());
    int threadChunkRem;

    DBManager dbManager = new DBManager();
    Vector<String> SpamUrls;

    Hashtable<String, List<org.bson.Document>> words_table = new Hashtable<String, List<org.bson.Document>>();

    Indexer(int cDocsNum, List<org.bson.Document> cDocs,int numberThreads,Vector<String> Spam_URLs , Hashtable<String, List<org.bson.Document>> wTable){

        this.crawlerDocs = cDocs;
        this.numberOfThreads = numberThreads;
        this.SpamUrls= Spam_URLs;
        this.words_table = wTable;
        this.crawlerDocsNum = cDocsNum;
        this.threadChunk = crawlerDocsNum/numberOfThreads;
        this.threadChunkRem = crawlerDocsNum%numberOfThreads;
    }
    @Override
    public void run() {
        int modified_threadChunk=threadChunk;
        if (threadID == numberOfThreads-1){
            modified_threadChunk+= threadChunkRem;
        }
        int docStart = threadChunk*threadID;
        int docEnd = (threadChunk*threadID) + modified_threadChunk;

        for(int i=docStart; i<docEnd; i++){

            String url = crawlerDocs.get(i).get("URL").toString();
            String htmlDoc = crawlerDocs.get(i).get("HTMLDoc").toString();
        }

        System.out.println("I am thread "+threadID+"Finished docs from index "+docStart +"to "+docEnd );
    }
}
