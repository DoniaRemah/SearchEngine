package Indexer;

public class TestingIndexer {

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        MasterIndexer masterIndexer = new MasterIndexer(100);

        long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;

        // Convert milliseconds to minutes and seconds
        long minutes = (executionTime / 1000) / 60;
        long seconds = (executionTime / 1000) % 60;

        // Print the execution time
        System.out.println("Program execution time: " + minutes + " minutes, " + seconds + " seconds");

    }
}
