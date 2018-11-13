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
import java.util.Vector;

public class SpiderTest{
//	private static String dir_;
//	private static WebClient amazonClient;	
//	private static final String AMAZON = "https://www.amazon.com/";
//	
//	@BeforeClass
//	public static void init() {
//		dir_ = "file://" + System.getProperty("user.dir") + "/unitTest_pages";
//		amazonClient = new WebClient();
//		amazonClient.getOptions().setCssEnabled(false);
//		amazonClient.getOptions().setJavaScriptEnabled(false);
//	}

//	@Test
//	public void spiderService() throws Exception{
//		Spider spider = new Spider("temp", "temp");
//		assertNull(spider.call());
//	}
}