package dev.scastillo.demo.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import growthbook.sdk.java.GrowthBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrowthBookClient {
    private final GrowthBook growthBook;
    private final HttpClient httpClient;

    @Value("${growthbook.api-host}")
    private String apiHost;

    @Value("${growthbook.client-key}")
    private String clientKey;

    public String getFeatureValueRaw(String featureKey) {
        try {

            var result = growthBook.evalFeature(featureKey, Object.class);
            Object value = result.getValue();
            if (value == null) {
                return null;
            }
            return value.toString();
        } catch (Exception e) {

            throw new InternalException("Error getting raw feature value: " + featureKey, e);
        }
    }

    public void refreshFeatures() {
        try {
            log.info("Refreshing GrowthBook features");
            String featuresJson = fetchFeatures();
            growthBook.setFeatures(featuresJson);
            log.info("Successfully refreshed GrowthBook features");
        } catch (Exception e) {
            log.error("Error refreshing features: {}", e.getMessage());
            throw new InternalException("Error refreshing features", e);
        }
    }

    private String fetchFeatures() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(getFeaturesEndpoint()))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractFeatures(response.body());
            }
            throw new InternalException("Failed to fetch features. Status: " + response.statusCode());
//        } catch (FeatureFlagException e) {
//            throw e;
        } catch (Exception e) {
            throw new InternalException("Error fetching features", e);
        }
    }

    private String extractFeatures(String apiResponse) {
        try {
            JsonObject responseObj = JsonParser
                    .parseString(apiResponse)
                    .getAsJsonObject();
            if (responseObj.has("features")) {
                return responseObj.get("features").toString();
            }
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
