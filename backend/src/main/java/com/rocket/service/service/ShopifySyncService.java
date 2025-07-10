package com.rocket.service.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.rocket.service.entity.VendorDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ShopifySyncService {

    private static final Logger log = LoggerFactory.getLogger(ShopifySyncService.class);

    private final RestTemplate restTemplate;

    @Autowired
    public ShopifySyncService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // Constructor for tests
    public ShopifySyncService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String baseUrl(VendorDto vendor) {
        String url = vendor.getShopifyStoreUrl();
        if (url == null) {
            return null;
        }
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String version = vendor.getShopifyApiVersion() != null ? vendor.getShopifyApiVersion() : "2024-04";
        return url + "/admin/api/" + version;
    }

    private HttpHeaders defaultHeaders(VendorDto vendor) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Shopify-Access-Token", vendor.getShopifyAccessToken());
        return headers;
    }

    public void createFulfillment(VendorDto vendor, String orderId) {
        String url = baseUrl(vendor) + "/orders/" + orderId + "/fulfillments.json";
        Map<String, Object> body = new HashMap<>();
        body.put("fulfillment", Collections.emptyMap());
        log.debug("Calling Shopify create fulfillment: {}", url);
        restTemplate.postForEntity(url, new HttpEntity<>(body, defaultHeaders(vendor)), String.class);
    }

    public void addTracking(VendorDto vendor, String orderId, String fulfillmentId, String trackingNumber, String company) {
        String url = baseUrl(vendor) + "/orders/" + orderId + "/fulfillments/" + fulfillmentId + "/update_tracking.json";
        Map<String, Object> tracking = new HashMap<>();
        tracking.put("tracking_number", trackingNumber);
        tracking.put("tracking_company", company);
        Map<String, Object> body = new HashMap<>();
        body.put("fulfillment", tracking);
        log.debug("Calling Shopify add tracking: {}", url);
        restTemplate.postForEntity(url, new HttpEntity<>(body, defaultHeaders(vendor)), String.class);
    }

    public void postFulfillmentEvent(VendorDto vendor, String orderId, String fulfillmentId, String status) {
        String url = baseUrl(vendor) + "/orders/" + orderId + "/fulfillments/" + fulfillmentId + "/events.json";
        Map<String, Object> event = new HashMap<>();
        event.put("status", status);
        Map<String, Object> body = new HashMap<>();
        body.put("event", event);
        log.debug("Calling Shopify fulfillment event: {}", url);
        restTemplate.postForEntity(url, new HttpEntity<>(body, defaultHeaders(vendor)), String.class);
    }
}
