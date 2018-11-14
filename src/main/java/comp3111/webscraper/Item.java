package comp3111.webscraper;

import java.util.Date;

/**
 * Class which is used to store the information of the posted items in both portals. It 
 * 	implements Comparable base class to extend its functionality to be sorted by the price
 * 	in a Collections-implemented containers.
 * 
 * @author nwihardjo
 *
 */

public class Item implements Comparable <Item> {
	private String title ; 
	// price in USD
	private double price ;
	private String url ;
	private String portal ;
	private Date postedDate;
	
	/**
	 * Public constructor which is used to instantiate an object while setting up its attributes
	 * 	in one line
	 * 
	 * @param title
	 * @param price
	 * @param url
	 * @param portal
	 * @param postedDate
	 */
	
	public Item(String title, Double price, String url, String portal, Date postedDate) {
		this.setTitle(title);
		this.setPrice(price);
		this.setUrl(url);
		this.setPortal(portal);
		this.setPostedDate(postedDate);
	}
	
	/**
	 * Default constructor of the item, instantiate the object without instantiate any values
	 */
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