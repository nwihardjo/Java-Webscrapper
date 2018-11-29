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
import java.util.ArrayList;
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
    void actionSearch() {
    	Thread thread = new Thread(() ->
    	    performSearchFunctionalities()
    	);
    	thread.start();
    }

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

    /**
     * Method that takes a string input and outputs it to the 'Console' tab in the application.
     * 
     * @param message a string message to be shown in the 'Console' tab
     */
    void printConsole (String message) {
        textAreaConsole.appendText(message);
    }

    /**
     * This function performs all the required tasks for the refine search functionality
     * such as filtering the items, refreshing the summary and table and disabling the function
     * by calling the respective helper functions.
     */
    @FXML
    void refineSearch() {
        System.out.println("refineSearch: " + refineKeyword.getText());
        checkIfItemsContainRefineKeyWord();
        printOutputToConsole();
        refreshSummaryTab();
        refreshTableTab(scraperResult);
        disableRefineSearch();
    }

    /**
     * This function performs the filtering operation in the refine search functionality
     * by iterating through the scraperResult items and removing items from the list if
     * it doesn't contain the refine keyword in the item's title.
     */
    private void checkIfItemsContainRefineKeyWord() {
        String refineStr = refineKeyword.getText().toLowerCase();
        Iterator<Item> resultItr = scraperResult.iterator();
        while (resultItr.hasNext()) {
            Item currentItem = resultItr.next();
            String itemTitle = currentItem.getTitle().toLowerCase();
            if (!itemTitle.contains(refineStr)) {
                resultItr.remove();
            }
        }
    }

    /**
     * This function ensures that updating the summary details is run on an FX application thread
     * or if not, it should be run later.
     */
    private void refreshSummaryTab() {
        if (Platform.isFxApplicationThread()) {
            updateSummaryDetails();
        }
        else {
            Platform.runLater(() -> updateSummaryDetails());
        }
    }

    /**
     * This function updates all the summary tab details (labelCount, labelPrice, labelMin, and
     * labelLatest) by calling the helper functions that performs these particular tasks.
     *
     */
    private void updateSummaryDetails() {
        int itemCount = getItemCount(scraperResult);
        double avgPrice = getAvgPrice(scraperResult);
        double lowestPrice = getLowestPrice(scraperResult);
        setLabelCount(itemCount);
        setLabelPrice(avgPrice);
        setLabelMin(lowestPrice, scraperResult);
        setLabelLatest(scraperResult);
    }

    /**
     * This function enables the refine search functionality in the GUI by enabling
     * the textbox for entering the refine keyword as well as the refine button.
     */

    private void enableRefineSearch() {
        refineKeyword.setDisable(false);
        refineButton.setDisable(false);
    }

    /**
     * This function disables the refine search functionality in the GUI by disabling
     * the textbox for entering the refine keyword as well as the refine button.
     */

    private void disableRefineSearch() {
        refineKeyword.setDisable(true);
        refineButton.setDisable(true);
    }

    /**
     * This function performs the actual printing to the console and sets the current output
     * to be the formatted output.
     */

    private void printOutputToConsole() {
        textAreaConsole.clear();
        String output = produceConsoleOutput(scraperResult);
        printConsole(output);
        currentOutput = output;
    }

    /**
     * This function formats the item's attributes into the correct console output.
     * This function is called in printOutputToConsole.
     *
     * @param scraperResult: the number of items in the list (scraperResult)
     * @return a string corresponding to the formatted console output
     */
    String produceConsoleOutput(List<Item> scraperResult) {
        String output = "";
        for (Item item : scraperResult) {
            output += item.getTitle() + "\t" + item.getPrice() + "\t" + item.getUrl() + "\n";
        }
        return output;
    }

    /**
     * This function retrieves the number of items in scraperResult.
     *
     * @param scraperResult: the number of items in the list (scraperResult)
     * @return an integer corresponding to number of items in scraperResult.
     */
    int getItemCount(List<Item> scraperResult) {
        return scraperResult.size();
    }

    /**
     * This function sets labelCount to the number of items in scraperResult.
     * @param itemCount: the number of items in the list (scraperResult)
     */

    private void setLabelCount(int itemCount) {
        labelCount.setText(Integer.toString(itemCount));
    }

    /**
     * This function retrieves the average price based on whether or not there are items
     * in scraperResult.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     * @return the average price as a double.
     */
    double getAvgPrice(List<Item> scraperResult) {
        boolean itemsFound = (getItemCount(scraperResult) != 0);
        if (itemsFound)
            return countAvgPrice(scraperResult);
        else
            return 0.0;
    }

    /**
     * This function calculates the average price of all items in scraperResult. This function is
     * called in getAvgPrice.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     * @return the average price as a double.
     */

    double countAvgPrice(List<Item> scraperResult) {
        double totalPrice = 0.0;
        int itemCount = 0;
        for (Item item : scraperResult) {
            if (item.getPrice() == 0.0)
                continue;
            totalPrice += item.getPrice();
            itemCount++;
        }
        boolean allItemsAreZero = (itemCount == 0);
        if (allItemsAreZero)
            return 0.0;
        return totalPrice / itemCount;
    }

    /**
     * This function sets labelPrice (denoting average price) from the average price parameter passed
     * to the function. The function sets labelPrice as "-" if price is not available.
     *
     * @param avgPrice: average price of the items in scraperResult.
     */

    private void setLabelPrice(double avgPrice) {
        if (priceAvailable(scraperResult) || priceAvailableButZeros(scraperResult))
            labelPrice.setText(String.format("%.2f", avgPrice));
        else
            labelPrice.setText("-");
    }

    /**
     * A helper function that checks if the average price of the items in scraperResult are not 0.0
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     * @return a boolean corresponding to price availability
     */
    boolean priceAvailable(List<Item> scraperResult) {
        return (getAvgPrice(scraperResult) != 0.0);
    }

    /**
     * A helper function that checks if the prices of the items in scraperResults are valid
     * (meaning its average price is not 0.0) and if scraperResult is empty or not.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     * @return a boolean corresponding to price availability and its average price not being zero.
     */
    boolean priceAvailableButZeros(List<Item> scraperResult) {
        if (scraperResult.size() == 0)
            return false;
        else if (getAvgPrice(scraperResult) == 0.0) {
            return false;
        }
        return true;
    }

    /**
     * This function retrieves the lowest price from a list of items containing the results.
     * The function returns a 0.0 if no items are found in scraperResult.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites
     * @return a double that represents the lowest priced item.
     */

    double getLowestPrice(List<Item> scraperResult) {
        boolean itemsFound = (getItemCount(scraperResult) != 0);
        if (itemsFound)
            return countLowestPrice(scraperResult);
        else
            return 0.0;
    }

    /**
     * This function calculates the item with the lowest price from the list of items in scraperResult.
     * The function is called in getLowestPrice.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     * @return lowestPrice: the price of the item with the lowest price.
     */

    private double countLowestPrice(List<Item> scraperResult) {
        double lowestPrice = 100000000.0;
        for (Item item : scraperResult) {
            if (item.getPrice() == 0.0)
                continue;
            if (item.getPrice() < lowestPrice)
                lowestPrice = item.getPrice();
        }
        boolean itemPricesAreZeros = (lowestPrice == 100000000.0);
        if (itemPricesAreZeros)
            lowestPrice = 0.0;
        return lowestPrice;
    }

    /**
     * This function will set LabelMin (denoting minimum price) to the price of the item
     * with the lowest price if there is an available price to display. Otherwise, it will
     * only place a "-" as text and it will not be clickable.
     *
     * @param lowestPrice: the price of the item with the lowest price
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     */

    private void setLabelMin(double lowestPrice, List<Item> scraperResult) {
        if (priceAvailable(scraperResult) || priceAvailableButZeros(scraperResult)) {
            labelMin.setText(String.format("%.2f", lowestPrice));
            showLowestPricedItemInBrowser(scraperResult);
        }
        else
            labelMin.setText("-");
    }


    /**
     * This function handles the event when labelMin (denoting minimum price) is clicked
     * and redirects the user to the post of the item that contains the minimum price in the browser.
     * The function is called in setLabelMin.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     */

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

    /**
     * This function returns the URI of the item with the lowest price. If there are two items with
     * the same lowest price, the function returns the first one. This function is called in
     * showLowestPricedItemInBrowser.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites
     * @return the URI of the item with the lowest price (as a String)
     */

    String getLowestPriceURI(List<Item> scraperResult) {
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

    /**
     * This function will set the text of labelLatest (denoting latest post) in the GUI depending on
     * whether or not there are items in scraperResult.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     */

    private void setLabelLatest(List<Item> scraperResult) {
        if (itemsFound(scraperResult)) {
            showLatestPostInBrowser(scraperResult);
        }
        else
            labelLatest.setText("-");
    }

    /**
     * A helper function that checks whether or not there are items in the list of results (scraperResult)
     * and returns a boolean result. The function is called in setLabelLatest, getLowestPrice, and getAvgPrice.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites.
     * @return a boolean condition corresponding to whether or not scraperResult is empty or not.
     */

    boolean itemsFound(List<Item> scraperResult) {
        if (getItemCount(scraperResult) == 0)
            return false;
        else
            return true;
    }

    /**
     * This function redirects the user to the page of the searched item that was last posted
     * given the results from the Webscraper. The function is called when labelLatest (denoting
     * latest post in the GUI is clicked. The function is called by setLabelLatest and is only called
     * if there are items that are found in the scraperResult.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites
     */

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

    /**
     * A helper function that returns the latest post's URI given the scraperResult. This function is
     * called in showLatestPostInBrowser.
     *
     * @param scraperResult: the list of items that contains the results of scraping the websites
     * @return latestPostURI: the URI of the item that was posted the latest in either of the websites
     * that were scraped.
     */

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

