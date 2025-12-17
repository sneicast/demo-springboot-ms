package dev.scastillo.demo.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import growthbook.sdk.java.GBContext;
import growthbook.sdk.java.GrowthBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrowthBookConfig {
    private final Gson gson = new Gson();

    @Value("${growthbook.api-host}")
    private String apiHost;

    @Value("${growthbook.client-key}")
    private String clientKey;

    @Value("${growthbook.enabled}")
    private boolean enabled;


    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public GBContext gbContext(String featuresJson) {
        return GBContext.builder()
                .featuresJson(featuresJson)
                .enabled(enabled)
                .build();
    }

    @Bean
    public GrowthBook growthBook(GBContext gbContext) {
        return new GrowthBook(gbContext);
    }




    @Bean
    public String featuresJson(HttpClient httpClient) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(getFeaturesEndpoint()))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Successfully fetched GrowthBook features");
                // Extract only the "features" object from the response
                String featuresOnly = extractFeatures(response.body());
                log.debug("Features JSON: {}", featuresOnly);
                return featuresOnly;
            } else {
                log.warn("Failed to fetch features. Status: {}", response.statusCode());
                return "{}";
            }
        } catch (Exception e) {
            log.error("Error fetching GrowthBook features: {}", e.getMessage());
            return "{}";
        }
    }

    private String extractFeatures(String apiResponse) {
        try {
            JsonObject responseObj = gson.fromJson(apiResponse, JsonObject.class);
            if (responseObj.has("features")) {
                return responseObj.get("features").toString();
            }
            // If no "features" key, assume the response is already the features object
            return apiResponse;
        } catch (Exception e) {
            log.error("Error parsing features response: {}", e.getMessage());
            return "{}";
        }
    }
    private String getFeaturesEndpoint() {
        return apiHost + "/api/features/" + clientKey;
    }
}
