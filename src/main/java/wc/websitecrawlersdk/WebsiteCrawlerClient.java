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
import org.json.JSONObject;

/**
 *
 * @author Pramod Choudhary
 */
public class WebsiteCrawlerClient {

    private final String basePath = "https://www.websitecrawler.org/api";
    private final String crawlEP = "/crawl/start?";
    private final String currentURLEP = "/crawl/currentURL?";
    private final String crawlDataEP = "/crawl/cwdata?";
    private final String crawlJobClearEP = "/crawl/clear?";
    private final String crawlWaitTimeEP = "/crawl/waitTime?";
    private ScheduledExecutorService sec;

    private final WebsiteCrawlerConfig cf;
    private ScheduledFuture<?>[] ftr = null;

    private String currentURL;
    private String crawlStatus;
    private String cwData;
    private boolean taskStarted;
    private final OkHttpClient client;
    private int waitTime;

    public WebsiteCrawlerClient(WebsiteCrawlerConfig cfg) {
        this.cf = cfg;
        this.client = new OkHttpClient();
        this.taskStarted = false;
        this.waitTime = 0;
    }

    public boolean getTaskStatus() {
        return this.taskStarted;
    }

    private String createCrawlWaitTimeRequest() {
        String apiKey = "key=" + cf.getApiKey();
        StringBuilder sbt = new StringBuilder();
        sbt.append(basePath)
                .append(crawlWaitTimeEP)
                .append(apiKey);
        return sbt.toString();

    }

    private String createCrawlRequest(String url, int limit) {
        String urlParam = "url=" + url;
        String limitParam = "&limit=" + String.valueOf(limit);
        String apiKey = "&key=" + cf.getApiKey();
        StringBuilder sbt = new StringBuilder();
        sbt.append(basePath)
                .append(crawlEP)
                .append(urlParam)
                .append(limitParam)
                .append(apiKey);
        return sbt.toString();

    }

    private String createcurrentURLRequest(String url) {
        String urlParam = "url=" + url;
        String apiKey = "&key=" + cf.getApiKey();
        StringBuilder sbt = new StringBuilder();
        sbt.append(basePath)
                .append(currentURLEP)
                .append(urlParam)
                .append(apiKey);
        return sbt.toString();

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

    private String createcwDataRequest(String url) {

        String urlParam = "url=" + url;
        String apiKey = "&key=" + cf.getApiKey();

        StringBuilder sbt = new StringBuilder();
        sbt.append(basePath)
                .append(crawlDataEP)
                .append(urlParam)
                .append(apiKey);
        return sbt.toString();

    }

    private String crawlJobClearEP(String url) {
        String urlParam = "url=" + url;
        String apiKey = "&key=" + cf.getApiKey();

        StringBuilder sbt = new StringBuilder();
        sbt.append(basePath)
                .append(crawlJobClearEP)
                .append(urlParam)
                .append(apiKey);
        return sbt.toString();

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

        this.ftr = new ScheduledFuture[2];

        sec = Executors.newScheduledThreadPool(2);
        Runnable task0 = () -> {
            this.taskStarted = true;
            String wtStr = this.createCrawlWaitTimeRequest();
            String responseFromApi = getResponseFromAPI(wtStr);
           // System.out.println(responseFromApi + "<<waitTime");
            if (responseFromApi != null) {
                JSONObject jobj = new JSONObject(responseFromApi);
                if (jobj.has("waitTime")) {
                    int waitingtime = jobj.getInt("waitTime");
                    if (waitingtime > 0) {
                       // System.out.println("Received wait time.. cancelling the task");
                        this.waitTime = waitingtime;
                        // System.out.println("DASKJSADJKSAJD::" + cwData);
                        ftr[0].cancel(false);
                        ftr[1] = sec.scheduleAtFixedRate(mainTask(url, limit), 0, 8, TimeUnit.SECONDS);

                    }
                }
            }

        };

        ftr[0] = sec.scheduleAtFixedRate(task0, 0, 8, TimeUnit.SECONDS);
    }

    private Runnable mainTask(String url, int limit) {
        Runnable task = () -> {
            try {
               // System.out.println("Main task has begun executing");
                String data;
                String cuStr = this.createcurrentURLRequest(url);
                String cwStr = this.createcwDataRequest(url);
                String urlStr = this.createCrawlRequest(url, limit);
                String responseFromApi = getResponseFromAPI(urlStr);
                String crrResponse = this.getResponseFromAPI(cuStr);
                String status = null, urlStatus = null;

                if (responseFromApi != null) {
                    JSONObject jobj = null;
                    jobj = new JSONObject(responseFromApi);
                    if (jobj.has("status")) {
                        status = jobj.getString("status");
                        this.crawlStatus = status;
                    }
                }

                if (crawlStatus != null && crawlStatus.equals("Crawling")) {
                    if (crrResponse != null) {
                        JSONObject jobj = new JSONObject(crrResponse);
                        if (jobj.has("currentURL")) {
                            urlStatus = jobj.getString("currentURL");
                            this.currentURL = urlStatus;
                        }
                    }
                }

                if (crawlStatus != null && crawlStatus.equals("Completed!")) {
                    System.out.println("Cancelling the task");
                    data = this.getResponseFromAPI(cwStr);
                    this.cwData = data;
                    // System.out.println("DASKJSADJKSAJD::" + cwData);
                    ftr[1].cancel(false);

                }
            } catch (Exception x) {
                if (ftr != null) {
                    ftr[1].cancel(false);
                }
                this.taskStarted = false;
            }

        };
        return task;
    }

}
