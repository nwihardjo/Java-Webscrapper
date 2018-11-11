package comp3111.webscraper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Spider implements Callable<Date> {
	
	private WebClient client;
	private String Url;
	// title attribute were added for the purpose of debugging
	private String title;
	
	public Spider(String Url, String title) throws Exception {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.waitForBackgroundJavaScript(100000);
		this.Url = Url;
		this.title = title;
	}
		
	private HtmlPage getPage(){
		try { 
			HtmlPage page = client.getPage(this.Url);
			client.close();
			return page;
		} catch (Exception e) {
			System.out.println("\t DEBUG: spiders couldn't retrieve page");
			e.printStackTrace();
			return null;
		}
	}

	private Date scrapeService(HtmlPage page) {
		// for amazon service listed in the website, its page layout is different than the others
		HtmlElement postedDate = (HtmlElement) page.getFirstByXPath("//div[@class='content']//*[contains(text(),'Date')]/parent::li");
		if (postedDate != null && postedDate.asText() != "")
			return parseDate(postedDate.asText().substring(postedDate.asText().indexOf(": ") + 2));
		else
			return null;
	}
	
	private Date getDate(List<HtmlElement> containPostedDate) {
		// retrieve the date of the posted item, if any
		HtmlElement postedDate = (HtmlElement) containPostedDate.get(containPostedDate.size() - 1).getFirstByXPath("./following-sibling::td");
		// return null if contains no date
		return (postedDate != null && postedDate.asText() != "") ? parseDate(postedDate.asText()) : null;
	}
	
	private Date parseDate(String strDate) {
		// System.out.println("\t DEBUG: parsing item " + this.title + " with argument passed : " + strDate);
		SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
		try {
			return format.parse(strDate);
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public Date call() {
		HtmlPage page = this.getPage();
		List<HtmlElement> containPostedDate = page.getByXPath("//*[contains(@class, 'prodDetSectionEntry')]");		
		// USE-CASE: check whether scraped item is a service
		return (containPostedDate == null || containPostedDate.isEmpty()) ? scrapeService(page) : getDate(containPostedDate);
	}
}