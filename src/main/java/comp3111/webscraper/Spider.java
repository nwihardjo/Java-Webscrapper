package comp3111.webscraper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Class implemented to provide concurrency on amazon portal scraping, which significantly
 * 	improve the performance of the websraper. Class implements Callable, a base class which can be
 * 	executed by thread while support the ability to return value when called. The class is managed
 * 	by an executor service which instantiate a thread pool with the size of the number of amazon
 * 	items, to further enhance the performance and time consumed. 
 * 
 * @author nwihardjo
 *
 */
public class Spider implements Callable<Date> {
	private WebClient client;
	private String Url;

	/**
	 * Default constructor which instantiate the webclient and set-up its configuration
	 * 	to scrape amazon's item page
	 * 
	 * @param Url absolute web address of the item page
	 */
	public Spider(String Url) {
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
	 * Method to scrape the date of the a service posting (i.e. amazon home & business services)
	 * 	, not an item. Service posting has different web layout, html structures, and css styles 
	 * 	which require different XPath address. This method utilises the parseDate method to return the 
	 * 	date in a desired format
	 * 
	 * @param page retrieved html page by the webclient 
	 * @return posted date of the service. Return null when no information on posted date available
	 * @see parseDate
	 */
	private Date scrapeService(HtmlPage page) {
		HtmlElement postedDate = (HtmlElement) page.getFirstByXPath("//div[@class='content']//*[contains(text(),'Date')]/parent::li");
		
		// if else condition += postedDate.asText() != ""
		if (postedDate != null)
			return parseDate(postedDate.asText().substring(postedDate.asText().indexOf(": ") + 2));
		else
			return null;
	}
	
	/**
	 * Convert the passed string as a date object based on the format of the date in the website
	 * 
	 * @param strDate date scraped from the html page
	 * @return Date object of the posted date
	 */
	public Date parseDate(String strDate) {
		SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
		try {
			return format.parse(strDate);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * A method which is override from the Callable class. Used to return the value of the parsed date in 
	 * 	Date object. This method holds the workflow of the spider, which retrieve the item page and scrape 
	 * 	the posted date. There are two html structures of the item page, for which two XPath addresses needed
	 * 	to be looked after. If the two locations do not contain the specified date, this method will try to look
	 * 	for the location of the date assuming the item is a service. 
	 * 
	 * @return Date object of the posted date item. Return null if there is no available information on the posted date 
	 */
	@Override
	public Date call() {
		HtmlPage page = this.getPage();
		HtmlElement iDate = page.getFirstByXPath("//*[contains(text(), 'Date')]/following-sibling::span");
		HtmlElement uDate = page.getFirstByXPath("//*[contains(text(), 'Date')]/following-sibling::td");
		
		if (iDate != null && iDate.asText().length() != 0)
			return parseDate(iDate.asText());			
		else if (uDate != null && uDate.asText().length() != 0)
			return parseDate(uDate.asText());
		else  
			return scrapeService(page);
	}
}