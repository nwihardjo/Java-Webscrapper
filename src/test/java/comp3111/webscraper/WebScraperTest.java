package comp3111.webscraper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static org.junit.Assert.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.lang.reflect.Method;

public class WebScraperTest{
	private static String dir_;
	private static WebClient craigsClient;
	private static WebClient amazonClient;	
	private static final String DEFAULT = "https://newyork.craigslist.org/";
	private static final String AMAZON = "https://www.amazon.com/";
	private static WebScraper scraper;
	private static Spider spider;
	private static craigsSpider cSpider;
	private static ArrayList<Method> methods;
	
	@BeforeClass
	public static void init() throws Exception {
		dir_ = "file://" + System.getProperty("user.dir") + "/unitTest_pages";
		scraper = new WebScraper();
		spider = new Spider("temp");
		cSpider = new craigsSpider("temp");
		
		craigsClient = new WebClient();
		craigsClient.getOptions().setCssEnabled(false);
		craigsClient.getOptions().setJavaScriptEnabled(false);
		
		amazonClient = new WebClient();
		amazonClient.getOptions().setCssEnabled(false);
		amazonClient.getOptions().setJavaScriptEnabled(false);
		
		methods = new ArrayList<Method>();
		
		Class[] paramTypes = new Class[2];
		paramTypes[0] = com.gargoylesoftware.htmlunit.html.HtmlElement.class;
		paramTypes[1] = java.lang.String.class;
		methods.add(scraper.getClass().getDeclaredMethod("getTitle", paramTypes));
		methods.add(scraper.getClass().getDeclaredMethod("getPrice", paramTypes));
		methods.add(scraper.getClass().getDeclaredMethod("getUrl", paramTypes));
		
		paramTypes = new Class[1];
		paramTypes[0] = java.util.ArrayList.class;
		methods.add(scraper.getClass().getDeclaredMethod("deploySpiders", paramTypes));
		
		paramTypes[0] = com.gargoylesoftware.htmlunit.html.HtmlElement.class;
		methods.add(scraper.getClass().getDeclaredMethod("getPostedDate", paramTypes));
		
		paramTypes[0] = com.gargoylesoftware.htmlunit.html.HtmlPage.class;
		methods.add(scraper.getClass().getDeclaredMethod("scrapePage", paramTypes));
	
		paramTypes[0] = java.lang.Double.class;
		methods.add(scraper.getClass().getDeclaredMethod("roundUp", paramTypes));
		
		paramTypes = new Class[2];
		paramTypes[0] = java.util.ArrayList.class;
		paramTypes[1] = java.util.ArrayList.class;
		methods.add(scraper.getClass().getDeclaredMethod("sortResult",  paramTypes));
		
		paramTypes = new Class[1];
		paramTypes[0] = java.lang.String.class;
		methods.add(spider.getClass().getDeclaredMethod("parseDate", paramTypes));
		
		for (Method method : methods) 
			method.setAccessible(true);
		}

	@Test
	public void roundingTest() throws Exception{
		int upper = (int) methods.get(6).invoke(null, 2.7);
		assertEquals(upper, 3);
		
		int lower = (int) methods.get(6).invoke(null,  2.034);
		assertEquals(lower, 3);
	}
	
	@Test
	public void amazonTitle() throws Exception {
		HtmlPage amazonPage = amazonClient.getPage(dir_ + "/amazon0.html");
		List<?> amazonItems = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");
		
		// normal case
		String amazonTitle = (String) methods.get(0).invoke(null, (HtmlElement) amazonItems.get(1), AMAZON);
		assertEquals(amazonTitle, "[Our brand]Amazon Essentials Men's 6-Pack Quick-Dry Low-Cut Socks");
		
		String amazonSprTitle = (String) methods.get(0).invoke(null, (HtmlElement) amazonItems.get(2), AMAZON);
		assertEquals(amazonSprTitle, "TuYuex Mens Low Cut Ankle Fashion Socks,Combed Cotton Socks Pack (6 Pairs Pack)");			

		// non-item case
		amazonPage = amazonClient.getPage(dir_ + "/amazon1.html");
		String amazonNonItemTitle = (String) methods.get(0).invoke(null, (HtmlElement) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]").get(7), AMAZON);
		assertEquals(amazonNonItemTitle, null);
	}
	
	@Test
	public void amazonPrice() throws Exception{
		HtmlPage amazonPage = amazonClient.getPage(dir_ + "/amazon0.html");
		List<?> amazonItems = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");
		// single price case
		Double amazonPrice = (Double) methods.get(1).invoke(null, (HtmlElement) amazonItems.get(1), AMAZON);
		assertEquals(amazonPrice, 11.00, 1e-2);
		
		// range price --> use average price 
		Double amazonAvgPrice = (Double) methods.get(1).invoke(null, (HtmlElement) amazonItems.get(4), AMAZON);
		assertEquals(amazonAvgPrice, 24.485, 1e-3);

		// range price missing fractional price class
		Double amazonMissingFractAvg = (Double) methods.get(1).invoke(null, (HtmlElement) amazonItems.get(5), AMAZON);
		assertEquals(amazonMissingFractAvg, 15.99, 1e-2);
		
		amazonPage = amazonClient.getPage(dir_ + "/amazon1.html");
		List<?> amazon1Items = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");		
		// item with no price case	
		Double amazonNoPrice = (Double) methods.get(1).invoke(null, (HtmlElement) amazon1Items.get(7), AMAZON);
		assertEquals(amazonNoPrice, 0.0, 1);

		// item with only missing fractional price class
		Double amazonMissingFract = (Double) methods.get(1).invoke(null, (HtmlElement) amazon1Items.get(1), AMAZON);
		assertEquals(amazonMissingFract, 0.0, 1);
			
		// item with offer price case
		amazonPage = amazonClient.getPage(dir_ + "/amazon2.html");
		Double amazonOfferItem = (Double) methods.get(1).invoke(null, (HtmlElement) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]").get(4),
				AMAZON);
		assertEquals(amazonOfferItem, 744.0, 1e-1);
	}

	@Test
	public void amazonUrl() throws Exception{
		HtmlPage amazonPage = amazonClient.getPage(dir_ + "/amazon0.html");
		List<?> amazonItems = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");
		// complete url
		String completeItemUrl = (String) methods.get(2).invoke(null, (HtmlElement) amazonItems.get(0), AMAZON);
		String completeUrl = "https://www.amazon.com/gp/slredirect/picassoRedirect.html/ref=pa_sp_atf_aps_sr_pg1_1?ie=UTF8&adId=A07889822Z4BXVZO92H9N&url=https%3A%2F%2Fwww.amazon.com%2FAmazon-Essentials-6-Pack-Quick-Dry-Socks%2Fdp%2FB01JRLH4FG%2Fref%3Dsr_1_1_sspa%3Fie%3DUTF8%26qid%3D1541952815%26sr%3D8-1-spons%26keywords%3Dsocks%26psc%3D1&qualifier=1541952815&id=3179112557127668&widgetName=sp_atf";
		assertEquals(completeItemUrl, completeUrl);
		
		// incomplete url
		String incompleteItemUrl = (String) methods.get(2).invoke(null, (HtmlElement) amazonItems.get(1), AMAZON);
		completeUrl = "https://www.amazon.com/gp/slredirect/picassoRedirect.html/ref=pa_sp_atf_aps_sr_pg1_2?ie=UTF8&adId=A00605332KNDUPPHXC2MZ&url=https%3A%2F%2Fwww.amazon.com%2FAmazon-Essentials-6-Pack-Quick-Dry-Low-Cut%2Fdp%2FB01JPF63AG%2Fref%3Dsr_1_2_sspa%3Fie%3DUTF8%26qid%3D1541952815%26sr%3D8-2-spons%26keywords%3Dsocks%26psc%3D1&qualifier=1541952815&id=3179112557127668&widgetName=sp_atf";
		assertEquals(incompleteItemUrl, completeUrl);
	}

	@Test
	public void amazonSpidersDeployment() throws Exception{
		ArrayList<Item> amazonItems = new ArrayList<Item>();
		
		amazonItems.add(new Item("temp", 16.99, dir_ + "/amazonItem.html", AMAZON, null));
		amazonItems.add(new Item("temp", 0.0, dir_ + "/amazonLockpick.html", AMAZON, null));
		
		// normal use case
		amazonItems = (ArrayList<Item>) methods.get(3).invoke(scraper, amazonItems);
		Date date0 = (Date) methods.get(8).invoke(null,  "April 17, 2014");
		Date date1 = (Date) methods.get(8).invoke(null,  "November 21, 2015");
		assertEquals(amazonItems.get(0).getPostedDate(), date0);
		assertEquals(amazonItems.get(1).getPostedDate(), date1);
		
		amazonItems.add(new Item("temp", 0.0, dir_ + "/amazonService.html", AMAZON, null));
		amazonItems.add(new Item("temp", 0.0, dir_ + "/amazonNoDate.html", AMAZON, null));
		amazonItems.add(new Item("temp", 0.0, "temp", AMAZON, null));
		
		amazonItems = (ArrayList<Item>) methods.get(3).invoke(scraper, amazonItems);
		Date date2 = (Date) methods.get(8).invoke(null,  "October 1, 2015");
		// test 3 different cases / html layout
		assertEquals(amazonItems.get(2).getPostedDate(), date2);
		assertNull(amazonItems.get(3).getPostedDate());
		assertNull(amazonItems.get(4).getPostedDate());
	}

	@Test
	public void craigsTitle() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		HtmlElement craigsItem = craigsPage.getFirstByXPath("//li[@class='result-row']");
		String craigsTitle = (String) methods.get(0).invoke(null,  craigsItem, DEFAULT);
		assertEquals(craigsTitle, "Like the Kind of SLIPPER SOCKS Used in Hospitals? Afraid of Falling?");
	}
	
	@Test
	public void craigsPrice() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		List<?> craigsItems = (List<?>) craigsPage.getByXPath("//li[@class='result-row']");
		
		// normal case
		Double craigsPrice = (Double) methods.get(1).invoke(null,  (HtmlElement) craigsItems.get(0), DEFAULT);
		assertEquals(craigsPrice, 2.00, 1e-2);
		
		// item containing no price case
		Double craigsNullPrice = (Double) methods.get(1).invoke(null,  (HtmlElement) craigsItems.get(1), DEFAULT);
		assertEquals(craigsNullPrice, 0.0, 1);
	}
	
	@Test
	public void craigsUrl() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		List<?> craigsItems = (List<?>) craigsPage.getByXPath("//li[@class='result-row']");	
		// complete item url
		String itemUrl = "https://newyork.craigslist.org/mnh/clo/d/like-the-kind-of-slipper/6746180949.html";
		String completeItemUrl = (String) methods.get(2).invoke(null, (HtmlElement) craigsItems.get(0), DEFAULT);
		assertEquals(completeItemUrl, itemUrl);
		
		// incomplete item url
		String incompleteItemUrl = (String) methods.get(2).invoke(null, (HtmlElement) craigsItems.get(1), DEFAULT);
		itemUrl = "https://newyork.craigslist.org/mnh/for/d/new-travel-kit-slipper-socks/6746167194.html";
		assertEquals(incompleteItemUrl, itemUrl);
	}

	@Test
	public void craigsPostedDate() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		List<?> craigsItems = (List<?>) craigsPage.getByXPath("//li[@class='result-row']");	
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		// normal case
		Date itemDate = (Date) methods.get(4).invoke(null, (HtmlElement) craigsItems.get(0));	
		assertEquals(itemDate, format.parse("2018-11-11 08:27"));
		
		// exception parsing date case
		Date nullItemDate = (Date) methods.get(4).invoke(null, (HtmlElement) craigsItems.get(2));
		assertNull(nullItemDate);
	}
	
	@Test
	public void craigsScrapePage() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		ArrayList<Item> craigsAL = (ArrayList<Item>) methods.get(5).invoke(null, craigsPage);
		assertEquals(craigsAL.size(), 120);
	}

	/*
	@Test
	public void craigsPagination() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslistEmpty.html");
		WebScraper scraper = new WebScraper();
		Controller controller = new Controller();
		
		ArrayList<Item> craigsItems = scraper.handlePagination(craigsPage, controller);
		assertEquals(craigsItems.size(), 0);
	}
	*/
	
	@Test
	public void resultSorting() throws Exception{
		ArrayList<Item> amazonItems = new ArrayList<Item>();
		ArrayList<Item> craigsItems = new ArrayList<Item>();		
		// both arrayLists empty
		Vector<Item> ret = (Vector<Item>) methods.get(7).invoke(scraper, amazonItems, craigsItems);
		assertEquals(ret, new Vector<Item>());
		
		// empty craigsArrayList --> none of the arraylist items were removed
		Item addItem = new Item("temp", 0.0, "temp", AMAZON, null);
		amazonItems.add(addItem);
		ret = (Vector<Item>) methods.get(7).invoke(scraper, amazonItems, craigsItems);
		assertEquals(ret.size(), 1);
		
		// size craigslist > size amazonlist --> items in the arraylist were removed
		craigsItems.add(addItem);
		craigsItems.add(new Item("temp", 10.0, "temp", DEFAULT, null));
		ret = (Vector<Item>) methods.get(7).invoke(scraper,  amazonItems, craigsItems);
		assertEquals(ret.size(), 3);
		
		// empty amazonArrayList --> none of the arraylist items were removed
		craigsItems.add(addItem);
		ret = (Vector<Item>) methods.get(7).invoke(scraper,  amazonItems, craigsItems);
		assertEquals(ret.size(), 1);
		
		// craigslist more item with different price --> items in the arraylist were removed
		for (int i = 0; i < 2; i ++) {
			amazonItems.add(new Item("temp"+i, Math.sqrt(new Double(i)), "temp", AMAZON, null));
			craigsItems.add(new Item("temp"+i+i, new Double(Math.pow(i, 3)), "temp", DEFAULT, null));
		}
		ret = (Vector<Item>) methods.get(7).invoke(scraper,  amazonItems, craigsItems);
		assertEquals(ret.size(), 5);
		
		// craigslist price < amazon's --> items in the arraylist were removed
		craigsItems.add(addItem);
		amazonItems.add(new Item("temp", 1000.0, "temp", AMAZON, null));
		ret = (Vector<Item>) methods.get(7).invoke(scraper,  amazonItems, craigsItems);
		assertEquals(ret.size(), 2);
		
		//TODO: generate same double price for 100% branch coverage
	}

	@Test
	public void scrapeTesting() throws Exception{
		WebScraper scraper = new WebScraper();
		Controller dummyController = new Controller();
		assertNull(scraper.scrape("-", dummyController));
	}
	
	@AfterClass
 	public static void destroy() {
		craigsClient.close();
		amazonClient.close();
	}
}
