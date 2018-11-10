/**
 * 
 */
package comp3111.webscraper;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Hyperlink;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import javafx.application.Platform;


/**
 * 
 * @author kevinw
 * @author hanifdean
 * @author nwihardjo
 * @author albertparedandan
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
    private TextField refineKeyword;

    @FXML
    private TextArea textAreaConsole;

    @FXML
    private Button goButton;

    @FXML
    private Button refineButton;

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
        refineKeyword.setDisable(true);
        refineButton.setDisable(true);
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
//    		refreshSummaryTab();
//    		togglePrimarySearch();
//    		toggleRefineSearch();
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

    @FXML
    private void refineSearch() {
        System.out.println("refineSearch: " + refineKeyword.getText());
        String refineStr = refineKeyword.getText().toLowerCase();
        Iterator<Item> resultItr = scraperResult.iterator();
//        System.out.println("DEBUG: Before refining, count is: " + scraperResult.size());
        while (resultItr.hasNext()) {
            Item currentItem = resultItr.next();
            String itemTitle = currentItem.getTitle().toLowerCase();
            if (!itemTitle.contains(refineStr)) {
                resultItr.remove();
            }
        }
//        System.out.println("DEBUG: After refining, count is: " + scraperResult.size());
//        System.out.println("DEBUG: After refining, the following items are");
        for (Item item : scraperResult) {
            System.out.println(item.getTitle());
        }
        refreshSummaryTab();
        togglePrimarySearch();
        toggleRefineSearch();
    }

    private void togglePrimarySearch() {
        if (textFieldKeyword.isDisabled()) {
            textFieldKeyword.setDisable(false);
            goButton.setDisable(false);
        }
        else {
            textFieldKeyword.setDisable(true);
            goButton.setDisable(true);
        }

    }

    private void toggleRefineSearch() {
        if (refineKeyword.isDisabled()) {
            refineKeyword.setDisable(false);
            refineButton.setDisable(false);
        }
        else {
            refineKeyword.setDisable(true);
            refineButton.setDisable(true);
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

    private void refreshSummaryTab() {
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
        /*
         * Returns the first item with the lowest price
         * if there are two items with the same lowest price,
         * function returns the first one.
         */
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

    private String getLatestPostURI() {
        Date latestDate = scraperResult.get(0).getPostedDate();
        String latestPostURI = scraperResult.get(0).getUrl();
        for (Item item: scraperResult) {
            if (item.getPostedDate() != null) {
                // Debug: to check if still enters
                // System.out.println(item.getPostedDate());
                if (item.getPostedDate().after(latestDate)) {
//                    System.out.println("Latest Date: " + latestDate.toString());
                    latestDate = item.getPostedDate();
                    latestPostURI = item.getUrl();
                }
            }
        }
        return latestPostURI;
    }
}

