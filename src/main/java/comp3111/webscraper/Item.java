package comp3111.webscraper;

import java.util.Date;

public class Item implements Comparable <Item> {
	private String title ; 
	// price in HKD
	private double price ;
	private String url ;
	private String portal ;
	private Date postedDate;
	
	public Item(String title, Double price, String url, String portal, Date postedDate) {
		this.setTitle(title);
		this.setPrice(price);
		this.setUrl(url);
		this.setPortal(portal);
		this.setPostedDate(postedDate);
	}
	
	public Item() {}
	
	
	public Date getPostedDate() {
		return postedDate;
	}
	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPortal() {
		return portal;
	}
	public void setPortal(String portal) {
		this.portal = portal;
	}
	public int compareTo (Item item) {
		// returns -1 if first argument is smaller, 0 if equal, 1 if larger
		return Double.compare(this.price, item.getPrice());
	}
}	