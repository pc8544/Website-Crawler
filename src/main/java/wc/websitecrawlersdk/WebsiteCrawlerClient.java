package wc.websitecrawlersdk;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Pramod Choudhary (https://www.websitecrawler.org)
 */
public class WebsiteCrawlerClient {

    private final String basePath = "https://www.websitecrawler.org/api";
    private final String crawlEP = "/crawl/start?";
    private final String currentURLEP = "/crawl/currentURL?";
    private final String crawlDataEP = "/crawl/cwdata?";
    private final String crawlJobClearEP = "/crawl/clear?";
    private ScheduledExecutorService sec;

    private final WebsiteCrawlerConfig cf;
    ScheduledFuture<?>[] ftr = null;

    private String currentURL;
    private String crawlStatus;
    private String cwData;
    boolean taskStarted;
    private final OkHttpClient client;

    public WebsiteCrawlerClient(WebsiteCrawlerConfig cfg) {
        this.cf = cfg;
        this.client = new OkHttpClient();
    }

    public boolean getTaskStatus() {
        return this.taskStarted;
    }

    private String createCrawlRequqest(String url, int limit) {
        String urlParam = "url=" + url;
        String limitParam = "&limit=" + String.valueOf(limit);
        String apiKey = "&key=" + cf.getApiKey();
        String crawlRequest = basePath + crawlEP + urlParam + limitParam + apiKey;
        return crawlRequest;

    }

    private String createcurrentURLRequqest(String url) {
        String urlParam = "url=" + url;
        String apiKey = "&key=" + cf.getApiKey();
        String crawlRequest = basePath + currentURLEP + urlParam + apiKey;
        return crawlRequest;

    }

    public String getCurrentURL() {
        return this.currentURL;
    }

    public String getcwData() {
        return this.cwData;
    }

    public String getCrawlStatus() {
        return this.crawlStatus;
    }

    private String createcwDataRequqest(String url) {
        String urlParam = "url=" + url;
        String apiKey = "&key=" + cf.getApiKey();
        String crawlRequest = basePath + crawlDataEP + urlParam + apiKey;
        return crawlRequest;

    }

    private String crawlJobClearEP(String url) {
        String urlParam = "url?=" + url;
        String apiKey = "&key=" + cf.getApiKey();
        String crawlRequest = basePath + this.crawlJobClearEP + urlParam + apiKey;
        return crawlRequest;

    }

    private String getResponseFromAPI(String API_URL) {
        String rs = null;
        try {
            //System.out.println("API_URL::" + API_URL);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response != null) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        rs = body.string();
                    }
                }
            }
        } catch (IOException x) {
        }
        return rs;
    }

    public void submitUrlToWebsiteCrawler(String url, int limit) {
        this.ftr = new ScheduledFuture[1];
        sec = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            try {
                this.taskStarted = true;

                String data;
                String cuStr = this.createcurrentURLRequqest(url);
                String cwStr = this.createcwDataRequqest(url);
                String urlStr = this.createCrawlRequqest(url, limit);
                String responseFromApi = getResponseFromAPI(urlStr);
                String crrResponse = this.getResponseFromAPI(cuStr);
                String status = null, urlStatus = null;

                if (responseFromApi != null) {
                    JSONObject jobj = null;
                    jobj = new JSONObject(responseFromApi);
                    status = jobj.getString("status");
                    this.crawlStatus = status;
                }

                if (crawlStatus != null && crawlStatus.equals("Crawling")) {
                    if (crrResponse != null) {
                        JSONObject jobj = new JSONObject(crrResponse);
                        urlStatus = jobj.getString("currentURL");
                        this.currentURL = urlStatus;
                    }
                }

                if (crawlStatus != null && crawlStatus.equals("Completed!")) {
                    System.out.println("Cancelling the task");
                    data = this.getResponseFromAPI(cwStr);
                    this.cwData = data;
                   // System.out.println("DASKJSADJKSAJD::" + cwData);
                    ftr[0].cancel(false);

                }

            } catch (JSONException x) {
                ftr[0].cancel(false);

            }
        };
        ftr[0] = sec.scheduleAtFixedRate(task, 0, 15, TimeUnit.SECONDS);
    }
}
