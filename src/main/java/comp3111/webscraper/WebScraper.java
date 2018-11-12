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

public class WebScraper {

	private static final String DEFAULT_URL = "https://newyork.craigslist.org/";
	private static final String AMAZON_URL = "https://www.amazon.com/";
	private WebClient client;
	private static final Boolean DEBUG = false;
	private ExecutorService amazonSpiderPool;
	
	/**
	 * Default Constructor 
	 */
	public WebScraper() {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.waitForBackgroundJavaScript(100000);
	}
	
	public static String getTitle(HtmlElement item, String portal) {
		String xPathAddr = (portal == AMAZON_URL) ? ".//h2[@data-attribute]" : ".//p[@class='result-info']/a";
		HtmlElement itemTitle = (HtmlElement) item.getFirstByXPath(xPathAddr);
		
		// USE:CASE non-item case, particularly on Amazon portal
		// if condition += itemTitle.asText() == ""
		return (itemTitle == null) ? null : cleanStr(itemTitle.asText(), "title");
	}

	// currently for craigslist item, scrape single page
	public static ArrayList<Item> scrapePage(HtmlPage page) {
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
	
	public static Double getPrice(HtmlElement item, String portal) {
		// return 0.0 if the price is not specified
		if (portal == AMAZON_URL) {
			// portal: amazon 
			// USE CASE: only consider the main item, not the buying options
			// USE CASE: only main price is used, not the offer with kindle, etc.
			ArrayList<HtmlElement> ItemWholePrice = new ArrayList<HtmlElement> (item.getByXPath(".//*[contains(@class, 'sx-price-whole')]"));
			ArrayList<HtmlElement> ItemFractionalPrice = new ArrayList<HtmlElement> (item.getByXPath(".//*[contains(@class, 'sx-price-fractional')]"));
			
			if (ItemWholePrice.size() == 0 || ItemFractionalPrice.size() == 0) {
				HtmlElement offeredPrice = (HtmlElement) item.getFirstByXPath(".//*[contains(text(),'offer')]");
				if (offeredPrice == null) 
					return 0.0;
				else {
					// USE CASE: no price available, but there some offers which contain price
					return new Double(cleanStr(offeredPrice.asText().replaceAll("\\(.*\\)", ""), "price"));
				}
			} else if (ItemWholePrice.size() > 1 && ItemFractionalPrice.size() > 1) {
				Double lowPrice = new Double (cleanStr(ItemWholePrice.get(0).asText(), "price") + "." + ItemFractionalPrice.get(0).asText());
				Double highPrice = new Double (cleanStr(ItemWholePrice.get(1).asText(), "price") + "." + ItemFractionalPrice.get(1).asText());
				// USE CASE: return average price if the price given is a range
				return (lowPrice + highPrice) / 2.0;
			} else { 
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

	
	public static String cleanStr(String str, String use) {
		if (use == "price") {
			return str.replace("$", "").replace(",", "");
		} else
			return (str.startsWith("[Sponsored]")) ? str.replace("[Sponsored]", "") : str;
	}
	
	//currently only for craigslist
	public static String getNextPage(HtmlPage page) {
		HtmlAnchor nextPageUrl = (HtmlAnchor) page.getFirstByXPath("//a[@class='button next']");
		if (nextPageUrl.getHrefAttribute().length() == 0) {
			return null;
		} else 
			return (nextPageUrl.getHrefAttribute().startsWith(DEFAULT_URL)) ? nextPageUrl.getHrefAttribute() :
				DEFAULT_URL + nextPageUrl.getHrefAttribute();
	}
	
	public static String getUrl(HtmlElement item, String portal) {
		String portal_url = (portal == AMAZON_URL) ? AMAZON_URL : DEFAULT_URL;
		String xPathAddr = (portal == AMAZON_URL) ? ".//h2[@data-attribute]/parent::a" : ".//p[@class='result-info']/a";
		HtmlAnchor itemUrl = (HtmlAnchor) item.getFirstByXPath(xPathAddr);
		return (itemUrl.getHrefAttribute().startsWith(portal_url)) ? itemUrl.getHrefAttribute() : 
			portal_url + itemUrl.getHrefAttribute();
	}

	// currently only for craigslist
	public static Date getPostedDate(HtmlElement item){
		try {
			DomAttr itemDate = (DomAttr) item.getFirstByXPath(".//*[@class='result-date']/@datetime");
			SimpleDateFormat dateFormatting = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return dateFormatting.parse(itemDate.getValue()); 
		} catch (Exception e) {
			return null;
		}
	}
		
	public static Vector<Item> sortResult(ArrayList<Item> amazonArrayList, ArrayList<Item> craigsArrayList){
		// sort ascending, for the same price, craigslist item goes first
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

	public ArrayList<Item> deploySpiders(ArrayList<Item> amazonArrayList){
		try {
			amazonSpiderPool = Executors.newFixedThreadPool(amazonArrayList.size());
			List<Future<Date>> spiders = new ArrayList<Future<Date>>();
			System.out.println("URL " + amazonArrayList.get(0).getUrl());
			System.out.println("DEBUG HERE");
			for (Item amazonItem : amazonArrayList) {
				Callable<Date> spider = new Spider(amazonItem.getUrl(), amazonItem.getTitle());
				spiders.add(amazonSpiderPool.submit(spider));
			}
			System.out.println("DEBUG NEXT HERE");
			for (int i = 0; i < amazonArrayList.size(); i++) {
				amazonArrayList.get(i).setPostedDate(spiders.get(i).get());
			}
			System.out.println(amazonArrayList.get(0).getPostedDate());
			amazonSpiderPool.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return amazonArrayList;
	}
	
	/**
	 * The only method implemented in this class, to scrape web content from the craigslist
	 * 
	 * @param keyword - the keyword you want to search
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
			
			if (DEBUG) System.out.println("\t DEBUG: [amazon] produce " + amazonResult.size() + " items");			
					
			// item retrieval
			// USECASE: if the item is not a single search, i.e. "book", which return a whole sub-section, meaning no item found
			for (int i = 0; i < amazonResult.size(); i++) {
				HtmlElement amazonItem = (HtmlElement) amazonResult.get(i);

				//non-item case
				if (getTitle(amazonItem, AMAZON_URL) == null) continue;
				if (DEBUG) System.out.println("\t DEBUG: entering item : " + getTitle(amazonItem, AMAZON_URL));
				
				Item item = new Item(getTitle(amazonItem, AMAZON_URL), getPrice(amazonItem, AMAZON_URL), getUrl(amazonItem, AMAZON_URL), AMAZON_URL, null);
				amazonArrayList.add(item);
				if (DEBUG) System.out.println("\t DEBUG: [amazon] stored item " + i + ": " + item.getPrice() + " HKD. Name: " +item.getTitle());
			}
			Collections.sort(amazonArrayList);
			// retrieve postedDate for amazonItems
			controller.printConsole("\t Scraping amazon's items page... \n");	
			amazonArrayList = deploySpiders(amazonArrayList);
			
			
			/*
			 * NEWYORK CRAIGSLIST SCRAPER
			 */
			controller.printConsole("Scraping craigslist \n"); System.out.println("   DEBUG: scraping craigslist...");
			String searchUrl = DEFAULT_URL + "search/sss?sort=rel&query=" + URLEncoder.encode(keyword, "UTF-8");
			HtmlPage page = client.getPage(searchUrl);
			ArrayList<Item> craigsArrayList = new ArrayList<Item>();
		
			// handle pagination
			int currentPage = 1;
			do {
				if (currentPage != 1) 
					page = client.getPage(getNextPage(page));
				controller.printConsole("\t Scraping page " + currentPage + "...\n");
				craigsArrayList.addAll(scrapePage(page));
				currentPage += 1;
			} while (getNextPage(page) != null);
			
			Collections.sort(craigsArrayList);
			Vector<Item> result = sortResult(amazonArrayList, craigsArrayList);
			
			// TODO: delete this line
			if (DEBUG) for (Item i: result) System.out.println("DEBUG: result " + i.getPrice() + " PORTAL " + i.getPortal());
			
			client.close();
			// TODO: delete following line
			System.out.println("DEBUG: scraping finished");
			return result;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

}
