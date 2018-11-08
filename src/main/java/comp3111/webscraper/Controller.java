/**
 * 
 */
package comp3111.webscraper;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Hyperlink;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.PopupFeatures;
import javafx.util.Callback;
import java.util.List;


/**
 * 
 * @author kevinw
 *
 *
 * Controller class that manage GUI interaction. Please see document about JavaFX for details.
 * 
 */
public class Controller {

    @FXML 
    private Label labelCount; 

    @FXML 
    private Label labelPrice; 

    @FXML 
    private Hyperlink labelMin; 

    @FXML 
    private Hyperlink labelLatest; 

    @FXML
    private TextField textFieldKeyword;
    
    @FXML
    private TextArea textAreaConsole;
    
    private WebScraper scraper;

    private List<Item> scraperResult;
    
    /**
     * Default controller
     */
    public Controller() {
    	scraper = new WebScraper();
    }

    /**
     * Default initializer. It is empty.
     */
    @FXML
    private void initialize() {
    	
    }
    
    /**
     * Called when the search button is pressed.
     */
    @FXML
    private void actionSearch() {
    	System.out.println("actionSearch: " + textFieldKeyword.getText());
    	List<Item> result = scraper.scrape(textFieldKeyword.getText());
    	String output = "";
    	for (Item item : result) {
    		output += item.getTitle() + "\t" + item.getPrice() + "\t" + item.getUrl() + "\n";
    	}
    	textAreaConsole.setText(output);
    	scraperResult = result;
    	fillSummaryTab();
    }
    
    /**
     * Called when the new button is pressed. Very dummy action - print something in the command prompt.
     */
    @FXML
    private void actionNew() {
        System.out.println("actionNew");
    }

    @FXML
    private void displaySummary(Event event) {
        // Right now no need
        System.out.println("Summary tab selected");
        // TODO: correctly fill the tab for summary
        // Get number of items fetched, average selling price, lowest selling price and display link
        // Get latest post and give hyperlink

    }

    private void fillSummaryTab() {
        int itemCount = getItemCount();
        double avgPrice = getAvgPrice();
        double lowestPrice = getLowestPrice();
        setLabelCount(itemCount);
        setLabelPrice(avgPrice);
        setLabelMin(lowestPrice);
    }

    private int getItemCount() {
        return scraperResult.size();
    }

    private void setLabelCount(int itemCount) {
        labelCount.setText(Integer.toString(itemCount));
    }

    private double getAvgPrice() {
        boolean resultFound = (scraperResult.size() != 0);
        if (resultFound)
            return countAvgPrice(scraperResult);
        else
            return 0.0;
    }

    private double countAvgPrice(List<Item> scraperResult) {
        double totalPrice = 0.0;
        int itemCount = 0;
        for (Item item : scraperResult) {
            if (item.getPrice() == 0.0)
                continue;
            totalPrice += item.getPrice();
            itemCount++;
        }
        return totalPrice / itemCount;
    }

    private void setLabelPrice(double avgPrice) {
        boolean priceAvailable = (avgPrice != 0.0);
        if (priceAvailable)
            labelPrice.setText(Double.toString(avgPrice));
        else
            labelPrice.setText("-");
    }

    private double getLowestPrice() {
        boolean resultFound = (scraperResult.size() != 0);
        if (resultFound)
            return countLowestPrice(scraperResult);
        else
            return 0.0;
    }

    private double countLowestPrice(List<Item> scraperResult) {
        double lowestPrice = 100000000.0;
        for (Item item : scraperResult) {
            if (item.getPrice() == 0.0)
                continue;
            if (item.getPrice() < lowestPrice)
                lowestPrice = item.getPrice();
        }
        return lowestPrice;
    }

    private void setLabelMin(double lowestPrice) {
        // if priceAvailable set hyperlink to page (getItemWithLowestPrice)
        // else display "-"
        boolean priceAvailable = (lowestPrice != 0.0);
        if (priceAvailable) {
            labelMin.setText(Double.toString(lowestPrice));
            handlePopUp();
        }
        else
            labelMin.setText("-");
    }

    private void handlePopUp() {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        labelMin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                webEngine.load(getLowestPriceUrl());
            }
        });
        webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
            @Override public WebEngine call(PopupFeatures config) {
                return webEngine;
            }
        });
    }

    private String getLowestPriceUrl() {
        // Returns the first item with the lowest price given the scraperResult (already sorted in ascending order)
        double lowestPrice = getLowestPrice();
        String url = "";
        for (Item item : scraperResult) {
            if (item.getPrice() == lowestPrice) {
                url = item.getUrl();
                break;
            }
        }
        return url;
    }



}

