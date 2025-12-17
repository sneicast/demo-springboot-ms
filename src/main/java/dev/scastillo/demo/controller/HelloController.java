package dev.scastillo.demo.controller;

import dev.scastillo.demo.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@RequiredArgsConstructor
public class HelloController {
    private final FeatureFlagService featureFlagService;

    @GetMapping
    public String sayHello() {
        return featureFlagService.getFeatureValueRaw("greeting");
    }

}
