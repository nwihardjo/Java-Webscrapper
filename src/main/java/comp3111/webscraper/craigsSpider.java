package comp3111.webscraper;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Class implemented to provide concurrency on scraping craigslist, particularly pagination, 
 *  which significantly improve the performance of the webscraper. This class extends WebScraper
 *  to provide neater solution of scraping each page of the craigslist using the function already
 *  implemented for amazon (i.e. getTitle, getPrice, etc). Class implements Callable, a base class
 *  which can be executed by thread while support the ability to return value when called. The 
 *  class is managed by an executor service which instantiate a thread pool with the size of the 
 *  number of craigslist total pages, to further enhance the performance and reduce time consumed. 
 * 
 * @author nwihardjo
 *
 */
public class craigsSpider extends WebScraper implements Callable<ArrayList<Item>>{
	private WebClient client;
	private String Url;

	/**
	 * Default constructor which instantiate the webclient and set-up its configuration
	 * 	to scrape one craigslist page
	 * 
	 * @param Url absolute web address of the item page
	 */
	public craigsSpider(String Url) {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.waitForBackgroundJavaScript(100000);
		this.Url = Url;
	}
		
	/**
	 * Retrieve page of the given url address. Then, the webdriver / client is closed for 
	 * 	cutting down time required to shutdown the thread pool (closing bunch of webclient
	 * 	at a time consumes more time).
	 * 
	 * @return HtmlPage of the retrieved page. Return null if the url is broken or any issue
	 * 	in retrieving the page
	 */
	private HtmlPage getPage(){
		try { 
			HtmlPage page = client.getPage(this.Url);
			client.close();
			return page;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * A method which is override from the Callable class. Used to return the list of items present
	 * 	in the page. Scraping is done using methods already implemented in WebScraper to provide
	 * 	neater solution. scrapePage which is a protected method of WebScraper, is called.
	 * 
	 * @return ArrayList of the items present in a single page of craigslist.
	 */
	@Override
	public ArrayList<Item> call() {
		HtmlPage page = this.getPage();
		return scrapePage(page);
	}	
}