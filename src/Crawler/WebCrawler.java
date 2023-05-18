package Crawler;
import java.net.URLEncoder;

import DatabaseManagement.DBManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.bson.*;

import javax.print.Doc;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
make 6 crawrlers =>
each crawler will have 1000 page,


and we would have N threads, each thread 1000/N page

 */
public class WebCrawler implements Runnable {
    //things that will be seen by all crawlers and all threads:
    /*
    => currentCrawledPages, disallowed_URLs, linksToCrawl, DocumentsAndUrlsWithPrio
    max crawled pages, maxpagespercrawler, maxpagesperthread
     */


    private static final String STATE_FILE_PATH = "crawlerState.txt";
    private static final int MAX_CRAWLED_PAGES = 6000 ;
//    private static final int MAX_PAGES_PER_CRAWLER = 1000;
//    private static final int MAX_PAGES_PER_THREAD = 100;

    private static int numberOfThreads; //input from user and this is per instance
    public static Vector<Thread> threads;

    public static AtomicInteger currentCrawledPages =  new AtomicInteger(0);

    public static Vector<String> disallowed_URLs= new Vector<String>();

    private static boolean exit = false;
    private static List<String> seeds;
    public static  Queue<String> linksToCrawl = new LinkedList<>();

    public int crawlerNum;

    //this map contains visited urls(keys) , their document and how many times this doc was referred
    public static HashMap<String, PageDocument> DocumentsAndUrlsWithPrio = new HashMap<>();

    public static HashSet <String> visitedLinks;

    static List<String> LinkPointsTo= new ArrayList<>();

    //----------------------------------Constructor-------------------------------------------------------
    public WebCrawler(List<String> seeds, int num , int numberOfThreads){
        //for any crawler at the beginning he needs the seed
        WebCrawler.seeds = seeds;
        //number
        this.crawlerNum= num;
        //print for debugging
        System.out.println("Webcrawler " + num + " created");

        //We also need to put the seeds in our queue
        linksToCrawl.addAll(seeds);

        WebCrawler.numberOfThreads = numberOfThreads;

        //for threads, each crawler should have a vector of 10 threads
        // Create and start 10 threads
        threads = new Vector<Thread>();
        for (int i = 0; i < numberOfThreads; i++) {
            Thread thread = new Thread(this); // Create thread using the current instance of WebCrawler
            //set name by ID
            thread.setName(Integer.toString(i));
            threads.add(thread);
            System.out.println("New Thread Creates"+thread.getName());
            thread.start();
        }

    }
    //-----------------------------------------------------------------------------------------------------

     //-----------------------------------------------------RUN-------------------------------------------
    @Override
    public void run() {
        //in the run we want to crawl as each thread will start crawling process
       // System.out.println("I am thread"+Thread.currentThread().getName());
        crawl();

    }
    //-----------------------------------------------------------------------------------------------------
    // for stopping the thread
    public void stop()
    {
        exit = true;
    }

    //-------------------------------------------Crawl Function---------------------------------------------------
    /*
    Notes:
    ------

    A while loop till linkstocrawl are empty or we reached max_num_per_threads  which is 100 right????
    Q. what about currentcrawls?? how will it work for each thread separately and not get shared???

    Things that would need mutex locks and synchronization:
                                                            linkstoCrawl, disallowedLinks, DocumentsAndUrlsWithPrio

    Sequence:
    -------
    1. Read the url from queue  done

    2. check that this url is not in the disallowed links or visited     doneeeee

    3. Normalize link  --SKIPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP XXXXX    (we already do it i extracting function)
    4. See if there exists a robot.txt    donee


    5. if yes we call teh function to see the disallowed urls and save them into the list  donee


    6. Now we download teh document from the url (function that returns the document) doneeee
    7. we extract the links, save them in queue[function that extracts link, normalize it and adds it to queue]
    note that links to crawl can have duplicates [we compare when adding to the map]  doneeee
    8. Add to the map by calling the function


    //In function Robot File====> we need to make mutex lock before adding to disallowed links

    //In function crawl when getting url do a lock before seeing if link is in disallowed or nor

    //In function Extarctlinks , we need to have a lock before adding to queue

    //In function AddDocument  we need to put a lock on the map (or the function overall)


    //At the end we MUSTTTT save STATE   to be able to retrieve it again later
    //and at the beginning we see if we have to loadstate or not

    Question===> in map document compression???????

     */
    public static void crawl(){
        //check that its generated + not empty
        //we want ti retrieve state if got interuupted
        //if it got interuppted it will alwasy be out of the while loop so it will check if file exists or not
        //if exists then load if not dont load



        //so max number of pages per thread is determined by user
       // int totalPages=0;

        int totalPages= loadStateFromFile();
        System.out.println("after loading, the current crawled are:" + totalPages);


        System.out.println("after loading, size of docs ar1e:" + DocumentsAndUrlsWithPrio.size());

        if (DocumentsAndUrlsWithPrio !=null){
            synchronized (DocumentsAndUrlsWithPrio){
                totalPages = DocumentsAndUrlsWithPrio.size();
                if(totalPages >= MAX_CRAWLED_PAGES){
                    return;
                }
            }
        }

        boolean isEmpty=false;
        while ( totalPages< MAX_CRAWLED_PAGES ){
            if (linksToCrawl != null){
                synchronized (linksToCrawl){
                    isEmpty=linksToCrawl.isEmpty();
                }
                if(isEmpty == false){
                    break;
                }
            }
//            if (DocumentsAndUrlsWithPrio !=null){
//                synchronized (DocumentsAndUrlsWithPrio){
//                    totalPages = DocumentsAndUrlsWithPrio.size();
//                    if(totalPages >= MAX_CRAWLED_PAGES){
//                        return;
//                    }
//                }
           // }
        }

        // TODO STOP EXECUTION OF ALL THREADS SOMEHOW
        System.out.println("//////////////////TOTAL COUNT://///////////// "+totalPages);

        // Loop until there are no more links to crawl or the maximum number of crawled pages is reached
        while (totalPages < MAX_CRAWLED_PAGES && !isEmpty) {

            String url="";
            if (linksToCrawl != null){
                synchronized (linksToCrawl) {
                    url = linksToCrawl.poll();
                    currentCrawledPages.incrementAndGet();
                }
            }

            //note that the url we got is already normalized as linkstoCRawl has normalzied links

            if(url==null){
                currentCrawledPages.decrementAndGet();
                continue;
            }
            System.out.println(Thread.currentThread().getName()+" after fetching url from linkstoCrawl: " + url);
            // Check if the URL is disallowed or not in visited
            boolean isDisallowed = false;

            if (disallowed_URLs != null) {
                synchronized (disallowed_URLs) {
                    for (String disallowedUrl : disallowed_URLs) {
                        if (url.equals(disallowedUrl)) {
                            isDisallowed = true;
                            break;
                        }
                    }
                }
            }
//            System.out.println("is disallwed? " + isDisallowed);
            if(isDisallowed){
                //skip
                currentCrawledPages.decrementAndGet();
                continue;
            }


            //if visited before:
            boolean isvisited= false;
            if (visitedLinks != null) {
                synchronized (visitedLinks) {
                    for (String visitedurl : visitedLinks) {
                        if (url.equals(visitedurl)) {
                            isvisited = true;
                            break;
                        }
                    }
                }
            }
//            System.out.println("is visited before? " + isvisited);
            if (isvisited) {
                //skip
                currentCrawledPages.decrementAndGet();
                continue;
            }else {
                //put in visited
                if (visitedLinks !=null){
                    synchronized (visitedLinks) {
                        visitedLinks.add(url);
                    }
                }

            }


            boolean callContinue = false;
            //now see robot file:
            URL roboturl = getRobotTxt(url);
            System.out.println("robot url if exists " + roboturl);
            if(roboturl != null){
                try {
                    ReadRobotTxt(roboturl,url);
                } catch (MalformedURLException e) {
                    currentCrawledPages.decrementAndGet();
                    //currentCrawledPages.decrementAndGet();
                    System.out.println (e.toString());
                    continue;
                }
            }



            // Download the document from the URL
            Document htmlDoc = download(url);
            //System.out.println("after downloading doc from url " + htmlDoc);

            // Check if the document was downloaded successfully
            if (htmlDoc == null) {
                currentCrawledPages.decrementAndGet();
                continue;
            }

            LinkPointsTo.clear();

            // Extract links from the document
            try {
                ExtractLinks(htmlDoc);

            } catch (IOException e) {
                currentCrawledPages.decrementAndGet();
                System.out.println (e.toString());
                continue;
            }


            //now lets clean up the document
            //htmlDoc = cleanup(htmlDoc);
            org.bson.Document DocToBeAdded = cleanup(htmlDoc);


            // Add the document to the map
            //NEWWWWWWWWWWWWWWWWWWWWWWWWW-------------------------------------------------
            //BEFORE ADDINGT TO DOC CHECK THAT SIZE DIDNT REACH MAX
            if (DocumentsAndUrlsWithPrio !=null){
                synchronized (DocumentsAndUrlsWithPrio) {
                    if(DocumentsAndUrlsWithPrio.size()>=MAX_CRAWLED_PAGES){
                        return;
                    }
                    else{
                        addDocument(DocToBeAdded, url);
                    }

                }
            }
            if (DocumentsAndUrlsWithPrio != null){
                synchronized (DocumentsAndUrlsWithPrio){
                    totalPages = DocumentsAndUrlsWithPrio.size();
                }
                if(totalPages >= MAX_CRAWLED_PAGES){
                    return;
                }

            }

            System.out.println("//////////////////TOTAL COUNT://///////////// "+totalPages);

           // System.out.println("after downloading doc from url " + htmlDoc);

            //incrementing page:

            //save to state every time we crawl
            saveStateToFile(totalPages);

        }

        System.out.println("Total crawled pages:  " + currentCrawledPages);


    }




    //---------------------------------------------------------------------------------------------------

    //-------------------------------------------Crawler Methods----------------------------------------------
    //---------------------------------------------------------------------------------------------------------

    //----------------------------------------For saving states--------------------------------------
    private static  void saveStateToFile(int totalPages) {
        try {
            // Create a file writer to write the crawler's state to a file
            FileWriter fileWriter ;

            synchronized (STATE_FILE_PATH) {
                fileWriter = new FileWriter(STATE_FILE_PATH);


                // Write the current state to the file
                //To save state we need the following:
            /*
                ==> currentCrawled pages
                ==> disallowed urls
                ==> linksToCrawl   (to start from them and continue)
                ==>  vistedLinks (to not revist them)
                ==> ofcourse the DocumentsAndUrlsWithPrio at the end
             */
                //This will save every thing in one line and the items are sperated by ,
                // fileWriter.write(currentCrawledPages.get() + "\n");

                fileWriter.write(totalPages + "\n");
                synchronized (disallowed_URLs) {
                    fileWriter.write(String.join(",", disallowed_URLs) + "\n");
                }
                synchronized (linksToCrawl) {
                    fileWriter.write(String.join(",", linksToCrawl) + "\n");
                }
                //fileWriter.write(String.join(",", visitedLinks) + "\n");

                if (visitedLinks != null) {
                    synchronized (visitedLinks) {
                        fileWriter.write(String.join(",", visitedLinks) + "\n");
                    }
                }


                // TODO IF WE DEPLOYED LOCAL, MAKE IT GET THE DOCUMENTS FROM DB
                //  fileWriter.write(DocumentsAndUrlsWithPrio + "\n");

                synchronized (DocumentsAndUrlsWithPrio) {
                    for (Map.Entry<String, PageDocument> entry : DocumentsAndUrlsWithPrio.entrySet()) {
                        String url = entry.getKey();
                        PageDocument pageDoc = entry.getValue();
                        String title = pageDoc.getTitle();
                        String document = pageDoc.getHtmlDoc();
                        //List<String> urls = pageDoc.getPointsTo();
                        List<String> urls = new ArrayList<>(pageDoc.getPointsTo());
                        String outputLine = url + "\\|" + title + "\\|" + document + "\\|" + String.join(",", urls);
                        //String outputLine = url + "\\|" + title + "\\|" + document + "\\|" + String.join(",", urls);
                        fileWriter.write(outputLine + "\n");
                    }
                }

                // Close the file writer
                fileWriter.close();
            }
        } catch (IOException e) {
            // Log the error
            System.err.println("Error while saving crawler state: " + e.getMessage());
        }
    }



    private static  int loadStateFromFile() {
        //checking first that file exists
       // File stateFile = new File(STATE_FILE_PATH);

        File stateFile ;

        synchronized (STATE_FILE_PATH){
            stateFile = new File(STATE_FILE_PATH);
        }


        int totalPages=0;
        if (!stateFile.exists()) {
            System.out.println("State file not found.");
            return 0;
        }
        try {
            // Create a file reader to read the crawler's state from a file
            FileReader fileReader = new FileReader(stateFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Check if the file is still empty  [nth to load]
            String firstLine= bufferedReader.readLine();
            if ( firstLine== null) {
                System.out.println("file is empty");
                bufferedReader.close();
                fileReader.close();
                return 0;
            }

            // Read the saved state from the file
            //System.out.println(bufferedReader.readLine());
           // System.out.println(Integer.parseInt(bufferedReader.readLine()));

            currentCrawledPages.set(Integer.parseInt(firstLine));
            totalPages = currentCrawledPages.get();
            disallowed_URLs = new Vector<>(Arrays.asList(bufferedReader.readLine().split(",")));
            linksToCrawl = new LinkedList<>(Arrays.asList(bufferedReader.readLine().split(",")));
            visitedLinks = new HashSet<>(Arrays.asList(bufferedReader.readLine().split(",")));
            DocumentsAndUrlsWithPrio = new HashMap<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                String url = parts[0];
                String title = parts[1];
                String stringdoc = parts[2];
                String[] urlList = parts[3].split(",");
                List<String> urls = Arrays.asList(urlList);

                //String[] parts = line.split(":");
                //String url = parts[0];
                //String stringdoc = parts[2];
                //String title = parts[1];
                //String[] urlList = parts[3].split(",");
                //List<String> urls = Arrays.asList(urlList);
                ///int prio = Integer.parseInt(parts[3]);
                Document doc = Jsoup.parse(stringdoc);
                PageDocument pageDoc = new PageDocument(stringdoc, title);
                pageDoc.setPointsTo(urls);
                synchronized (DocumentsAndUrlsWithPrio) {
                    DocumentsAndUrlsWithPrio.put(url, pageDoc);
                }
            }

            // Close the file reader
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            // Log the error
            System.err.println("Error while loading crawler state: " + e.getMessage());
        }
        return totalPages;
    }

    //------------------------------------------------------------------------------------------------


    //------------------------------------------Download DOc from URL-------------------------------
    public static Document download(String url){
        try{
            Connection con= Jsoup.connect(url);
            Document htmlDoc= con.get();

            if(con.response().statusCode()==200){
                System.out.println("Received webpage at " + url);
                //if we need the title for later???

//                String title= htmlDoc.title();
            }

            return htmlDoc;
        }
        catch(IOException e){
            System.err.println("Error while downloading document: " + e.getMessage());

            return null;
        }
    }

    //---------------------------------Adding Document and its details to the map---------------------------------------------------------------

    //when we add the doc we want to check 2 things
    //if url we have now already exists=> we increment counter
    //if not we add a new entry
    //we also check that the document didn't exist before after applying string compaction
    //as we can have 2 different links still pointing on same page even after normalization
    //this is static as its seen by all instances***
    public static void addDocument (org.bson.Document htmlDoc, String url){
        //url get it from the mapofpages and if it doesn't exist we will add new page to map
        //else we will increment the count of that existing page
        if (DocumentsAndUrlsWithPrio != null) {
            synchronized (DocumentsAndUrlsWithPrio) {
                String title = htmlDoc.get("title").toString();

                boolean documentExists = false;
                boolean urlExists = DocumentsAndUrlsWithPrio.containsKey(url);

                PageDocument currentPage;

                //if url exists get currentpage and incremeent it
                if (urlExists) {
                    //this returns PageDocument
                    currentPage = DocumentsAndUrlsWithPrio.get(url);
                    currentPage.incrementCount();
                    //return if url already exists
                    return;
                }

                //now check for the document:
                for (Map.Entry<String, PageDocument> entry : DocumentsAndUrlsWithPrio.entrySet()) {
                    if (entry.getValue().getHtmlDoc().equals(htmlDoc)) {
                        documentExists = true;
                        break;
                    }
                }
                //now check if document didn't exist add
                if (!documentExists) {

                    // add to map
                    PageDocument myNewPage = new PageDocument(htmlDoc.get("HTMLDoc").toString(), title);
                    myNewPage.setPointsTo(LinkPointsTo);
                    DocumentsAndUrlsWithPrio.put(url,myNewPage );

                }
            }
        }

        //if document exists before dont do anything

        //saving state: visited, linkstocrawl, currentcrawledpages (total)
        //desrtuctor
    }
    //----------------------------------------------------------------------------------------------------

    //---------------------------------------------Document compaction----------------------------------

    public static org.bson.Document compressHtml(String html) {
        // REMOVE HTML TAGS
        Document parsedHtmlDoc = Jsoup.parse(html);
        String docTitle = parsedHtmlDoc.title() ;
        String docContent = parsedHtmlDoc.text();

        // Removing all line breaks, any characters apart from letters and Whitespace.
        docContent = docContent.replaceAll("\n", " ")
                .replaceAll("[^a-zA-Z ]", " ")
                .toLowerCase();

        return new org.bson.Document("HTMLDoc",docContent).append("title",docTitle);


    }

    //function to remove stop words:
    //after compressing
    public static org.bson.Document RemoveStopWords(org.bson.Document htmlDoc) {
        String htmlDocString = htmlDoc.get("HTMLDoc").toString();
        //String filteredhtmlDocString;
        try {
            File StopWords = new File("src/StopWords.txt");
            Scanner Words = new Scanner(StopWords);

            while (Words.hasNextLine()) {
                htmlDocString=htmlDocString.replaceAll("\\s+" + Words.nextLine() + "\\s+", " ");
            }
            Words.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        return htmlDoc.append("HTMLDoc",htmlDocString);
    }

    public static org.bson.Document cleanup(Document htmlDoc){

        org.bson.Document newdoc = compressHtml(htmlDoc.toString());

        newdoc= RemoveStopWords(newdoc);

        return newdoc;

    }


    //----------------------------------------------------------------------------------------------------------
    //--------------------------------------------Extracting links and adding them to queue----------------------------
    //Function to extract the links in a given page:
    public static void ExtractLinks(Document htmlDoc) throws IOException {
        String normalizedLink;

        Elements links = htmlDoc.select("a[href]"); // a with href

        //htmlExtractedLinks.clear();

        for (Element link : links) {
            String linkHref = link.absUrl("href");
            //first we want to check if the link was added before in the queue already
            //or if the link was already visited (should be in the map)
            //to do that we must normalize first

            try {
               normalizedLink = normalizeURL(linkHref);
            } catch (URISyntaxException e) {
                System.out.println (e.toString());
                continue;
            }

            LinkPointsTo.add(normalizedLink);
            //now compare before adding to queue   -- we dont compare here i guess
            if (linksToCrawl != null){
                synchronized (linksToCrawl) {
                    linksToCrawl.add(normalizedLink);
                }
            }

//            if (!linksToCrawl.contains(normalizedLink) && DocumentsAndUrlsWithPrio.containsKey(normalizedLink)) {
//                linksToCrawl.add(normalizedLink);
//            }
        }
    }
    //----------------------------------------------------------------------------------------------------

    //--------------------------------------------URL normalization----------------------------------------
    public static String normalizeURL(String url) throws URISyntaxException, UnsupportedEncodingException {


        //  TODO ERG3 SHUF EL COMPRESSOR
        //String encodedUrl = URLEncoder.encode(url, StandardCharsets.US_ASCII);
//
        URI uri = new URI(url);

        //return uri.normalize().toString();
        uri = uri.normalize();

        //port -- need normalization
        //will return either -1 or the port
        int port = uri.getPort();
        int normalizedPort = normalizePort(uri.getScheme(), port);


        //normalize host

        String host = uri.getHost();
        URI normalized_URL;
        if (host != null && host.toLowerCase().startsWith("www.")) {
            host = host.substring(4); // remove "www."
             normalized_URL = new URI(uri.getScheme(), uri.getUserInfo(), host, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        } else {
             normalized_URL = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), normalizedPort, uri.getPath(), uri.getQuery(), uri.getFragment());
        }

        return normalized_URL.toString();


    }


    //in port normalization, we check if the port number is = to the scheme default then we remove it else we keep it
    //http default port is 80, while https default is 443
    private static int normalizePort(String scheme, int port) {
        switch (port) {
            case 80 -> {
                if ("http".equals(scheme)) {
                    return -1;   //remove it
                }
            }
            case 443 -> {
                if ("https".equals(scheme)) {
                    return -1;    //remove it
                }
            }
        }
        return port;
    }


    //---------------------------------------------------------------------------------------------------

    //--------------------------------------Robot Txt File-------------------------------------------------
    //this function checks if robot text file exists if yes returns it else return null
    //dooesnt need synch
    public static URL getRobotTxt(String normalizedUrl) {
        boolean doesRobotTxtExist =false;
        URL robotUrl = null;
        try {
            URL url = new URL(normalizedUrl);
            String host = url.getHost();
            robotUrl = new URL(url.getProtocol() + "://" + host + "/robots.txt");
            HttpURLConnection connection = (HttpURLConnection) robotUrl.openConnection();
            connection.setRequestMethod("GET");
            //this.robotTxtUrl = robotUrl.toString();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                doesRobotTxtExist = true;
            }
        } catch (Exception e) {
            // handle exceptions
        }
        if(doesRobotTxtExist){
            return robotUrl;
        }
        else{
            return null;
        }

    }

    //function to read the robot text (parse) and extract disallowed
    public static void ReadRobotTxt( URL robotUrl,String normalizedUrl ) throws MalformedURLException {
        //URL url = new URL(this.robotTxtUrl);
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(robotUrl.openStream()))) {
            boolean user_agent = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("User-Agent") || line.contains("User-agent:")) {
                    // check if it has all agents (*) then we store the lines beneath
                    user_agent = line.equals("User-Agent: *") || line.equals("User-agent: *");
                    continue;  //as we will skip and see whether user_agent was true or not to see if we will save the lines after it
                }
                //if useragent = true and the line has Disallow save whats after it
                if (user_agent && line.contains("Disallow:")){

                    URL url = new URL(normalizedUrl);
                    String host = url.getHost();

                    //remove disallow and any space after it

                    String[] disallowedUrl_array = line.replaceAll("\\s+", "").split(":");
                    String disallowedUrl="";

                    if (disallowedUrl_array.length == 2){
                        disallowedUrl=disallowedUrl_array[1];
                    }

                    String disallowed_url_host = new URL(url.getProtocol() + "://" + host +disallowedUrl ).toString();
                    if (disallowed_URLs != null){
                        synchronized (disallowed_URLs) {
                            disallowed_URLs.add(disallowed_url_host);
                        }
                    }
                }
            }


        } catch (IOException e) {
            System.out.println (e.toString());
        }


    }



    //---------------------------------------------------------------------------------------------


    //---------------------------------------setters & getters----------------------------------------------------
    public Vector<Thread> getThreads() {
        return threads;
    }




    //---------------------------------------------------------------------------------------------------------




    //----------------------------AAAAAAAAAAAAA END of Web Crawler el7 AAAAAAAAAAAAA-----------------------

}



//--------------------------------------------------------Draft------------------------------------------------

 /*
    steps:
    -----
    note we can have multiple links (same links) in teh queue
    1. get a url from the queue of normalized links 1 link 1 document (seed)
    2. see if it has a robot.txt file
    3. if yes put the disallowed urls in the vector after normalizing them
    4. extract document
    6. put document in map [page, link, no.of referrals] at the beg 1
    7. extract links from the document
    8. normalize them , compare them with list of disallowed, put in queue if allowed

    loop till no of pages in the map are 6000
    1. take a link (normalized)
    2. see robot.txt
    3. if yes put disallowed urls in teh vector
    4. extarct doc
    5. compare the link with all links we have in the map [in normalized form] if a link exists before incremnet the referrals
    6. put doc in map if link is not repeated and page is not repeated
    7. extract links from document
    8.normalize, compare with list of disallowed, put in queue if allowed

     */
