class WebsiteCrawlerConfig {
  constructor(apiKey) {
    this.apiKey = apiKey;
  }

  getApiKey() {
    return this.apiKey;
  }
}

module.exports = WebsiteCrawlerConfig;
