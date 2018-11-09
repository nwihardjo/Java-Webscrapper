/**
 * 
 */
package comp3111.webscraper;


import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Hyperlink;
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
    		printConsole(output); 
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
}

