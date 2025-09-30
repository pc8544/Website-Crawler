import threading
import time
import requests
import json
from typing import Optional, Union, Dict

"""
Author: Pramod Choudhary (websitecrawler.org)
Version: 1.2
Date: July 21, 2025
"""

class WebsiteCrawlerClient:

    BASE_URL = "https://www.websitecrawler.org/api"

    def __init__(self, config):
        self.endpoints = {
            "authenticate": "/crawl/authenticate",
            "waitTime":     "/crawl/waitTime",
            "start":        "/crawl/start",
            "currentURL":   "/crawl/currentURL",
            "cwdata":       "/crawl/cwdata",
            "clear":        "/crawl/clear",
        }
        self.session      = requests.Session()
        self.api_key      = config.get_api_key()
        self.token        = None
        self.url_to_crawl = None
        self.limit        = None
        self.wait_time    = 0

        self.task_started = False
        self.crawl_status = None
        self.current_url  = None
        self.cw_data      = None
        self._stop_auth   = threading.Event()
        self._stop_wait   = threading.Event()
        self._stop_main   = threading.Event()

    def get_task_status(self) -> bool:
        return self.task_started

    def get_current_url(self) -> Optional[str]:
        return self.current_url

    def get_crawl_status(self) -> Optional[str]:
        return self.crawl_status

    def get_crawl_data(self) -> Optional[str]:
        return self.cw_data

    def _get_response_from_api(self, endpoint: str) -> Optional[str]:
        url = self.BASE_URL + endpoint
        headers = {"Accept": "application/json"}
        payload = {}  # type: Dict[str, Union[int, str]]

        if endpoint.endswith("authenticate"):
            payload["apiKey"] = self.api_key
            headers["Content-Type"] = "application/json"
        else:
            payload["url"] = self.url_to_crawl
            if endpoint.endswith("start"):
                payload["limit"] = self.limit
            if self.token:
                headers["Authorization"] = f"Bearer {self.token}"

        try:
            resp = self.session.post(url, headers=headers, json=payload, timeout=10)
            resp.raise_for_status()
            return resp.text
        except requests.RequestException as e:
            print(f"Request to {url} failed:", e)
            return None

    def submit_url_to_website_crawler(self, url: str, limit: int):
        self.url_to_crawl = url
        self.limit        = limit
        self.task_started = True

        self._stop_auth.clear()
        self._stop_wait.clear()
        self._stop_main.clear()

        threading.Thread(target=self._auth_loop, daemon=True).start()

    def _auth_loop(self):
        while not self._stop_auth.is_set():
            body = self._get_response_from_api(self.endpoints["authenticate"])
            if body:
                data = json.loads(body)
                token = data.get("token")
                if token:
                    self.token = token
                    self._stop_auth.set()
                    threading.Thread(target=self._wait_loop, daemon=True).start()
                    return
            time.sleep(2)

    def _wait_loop(self):
        while not self._stop_wait.is_set():
            body = self._get_response_from_api(self.endpoints["waitTime"])
            if body:
                data = json.loads(body)
                rwt = data.get("waitTime", 0)
                wt = int(rwt)
                if wt > 0:
                    self.wait_time = wt
                    self._stop_wait.set()
                    threading.Thread(target=self._main_loop, daemon=True).start()
                    return
            time.sleep(2)

    def _main_loop(self):
        while not self._stop_main.is_set():
            try:
                start_body  = self._get_response_from_api(self.endpoints["start"])
                status_body = self._get_response_from_api(self.endpoints["currentURL"])

                if start_body:
                    data = json.loads(start_body)
                    self.crawl_status = data.get("status")

                if self.crawl_status == "Crawling" and status_body:
                    data = json.loads(status_body)
                    self.current_url = data.get("currentURL")

                if self.crawl_status == "Completed!":
                    cw_body = self._get_response_from_api(self.endpoints["cwdata"])
                    if cw_body:
                        self.cw_data = cw_body
                    self.task_started = False
                    self._stop_main.set()
                    return

            except Exception as exc:
                print("Error in main loop:", exc)
                self.task_started = False
                self._stop_main.set()
                return

            time.sleep(self.wait_time)

    def clear_job(self):
        self._get_response_from_api(self.endpoints["clear"])
        self.task_started = False
        self.current_url  = None
        self.crawl_status = None
        self.cw_data      = None
        self.wait_time    = 0
        self.token        = None
        self._stop_auth.set()
        self._stop_wait.set()
        self._stop_main.set()
