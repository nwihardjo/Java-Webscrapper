# COMP3111: Software Engineering Project - Webscrapper

## Group Name: #35-SHIBE

| Member                                                   | Task    | Task    |
|----------------------------------------------------------|---------|---------|
| [albertparedandan](https://github.com/albertparedandan/) | Basic 4 | Basic 6 |
| [hanifdean](https://github.com/hanifdean/)               | Basic 1 | Basic 5 |
| [nwihardjo](https://github.com/nwihardjo/)               | Basic 2 | Basic 3 |

## Assumptions

1. Use amazon.com as addition reselling portal
2. Price shown is in USD. Price as 0 will be used if no information regarding the price is available
3. Posted date timezone is HKT (Hong Kong Time) (TODO: check what shown in the posted date for null)
4. Pagination of *amazon* portal is not handled
5. Keyword which return whole new sub-section on *amazon*, i.e. book, is not handled since it is not specific enough which does not return solely list of available items in the portal. The result will return nothing in this case
6. Item listed without any title / name will not be scraped as it is not a valid item
7. Main price of *amazon* item is used, not the 'more buying options' or 'offer price' (usually cheaper price of same item listed in the portal from different seller). Average of the main price is used when the main price is a range between two prices (usually due to different sizes, colours, etc). Cheapest 'more buying options' or 'offer' price is used when no information available on the main price, as a rough estimate on the price of the item
8. Posted date from *amazon* portal is scraped from the date of which the item is posted for the first time
9. Service listing on *amazon* portal (not an item) is handled as well
10. If there are results found but prices are all 0, average selling price and lowest selling price will be displayed as 0.0 as opposed to "-". "-" will only be displayed if there are no results found


---

## TL;DR
WebScraper to scrape both amazon and newyork craigslist website based on the keyword specified. Utilised multi-threading to 
support concurrency on craiglist pagination and amazon items' posted date retrieval which significantly improve the performance.

---

## Dependencies

1. Java 8 JDK with Gradle
1. JavaFX for GUI framework
1. JUnit 4.12 for testing suite
1. Jacoco for test coverage measurement

---

## Running the programme

We configure the project with Gradle. Gradle can be considered as Makefile like tools that streamline the compilation for you.

### Compile with Windows Command Prompt 

- Goto your project root folder
- Type `gradlew run`. This will build and run the project. 

If you want to just rerun the project without rebuilding it, 
- Go to the project root folder `build\jar\` 
- Double click jar file (e.g. `webscraper-0.1.0.jar`) yes, you need a GUI screen to run it. 

### Compile with Mac/Linux terminal 

- Goto your project root folder
- `./gradlew build`. This will build the project.
- `./gradlew run` which going to run the application 

If you want to just rerun the project without rebuilding it, 
- Go to the folder `build/jar/` 
- Double click jar file (e.g. `webscraper-0.1.0.jar`) or simply `./gradlew run`

#### Unit test and jacoco coverage report

- Go to the project root directory
- `./gradlew test jacocoTestReport` to generate the test report anc coverage. It will run all unit tests and generate the coverage report 

- Jacoco coverage report can be accessed from `./build/jacocoHTML/index.html` 
- Unit tests report is on `./build/reports/tests/test/index.html`

Some of the unit tests use cached pages from both portals. Testing utilises Reflection method to unit test private functions (not a good practise i know).

#### Documentation / javadoc

- In project root directory, `./gradlew javadoc` to generate javadoc
- Documentation is available at `./build/docs/javadoc/index.html`.
