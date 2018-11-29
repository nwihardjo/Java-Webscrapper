package comp3111.webscraper;

import org.junit.*;
import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import sun.reflect.annotation.ExceptionProxy;


public class ControllerTest {

    private static Controller testingController;

    @BeforeClass
    public static void init() throws Exception {
        testingController = new Controller();
    }

    @Test
    public void testGetAvgPriceIfEmpty() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        assertEquals(testingController.getAvgPrice(tempList), 0.0, 0);
    }

    @Test
    public void testGetAvgPriceIfValid() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(1370.5);
        item3.setPrice(2003.2);
        item4.setPrice(3000.1);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertEquals(testingController.getAvgPrice(tempList), 2124.6, 0);
    }

    @Test
    public void testCountAvgPriceIfValid() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(1370.5);
        item3.setPrice(2003.2);
        item4.setPrice(3000.1);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertEquals(testingController.countAvgPrice(tempList), 2124.6, 0);
    }

    @Test
    public void testCountAvgPriceIfInvalid() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(0.0);
        item3.setPrice(0.0);
        item4.setPrice(0.0);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertEquals(testingController.countAvgPrice(tempList), 0, 0);
    }

    @Test
    public void testGetLowestPriceIfValid() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(1370.5);
        item3.setPrice(2003.2);
        item4.setPrice(3000.1);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertEquals(testingController.getLowestPrice(tempList), 1370.5, 0);
    }

    @Test
    public void testGetLowestPriceIfEmpty() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        assertEquals(testingController.getLowestPrice(tempList),0.0, 0 );
    }

    @Test
    public void testGetLowestPriceIfZeros() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(0.0);
        item3.setPrice(0.0);
        item4.setPrice(0.0);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertEquals(testingController.getLowestPrice(tempList), 0, 0);
    }

    @Test
    public void testGetLowestPriceURIValid() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        item1.setUrl("https://www.amazon.com/Morange-Replacement-Magsafe2-Adapter-17-inch/dp/B00W3BY0JG/ref=sr_1_11_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-11-spons&keywords=macbook+pro+2015+15&psc=1");
        item2.setUrl("https://www.amazon.com/Aluminum-Adapter-MicroSD-MacBook-Chromebook/dp/B07G7YN4RW/ref=sr_1_21_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-21-spons&keywords=macbook+pro+2015+15&psc=1");
        item3.setUrl("https://www.amazon.com/Charger-Compatible-MacBook-Replacement-Notebook/dp/B07DN4D7NL/ref=sr_1_1_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-1-spons&keywords=macbook+pro+2015+15&psc=1");
        item1.setPrice(17.9);
        item2.setPrice(18.88);
        item3.setPrice(21.88);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        assertEquals(testingController.getLowestPriceURI(tempList), "https://www.amazon.com/Morange-Replacement-Magsafe2-Adapter-17-inch/dp/B00W3BY0JG/ref=sr_1_11_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-11-spons&keywords=macbook+pro+2015+15&psc=1");

    }

    @Test
    public void testGetLowestPriceURIOtherCase() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        item3.setUrl("https://www.amazon.com/Morange-Replacement-Magsafe2-Adapter-17-inch/dp/B00W3BY0JG/ref=sr_1_11_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-11-spons&keywords=macbook+pro+2015+15&psc=1");
        item2.setUrl("https://www.amazon.com/Aluminum-Adapter-MicroSD-MacBook-Chromebook/dp/B07G7YN4RW/ref=sr_1_21_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-21-spons&keywords=macbook+pro+2015+15&psc=1");
        item1.setUrl("https://www.amazon.com/Charger-Compatible-MacBook-Replacement-Notebook/dp/B07DN4D7NL/ref=sr_1_1_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-1-spons&keywords=macbook+pro+2015+15&psc=1");
        item3.setPrice(17.9);
        item2.setPrice(18.88);
        item1.setPrice(21.88);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        assertEquals(testingController.getLowestPriceURI(tempList), "https://www.amazon.com/Morange-Replacement-Magsafe2-Adapter-17-inch/dp/B00W3BY0JG/ref=sr_1_11_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-11-spons&keywords=macbook+pro+2015+15&psc=1");

    }

    @Test
    public void testGetLowestPriceURIInvalid() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        assertEquals(testingController.getLowestPriceURI(tempList), "");
    }

    @Test
    public void testPriceAvailable() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(1370.5);
        item3.setPrice(2003.2);
        item4.setPrice(3000.1);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertTrue(testingController.priceAvailable(tempList));
    }

    @Test
    public void testPriceNotAvailable() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(0.0);
        item3.setPrice(0.0);
        item4.setPrice(0.0);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertFalse(testingController.priceAvailable(tempList));
    }

    @Test
    public void testPriceAvailableButZeros() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(1370.5);
        item3.setPrice(2003.2);
        item4.setPrice(3000.1);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertTrue(testingController.priceAvailableButZeros(tempList));
    }

    @Test
    public void testPriceAvailableButZerosAndEmpty() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        assertFalse(testingController.priceAvailableButZeros(tempList));
    }

    @Test
    public void testPriceExistButZeros() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        Item item4 = new Item();
        item1.setPrice(0.0);
        item2.setPrice(0.0);
        item3.setPrice(0.0);
        item4.setPrice(0.0);
        tempList.add(item1);
        tempList.add(item2);
        tempList.add(item3);
        tempList.add(item4);
        assertFalse(testingController.priceAvailableButZeros(tempList));
    }

    @Test
    public void testItemNotFound() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        assertFalse(testingController.itemsFound(tempList));
    }

    @Test
    public void testItemFound() throws Exception {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        tempList.add(item1);
        assertTrue(testingController.itemsFound(tempList));
    }

    @Test
    public void testProducingConsoleInvalidOutput() {
        ArrayList<Item> tempList = new ArrayList<Item>();
        assertEquals(testingController.produceConsoleOutput(tempList), "");
    }

    @Test
    public void testProducingConsoleOutput() {
        ArrayList<Item> tempList = new ArrayList<Item>();
        Item item1 = new Item();
        item1.setTitle("MBP");
        item1.setPrice(17.9);
        item1.setUrl("https://www.amazon.com/Morange-Replacement-Magsafe2-Adapter-17-inch/dp/B00W3BY0JG/ref=sr_1_11_sspa/140-7515294-7418346?ie=UTF8&qid=1543414385&sr=8-11-spons&keywords=macbook+pro+2015+15&psc=1");
        tempList.add(item1);
        String actual = item1.getTitle() + "\t" + item1.getPrice() + "\t" + item1.getUrl() + "\n";
        assertEquals(testingController.produceConsoleOutput(tempList), actual);
    }


}

