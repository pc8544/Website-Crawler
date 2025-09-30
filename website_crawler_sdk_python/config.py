"""
Author: Pramod Choudhary (Websitecrawler.org)
Version: 1.0
Date: July 9, 2025
"""
class WebsiteCrawlerConfig:
    def __init__(self, api_key):
        self.api_key = api_key

    def get_api_key(self):
        return self.api_key
