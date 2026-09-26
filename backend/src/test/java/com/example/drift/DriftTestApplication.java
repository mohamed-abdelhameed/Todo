package com.example.drift;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EntityScan("com.example.drift")
public class DriftTestApplication {
}
