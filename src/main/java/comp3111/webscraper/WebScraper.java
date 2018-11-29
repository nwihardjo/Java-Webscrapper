package comp3111.webscraper;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.util.Vector;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class which is used to scrape the queried item from 2 portals: amazon and newyork craigslist.
 * Title or name, price, posted date, url of the item, portal of each item were scraped.
 * This class massively parallelised the posted date retrieval of amazon items and craigslist pagination
 * 	using multi-threading in java application, resulting in significantly improved performance of the webscraper.
 * HtmlUnit web driver (netscape as its default) is used for the web driver.
 * 
 * @author nwihardjo
 */
public class WebScraper {
	private static final String DEFAULT_URL = "https://newyork.craigslist.org/";
	private static final String AMAZON_URL = "https://www.amazon.com/";
	private WebClient client;
	private ExecutorService amazonSpiderPool;
	
	/**
	 * Default Constructor, instantiated webClient of the HtmlUnit and configured for javascript renderring
	 * 
	 *
	 * @see WebScraper 
	 */
	public WebScraper() {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.waitForBackgroundJavaScript(100000);
	}

	/**
	 * Get the title of the item using a specific XPath address for each portal. Amazon could have a result
	 * 	which is not an item, known by the title of the result having different XPath address. Utilised cleanStr method to
	 *  parse / clean the title for amazon (i.e. containing [sponsored], etc).
	 * 
	 * @param item  containing one specific item from either portal
	 * @param portal which the item originated (amazon / craigslist)
	 * @return title of the item
	 */
	private static String getTitle(HtmlElement item, String portal) {
		String xPathAddr = (portal == AMAZON_URL) ? ".//h2[@data-attribute]" : ".//p[@class='result-info']/a";
		HtmlElement itemTitle = (HtmlElement) item.getFirstByXPath(xPathAddr);
		
		// if condition += itemTitle.asText() == ""
		return (itemTitle == null) ? null : cleanStr(itemTitle.asText(), "title");
	}

	/**
	 * Get every items and its information from a single page of craigslist. Used as a method to support
	 * 	pagination feature for craigslist portal, pages were retrieved by scrape method. Called by craigslist spiders
	 * 
	 * @param page a single page of the craigslist portal
	 * @return  list of the item present in the page
	 */
	protected static ArrayList<Item> scrapePage(HtmlPage page) {
		List<?> items = (List<?>) page.getByXPath("//li[@class='result-row']");
		ArrayList<Item> craigsArrayList = new ArrayList<Item>();
		for (int i = 0; i < items.size(); i++) {
			HtmlElement htmlItem = (HtmlElement) items.get(i);	
			Item item = new Item(getTitle(htmlItem, DEFAULT_URL), getPrice(htmlItem, DEFAULT_URL), getUrl(htmlItem, DEFAULT_URL), DEFAULT_URL, 
					getPostedDate(htmlItem));
			craigsArrayList.add(item);
		}
		return craigsArrayList;
	}
	
	/**
	 * Retrieve the price of an amazon's or craigslist's item based on its XPath address. For amazon item, if main price 
	 * 	present, only the main price will be used. If main price does not present, cheapest price of offers / other buying options
	 * 	, if any, will be used. If the stated price were a range, avose your Free Limited Edition Gift! erage of the price range will be used. Utilised
	 * 	cleanStr method to clean / parse the price (i.e. containing $, ",", etc). If no price exists, price will be 0.
	 * 
	 * @param item HtmlElement which containing only a single item
	 * @param portal name of the portal the passed item came from
	 * @return price of the item in USD based on the condition described
	 */
	private static Double getPrice(HtmlElement item, String portal) {
		// return 0.0 if the price is not specified
		if (portal == AMAZON_URL) {
			// portal: amazon 
			ArrayList<HtmlElement> ItemWholePrice = new ArrayList<HtmlElement> (item.getByXPath(".//*[contains(@class, 'sx-price-whole')]"));
			ArrayList<HtmlElement> ItemFractionalPrice = new ArrayList<HtmlElement> (item.getByXPath(".//*[contains(@class, 'sx-price-fractional')]"));
			
			if (ItemWholePrice.size() == 0 || ItemFractionalPrice.size() == 0) {
				// offer / other buying options price when main price does not exist
				HtmlElement offeredPrice = (HtmlElement) item.getFirstByXPath(".//*[contains(text(),'offer')]");
				if (offeredPrice == null) 
					return 0.0;
				else {
					return new Double(cleanStr(offeredPrice.asText().replaceAll("\\(.*\\)", ""), "price"));
				}
			} else if (ItemWholePrice.size() > 1 && ItemFractionalPrice.size() > 1) {
				// range price
				Double lowPrice = new Double (cleanStr(ItemWholePrice.get(0).asText(), "price") + "." + ItemFractionalPrice.get(0).asText());
				Double highPrice = new Double (cleanStr(ItemWholePrice.get(1).asText(), "price") + "." + ItemFractionalPrice.get(1).asText());
				return (lowPrice + highPrice) / 2.0;
			} else { 
				// main price / normal case
				return new Double (cleanStr(ItemWholePrice.get(0).asText(), "price") + "." + ItemFractionalPrice.get(0).asText()); }
		} else {
			// portal: craigslist
			HtmlElement itemPrice = ((HtmlElement) item.getFirstByXPath(".//*[@class='result-price']"));
			if (itemPrice == null) 
				return 0.0;
			else 
				return new Double(cleanStr(itemPrice.asText(), "price"));
		}
		}
	
	/**
	 * Clean or parse string based on the usage (for title or price) 
	 * 
	 * @param str string going to be parsed / cleaned
	 * @param use name of the method which call this method
	 * @return cleaned / parsed string
	 */
	private static String cleanStr(String str, String use) {
		if (use == "price") {
			return str.replace("$", "").replace(",", "");
		} else
			return (str.startsWith("[Sponsored]")) ? str.replace("[Sponsored]", "") : str;
	}
	
	/**
	 * Scrape the url of the page of the item from either portal (amazon / craigslist)
	 * 
	 * @param item HtmlElement consisting of a single item
	 * @param portal name of the website where the passed item came from
	 * @return url of the item page in its respective portal
	 */
	private static String getUrl(HtmlElement item, String portal) {
		String portal_url = (portal == AMAZON_URL) ? AMAZON_URL : DEFAULT_URL;
		String xPathAddr = (portal == AMAZON_URL) ? ".//h2[@data-attribute]/parent::a" : ".//p[@class='result-info']/a";
		HtmlAnchor itemUrl = (HtmlAnchor) item.getFirstByXPath(xPathAddr);
		return (itemUrl.getHrefAttribute().startsWith(portal_url)) ? itemUrl.getHrefAttribute() : 
			portal_url + itemUrl.getHrefAttribute();
	}

	/**
	 * Retrieve the date of the time when the item is posted. This method is only implemented for craigslist only.
	 * 	Date of the posted time of amazon's items were retrieved in deploySpider function. 
	 * 
	 * @param item HtmlElement consisting of a single item
	 * @return date of when the item is posted in HKT (Hong Kong Time). Return null when there is no available information
	 * 	on the posted date.
	 */
	private static Date getPostedDate(HtmlElement item){
		try {
			DomAttr itemDate = (DomAttr) item.getFirstByXPath(".//*[@class='result-date']/@datetime");
			SimpleDateFormat dateFormatting = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return dateFormatting.parse(itemDate.getValue()); 
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Sort the result (items) retrieved from both portals ascending. When two items from two portals has the same price
	 * 	the item from craigslist will go first. The items from each portal were sorted ascendingly first. 
	 * 
	 * @param amazonArrayList list of the items retrieved from amazon portal
	 * @param craigsArrayList list of the items retrieved from craigslist portal
	 * @return list of sorted items based on the condition described
	 */
	private static Vector<Item> sortResult(ArrayList<Item> amazonArrayList, ArrayList<Item> craigsArrayList){
		Collections.sort(amazonArrayList);
		Collections.sort(craigsArrayList);
		if (amazonArrayList.isEmpty() && !craigsArrayList.isEmpty())
			return new Vector<Item>(craigsArrayList);
		else if (craigsArrayList.isEmpty() && !amazonArrayList.isEmpty())
			return new Vector<Item>(amazonArrayList);
		else {
			Vector<Item> result = new Vector<Item>();
			for (int i=0, j=0; !amazonArrayList.isEmpty() || !craigsArrayList.isEmpty();) {
				if (amazonArrayList.isEmpty()) 
					result.add(craigsArrayList.remove(j));
				else if (craigsArrayList.isEmpty()) 
					result.add(amazonArrayList.remove(i));
				else if (craigsArrayList.get(j).getPrice() > amazonArrayList.get(i).getPrice()) 
					result.add(amazonArrayList.remove(i));
				else if (craigsArrayList.get(j).getPrice() < amazonArrayList.get(i).getPrice()) 
					result.add(craigsArrayList.remove(j));
				else if (craigsArrayList.get(j).getPrice() == amazonArrayList.get(i).getPrice())
					result.add(craigsArrayList.remove(j));
			}
			return result;
		}
	}

	/**
	 * Parallelisation to retrieve the posted date of the amazon item which is stored in each of the item's page.
	 * 	Multi-threading were used to intialise multiple webclients to scrape amazon's items pages concurrently, using a 
	 * 	thread pool with the size of the number of item present in a single page of the amazon. Each thread then return the
	 * 	scraped posted date of each item and update the value of the posted date of the item. 
	 * 
	 * @param amazonArrayList list of items present in a single page of amazon portal
	 * @return same list of items passed with updated posted date value, if any
	 */
	private ArrayList<Item> deploySpiders(ArrayList<Item> amazonArrayList){
		try {
			amazonSpiderPool = Executors.newFixedThreadPool(amazonArrayList.size());
			List<Future<Date>> spiders = new ArrayList<Future<Date>>();
			for (Item amazonItem : amazonArrayList) {
				Callable<Date> spider = new Spider(amazonItem.getUrl());
				spiders.add(amazonSpiderPool.submit(spider));
			}
			for (int i = 0; i < amazonArrayList.size(); i++) {
				amazonArrayList.get(i).setPostedDate(spiders.get(i).get());
			}
			amazonSpiderPool.shutdown();
			return amazonArrayList;
		} catch (Exception e) {
			return amazonArrayList;
		}
	}

	/**
	 * Main framework method which is used to scrape and handle concurrency for scraping craigslist while handling the
	 * 	pagination. Instantiate a thread pool which able to cut down the time of scraping significantly by parallelised
	 * 	the scraping
	 * @param pageStatistics integer array which contain the total number of pages and the number of searches in a page
	 * @param keyword of the search
	 * @param firstPageUrl 
	 * @return arraylist of all items scraped through all pages
	 */
	private ArrayList<Item> handlePagination (ArrayList<Integer> pageStatistics, String keyword, String firstPageUrl){
		ArrayList<Item> craigsArrayList = new ArrayList<Item>();
		try {
			ExecutorService craigsSpiderPool = Executors.newFixedThreadPool(pageStatistics.get(0));
			List<Future<ArrayList<Item>>> spiders = new ArrayList<Future<ArrayList<Item>>>();
			
			// for scraping the first page
			Callable<ArrayList<Item>> cSpider = new craigsSpider(firstPageUrl);
			spiders.add(craigsSpiderPool.submit(cSpider));
					
			// scrape the next pages
			for (int i = 2; i <= pageStatistics.get(0); i++) {
				String urlPage = DEFAULT_URL + "search/sss?s=" + (i-1)*pageStatistics.get(1) + "&query=" + keyword + "&sort=rel";
				Callable<ArrayList<Item>> cSpider_ = new craigsSpider(urlPage);
				spiders.add(craigsSpiderPool.submit(cSpider_));
			}
			
			// craigslist's list of items retrieval
			for (int i = 0; i < spiders.size(); i++) {
				craigsArrayList.addAll(spiders.get(i).get());
			}
			craigsSpiderPool.shutdown();		
			return craigsArrayList;
		} catch (Exception e) { 
			return craigsArrayList;
		}
	}
	
	/**
	 * Scrape the page statistics given a htmlpage, used only for craigslist portal
	 * @param page
	 * @return array integer which contain the total number of pages resulted from searching the keyword, and the number
	 * 	of items in each page
	 */
	private static ArrayList<Integer> getPageStatistics (HtmlPage page) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		Integer totResultCount = new Integer (((HtmlElement) page.getFirstByXPath("//span[@class='totalcount']")).asText());
		String rangeResult = ((HtmlElement) page.getFirstByXPath("//span[@class='range']")).asText();
		Integer numResultOnePage = new Integer (rangeResult.substring(rangeResult.lastIndexOf("-")+1).replaceAll(" ", ""));
		
		ret.add((totResultCount % numResultOnePage != 0) ? (totResultCount / numResultOnePage)+1 : (totResultCount/numResultOnePage));
		ret.add(numResultOnePage);
		return ret;
	}
	
	/**
	 * Method to manage the workflow of scraping both portals based on the keyword specified. Handle amazon portal first, then the 
	 * 	craigslist sequentially. Pagination of amazon is not handled, item's posted date retrieval is done concurrently, after all
	 * 	of the items have been collected. Pagination of craigslist is handled concurrently.
	 * 
	 * @param keyword the keyword you want to search
	 * @param controller instance of which call this function, to update user on the scraping progress
	 * @return A list of Item that has found. A zero size list is return if nothing is found. Null if any exception (e.g. no connectivity)
	 */
	public List<Item> scrape(String keyword, Controller controller) {
		try {
			/*
			 * AMAZON SCRAPER
			 */
			controller.printConsole("Scraping amazon \n"); System.out.println("   DEBUG: scraping amazon...");
			String amazonUrl = AMAZON_URL+"s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=" + URLEncoder.encode(keyword,"UTF-8");
			HtmlPage amazonPage = client.getPage(amazonUrl);
			List<?> amazonResult = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");
			ArrayList<Item> amazonArrayList = new ArrayList<Item>();
			
			// item retrieval
			for (int i = 0; i < amazonResult.size(); i++) {
				HtmlElement amazonItem = (HtmlElement) amazonResult.get(i);

				//non-item case
				if (getTitle(amazonItem, AMAZON_URL) == null) continue;
				
				Item item = new Item(getTitle(amazonItem, AMAZON_URL), getPrice(amazonItem, AMAZON_URL), getUrl(amazonItem, AMAZON_URL), AMAZON_URL, null);
				amazonArrayList.add(item);
			}
			// retrieve postedDate for amazonItems
			controller.printConsole("\t Scraping " + amazonResult.size() + " amazon's item page(s) in parallel (if any) ... \n");	
			amazonArrayList = deploySpiders(amazonArrayList);
			
			
			/*
			 * NEWYORK CRAIGSLIST SCRAPER
			 */
			controller.printConsole("Scraping craigslist \n"); System.out.println("   DEBUG: scraping craigslist...");
			String searchUrl = DEFAULT_URL + "search/sss?sort=rel&query=" + URLEncoder.encode(keyword, "UTF-8");
			HtmlPage page = client.getPage(searchUrl);
			ArrayList<Item> craigsArrayList = new ArrayList<Item>();
		
			// handle pagination using multi-threading to support concurrency
			// check whether craigslist has any listings on the item searched
			if (page.getFirstByXPath("//span[@class='totalcount']") != null) {
				ArrayList<Integer> pageStatistics = getPageStatistics(page);
				controller.printConsole("\t" + pageStatistics.get(0) + " page(s) of craigslist are being scraped in parallel ...");
				craigsArrayList.addAll(handlePagination(pageStatistics, URLEncoder.encode(keyword, "UTF-8"), searchUrl));
			}
			
			// sort result retrieved from both portals
			Vector<Item> result = sortResult(amazonArrayList, craigsArrayList);
	 
			client.close();
			System.out.println("DEBUG: scraping finished");
			return result;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
}
