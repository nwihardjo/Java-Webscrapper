package comp3111.webscraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.Callable;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SubScraper implements Callable<String> {
	
	private WebClient client;
	private String Url;
	private HtmlPage page;
	
	public SubScraper (String Url) throws Exception {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.waitForBackgroundJavaScript(100000);
		this.Url = Url;
	}
	
	@Override
	public String call() {
		try { page = client.getPage(this.Url); }
		catch (Exception e) {System.out.println(e);}
		
		List<HtmlElement> postedDate = page.getByXPath("//th[contains(@class, 'prodDetSectionEntry')]");
		if (postedDate == null || postedDate.isEmpty()) {
			System.out.println("\t DEBUG SUBSCRAPER: return null");
			return null;
		} else {
			HtmlElement target = (HtmlElement) postedDate.get(postedDate.size() - 1).getFirstByXPath("./following-sibling::td");
			if (target != null && target.asText() != "") 
				System.out.println("\t DEBUG: date is " + target.asText());
			else 
				System.out.println("\t DEBUG: SIBLING NODE WRONG");
			System.out.println("\t DEBUG: return " + postedDate.get(postedDate.size() -1 ).asText());
			return postedDate.get(postedDate.size() - 1).asText();
		}
	}
}