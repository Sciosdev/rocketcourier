package com.rocket.service.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.VendorDto;
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

    public String createFulfillmentWithTracking(VendorDto vendor, RegistryDto registryDto, String siteUrl) {
        if (vendor == null || registryDto == null || registryDto.getOrder() == null || siteUrl == null) {
            log.error("Vendor, RegistryDto, OrderDto o siteUrl es nulo. No se puede crear fulfillment.");
            return null;
        }

        String shopifyOrderId = registryDto.getOrder().getId(); // Este es el ID de la orden en Shopify
        String orderKey = registryDto.getOrder().getOrderKey() != null ? registryDto.getOrder().getOrderKey().toString() : null;
        String fulfillmentOrderId = registryDto.getOrder().getFulfillmentOrderId();
        String lineItemId = registryDto.getOrder().getFulfillmentLineItemId();
        Integer quantity = registryDto.getOrder().getFulfillmentLineItemQty();

        if (shopifyOrderId == null || orderKey == null || fulfillmentOrderId == null || lineItemId == null || quantity == null) {
            log.error("Datos incompletos para crear fulfillment para orderKey {}: shopifyOrderId={}, fulfillmentOrderId={}, lineItemId={}, quantity={}",
                    orderKey, shopifyOrderId, fulfillmentOrderId, lineItemId, quantity);
            return null;
        }

        String url = baseUrl(vendor) + "/fulfillments.json";
        log.info("Creando fulfillment con tracking para Shopify Order ID: {}, URL: {}", shopifyOrderId, url);

        // Construir el cuerpo de la solicitud
        JsonObject fulfillmentPayload = new JsonObject();
        JsonObject fulfillmentDetails = new JsonObject();

        // Tracking Info
        JsonObject trackingInfo = new JsonObject();
        trackingInfo.addProperty("number", orderKey); // Usamos orderKey como tracking number
        trackingInfo.addProperty("company", "Rocket Courier");
        trackingInfo.addProperty("url", siteUrl + "/intranet/inicio?orderKey=" + orderKey);
        fulfillmentDetails.add("tracking_info", trackingInfo);
        fulfillmentDetails.addProperty("notify_customer", true);

        // Line Items by Fulfillment Order
        JsonArray lineItemsByFulfillmentOrderArray = new JsonArray();
        JsonObject fulfillmentOrderDetails = new JsonObject();
        fulfillmentOrderDetails.addProperty("fulfillment_order_id", Long.parseLong(fulfillmentOrderId)); // Shopify espera un Long

        JsonArray fulfillmentOrderLineItemsArray = new JsonArray();
        JsonObject lineItemDetails = new JsonObject();
        lineItemDetails.addProperty("id", Long.parseLong(lineItemId)); // Shopify espera un Long
        lineItemDetails.addProperty("quantity", quantity);
        fulfillmentOrderLineItemsArray.add(lineItemDetails);

        fulfillmentOrderDetails.add("fulfillment_order_line_items", fulfillmentOrderLineItemsArray);
        lineItemsByFulfillmentOrderArray.add(fulfillmentOrderDetails);
        fulfillmentDetails.add("line_items_by_fulfillment_order", lineItemsByFulfillmentOrderArray);

        fulfillmentPayload.add("fulfillment", fulfillmentDetails);

        HttpHeaders headers = defaultHeaders(vendor);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(fulfillmentPayload.toString(), headers);

        try {
            log.debug("Payload para crear fulfillment: {}", fulfillmentPayload.toString());
            String response = restTemplate.postForObject(url, entity, String.class);
            log.info("Respuesta de Shopify al crear fulfillment: {}", response);

            if (response != null) {
                JsonElement jsonElement = JsonParser.parseString(response);
                if (jsonElement.isJsonObject()) {
                    JsonObject responseObject = jsonElement.getAsJsonObject();
                    if (responseObject.has("fulfillment") && responseObject.get("fulfillment").isJsonObject()) {
                        JsonObject fulfillmentObject = responseObject.getAsJsonObject("fulfillment");
                        if (fulfillmentObject.has("id")) {
                            return fulfillmentObject.get("id").getAsString();
                        }
                    }
                }
            }
            log.error("No se pudo extraer el fulfillment ID de la respuesta de Shopify para orderKey {}. Respuesta: {}", orderKey, response);
        } catch (JsonSyntaxException e) {
            log.error("Error al parsear JSON de respuesta de Shopify para orderKey {}: {}", orderKey, e.getMessage());
        } catch (Exception e) {
            log.error("Error al crear fulfillment en Shopify para orderKey {}: {}", orderKey, e.getMessage(), e);
        }
        return null;
    }
}
