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

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    private static final int MAX_CRAWLED_PAGES = 50;
//    private static final int MAX_PAGES_PER_CRAWLER = 1000;
//    private static final int MAX_PAGES_PER_THREAD = 100;

    private static int numberOfThreads; //input from user and this is per instance
    public Vector<Thread> threads;

    public static AtomicInteger currentCrawledPages =  new AtomicInteger(0);

    public static Vector<String> disallowed_URLs= new Vector<String>();

    public static DBManager dbManager = new DBManager();

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
        System.out.println("I am thread"+Thread.currentThread().getName());
        crawl();
    }
    //-----------------------------------------------------------------------------------------------------


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

        // TODO TEST LOAD STATE
        //loadStateFromFile();


        //so max number of pages per thread is determined by user

        boolean isEmpty=false;
        while (isEmpty && currentCrawledPages.get() < MAX_CRAWLED_PAGES ){
            if (linksToCrawl != null){
                synchronized (linksToCrawl){
                    isEmpty=linksToCrawl.isEmpty();
                }
            }
        }



        // Loop until there are no more links to crawl or the maximum number of crawled pages is reached
        while (currentCrawledPages.get() < MAX_CRAWLED_PAGES && !isEmpty) {

            currentCrawledPages.incrementAndGet();

            String url="";
            if (linksToCrawl != null){
                synchronized (linksToCrawl) {
                    url = linksToCrawl.poll();
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
            System.out.println("is disallwed? " + isDisallowed);
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
            System.out.println("is visited before? " + isvisited);
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



            //now see robot file:
            URL roboturl = getRobotTxt(url);
            System.out.println("robot url if exists " + roboturl);
            if(roboturl != null){
                try {
                    ReadRobotTxt(roboturl,url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }



            //now lets clean up the document
            //htmlDoc = cleanup(htmlDoc);
            org.bson.Document DocToBeAdded = cleanup(htmlDoc);


            // Add the document to the map
            if (DocumentsAndUrlsWithPrio !=null){
                synchronized (DocumentsAndUrlsWithPrio) {
                    addDocument(DocToBeAdded, url);
                }
            }


           // System.out.println("after downloading doc from url " + htmlDoc);

            //incrementing page:

            //save to state every time we crawl
            //saveStateToFile();

        }

        System.out.println("Total crawled pages:  " + currentCrawledPages);
        //dbManager.close();


    }




    //---------------------------------------------------------------------------------------------------

    //-------------------------------------------Crawler Methods----------------------------------------------
    //---------------------------------------------------------------------------------------------------------

    //----------------------------------------For saving states--------------------------------------
    private static void saveStateToFile() {
        try {
            // Create a file writer to write the crawler's state to a file
            FileWriter fileWriter = new FileWriter(STATE_FILE_PATH);

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
            fileWriter.write(currentCrawledPages.get() + "\n");
            fileWriter.write(String.join(",", disallowed_URLs) + "\n");
            fileWriter.write(String.join(",", linksToCrawl) + "\n");
            fileWriter.write(String.join(",", visitedLinks) + "\n");

            // TODO IF WE DEPLOYED LOCAL, MAKE IT GET THE DOCUMENTS FROM DB
            fileWriter.write(DocumentsAndUrlsWithPrio + "\n");

            // Close the file writer
            fileWriter.close();
        } catch (IOException e) {
            // Log the error
            System.err.println("Error while saving crawler state: " + e.getMessage());
        }
    }



    private static void loadStateFromFile() {
        //checking first that file exists
        File stateFile = new File(STATE_FILE_PATH);
        if (!stateFile.exists()) {
            System.out.println("State file not found.");
            return;
        }
        try {
            // Create a file reader to read the crawler's state from a file
            FileReader fileReader = new FileReader(stateFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Read the saved state from the file
            currentCrawledPages.set(Integer.parseInt(bufferedReader.readLine()));
            disallowed_URLs = new Vector<>(Arrays.asList(bufferedReader.readLine().split(",")));
            linksToCrawl = new LinkedList<>(Arrays.asList(bufferedReader.readLine().split(",")));
            visitedLinks = new HashSet<>(Arrays.asList(bufferedReader.readLine().split(",")));
            DocumentsAndUrlsWithPrio = new HashMap<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(":");
                String url = parts[0];
                String stringdoc = parts[1];
                String title = parts[2];
                int prio = Integer.parseInt(parts[3]);
                Document doc = Jsoup.parse(stringdoc);
                DocumentsAndUrlsWithPrio.put(url, new PageDocument(stringdoc, title));
            }

            // Close the file reader
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            // Log the error
            System.err.println("Error while loading crawler state: " + e.getMessage());
        }
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

                    synchronized (dbManager){
                        List<String> newList = myNewPage.getPointsTo();
                        dbManager.insertCrawlerDocument(myNewPage.getHtmlDoc(),url,myNewPage.getTitle(),newList);
                    }

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

//
//        // Configure the whitelist for HTML sanitization
//        Document inputDoc = Jsoup.parse(html);
//        Cleaner cleaner = new Cleaner(Safelist.none());
//        //boolean isValid = cleaner.isValidBodyHtml(html);
//        return cleaner.clean(inputDoc);

        // Get the compressed HTML
        //String compressedHtml = cleanedDoc.html();

//        HtmlCompressor compressor = new HtmlCompressor();
//
//        //we don't need css or js code for indexer so remove them
//        compressor.setCompressCss(true);
//        compressor.setCompressJavaScript(true);
//
//       //to remove any comments , not needed for indexer
//        compressor.setRemoveComments(true);
//
//        //to preserve the spaces for indexer:
//        compressor.setRemoveIntertagSpaces(false);
//        compressor.setPreserveLineBreaks(true);
//        compressor.setRemoveQuotes(false);
//
//        //i am not sure about this
//        compressor.setRemoveMultiSpaces(false);
//
//        //for simplifying doctype -will not affect indexer
//        compressor.setSimpleDoctype(true);
//
//
//        compressor.compress(html);
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
                htmlDocString=htmlDocString.replaceAll("\s+" + Words.nextLine() + "\s+", " ");
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
                throw new RuntimeException(e);
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

        return uri.normalize().toString();

        //we need to seperate each part in order to apply normalization:
        //-----------------------------------------------------------------

//        //scheme -- needs normalization  by converting to lower case    --shceme is http and https
//        String scheme = uri.getScheme();
//        if (scheme == null) {
//            throw new RuntimeException("URL scheme is required.");
//        }
//        String normalizedScheme = scheme.toLowerCase();
//
//        //userinfo -- no need:
//        String userInfo = uri.getUserInfo();
//
//        //host -- needs normalization by converting to lower case
//        String host = uri.getHost();
//
//
//        String normalizedHost = host.toLowerCase();
//
//
//        //port -- need normalization
//        //will return either -1 or the port
//        int port = uri.getPort();
//        int normalizedPort = normalizePort(normalizedScheme, port);
//
//        //path --need normalization
//        //the first thing we do is remove .. or . using normalize function in uri
//        URI normalizedUrl_1 = uri.normalize();
//        String path = normalizedUrl_1.getPath();
//        //then we need to normalize stuff in the path itself too
//        String normalizedPath = normalizePath(path);
//
//        //query --need normalization
//        String query =uri.getQuery();
//        String normalizedQuery = normalizeQuery(query);
//
//
//        //fragment --need normalization
//        String fragment =uri.getFragment();
//        String normalizedFragment = normalizeFragment(fragment);
//
//        URI normalized_URL = new URI(normalizedScheme, userInfo, normalizedHost, normalizedPort, normalizedPath, normalizedQuery, normalizedFragment);
//
//        return normalized_URL.toString();


    }

    private static String normalizeQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        // Split query parameters (they are sperated by &)
        String[] params = query.split("&");
        if (params.length > 1) {
            //sort the parameters alphabetically:
            Arrays.sort(params);
            //new builder to put our parameters (non-empty ones)
            StringBuilder builder = new StringBuilder();
            for (String par : params) {
                if (par.isEmpty()) {
                    //ignore it
                    continue;
                }
                int length = builder.length();
                //check if its tha first par or not to add &
                if (length > 0) {
                    builder.append("&");
                }
                builder.append(par);
            }
            return builder.toString();
        }
        return query;
    }


    //in path normalization we remove any duplicate / and remove / at the end
    private static String normalizePath(String path) {
        String removeDuplicateResult = removeDuplicates(path);
        if (removeDuplicateResult == null || removeDuplicateResult.isEmpty()) {
            return null;
        }

        //we capitalize any necessary encoded
        int length = removeDuplicateResult.length();
        // capitalize letters in escape sequences
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = removeDuplicateResult.charAt(i);
            if (c == '%' && i + 2 < length) {
                String hex = removeDuplicateResult.substring(i + 1, i + 3);
                try {
                    c = (char) Integer.parseInt(hex, 16);
                    i += 2;
                } catch (NumberFormatException e) {
                    // Invalid hex code, keep the original value
                }
            } else if (isUnreserved(c)) {
                // unreserved characters are already decoded, keep them as is
            } else {
                // capitalize escape sequence letters
                c = Character.toUpperCase(c);
            }
            sb.append(c);
        }


        String capitalizedResult = sb.toString();

        // decode percent-encoded octets of unreserved characters
        //in this step, the url might contain encoded undreserverd chars such as %20 for space
        //ALPHA (%41–%5A and %61–%7A), DIGIT (%30–%39), hyphen (%2D), period (%2E), underscore (%5F), or tilde (%7E)
        //the URLdecoder will decode it to its correct format
        String decoderResult = URLDecoder.decode(capitalizedResult, StandardCharsets.UTF_8);


        int lengthh = decoderResult.length();
        //remove the last slash if exists
        char value = decoderResult.charAt(lengthh - 1);
        if (value == '/') {
            return decoderResult.substring(0, lengthh - 1);
        }

        return decoderResult;
    }

    // check if a character is unreserved
    private  static boolean isUnreserved(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') || c == '-' || c == '.' ||
                c == '_' || c == '~';
    }

    private static  String removeDuplicates(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        //we want to return a string after we remove any duplicate /
        StringBuilder builder = new StringBuilder();
        int duplicatesCount = 0;
        int textLength = text.length();
        for (int i = 0; i < textLength; ++i) {
            char value = text.charAt(i);
            if (value == '/') {
                //meaning we have consecutive   /
                duplicatesCount += 1;
                if (duplicatesCount > 1) {
                    continue;
                }
            } else {
                //we reset
                duplicatesCount = 0;
            }
            builder.append(value);
        }
        return builder.toString();
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

    private static String normalizeFragment(String fragment) {
        if (fragment == null || fragment.isEmpty()) {
            return null;
        }
        return fragment;
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
            throw new RuntimeException(e);
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
