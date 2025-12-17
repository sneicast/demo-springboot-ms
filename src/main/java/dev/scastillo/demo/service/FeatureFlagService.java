package dev.scastillo.demo.service;

import dev.scastillo.demo.client.GrowthBookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FeatureFlagService {
    private final GrowthBookClient  growthBookClient;

    public String getFeatureValueRaw(String featureKey) {
       return growthBookClient.getFeatureValueRaw(featureKey);
    }
    public void refreshFeatures() {
        growthBookClient.refreshFeatures();
    }


}
