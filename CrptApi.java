import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class CrptApi {
    private final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final RateLimiter rateLimiter;
    private final HttpClient httpClient;

    public CrptApi(double requestsPerSecond) {
        this.rateLimiter = RateLimiter.create(requestsPerSecond);
        this.httpClient = HttpClients.createDefault();
    }

    public void createDocument(String documentJson, String signature) throws IOException {
        rateLimiter.acquire(); // Rate limit before making the request

        try {
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity requestEntity = new StringEntity(documentJson, ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String responseBody = EntityUtils.toString(responseEntity);
                EntityUtils.consume(responseEntity); // Ensure response entity is fully consumed
                System.out.println("Response Body: " + responseBody);
            }

            if (statusCode == 200) {
                System.out.println("Document creation success");
            } else {
                System.err.println("Failed to create document. Error code: " + statusCode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        CrptApi api = new CrptApi(2.0); // Allow 2 requests per second

        String documentJson = "{\"description\": \"Sample document\"}";
        String signature = "xyz123";

        for (int i = 0; i < 5; i++) {
            api.createDocument(documentJson, signature);
        }
    }
}
