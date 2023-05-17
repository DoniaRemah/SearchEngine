package Crawler;

import org.jsoup.nodes.Document;

///This is a class for the page document (html) where it has url and count as its objects
/// It has a setter and getter for each and a constructor that takes in the url for the page and make the count=1
///we also have increment in case the page is referred by more than one link
public class PageDocument {
   // private String url;
    private Document htmlDoc;
    private String title;
    private int count;

    //we will add title

    public PageDocument(Document htmlDoc, String title) {
        this.htmlDoc = htmlDoc;
        this.title= title;
        this.count = 1;
    }




    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incrementCount() {
        this.count++;
    }

    public void setTitle(String title){this.title=title;}

    public String getTitle(){return title;}

    public Document getHtmlDoc() {
        return htmlDoc;
    }

    public void setHtmlDoc(Document htmlDoc) {
        this.htmlDoc = htmlDoc;
    }
}