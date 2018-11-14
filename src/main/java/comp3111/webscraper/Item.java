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
	private double price ;
	private String url ;
	private String portal ;
	private Date postedDate;
	
	/**
	 * Public constructor which is used to instantiate an object while setting up its attributes
	 * 	in one line
	 * 
	 * @param title name of the item posted
	 * @param price in USD. If no price specified, will have 0.0.
	 * @param url address of the posted item page in its respective portal
	 * @param portal amazon / craigslist address
	 * @param postedDate Date object of which the date is posted, in HKT
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
	/**
	 * Public method to return the date of the posted date of the item in its corresponding portal 
	 * @return date object of the posted date in HKT. Null if no information available
	 */
	public Date getPostedDate() {
		return postedDate;
	}
	
	/**
	 * Public method to set the posted date of the item, scraped from the portal
	 * @param postedDate date which the item is posted in HKT
	 */
	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}
	
	/**
	 * Public method to retrieve the name of the item posted
	 * @return title / name of the posted item
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Public method to set the title of the posted item
	 * @param title string consisting the name or title of the posted item
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Public method to retrieve the price of the posted item. Price is stored in USD.
	 * @return price of the item in USD. Price is 0.0 if no information available
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * Public method to set the price of the item in USD.
	 * @param price in USD. Double-type of object
	 */
	public void setPrice(double price) {
		this.price = price;
	}
	
	/**
	 * Method to retrieve the url address of the item page from its portal
	 * @return absolute url address of the item page 
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Public function to set the url of the item page
	 * @param url absolute url of the item page
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Public method to get the portal where the item came from
	 * @return absolute address of the portal 
	 */
	public String getPortal() {
		return portal;
	}
	
	/**
	 * Function to set the absolute url address of the portal where the item is scraped from
	 * @param portal absolute url address of the portal
	 */
	public void setPortal(String portal) {
		this.portal = portal;
	}
	/**
	 * Method used for the purpose of sorting the collections of items in the Collections-implemented container.
	 * 	It compares the item based on the price.
	 * @return -1 if the this (first argument) item has less price, 0 if both items have equal price, 1 if this (
	 * 	first argument) item has larger price.
	 */
	public int compareTo (Item item) {
		return Double.compare(this.price, item.getPrice());
	}
}	