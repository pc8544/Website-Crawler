# Website-Crawler

# 🕷️ Website Crawler API

The **Website Crawler API** allows developers to programmatically crawl websites and access structured metadata via two simple endpoints. Whether you're indexing pages, auditing SEO data, or monitoring content, this tool gives you clean JSON responses and real-time crawl updates.

---

## 🔐 Authentication

To use the API, you'll need an **API Key**.

**How to get one:**
1. Visit [websitecrawler.org](https://www.websitecrawler.org)
2. Create an account or log in
3. Go to the **Settings** page to generate your API key

Pass your key as a query parameter (`key`) in all requests.

---

## 🌐 Base URL


---

## 📡 Endpoints

### 1. `GET /crawl/start`

Initiate a new crawl for a given domain.

- **Query Parameters**:
  - `domain` (string, required): Target website (e.g. `example.com`)
  - `limit` (integer, required): Max pages to crawl
  - `key` (string, required): Your API Key

- **Sample Request**:
