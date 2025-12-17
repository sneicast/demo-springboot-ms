package dev.scastillo.demo.service;

import growthbook.sdk.java.GrowthBook;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {
    private final GrowthBook growthBook;
    private final HttpClient httpClient;


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

//        var value = growthbook.getFeatureValue(
//                "greeting",
//                "fallback"
//        );
//        System.out.println(value);
    }
}
