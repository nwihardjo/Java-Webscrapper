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
 *
 * Controller class that manage GUI interaction. 
 * Please see document about JavaFX for details.
 * It handles the filling of data obtained from the scraper into GUI components such as console, summary and table tab for this project.
 * @author kevinw
 * @author kevinw
 * @author hanifdean
 * @author nwihardjo
 * @author albertparedandan
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
     * 
     * Default controller which instantiates the Controller and webscraper
     */
    public Controller() {
        scraper = new WebScraper();
    }

    /**
     * 
     * Default initializer initialises the state of the webscraper to its starting state.
     * The 'refine search bar', 'refine' button and 'Last Search' menu option is disabled. 
     */

    @FXML
    private void initialize() {
        refineKeyword.setDisable(true);
        refineButton.setDisable(true);
        lastSearchMenuItem.setDisable(true);
    }

    /**
     * 
     * Method that is called when 'About Your Team' menu option is clicked. 
     * JOptionPane is used to display a new window showing the details about the team for this project.
     *
     */
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

    /**
     * Method that is called when the 'Quit' button is clicked. 
     * It exits the program and closes all connections.
     */
    @FXML
    private void quit() {
        System.exit(0);
    }

    /**
     * Method that is called when the 'Close' button is clicked. 
     * It initialises the webscraper to its initial state where the Console, Summary and Table tab is empty.
     */
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
     * Method that is called when the 'Last Search' button is pressed.
     * It checks if lastResult is empty, then it does nothing.
     * However, when lastResult has a value, it will clear the 'Console' tab, and replace the 'Console', 'Summary' and 'Table' tab with the previous search result data.
     * 
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

    /**
     * Called when the search button is pressed. 
     * Instantiates threads to perform search functionalities and is called when the search button is pressed.
     * This enables the search to run faster given that the the webscraper parallelizes its operations
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
    		//togglePrimarySearch();
            toggleRefineSearch();
            lastSearchMenuItem.setDisable(false);
    	});
    	thread.start();
    }

<<<<<<< Updated upstream
=======
    /**
     * Method called by actionSearch.
     * Clears the output console area.
     * Calls the webscrapers to scrape Amazon and New York Craiglist using the keyword obtained.
     * Assigns the results obtained by the webscraper into lastOutput and lastResult for backup.
     * Assigns the results obtained by the webscraper into scraperResult for further use.
     * Calls the printOutputToConsole method to produce output accordingly to console.
     * Updates the 'Summary' and 'Table' tab by calling the refreshSummaryTab and refreshTableTab methods.
     * Also enables the Refine Search feature by enabling the 'Refine Search' input box and button and the 'Last Search' button.
     * 
     * @see actionSearch
     * lastOutput
     * lastResult
     * scraperResult
     * printOutputToConsole
     * refreshSummaryTab
     * refreshTableTab
     * 
     */    
    void performSearchFunctionalities() {
        textAreaConsole.clear();
        System.out.println("actionSearch: " + textFieldKeyword.getText());
        List<Item> result = scraper.scrape(textFieldKeyword.getText(), this);
        lastOutput = currentOutput;
        lastResult = scraperResult;
        scraperResult = result;
        printOutputToConsole();
        refreshSummaryTab();
        refreshTableTab(scraperResult);
        enableRefineSearch();
        lastSearchMenuItem.setDisable(false);
    }

    /**
     * Method that is called everytime a new search/refine is being made to refresh and fill the Table data correctly.
     * Table data is sorted when the Column tab is clicked.
     * Also enables URL in Table Tab to be clicked and redirects to webpage using device's local browser.
     * This is done by changing TableView mode 'CellSelection'. If the the selected cell is a URL cell, get the value and redirect to browser.
     * 
     * 
     * @param dummy a List of Items returned from the webscraper
     * 
     * @see TableView
     * List
     * Item
     */
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    // enable asynchronous printing on console tab
    public void printConsole (String message) {
    	if (Platform.isFxApplicationThread()) {
    		textAreaConsole.appendText(message);
    	} else {
    		Platform.runLater(() -> textAreaConsole.appendText(message));
    	}
=======
    /**
     * Method that takes a string input and outputs it to the 'Console' tab in the application.
     * 
     * @param message a string message to be shown in the 'Console' tab
     */
    void printConsole (String message) {
        textAreaConsole.appendText(message);
>>>>>>> Stashed changes
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
//        for (Item item : scraperResult) {
//            System.out.println(item.getTitle());
//        }
        textAreaConsole.clear();
        refreshSummaryTab();
        //togglePrimarySearch();
        toggleRefineSearch();
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

