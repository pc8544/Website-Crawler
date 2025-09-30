const axios = require('axios');

class WebsiteCrawlerClient {
  constructor(config) {
    this.basePath = 'https://www.websitecrawler.org/api';
    this.endpoints = {
      crawl: '/crawl/start',
      currentURL: '/crawl/currentURL',
      cwdata: '/crawl/cwdata',
      clear: '/crawl/clear',
      waitTime: '/crawl/waitTime',
      authenticate: '/crawl/authenticate',
    };

    this.config = config;
    this.token = null;
    this.urlToCrawl = null;
    this.limit = 0;
    this.waitTime = 0;
    this.taskStarted = false;
    this.currentURL = null;
    this.crawlStatus = null;
    this.cwData = null;

    this.intervals = {};
  }

  async getResponse(endpoint, payload = {}) {
    const url = this.basePath + endpoint;
    const headers = {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    };

    if (endpoint !== this.endpoints.authenticate) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    try {
      const response = await axios.post(url, payload, { headers });
      return response.data;
    } catch (err) {
      console.error(`Error calling ${endpoint}:`, err.message);
      return null;
    }
  }

  async submitUrlToWebsiteCrawler(url, limit) {
    this.urlToCrawl = url;
    this.limit = limit;
    this.taskStarted = true;

    this.intervals.auth = setInterval(async () => {
      const res = await this.getResponse(this.endpoints.authenticate, {
        apiKey: this.config.getApiKey(),
      });

      if (res?.token) {
        this.token = res.token;
        clearInterval(this.intervals.auth);

        this.intervals.waitTime = setInterval(async () => {
          const res = await this.getResponse(this.endpoints.waitTime, {
            url: this.urlToCrawl,
          });

          if (res?.waitTime > 0) {
            this.waitTime = res.waitTime;
            clearInterval(this.intervals.waitTime);

            this.intervals.main = setInterval(() => {
              this.mainTask();
            }, this.waitTime * 1000);
          }
        }, 2000);
      }
    }, 2000);
  }

  async mainTask() {
    const crawlRes = await this.getResponse(this.endpoints.crawl, {
      url: this.urlToCrawl,
      limit: this.limit,
    });

    if (crawlRes?.status) {
      this.crawlStatus = crawlRes.status;
    }

    if (this.crawlStatus === 'Crawling') {
      const urlRes = await this.getResponse(this.endpoints.currentURL, {
        url: this.urlToCrawl,
      });

      if (urlRes?.currentURL) {
        this.currentURL = urlRes.currentURL;
      }
    }

    if (this.crawlStatus === 'Completed!') {
      const dataRes = await this.getResponse(this.endpoints.cwdata, {
        url: this.urlToCrawl,
      });

      if (dataRes) {
        this.cwData = dataRes;
        clearInterval(this.intervals.main);
        this.taskStarted = false;
      }
    }
  }

  getTaskStatus() {
    return this.taskStarted;
  }

  getCurrentURL() {
    return this.currentURL;
  }

  getCrawlStatus() {
    return this.crawlStatus;
  }

  getCrawlData() {
    return this.cwData;
  }
}

module.exports = WebsiteCrawlerClient;
