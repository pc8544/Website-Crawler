
package wc.WebsiteCrawlerAPIUsageDemo;

import wc.websitecrawlersdk.WebsiteCrawlerClient;
import wc.websitecrawlersdk.WebsiteCrawlerConfig;

/**
 *
 * @author Pramod
 */
public class WebsiteCrawlerAPIUsageDemo {

    public static void main(String[] args) throws InterruptedException {
        String status;
        String currenturl;
        String data;
        WebsiteCrawlerConfig cfg = new WebsiteCrawlerConfig(YOUR_API_KEY); //replace YOUR_API_KEY with your api key
        WebsiteCrawlerClient client = new WebsiteCrawlerClient(cfg);

        client.submitUrlToWebsiteCrawler(URL, LIMIT); //replace "URL" with the URL you want Websitecrawler.org to crawl and the number of URLs
        boolean taskStatus;
        while (true) {
            taskStatus = client.getTaskStatus(); //getTaskStatus() should be true before you call any methods
            System.out.println(taskStatus + "<<task status");
            Thread.sleep(9000);
            if (taskStatus == true) {
                status = client.getCrawlStatus(); // getCrawlStatus() method returns the live crawling status
                currenturl = client.getCurrentURL(); //getCurrentURL() method returns the URL being processed by WebsiteCrawler.org
                data = client.getcwData(); // getcwData() returns the JSON array of the website data;
                System.out.println("Crawl status::");
                if (status != null) {
                    System.out.println(status);
                }
                if (status != null && status.equals("Crawling")) { // status: Crawling  ----> Crawl job is in progresss
                    System.out.println("Current URL::" + currenturl);
                }
                if (status != null && status.equals("Completed!")) { // status: Completed! ---> Crawl job has completed succesfully 
                    System.out.println("Task has been completed.. closing the while loop");
                    if (data != null) {
                        System.out.println("Json Data::" + data);
                        Thread.sleep(20000); // JSON data might be huge. Thread.sleep makes the program wait until json data is retrieved
                        break; // exits the while(true) loop
                    }
                }

            }
        }
        System.out.println("job over");
    }
}

