package dmbtabs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class DmbTabsScraper {

    public static void main (String[] args) {

        DmbTabsScraper scraper = new DmbTabsScraper();
        
        scraper.run();
        
    }
    
    
    /**
     * kick off scraper by parsing the album menu and fetching each song
     */
    public void run() {
        
        try {

            // can't use this because the menu on dmbtabs.com is generated with javascript
            // Document doc = Jsoup.connect("http://www.dmbtabs.com/").get();
            
            // hide numerous warnings from htmlunit
            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

            // create htmlunit and get the page.  This will exectute the javascript 
            // that builds the menu
            String urlBase = "http://www.dmbtabs.com";
            final WebClient webClient = new WebClient();
            final HtmlPage page = webClient.getPage(urlBase + "/");
            
            // create jsoup document with the result of the htmlunit
            Document doc = Jsoup.parse(page.asXml());
            
            // each menu item coorisponds to an album
            HashMap<String, String> albumMap = new LinkedHashMap<String, String>() {{
                  put("menu2", "Remember Two Things");   
                  put("menu3", "Under the Table and Dreaming"); 
                  put("menu4", "Crash"); 
                  put("menu5", "Before These Crowded Streets");
                  put("menu6", "The Lillywhite Sessions");
                  put("menu7", "Everyday");
                  put("menu8", "Busted Stuff");
                  put("menu9", "Some Devil");
                  put("menu10", "Covers");
                  put("menu11", "Live");  
            }};
            
            
            // get all songs under each album
            for(Map.Entry<String, String> album : albumMap.entrySet()) {
                
                // find all the song links in each album menu (eg. "#menu2 a")    
            	String albumKey = album.getKey();
                String selector = "#" + albumKey + " a";
                Elements songs = doc.select(selector);
                
                int songIndex = 0;
    
                for(Element song : songs) {
                                    	
                	Map<String, String> songInfo = this.getSongInfo(urlBase + song.attr("href"));

                	// if the album is not "Live" or "Covers", put a number in front of the song title to
                	// keep the album order.  Pad that number with 0 if needed.
                	String title = songInfo.get("title");
                	if (albumKey != "menu10" && albumKey != "menu11") {
                		title = ((++songIndex < 10) ? "0" : "")  + songIndex + " - " + songInfo.get("title");
                	}
  
                    this.saveFile(album.getValue(), title, songInfo.get("text"));
                    
                    System.out.println("saving " + title);
                    
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
        
    /**
     * 
     * @param url full url of song (eg. http://www.dmbtabs.com/song.php?sid=1)
     * @return a map with song title and tab text {title: "...", text: "..."}
     */
    public Map<String, String> getSongInfo(String url) {

        try {
            
            String newline = System.getProperty("line.separator");
            String text = "";
            
            Document doc = Jsoup.connect(url).get();
            
            // get title
            String title = doc.select(".pageTitle").first().ownText();
            text += title + newline;
            
            // get info blocks. tunings, comments, etc
            Elements infos = doc.select(".pageInfo");
            for(Element info : infos) {
                text += info.text() + newline;
            }
            
            // add line break between infos and the tabs
            text += newline + newline;
            
            // find the first element before the tab
            int pElement = doc.select("p.pageCopy").first().elementSiblingIndex();
            Elements children = doc.select("p.pageCopy").first().parent().children();

            // go through the children starting with the first element before the tabs
            for(int i = pElement + 1; i < children.size(); i++){
                Element e = children.get(i);
                
                // only keep the line if it's a <pre> or <br> and if it's 
                // not a View Video link
                if (e.tagName() == "pre" || e.tagName() == "br"){
                    
                    if (!e.text().contains("View Video")) {
                        text += children.get(i).text() + newline;
                    }
                }
            }
            
            Map<String, String> returnMap = new HashMap<String, String>();
            returnMap.put("text", text);
            returnMap.put("title", title);
            
            return returnMap;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        
    }
    
    
    /**
     * 
     * @param album Name of the album, used in the filepath
     * @param title Title of the song, used as the filename.txt
     * @param text the tab text
     */
    public void saveFile(String album, String title, String text) {
        
    	// generate filepath and filename
        String filepath = "Downloaded";
        filepath += File.separator;
        filepath += album;
        filepath += File.separator; 
        
        String filename = title;
        filename += ".txt";

        try
        {
        	// create file if it doesn't exists already
            File songFile = new File(filepath+filename);
            if(!songFile.exists()) {
                songFile.getParentFile().mkdirs(); // create dirs if necessary
                songFile.createNewFile(); // write the blank file
            } 
            
            // write the tab text to the file. this will overwrite anything 
            // already in the file
            FileWriter fw = new FileWriter(filepath+filename); 
            fw.write(text);
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
        
    
    }
        
}
