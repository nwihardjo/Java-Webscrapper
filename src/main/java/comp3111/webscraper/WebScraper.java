package comp3111.webscraper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.util.Vector;

/**
 * WebScraper provide a sample code that scrape web content. After it is constructed, you can call the method scrape with a keyword, 
 * the client will go to the default url and parse the page by looking at the HTML DOM.  
 * <br/>
 * In this particular sample code, it access to craigslist.org. You can directly search on an entry by typing the URL
 * <br/>
 * https://newyork.craigslist.org/search/sss?sort=rel&amp;query=KEYWORD
 *  <br/>
 * where KEYWORD is the keyword you want to search.
 * <br/>
 * Assume you are working on Chrome, paste the url into your browser and press F12 to load the source code of the HTML. You might be freak
 * out if you have never seen a HTML source code before. Keep calm and move on. Press Ctrl-Shift-C (or CMD-Shift-C if you got a mac) and move your
 * mouse cursor around, different part of the HTML code and the corresponding the HTML objects will be highlighted. Explore your HTML page from
 * body &rarr; section class="page-container" &rarr; form id="searchform" &rarr; div class="content" &rarr; ul class="rows" &rarr; any one of the multiple 
 * li class="result-row" &rarr; p class="result-info". You might see something like this:
 * <br/>
 * <pre>
 * {@code
 *    <p class="result-info">
 *        <span class="icon icon-star" role="button" title="save this post in your favorites list">
 *           <span class="screen-reader-text">favorite this post</span>
 *       </span>
 *       <time class="result-date" datetime="2018-06-21 01:58" title="Thu 21 Jun 01:58:44 AM">Jun 21</time>
 *       <a href="https://newyork.craigslist.org/que/clt/d/green-star-polyp-gsp-on-rock/6596253604.html" data-id="6596253604" class="result-title hdrlnk">Green Star Polyp GSP on a rock frag</a>
 *       <span class="result-meta">
 *               <span class="result-price">$15</span>
 *               <span class="result-tags">
 *                   pic
 *                   <span class="maptag" data-pid="6596253604">map</span>
 *               </span>
 *               <span class="banish icon icon-trash" role="button">
 *                   <span class="screen-reader-text">hide this posting</span>
 *               </span>
 *           <span class="unbanish icon icon-trash red" role="button" aria-hidden="true"></span>
 *           <a href="#" class="restore-link">
 *               <span class="restore-narrow-text">restore</span>
 *               <span class="restore-wide-text">restore this posting</span>
 *           </a>
 *       </span>
 *   </p>
 *}
 *</pre>
 * <br/>
 * The code 
 * <pre>
 * {@code
 * List<?> items = (List<?>) page.getByXPath("//li[@class='result-row']");
 * }
 * </pre>
 * extracts all result-row and stores the corresponding HTML elements to a list called items. Later in the loop it extracts the anchor tag 
 * &lsaquo; a &rsaquo; to retrieve the display text (by .asText()) and the link (by .getHrefAttribute()). It also extracts  
 * 
 *
 */
public class WebScraper {

	private static final String DEFAULT_URL = "https://newyork.craigslist.org/";
	private static final String AMAZON_URL = "https://www.amazon.com/";
	private WebClient client;
	private static final Boolean DEBUG = false;

	/**
	 * Default Constructor 
	 */
	public WebScraper() {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.waitForBackgroundJavaScript(100000);
	}

	// handle craigslist portal only
	public String getNumPages (String keyword, Controller controller) {
		try {
			String searchUrl = DEFAULT_URL + "search/sss?sort=rel&query=" + URLEncoder.encode(keyword, "UTF-8");
			HtmlPage portalPage = client.getPage(searchUrl);
			HtmlElement lowerBoundRange = (HtmlElement) portalPage.getFirstByXPath("//*[@class='rangeFrom']");
			HtmlElement upperBoundRange = (HtmlElement) portalPage.getFirstByXPath("//*[@class='rangeTo']");
			HtmlElement totalItem = (HtmlElement) portalPage.getFirstByXPath("//*[@class='totalcount']");
			String ret = lowerBoundRange.asText() + " - " + upperBoundRange.asText() + " out of " + totalItem.asText();
			return ret;
		} catch (Exception e) {
			System.out.println(e);
			return null;
			}
	}
	
	private static String getTitle(HtmlElement item, String portal) {
		if (DEBUG) System.out.println("\t DEBUG: entering getTitle method");
		String xPathAddr = (portal == AMAZON_URL) ? ".//h2[@data-attribute]" : ".//p[@class='result-info']/a";
		HtmlElement itemTitle = (HtmlElement) item.getFirstByXPath(xPathAddr);
		
		// USE:CASE non-item case, particularly on Amazon portal
		if (itemTitle == null || itemTitle.asText() == "") {
			if (DEBUG) System.out.println("\t DEBUG: NON-ITEM ALERT!!!!");
			return null;
		}
		return itemTitle.asText();		
	}

	// currently for craigslist item
	private static ArrayList<Item> scrapePage(HtmlPage page) {
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
	
	private static Double getPrice(HtmlElement item, String portal) {
		if (DEBUG) System.out.println("\t DEBUG: entering getPrice method");
		// return 0.0 if the price is not specified
		if (portal == AMAZON_URL) {
			// portal: amazon 
			// USE CASE: only consider the main item, not the buying options
			// USE CASE: only main price is used, not the offer with kindle, etc.
			ArrayList<HtmlElement> ItemWholePrice = new ArrayList<HtmlElement> (item.getByXPath(".//*[contains(@class, 'sx-price-whole')]"));
			ArrayList<HtmlElement> ItemFractionalPrice = new ArrayList<HtmlElement> (item.getByXPath(".//*[contains(@class, 'sx-price-fractional')]"));
			
			if (ItemWholePrice == null || ItemFractionalPrice == null || ItemWholePrice.size() == 0 || ItemFractionalPrice.size() == 0) {
				HtmlElement offeredPrice = (HtmlElement) item.getFirstByXPath(".//*[contains(text(),'offer')]");
				if (offeredPrice == null) 
					return 0.0;
				else {
					// USE CASE: no price available, but there some offers which contain price
					if (DEBUG) System.out.println("\t DEBUG: no price available, but there are offers at " + offeredPrice.asText());
					return new Double(offeredPrice.asText().replaceAll("\\(.*\\)", "").replace("$", "").replace(",", ""));
				}
			} else if (ItemWholePrice.size() > 1 || ItemFractionalPrice.size() > 1) {
				Double lowPrice = new Double (ItemWholePrice.get(0).asText().replace("$", "").replace(",", "") + "." + ItemFractionalPrice.get(0).asText());
				Double highPrice = new Double (ItemWholePrice.get(1).asText().replace("$", "").replace(",","") + "." + ItemFractionalPrice.get(1).asText());
				// USE CASE: return average price if the price given is a range
				return (lowPrice + highPrice) / 2.0;
			} else { 
				if (DEBUG) System.out.println("\t DEBUG: GETPRICE FINAL " + ItemWholePrice.size() + " and " + ItemFractionalPrice.size());
				return new Double (ItemWholePrice.get(0).asText().replace(",", "") + "." + ItemFractionalPrice.get(0).asText()); }
		} else {
			// portal: craigslist
			HtmlElement itemPrice = ((HtmlElement) item.getFirstByXPath(".//a/span[@class='result-price']"));
			if (itemPrice == null) 
				return 0.0;
			else 
				return new Double(itemPrice.asText().replace("$", "").replace(",",""));
		}
		}

	private static String getUrl(HtmlElement item, String portal) {
		if (DEBUG) System.out.println("\t DEBUG: entering getUrl method");
		String portal_url = (portal == AMAZON_URL) ? AMAZON_URL : DEFAULT_URL;
		String xPathAddr = (portal == AMAZON_URL) ? ".//h2[@data-attribute]/parent::a" : ".//p[@class='result-info']/a";
		HtmlAnchor itemUrl = (HtmlAnchor) item.getFirstByXPath(xPathAddr);
		if (itemUrl.getHrefAttribute().startsWith(portal_url)) 
			return itemUrl.getHrefAttribute();
		else 
			return portal_url+itemUrl.getHrefAttribute();
	}

	private static Date getPostedDate(HtmlElement item) {
		// currently only for craigslist
		DomAttr itemDate = (DomAttr) item.getFirstByXPath(".//*[@class='result-date']/@datetime");
		SimpleDateFormat dateFormatting = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try { 
			return dateFormatting.parse(itemDate.getValue()); 
		} catch (Exception e) { 
			if (DEBUG) System.out.println("\t DEBUG: postedDate non-existence!!!");
			return null;
			}
		}
		
	private static Vector<Item> sortResult(ArrayList<Item> amazonArrayList, ArrayList<Item> craigsArrayList){
		if (DEBUG) System.out.println("\t DEBUG: entering getTitle method");
		Vector<Item> result = new Vector<Item>();
		// sort ascending, for the same price, craigslist item goes first
		for (int i=0, j=0; !amazonArrayList.isEmpty() && !craigsArrayList.isEmpty();) {
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
			controller.printConsole("Scraping amazon... \n"); System.out.println("   DEBUG: scraping amazon...");
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
			
			
			/*
			 * NEWYORK CRAIGSLIST SCRAPER
			 */
			controller.printConsole("Scraping craigslist... \n"); System.out.println("   DEBUG: scraping craigslist...");
			String searchUrl = DEFAULT_URL + "search/sss?sort=rel&query=" + URLEncoder.encode(keyword, "UTF-8");
			HtmlPage page = client.getPage(searchUrl);
//			List<?> items = (List<?>) page.getByXPath("//li[@class='result-row']");
			ArrayList<Item> craigsArrayList = new ArrayList<Item>();
			HtmlElement lowerBoundRange = (HtmlElement) page.getFirstByXPath("//*[@class='rangeFrom']");
			HtmlElement upperBoundRange = (HtmlElement) page.getFirstByXPath("//*[@class='rangeTo']");
			HtmlElement totalItem = (HtmlElement) page.getFirstByXPath("//*[@class='totalcount']");
			
			int currentPage = 1;
			int numOfPages = (lowerBoundRange == null || upperBoundRange == null || totalItem == null) ? 0 : 
				((int) Math.ceil(Integer.parseInt(totalItem.asText())/Integer.parseInt(upperBoundRange.asText())));
			controller.printConsole("Scraping page " + currentPage + " out of " + numOfPages + "\n");
			
			// handle pagination
			craigsArrayList.addAll(scrapePage(page));
			while ((HtmlElement) page.getFirstByXPath("//a[@class='button next']") != null) {
				currentPage += 1;
				controller.printConsole("Scraping page " + currentPage + " out of " + numOfPages + "\n");
				page = client.getPage(DEFAULT_URL + ((HtmlAnchor)page.getFirstByXPath("//a[@class='button next']")).getHrefAttribute());
				craigsArrayList.addAll(scrapePage(page));
			}
			
//			for (int i = 0; i < items.size(); i++) {
//				HtmlElement htmlItem = (HtmlElement) items.get(i);	
//
//				Item item = new Item(getTitle(htmlItem, DEFAULT_URL), getPrice(htmlItem, DEFAULT_URL), getUrl(htmlItem, DEFAULT_URL), DEFAULT_URL, 
//						getPostedDate(htmlItem));
//				craigsArrayList.add(item);
//			}
			Collections.sort(craigsArrayList);
			
			// append final result to be returned based on sorting
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
