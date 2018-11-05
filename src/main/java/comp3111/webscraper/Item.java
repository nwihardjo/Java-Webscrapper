package comp3111.webscraper;

import java.util.Arrays;


public class Item implements Comparable <Item> {
	private String title ; 
	// price in HKD
	private double price ;
	private String url ;
	private String portal ;
	
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