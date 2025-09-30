package wc.websitecrawlersdk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

/**
 *
 * @author Pramod Choudhary
 */
public class WebsiteCrawlerClient {

    private final String basePath = "https://www.websitecrawler.org/api";
    private final String crawlEP = "/crawl/start";
    private final String currentURLEP = "/crawl/currentURL";
    private final String crawlDataEP = "/crawl/cwdata";
    private final String crawlJobClearEP = "/crawl/clear";
    private final String crawlWaitTimeEP = "/crawl/waitTime";
    private final String authenticateEP = "/crawl/authenticate";

    private ScheduledExecutorService sec;

    private final WebsiteCrawlerConfig cf;
    private ScheduledFuture<?>[] ftr = null;

    private String currentURL;
    private String crawlStatus;
    private String cwData;
    private boolean taskStarted;
    private final OkHttpClient client;
    private int waitTime;
    private String token;
    private String urlToCrawl;
    private int limit;

    public WebsiteCrawlerClient(WebsiteCrawlerConfig cfg) {
        this.cf = cfg;
        this.client = new OkHttpClient();
        this.taskStarted = false;
        this.waitTime = 0;
    }

    public boolean getTaskStatus() {
        return this.taskStarted;
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

    private String getResponseFromAPI(String API_URL) {
        String rs = null;
        Request request = null;
        String murl = this.basePath + API_URL;
        Map mp = new HashMap<>();

        String bearer = "Bearer " + token;
        if (API_URL.endsWith("authenticate")) {
            mp.put("apiKey", cf.getApiKey());
        } else if (API_URL.endsWith("currentURL")) {
            mp.put("url", urlToCrawl);
        } else if (API_URL.endsWith("waitTime")) {
            mp.put("url", urlToCrawl);
        } else if (API_URL.endsWith("start")) {
            mp.put("url", urlToCrawl);
            mp.put("limit", limit);
        } else if (API_URL.endsWith("clear")) {
            mp.put("url", urlToCrawl);
        } else if (API_URL.endsWith("cwdata")) {
            mp.put("url", urlToCrawl);
        }

        String mptojs = new JSONObject(mp).toString();
        RequestBody body = RequestBody.create(
                mptojs, MediaType.parse("application/json")
        );
        try {
            switch (API_URL) {
                case "/crawl/waitTime":

                    request = new Request.Builder()
                            .url(murl)
                            .addHeader("Authorization", bearer)
                            .addHeader("Accept", "application/json")
                            .post(body)
                            .build();
                    break;
                case "/crawl/start":
                    request = new Request.Builder()
                            .url(murl)
                            .addHeader("Authorization", bearer)
                            .addHeader("Accept", "application/json")
                            .post(body)
                            .build();
                    break;
                case "/crawl/cwdata":
                    System.out.println("Entered cwdata case " + API_URL);
                    System.out.println("murl::" + murl);
                    System.out.println("Bearer:?:" + bearer);
                    request = new Request.Builder()
                            .url(murl)
                            .addHeader("Accept", "application/json")
                            .addHeader("Authorization", bearer)
                            .post(body)
                            .build();
                    break;
                case "/crawl/clear":
                    request = new Request.Builder()
                            .url(murl)
                            .addHeader("Authorization", bearer)
                            .addHeader("Accept", "application/json")
                            .post(body)
                            .build();
                    break;
                case "/crawl/currentURL":
                    request = new Request.Builder()
                            .url(murl)
                            .addHeader("Authorization", bearer)
                            .addHeader("Accept", "application/json")
                            .post(body)
                            .build();
                    break;
                case "/crawl/authenticate":
                    request = new Request.Builder()
                            .url(murl)
                            .addHeader("Content-Type", "application/json")
                            .post(body)
                            .build();
                    break;
            }

            if (request != null) {
                try (Response response = client.newCall(request).execute()) {
                    if (response.body() != null) {
                        rs = response.body().string();
                    }
                }
            }
        } catch (IOException x) {
        }
        return rs;
    }

    public void submitUrlToWebsiteCrawler(String url, int limit) {

        this.urlToCrawl = url;
        this.limit = limit;
        this.ftr = new ScheduledFuture[3];

        sec = Executors.newScheduledThreadPool(3);
        Runnable task0 = () -> {
            String responseFromApi = getResponseFromAPI(this.crawlWaitTimeEP);
            System.out.println(responseFromApi + "<<waitTime");
            if (responseFromApi != null) {
                JSONObject jobj = new JSONObject(responseFromApi);
                if (jobj.has("waitTime")) {
                    int waitingtime = jobj.getInt("waitTime");
                    if (waitingtime > 0) {
                        this.waitTime = waitingtime;
                        ftr[1].cancel(false);
                        System.out.println("Starting the main task..");
                        ftr[2] = sec.scheduleAtFixedRate(mainTask(url, limit), 0, this.waitTime, TimeUnit.SECONDS);

                    }
                }
            }

        };

        Runnable taskm = () -> {
            this.taskStarted = true;
            String responseFromApi = getResponseFromAPI(this.authenticateEP);
            if (responseFromApi != null) {
                JSONObject jobj = new JSONObject(responseFromApi);
                if (jobj.has("token")) {
                    this.token = jobj.getString("token");
                    if (token != null || !token.isBlank()) {
                        ftr[0].cancel(false);
                        ftr[1] = sec.scheduleAtFixedRate(task0, 0, 2, TimeUnit.SECONDS);

                    }
                }
            }

        };

        ftr[0] = sec.scheduleAtFixedRate(taskm, 0, 2, TimeUnit.SECONDS);
    }

    private Runnable mainTask(String url, int limit) {
        Runnable task = () -> {
            try {
                String responseFromApi = getResponseFromAPI(this.crawlEP);
                String crrResponse = this.getResponseFromAPI(this.currentURLEP);
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
                    String data = this.getResponseFromAPI(this.crawlDataEP);
                    if (data != null) {
                        this.cwData = data;
                        ftr[2].cancel(false);
                        this.taskStarted = false;
                    }
                }
            } catch (Exception x) {
                if (ftr != null) {
                    ftr[2].cancel(false);
                }
                this.taskStarted = false;
            }

        };
        return task;
    }

}
