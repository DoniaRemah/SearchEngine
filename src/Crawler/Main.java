package Crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Main {
    private static List<String> seeds;

    //now we want to add the seeds:


    private static List<String> readSeeds(String filename) {
        List<String> seeds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                seeds.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seeds;
    }


    public static void main(String[] args) {
        seeds = readSeeds("src/Crawler/seed.txt");

        // TODO REMOVE THIS
        // Print the seeds for verification
        for (String seed : seeds) {
            System.out.println("Seeds for the crawler:"+seed);
        }

        Vector<WebCrawler> crawlers = new Vector<>();


        //now let the user decide on number of threads for all crawlers:

        System.out.print("Please enter number of threads u need for the 6 crawlers: ");
        Scanner scanner = new Scanner(System.in);
        int numberOfThreads = scanner.nextInt();
        //System.out.println("User entered: " + numberOfThreads);



        //we will add 6 crawlers:
        crawlers.add(new WebCrawler(seeds, 1, numberOfThreads));

        // TODO RETURN TO THIS IDEA AT THE END
//        crawlers.add(new WebCrawler(seeds, 2, numberOfThreads));
//        crawlers.add(new WebCrawler(seeds, 3, numberOfThreads));
//        crawlers.add(new WebCrawler(seeds, 4, numberOfThreads));
//        crawlers.add(new WebCrawler(seeds, 5, numberOfThreads));
//        crawlers.add(new WebCrawler(seeds, 6, numberOfThreads));



        for(WebCrawler bot: crawlers){
            Vector<Thread> threads;
            //get thread.join
            threads= bot.getThreads();
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}