package comp3111.webscraper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebScraperTest{
	private static String dir_;
	private static WebClient craigsClient;
	private static WebClient amazonClient;	
	private static final String DEFAULT = "https://newyork.craigslist.org/";
	private static final String AMAZON = "https://www.amazon.com/";
	
	@BeforeClass
	public static void init() {
		dir_ = "file://" + System.getProperty("user.dir");
		craigsClient = new WebClient();
		craigsClient.getOptions().setCssEnabled(false);
		craigsClient.getOptions().setJavaScriptEnabled(false);
		amazonClient = new WebClient();
		amazonClient.getOptions().setCssEnabled(false);
		amazonClient.getOptions().setJavaScriptEnabled(false);
	}
	
	@Test
	public void scraperInstantiation() throws Exception{
		assertNotNull(new WebScraper());
	}
	
	@Test
	public void amazonTitle() throws Exception {
		HtmlPage amazonPage = amazonClient.getPage(dir_ + "/amazon0.html");
		List<?> amazonItems = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");
		
		// normal case
		String amazonTitle = WebScraper.getTitle((HtmlElement) amazonItems.get(1), AMAZON);
		assertEquals(amazonTitle, "[Our brand]Amazon Essentials Men's 6-Pack Quick-Dry Low-Cut Socks");

		// title with [sponsored] case
		String amazonSprTitle = WebScraper.getTitle((HtmlElement) amazonItems.get(2), AMAZON);
		assertEquals(amazonSprTitle, "TuYuex Mens Low Cut Ankle Fashion Socks,Combed Cotton Socks Pack (6 Pairs Pack)");			

		// non-item case
		amazonPage = amazonClient.getPage(dir_ + "/amazon1.html");
		HtmlElement amazonNullItem = (HtmlElement) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]").get(7);
		assertEquals(WebScraper.getTitle(amazonNullItem, AMAZON), null);
	}
	
	@Test
	public void amazonPrice() throws Exception{
		HtmlPage amazonPage = amazonClient.getPage(dir_ + "/amazon0.html");
		List<?> amazonItems = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");
		// single price case
		Double amazonPrice = WebScraper.getPrice((HtmlElement) amazonItems.get(1), AMAZON);
		assertEquals(amazonPrice, 11.00, 1e-2);
		
		// range price --> use average price 
		Double amazonAvgPrice = WebScraper.getPrice((HtmlElement) amazonItems.get(4), AMAZON);
		assertEquals(amazonAvgPrice, 24.485, 1e-3);

		// range price missing fractional price class
		Double amazonMissingFractAvg = WebScraper.getPrice((HtmlElement) amazonItems.get(5), AMAZON);
		assertEquals(amazonMissingFractAvg, 15.99, 1e-2);
		
		amazonPage = amazonClient.getPage(dir_ + "/amazon1.html");
		List<?> amazon1Items = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");		
		// item with no price case	
		Double amazonNoPrice = WebScraper.getPrice((HtmlElement) amazon1Items.get(7), AMAZON);
		assertEquals(amazonNoPrice, 0.0, 1);

		// item with only missing fractional price class
		Double amazonMissingFract = WebScraper.getPrice((HtmlElement) amazon1Items.get(1), AMAZON);
		assertEquals(amazonMissingFract, 0.0, 1);
	
		
		// item with offer price case
		amazonPage = amazonClient.getPage(dir_ + "/amazon2.html");
		HtmlElement amazonOfferItem = (HtmlElement) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]").get(4);
		assertEquals(WebScraper.getPrice(amazonOfferItem, AMAZON), 744.0, 1e-1);
	}

	@Test
	public void amazonUrl() throws Exception{
		HtmlPage amazonPage = amazonClient.getPage(dir_ + "/amazon0.html");
		List<?> amazonItems = (List<?>) amazonPage.getByXPath("//li[starts-with(@id, 'result_')]");
		// complete url
		String completeItemUrl = WebScraper.getUrl((HtmlElement) amazonItems.get(0), AMAZON);
		String completeUrl = "https://www.amazon.com/gp/slredirect/picassoRedirect.html/ref=pa_sp_atf_aps_sr_pg1_1?ie=UTF8&adId=A07889822Z4BXVZO92H9N&url=https%3A%2F%2Fwww.amazon.com%2FAmazon-Essentials-6-Pack-Quick-Dry-Socks%2Fdp%2FB01JRLH4FG%2Fref%3Dsr_1_1_sspa%3Fie%3DUTF8%26qid%3D1541952815%26sr%3D8-1-spons%26keywords%3Dsocks%26psc%3D1&qualifier=1541952815&id=3179112557127668&widgetName=sp_atf";
		assertEquals(completeItemUrl, completeUrl);
		
		// incomplete url
		String incompleteItemUrl = WebScraper.getUrl((HtmlElement) amazonItems.get(1), AMAZON);
		completeUrl = "https://www.amazon.com/gp/slredirect/picassoRedirect.html/ref=pa_sp_atf_aps_sr_pg1_2?ie=UTF8&adId=A00605332KNDUPPHXC2MZ&url=https%3A%2F%2Fwww.amazon.com%2FAmazon-Essentials-6-Pack-Quick-Dry-Low-Cut%2Fdp%2FB01JPF63AG%2Fref%3Dsr_1_2_sspa%3Fie%3DUTF8%26qid%3D1541952815%26sr%3D8-2-spons%26keywords%3Dsocks%26psc%3D1&qualifier=1541952815&id=3179112557127668&widgetName=sp_atf";
		assertEquals(incompleteItemUrl, completeUrl);
	}
	
	@Test
	public void craigsTitle() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		HtmlElement craigsItem = craigsPage.getFirstByXPath("//li[@class='result-row']");
		String craigsTitle = WebScraper.getTitle(craigsItem, DEFAULT);
		assertEquals(craigsTitle, "Like the Kind of SLIPPER SOCKS Used in Hospitals? Afraid of Falling?");
	}
	
	@Test
	public void craigsPrice() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		List<?> craigsItems = (List<?>) craigsPage.getByXPath("//li[@class='result-row']");
		
		// normal case
		Double craigsPrice = WebScraper.getPrice((HtmlElement) craigsItems.get(0), DEFAULT);
		assertEquals(craigsPrice, 2.00, 1e-2);
		
		// item containing no price case
		Double craigsNullPrice = WebScraper.getPrice((HtmlElement) craigsItems.get(1), DEFAULT);
		assertEquals(craigsNullPrice, 0.0, 1);
	}
	
	@Test
	public void craigsNextPage() throws Exception{
		// next page exists with complete url
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");	
		String completeNextPageUrl = WebScraper.getNextPage(craigsPage);
		String completeUrl = "https://newyork.craigslist.org/search/sss?s=120&query=socks&sort=rel";
		assertEquals(completeNextPageUrl, completeUrl);
		
		// next page exists with incomplete url
		HtmlPage craigs1Page = craigsClient.getPage(dir_ + "/craigslist1.html");	
		String incompleteNextPageUrl = WebScraper.getNextPage(craigs1Page);
		assertEquals(incompleteNextPageUrl, completeUrl);
		
		// null next page
		HtmlPage craigs2Page = craigsClient.getPage(dir_ + "/craigslist2.html");	
		String nullNextPageUrl = WebScraper.getNextPage(craigs2Page);
		assertNull(nullNextPageUrl);
	}

	@Test
	public void craigsUrl() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		List<?> craigsItems = (List<?>) craigsPage.getByXPath("//li[@class='result-row']");	
		// complete item url
		String itemUrl = "https://newyork.craigslist.org/mnh/clo/d/like-the-kind-of-slipper/6746180949.html";
		String completeItemUrl = WebScraper.getUrl((HtmlElement) craigsItems.get(0), DEFAULT);
		assertEquals(completeItemUrl, itemUrl);
		
		// incomplete item url
		String incompleteItemUrl = WebScraper.getUrl((HtmlElement) craigsItems.get(1), DEFAULT);
		itemUrl = "https://newyork.craigslist.org/mnh/for/d/new-travel-kit-slipper-socks/6746167194.html";
		assertEquals(incompleteItemUrl, itemUrl);
	}

	@Test
	public void craigsPostedDate() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		List<?> craigsItems = (List<?>) craigsPage.getByXPath("//li[@class='result-row']");	
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		// normal case
		Date itemDate = WebScraper.getPostedDate((HtmlElement) craigsItems.get(0));	
		assertEquals(itemDate, format.parse("2018-11-11 08:27"));
		
		// exception parsing date case
		Date nullItemDate = WebScraper.getPostedDate((HtmlElement) craigsItems.get(2));
		assertNull(nullItemDate);
	}
	
	@Test
	public void craigsScrapePage() throws Exception{
		HtmlPage craigsPage = craigsClient.getPage(dir_ + "/craigslist0.html");
		ArrayList<Item> craigsAL = WebScraper.scrapePage(craigsPage);
		assertEquals(craigsAL.size(), 120);
	}
	
	@AfterClass
	public static void destroy() {
		craigsClient.close();
		amazonClient.close();
	}
}
