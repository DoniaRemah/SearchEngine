package PageRanker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.*;

/**
 * This class calculates the ranking of pages according to Popularity
 */
public class PopularityRanker {

    HashMap<String,List<String>> path = new HashMap<>();

    HashMap<String ,Double> pagerank = new HashMap<>();

    int numberOfIterations;

    /**
     * Constructor calls on readFromJson function and calls on the Ranking function
     */
    PopularityRanker(int numberOfIterations){
        readFromJson();
        Rank(path.size());
        this.numberOfIterations = numberOfIterations;

    }

    /**
     * This function calculates the page rank according to popularity according to PageRank Algorithm.
     */
    public void Rank(int totalNodes) {

        double InitialPageRank;

        //The number of links that each node points to
        double outgoingLinks = 0;

        // The probability, at any step, that the person will continue following links is a damping factor d.
        // The probability that they instead jump to any random page is 1 - d.
        double DampingFactor = 0.85;

        int IterationIndex = 1;
        InitialPageRank =(float) 1 /  totalNodes;

        // Initializing pagerank
        for (String url: path.keySet()) {
            pagerank.put(url,InitialPageRank);
        }


        while (IterationIndex <= numberOfIterations) // Iterations
        {
            // Looping over all pages
            for (String page:path.keySet()){

                outgoingLinks = path.get(page).size();
                double newRank = pagerank.get(page)+(pagerank.get(page) * (1/outgoingLinks));
                newRank = (1-DampingFactor)+ (DampingFactor*newRank);
                pagerank.put(page,newRank);

            }

            IterationIndex = IterationIndex + 1;
        }

        //TODO Export Ranks to db

    }


    /**
     * Function to read from json file and construct paths map.
     */
    void readFromJson(){

        ObjectMapper mapper = new ObjectMapper();

        try {
            PathData data = mapper.readValue(new File("paths.json"), PathData.class);
            String url = data.getURL();
            List<String> pointsTo = data.getPointsTo();

            path.put(url,pointsTo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
