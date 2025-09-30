const { WebsiteCrawlerConfig, WebsiteCrawlerClient } = require('website-crawler-sdk');

const config = new WebsiteCrawlerConfig('YOUR_API_KEY');
const client = new WebsiteCrawlerClient(config);

client.submitUrlToWebsiteCrawler('YOUR_URL', 'YOUR_LIMIT');

const intervalId = setInterval(() => {
  const status = client.getCrawlStatus();
  console.log('Status:', status);
  console.log('Current URL:', client.getCurrentURL());

  if (status === 'Completed!') {
    console.log('Crawl Data:', client.getCrawlData());
    console.log('Job completed...');
    clearInterval(intervalId);
  }
}, 3000);
