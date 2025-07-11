package com.rocket.service.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.rocket.service.entity.VendorDto;
import com.rocket.service.entity.OrderDto;
import com.rocket.service.model.ShopifyFulfillmentData;

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

    public ShopifyFulfillmentData fetchFulfillmentData(VendorDto vendor, String orderId) {
        String url = baseUrl(vendor) + "/orders/" + orderId + "/fulfillment_orders.json";
        log.debug("Fetching Shopify fulfillment data: {}", url);
        String response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(defaultHeaders(vendor)), String.class).getBody();
        if (response == null) {
            return null;
        }
        try {
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("fulfillment_orders");
            if (arr != null && arr.size() > 0) {
                JsonObject fo = arr.get(0).getAsJsonObject();
                String fId = fo.get("id").getAsString();
                JsonArray li = fo.getAsJsonArray("line_items");
                if (li != null && li.size() > 0) {
                    JsonObject item = li.get(0).getAsJsonObject();
                    String liId = item.get("id").getAsString();
                    int qty = item.get("quantity").getAsInt();
                    return new ShopifyFulfillmentData(fId, liId, qty);
                } else {
                    return new ShopifyFulfillmentData(fId, null, null);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Shopify fulfillment data", e);
        }
        return null;
    }

    public String createFulfillmentWithTracking(VendorDto vendor, OrderDto order, String baseSiteUrl) {
        String url = baseUrl(vendor) + "/fulfillments.json";

        Map<String, Object> tracking = new HashMap<>();
        String orderKey = order.getOrderKey() != null ? order.getOrderKey().toHexString() : order.getId();
        tracking.put("number", orderKey);
        tracking.put("company", "Rocket Courier");
        tracking.put("url", baseSiteUrl + "/intranet/inicio?orderKey=" + orderKey);

        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("id", order.getFulfillmentLineItemId());
        lineItem.put("quantity", order.getFulfillmentLineItemQty());

        Map<String, Object> fo = new HashMap<>();
        fo.put("fulfillment_order_id", order.getFulfillmentOrderId());
        fo.put("fulfillment_order_line_items", Collections.singletonList(lineItem));

        Map<String, Object> fulfillment = new HashMap<>();
        fulfillment.put("notify_customer", true);
        fulfillment.put("tracking_info", tracking);
        fulfillment.put("line_items_by_fulfillment_order", Collections.singletonList(fo));

        Map<String, Object> body = new HashMap<>();
        body.put("fulfillment", fulfillment);

        log.debug("Calling Shopify create fulfillment with tracking: {}", url);
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                url, new HttpEntity<>(body, defaultHeaders(vendor)), String.class);

        String fulfillmentId = null;
        String respBody = response.getBody();
        if (respBody != null) {
            try {
                JsonObject obj = JsonParser.parseString(respBody).getAsJsonObject();
                JsonObject fulfillmentResp = obj.getAsJsonObject("fulfillment");
                if (fulfillmentResp != null && fulfillmentResp.has("id")) {
                    fulfillmentId = fulfillmentResp.get("id").getAsString();
                }
            } catch (Exception e) {
                log.error("Error parsing fulfillment creation response", e);
            }
        }

        order.setFulfillmentId(fulfillmentId);
        return fulfillmentId;
    }
}
