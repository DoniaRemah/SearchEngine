package Indexer;

import DatabaseManagement.DBManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.tartarus.snowball.ext.englishStemmer;

public class Indexer implements Runnable{

    AtomicInteger crawlerDocsNum;
    List<org.bson.Document> crawlerDocs;

    int threadChunk;

    int numberOfThreads;
    int  threadID;
    int threadChunkRem;

    DBManager dbManager = new DBManager();
    Vector<String> SpamUrls;

    static Hashtable<String, List<org.bson.Document>> words_table = new Hashtable<String, List<org.bson.Document>>();

    Indexer(AtomicInteger cDocsNum, List<org.bson.Document> cDocs, int numberThreads, Hashtable<String, List<org.bson.Document>> wTable){

        this.crawlerDocs = cDocs;
        this.numberOfThreads = numberThreads;
        this.words_table = wTable;
        this.crawlerDocsNum = cDocsNum;
        this.threadChunk =  crawlerDocsNum.intValue()/numberOfThreads;
        this.threadChunkRem = crawlerDocsNum.intValue() % numberOfThreads;
    }
    @Override
    public void run() {
        threadID = Integer.parseInt(Thread.currentThread().getName());
        int modified_threadChunk=threadChunk;

        if (threadID == numberOfThreads-1){
            modified_threadChunk+= threadChunkRem;
        }

        int docStart = threadChunk*threadID;
        int docEnd = (threadChunk*threadID) + modified_threadChunk;

        for(int i=docStart; i<docEnd; i++){

            String url = crawlerDocs.get(i).get("URL").toString();
            if(crawlerDocs.get(i) !=null){
                org.bson.Document htmlDoc =crawlerDocs.get(i);
                // CALL ON INDEXING FUNCTION
                Indexing(url,htmlDoc);
            }


        }

        System.out.println("I am thread "+threadID+" Finished docs from index "+docStart +" to "+ (docEnd-1) );
    }

    private void Indexing(String URL, org.bson.Document htmlDoc){

        // TODO Remove all html doc processing and start from Stemming (WHEN CRAWLER IS FINISHED)

        String docTitle="";
//        // REMOVE HTML TAGS
        if (htmlDoc.get("Title") !=null){
            docTitle = htmlDoc.get("Title").toString();
        }
        String docContent="";
        if (htmlDoc.get("Content") != null){
            docContent = htmlDoc.get("Content").toString();
        }


//        // Removing all line breaks, any characters apart from letters and Whitespace.
//        docContent = docContent.replaceAll("\n", " ")
//                .replaceAll("[^a-zA-Z ]", " ")
//                .toLowerCase();
//
//        // Removing all Stop words.
//        try {
//            File StopWords = new File("StopWords.txt");
//            Scanner Words = new Scanner(StopWords);
//
//            while (Words.hasNextLine()) {
//                docContent = docContent.replaceAll("\\s+" +Words.nextLine() + "\\s+", " ");
//            }
//
//            Words.close();
//        } catch (IOException e) {
//            System.out.println(e);
//        }

        // Extracting words by splitting on whitespace
        String[] words = docContent.split("\\s+");

        // Stemming
        englishStemmer EnglishStemmer = new englishStemmer();

        // List of TermValue and index of word in document.
        Map<String, List<Integer>> stemmedWords = new HashMap<>();

        int word_index=0;
        for (String word: words) {
            if(word!="") {
                EnglishStemmer.setCurrent(word);
                EnglishStemmer.stem();
                String stemmed_word = EnglishStemmer.getCurrent();

                // Checking if the word already exists and increasing term frequency
                if (stemmedWords.containsKey(stemmed_word)) {
                    int old_tf = stemmedWords.get(stemmed_word).get(0);
                    old_tf++;

                    // Updating TF
                    stemmedWords.get(stemmed_word).set(0, old_tf);
                } else {
                    // Inserting TF and Index of word in document.
                    List<Integer> value_list = new ArrayList<>();
                    value_list.add(1);
                    value_list.add(word_index);
                    stemmedWords.put(stemmed_word, value_list);

                }
                word_index++;
            }

        }

        for(String word:stemmedWords.keySet()){
            double normalized_tf = stemmedWords.get(word).get(0)/ (double)words.length;

            // Checking if this document is a spam
            if (normalized_tf > 0.5){
                // Deleting document from crawler database to prevent further indexing.
                dbManager.deleteCrawlerDocument(URL);
                crawlerDocsNum.getAndDecrement();
                return;
            }

//            //   GETTING SNIPPET STRING
//            String snippetString = "";
//            int wordIndex = stemmedWords.get(word).get(1);
//            if (wordIndex - 400 < 0) {
//                if (wordIndex + 400< words.length){
//                    snippetString = unEditedParsedDoc.substring(0,wordIndex+400);
//                }else{
//                    snippetString = unEditedParsedDoc.substring(0,wordIndex);
//                }
//
//            } else if (wordIndex + 400 > words.length) {
//                if (wordIndex - 400 > 0){
//                    snippetString = unEditedParsedDoc.substring(wordIndex-400,wordIndex);
//                }
//            } else{
//                snippetString = unEditedParsedDoc.substring(wordIndex-400,wordIndex+400);
//            }

            org.bson.Document finalIndexerDoc =new org.bson.Document("Word",word).append("URL",URL)
                    .append("Title",docTitle)
                    .append("TF",normalized_tf);
//                    .append("Content",snippetString);

            // If word previously exists in map
            synchronized (words_table){
                if(words_table.containsKey(word)){
                    words_table.get(word).add(finalIndexerDoc);
                }else{
                    List<org.bson.Document> newdocList = new ArrayList<org.bson.Document>();
                    newdocList.add(finalIndexerDoc);
                    words_table.put(word,newdocList);
                }
            }

        }


    }


}
