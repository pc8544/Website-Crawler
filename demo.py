import time
from website_crawler_sdk import WebsiteCrawlerConfig, WebsiteCrawlerClient

"""
Author: Pramod Choudhary (websitecrawler.org)
Version: 1.1
Date: July 10, 2025
"""

# Replace with your actual API key, target URL, and limit
YOUR_API_KEY = "YOUR_API_KEY" #Your API key goes here
URL = "YOUR_URL" #Enter a non redirecting URL/domain with https or http
LIMIT = YOUR_LIMIT #Change YOUR_LIMIT 

def main():
    cfg = WebsiteCrawlerConfig(YOUR_API_KEY)
    client = WebsiteCrawlerClient(cfg)

    # Submit URL to WebsiteCrawler.org for crawling
    client.submit_url_to_website_crawler(URL, LIMIT) #Submit the URL and Limit to websitecrawler via API

    while True:
        task_status = client.get_task_status() #Start retrieving data if the task_status is true
        print(f"{task_status} << task status")
        time.sleep(2)  #Wait for 2 seconds

        if not task_status:
           break

        if task_status:
            status = client.get_crawl_status() #get_crawl_status() method gets the crawl status
            currenturl = client.get_current_url() #get_current_url() method gets the current URL
            data = client.get_crawl_data() # get_crawl_data() method gets the structured data once crawling has completed

            if status:
                print(f"Current Status:: {status}")


            if status == "Crawling": #Crawling is one of the status
                print(f"Current URL:: {currenturl}")

            if status == "Completed!":  #Completed! (with exclamation) is one of the status
                print("Task has been completed... closing the loop and gettint the data...")
                if data:
                    print(f"JSON Data:: {data}")
                    time.sleep(20)  # Give extra time for large JSON response
                    break
            
           

    print("Job over")

if __name__ == "__main__":
    main()
