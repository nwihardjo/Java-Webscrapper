/**
 * 
 */
package comp3111.webscraper;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javax.swing.JOptionPane;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

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

    @FXML
    private MenuItem lastSearchMenuItem;

    @FXML
    private TableView itemTable;

    @FXML
    private TableColumn tableTitle;

    @FXML
    private TableColumn tablePrice;

    @FXML
    private TableColumn tableURL;

    @FXML
    private TableColumn tablePostedDate;

    private WebScraper scraper;

    private List<Item> scraperResult;

    private List<Item> lastResult;

    private String currentOutput = "";

    private String lastOutput = "";

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
        lastSearchMenuItem.setDisable(true);
    }

    @FXML
    private void aboutYourTeam() {
        JOptionPane.showMessageDialog(null, 
            "Albert Paredandan\n" +
            "   ITSC: aparedandan\n" +
            "   GitHub: albertparedandan\n\n" +
            "Hanif Dean\n" +
            "   ITSC: mhdnadhif\n" +
            "   GitHub: hanifdean\n\n" +
            "Nathaniel Wihardjo\n" +
            "   ITSC: nwihardjo\n" +
            "   GitHub: nwihardjo", "About Your Team", JOptionPane.PLAIN_MESSAGE);
    }

    @FXML
    private void quit() {
        System.exit(0);
    }

    @FXML
    private void close() {
        // @@@@@ set scraperResult null or not
        scraperResult = null;
        textAreaConsole.clear();
        labelCount.setText("<total>");
        labelPrice.setText("<AvgPrice>");
        labelMin.setText("<Lowest>");
        labelLatest.setText("<Latest>");
        itemTable.getItems().clear();
    }
    /**
     * Called when the new button is pressed. Very dummy action - print something in the command prompt.
     */
    @FXML
    private void lastSearch() {
        if (lastResult == null) {
        }
        else {
            textAreaConsole.clear();
            printConsole(lastOutput);
            setLabelCount(lastResult.size());
            setLabelPrice(countAvgPrice(lastResult));
            setLabelMin(countLowestPrice(lastResult), lastResult);
            setLabelLatest(lastResult);
            refreshTableTab(lastResult);
            lastSearchMenuItem.setDisable(true);
        }
    }

    @FXML
    private void displaySummary(Event event) {
        //System.out.println("Summary tab selected");
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
            lastOutput = currentOutput;
            lastResult = scraperResult;
    		// prevent thrown exception when there's no result
    		if (result != null) {
	    		for (Item item : result) {
	    			output += item.getTitle() + "\t" + item.getPrice() + "\t" + item.getUrl() + "\n";
	    		}
            }
            
            currentOutput = output;

    		textAreaConsole.clear();
    		printConsole(output); 
    		scraperResult = result;
            refreshSummaryTab();
            refreshTableTab(scraperResult);
            enableRefineSearch();
            lastSearchMenuItem.setDisable(false);
    	});
    	thread.start();
    }

    private void refreshTableTab(List<Item> dummy) {
        List<Item> tempResult = dummy;
    
        tableTitle.setCellValueFactory(new PropertyValueFactory<Item, String>("title"));
        tablePrice.setCellValueFactory(new PropertyValueFactory<Item, Double>("price"));
        tableURL.setCellValueFactory(new PropertyValueFactory<Item, String>("url"));
        tablePostedDate.setCellValueFactory(new PropertyValueFactory<Item, Date>("postedDate"));

        itemTable.setItems(FXCollections.observableList(tempResult));
        itemTable.getSelectionModel().setCellSelectionEnabled(true);
        itemTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 1) {
                    //@SuppressWarnings("rawtypes")
                    if (itemTable.getSelectionModel().isEmpty()) {}
                    else {
                        TablePosition pos = (TablePosition) itemTable.getSelectionModel().getSelectedCells().get(0);
                        int row = pos.getRow();
                        int col = pos.getColumn();
                        //@SuppressWarnings("rawtypes")
                        TableColumn column = pos.getTableColumn();
                        if (col == 2) {
                            String val = column.getCellData(row).toString();
                            try {
                                Desktop.getDesktop().browse(new URI(val));
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } catch (URISyntaxException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
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
        while (resultItr.hasNext()) {
            Item currentItem = resultItr.next();
            String itemTitle = currentItem.getTitle().toLowerCase();
            if (!itemTitle.contains(refineStr)) {
                resultItr.remove();
            }
        }
        textAreaConsole.clear();
        refreshSummaryTab();
        disableRefineSearch();
        printOutputToConsole();
    }

    private void refreshSummaryTab() {
        if (Platform.isFxApplicationThread()) {
            updateSummaryDetails();
        }
        else {
            Platform.runLater(() -> updateSummaryDetails());
        }
    }

    private void updateSummaryDetails() {
        int itemCount = getItemCount(scraperResult);
        double avgPrice = getAvgPrice();
        double lowestPrice = getLowestPrice(scraperResult);
        setLabelCount(itemCount);
        setLabelPrice(avgPrice);
        setLabelMin(lowestPrice, scraperResult);
        setLabelLatest(scraperResult);
    }

    private void enableRefineSearch() {
        refineKeyword.setDisable(false);
        refineButton.setDisable(false);
    }

    private void disableRefineSearch() {
        refineKeyword.setDisable(true);
        refineButton.setDisable(true);
    }

    private void printOutputToConsole() {
        String output = "";
        for (Item item : scraperResult) {
            output += item.getTitle() + "\t" + item.getPrice() + "\t" + item.getUrl() + "\n";
        }
        printConsole(output);
    }

    private int getItemCount(List<Item> scraperResult) {
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

    private double getLowestPrice(List<Item> scraperResult) {
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

    private void setLabelMin(double lowestPrice, List<Item> scraperResult) {
        // if priceAvailable set hyperlink to page (getItemWithLowestPrice)
        // else display "-"
        boolean priceAvailable = (lowestPrice != 0.0);
        if (priceAvailable) {
            labelMin.setText(String.format("%.2f", lowestPrice));
            showLowestPricedItemInBrowser(scraperResult);
        }
        else
            labelMin.setText("-");
    }

    private void showLowestPricedItemInBrowser(List<Item> scraperResult) {
        labelMin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI(getLowestPriceURI(scraperResult)));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private String getLowestPriceURI(List<Item> scraperResult) {
        /*
         * Returns the first item with the lowest price
         * if there are two items with the same lowest price,
         * function returns the first one.
         */
        double lowestPrice = getLowestPrice(scraperResult);
        String url = "";
        for (Item item : scraperResult) {
            if (item.getPrice() == lowestPrice) {
                url = item.getUrl();
                break;
            }
        }
        return url;
    }

    private void setLabelLatest(List<Item> scraperResult) {
        boolean itemsFound = (getItemCount(scraperResult) != 0);
        if (itemsFound) {
            showLatestPostInBrowser(scraperResult);
        }
        else
            labelLatest.setText("-");
    }

    private void showLatestPostInBrowser(List<Item> scraperResult) {
        labelLatest.setText("See latest post");
        labelLatest.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI(getLatestPostURI(scraperResult)));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private String getLatestPostURI(List<Item> scraperResult) {
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

