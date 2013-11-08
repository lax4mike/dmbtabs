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
	
	public void run() {
		
		try {

//			Document doc = Jsoup.connect("http://www.dmbtabs.com/").get();
			
			java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
		    java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

		    
			String urlBase = "http://www.dmbtabs.com";
			final WebClient webClient = new WebClient();
		    final HtmlPage page = webClient.getPage(urlBase + "/");
		    
		    Document doc = Jsoup.parse(page.asXml());
			
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
							
				String selector = "#" + album.getKey() + " a";
				Elements songs = doc.select(selector);
	
				for(Element song : songs) {
					
					System.out.println("saving " + song.text() + " : " + song.attr("href"));
					
					Map<String, String> songInfo = this.getSongInfo(urlBase + song.attr("href"));
					this.saveFile(album.getValue(), songInfo.get("title"), songInfo.get("text"));
					
				}
			}

						
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
		

	public Map<String, String> getSongInfo(String url) {

		try {
			
			String newline = System.getProperty("line.separator");
			
			String text = "";
			
			Document doc = Jsoup.connect(url).get();
			
			
			String title = doc.select(".pageTitle").first().ownText();
			text += title + newline;
			
			Elements infos = doc.select(".pageInfo");
			
			for(Element info : infos) {
				text += info.text() + newline;
			}
			
			
			text += newline + newline;
			
			int pElement = doc.select("p.pageCopy").first().elementSiblingIndex();
			Elements children = doc.select("p.pageCopy").first().parent().children();

			for(int i = pElement + 1; i < children.size(); i++){
				Element e = children.get(i);
				
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
	
	public void saveFile(String album, String title, String text) {
		
		String filepath = "Downloaded";
		filepath += File.separator;
		filepath += album;
		filepath += File.separator;	
		
		String filename = title;
		filename += ".txt";

		try
		{
			File songFile = new File(filepath+filename);
			if(!songFile.exists()) {
				songFile.getParentFile().mkdirs();
			    songFile.createNewFile();
			} 
			
		    FileWriter fw = new FileWriter(filepath+filename); //the true will append the new data
		    fw.write(text);
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
		
	
	}
		
}
