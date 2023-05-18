package Indexer;

import DatabaseManagement.DBManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class mainzft {

    public static void main(String[] args) {
        Hashtable<String, List<Document>> IndexerTable = new Hashtable<>();
        List<Document> doc = new ArrayList<>(2);
        doc.add(0, new Document().append("URL", "bla bla").append("Title", "ay zft").append("IDF_TF", "3374"));
        doc.add(1, new Document().append("URL", "donia").append("Title", "yrb").append("IDF_TF", "sd"));
        IndexerTable.put("peculiar",doc);
        DBManager dbManager = new DBManager();
        dbManager.insertIndexerDocs(IndexerTable);
    }
}
