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

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javafx.application.Platform;


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
    	Thread thread = new Thread(() -> {
    		textAreaConsole.clear();
    		System.out.println("actionSearch: " + textFieldKeyword.getText());
    		List<Item> result = scraper.scrape(textFieldKeyword.getText(), this);
    		String output = "";
    		for (Item item : result) {
    			output += item.getTitle() + "\t" + item.getPrice() + "\t" + item.getUrl() + "\n";
    		}	
    		textAreaConsole.clear();
    		printConsole(output); 
    		scraperResult = result;
//    		fillSummaryTab();
    	});
    	thread.start();
    }
    
    // enable asynchronous printing on console tab
    public void printConsole (String message) {
    	if (Platform.isFxApplicationThread()) {
    		textAreaConsole.appendText(message);
    	} else {
    		Platform.runLater(() -> textAreaConsole.appendText(message));
    	}
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
        System.out.println("Summary tab selected");
    }

    private void fillSummaryTab() {
        int itemCount = getItemCount();
        double avgPrice = getAvgPrice();
        double lowestPrice = getLowestPrice();
        setLabelCount(itemCount);
        setLabelPrice(avgPrice);
        setLabelMin(lowestPrice);
        setLabelLatest();
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
            labelPrice.setText(String.format("%.2f", avgPrice));
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
            labelMin.setText(String.format("%.2f", lowestPrice));
            showLowestPricedItemInBrowser();
        }
        else
            labelMin.setText("-");
    }

    private void showLowestPricedItemInBrowser() {
        labelMin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI(getLowestPriceURI()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private String getLowestPriceURI() {
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

    private void setLabelLatest() {
        boolean itemsFound = (getItemCount() != 0);
        if (itemsFound) {
            showLatestPostInBrowser();
        }
        else
            labelLatest.setText("-");
    }

    private void showLatestPostInBrowser() {
        labelLatest.setText("See latest post");
        labelLatest.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI(getLatestPostURI()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    // TODO: finish this function
    private String getLatestPostURI() {
        return "Dummy string that represents latest post URI";
    }

}

