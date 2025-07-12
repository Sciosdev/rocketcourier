package com.rocket.service.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

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

    private String baseUrl(VendorDto vendor, boolean isGraphQL) {
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
        String endpoint = isGraphQL ? "/graphql.json" : "";
        return url + "/admin/api/" + version + endpoint;
    }

    private String baseUrl(VendorDto vendor) {
        return baseUrl(vendor, false);
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

    private ShopifyFulfillmentData fetchFulfillmentDataRest(VendorDto vendor, String orderId) {
        String url = baseUrl(vendor) + "/orders/" + orderId + "/fulfillment_orders.json";
        log.debug("Fetching Shopify fulfillment data via REST: {}", url);
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
            log.error("Error parsing Shopify REST fulfillment data", e);
        }
        return null;
    }

    private ShopifyFulfillmentData fetchFulfillmentDataGraphQL(VendorDto vendor, String shopifyOrderId, OrderDto orderDto) {
        String url = baseUrl(vendor, true);
        String orderGid = "gid://shopify/Order/" + shopifyOrderId;

        String query = "query getFO($orderId: ID!) { " +
                       "order(id: $orderId) { " +
                       "fulfillmentOrders(first: 5) { " +
                       "edges { node { " +
                       "id, " + // GID del FulfillmentOrder
                       "lineItems(first: 10) { " +
                       "edges { node { id, quantity } } " + // id es GID del FulfillmentOrderLineItem
                       "} } } } } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("orderId", orderGid);

        JsonObject payload = new JsonObject();
        payload.addProperty("query", query);
        payload.add("variables", variables);

        log.debug("Fetching Shopify fulfillment data via GraphQL. Payload: {}", payload.toString());

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), defaultHeaders(vendor));

        try {
            String response = restTemplate.postForObject(url, entity, String.class);
            if (response == null) {
                log.error("GraphQL response for fetchFulfillmentData was null for order GID {}", orderGid);
                return null;
            }

            JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
            if (responseJson.has("errors")) {
                log.error("GraphQL errors for fetchFulfillmentData on order GID {}: {}", orderGid, responseJson.get("errors").toString());
                return null;
            }

            JsonObject orderNode = responseJson.getAsJsonObject("data").getAsJsonObject("order");
            JsonArray foEdges = orderNode.getAsJsonObject("fulfillmentOrders").getAsJsonArray("edges");

            if (foEdges.size() > 0) {
                JsonObject foNode = foEdges.get(0).getAsJsonObject().getAsJsonObject("node");
                String fulfillmentOrderGid = foNode.get("id").getAsString();
                orderDto.setShopifyFulfillmentOrderGid(fulfillmentOrderGid); // Guardar GID

                JsonArray liEdges = foNode.getAsJsonObject("lineItems").getAsJsonArray("edges");
                if (liEdges.size() > 0) {
                    JsonObject liNode = liEdges.get(0).getAsJsonObject().getAsJsonObject("node");
                    String lineItemGid = liNode.get("id").getAsString();
                    int quantity = liNode.get("quantity").getAsInt();
                    orderDto.setShopifyLineItemGid(lineItemGid); // Guardar GID

                    // Extraer ID numérico del GID para devolver en ShopifyFulfillmentData por compatibilidad
                    String fulfillmentOrderIdNum = fulfillmentOrderGid.substring(fulfillmentOrderGid.lastIndexOf('/') + 1);
                    String lineItemIdNum = lineItemGid.substring(lineItemGid.lastIndexOf('/') + 1);

                    return new ShopifyFulfillmentData(fulfillmentOrderIdNum, lineItemIdNum, quantity);
                } else {
                     return new ShopifyFulfillmentData(fulfillmentOrderGid.substring(fulfillmentOrderGid.lastIndexOf('/') + 1), null, null);
                }
            }
        } catch (Exception e) {
            log.error("Error during GraphQL fetchFulfillmentData for order GID {}", orderGid, e);
        }
        return null;
    }

    public ShopifyFulfillmentData fetchFulfillmentData(VendorDto vendor, String shopifyOrderId, OrderDto orderDto) {
        if (vendor.isUseGraphQL()) {
            log.info("Dispatching to GraphQL implementation for fetchFulfillmentData.");
            return fetchFulfillmentDataGraphQL(vendor, shopifyOrderId, orderDto);
        } else {
            log.info("Dispatching to REST implementation for fetchFulfillmentData.");
            return fetchFulfillmentDataRest(vendor, shopifyOrderId);
        }
    }

    private String createFulfillmentWithTrackingRest(VendorDto vendor, RegistryDto registryDto, String siteUrl) {
        if (vendor == null || registryDto == null || registryDto.getOrder() == null || siteUrl == null) {
            log.error("Vendor, RegistryDto, OrderDto o siteUrl es nulo. No se puede crear fulfillment con REST.");
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
            log.error("Error al crear fulfillment en Shopify (REST) para orderKey {}: {}", orderKey, e.getMessage(), e);
        }
        return null;
    }

    private String createFulfillmentWithTrackingGraphQL(VendorDto vendor, RegistryDto registryDto, String siteUrl) {
        OrderDto orderDto = registryDto.getOrder();
        if (orderDto == null) return null;

        String fulfillmentOrderGid = orderDto.getShopifyFulfillmentOrderGid();
        String lineItemGid = orderDto.getShopifyLineItemGid();
        Integer quantity = orderDto.getFulfillmentLineItemQty();
        String orderKey = orderDto.getOrderKey() != null ? orderDto.getOrderKey().toString() : null;

        if (fulfillmentOrderGid == null || lineItemGid == null || quantity == null || orderKey == null) {
            log.error("Datos GID incompletos para crear fulfillment con GraphQL para orderKey {}", orderKey);
            return null;
        }

        String url = baseUrl(vendor, true);
        String mutation = "mutation createFulfillment($in: FulfillmentV2Input!) { " +
                          "fulfillmentCreateV2(fulfillment: $in) { " +
                          "fulfillment { id, status, trackingInfo { number, company, url } } " +
                          "userErrors { field, message } } }";

        // Construir el payload
        JsonObject trackingInfo = new JsonObject();
        trackingInfo.addProperty("number", orderKey);
        trackingInfo.addProperty("company", "Rocket Courier");
        trackingInfo.addProperty("url", siteUrl + "/intranet/inicio?orderKey=" + orderKey);

        JsonObject lineItem = new JsonObject();
        lineItem.addProperty("id", lineItemGid);
        lineItem.addProperty("quantity", quantity);
        JsonArray lineItemsArray = new JsonArray();
        lineItemsArray.add(lineItem);

        JsonObject lineItemsByFulfillmentOrder = new JsonObject();
        lineItemsByFulfillmentOrder.addProperty("fulfillmentOrderId", fulfillmentOrderGid);
        lineItemsByFulfillmentOrder.add("fulfillmentOrderLineItems", lineItemsArray);
        JsonArray lineItemsByFOArray = new JsonArray();
        lineItemsByFOArray.add(lineItemsByFulfillmentOrder);

        JsonObject fulfillmentInput = new JsonObject();
        fulfillmentInput.add("lineItemsByFulfillmentOrder", lineItemsByFOArray);
        fulfillmentInput.add("trackingInfo", trackingInfo);
        fulfillmentInput.addProperty("notifyCustomer", true);

        JsonObject variables = new JsonObject();
        variables.add("in", fulfillmentInput);

        JsonObject payload = new JsonObject();
        payload.addProperty("query", mutation);
        payload.add("variables", variables);

        log.debug("Creando fulfillment con tracking via GraphQL. Payload: {}", payload.toString());
        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), defaultHeaders(vendor));

        try {
            String response = restTemplate.postForObject(url, entity, String.class);
            if (response == null) {
                log.error("GraphQL response for createFulfillment was null for orderKey {}", orderKey);
                return null;
            }

            JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
            if (responseJson.has("errors")) {
                log.error("GraphQL errors for createFulfillment on orderKey {}: {}", orderKey, responseJson.get("errors").toString());
                return null;
            }

            JsonObject fulfillmentNode = responseJson.getAsJsonObject("data").getAsJsonObject("fulfillmentCreateV2").getAsJsonObject("fulfillment");
            if (fulfillmentNode != null && fulfillmentNode.has("id")) {
                String fulfillmentGid = fulfillmentNode.get("id").getAsString();
                orderDto.setShopifyFulfillmentGid(fulfillmentGid); // Guardar GID del fulfillment
                return fulfillmentGid.substring(fulfillmentGid.lastIndexOf('/') + 1); // Devolver ID numérico por compatibilidad
            } else {
                 log.error("GraphQL userErrors for createFulfillment on orderKey {}: {}", orderKey, responseJson.getAsJsonObject("data").getAsJsonObject("fulfillmentCreateV2").get("userErrors").toString());
            }

        } catch (Exception e) {
            log.error("Error during GraphQL createFulfillment for orderKey {}", orderKey, e);
        }
        return null;
    }

    public String createFulfillmentWithTracking(VendorDto vendor, RegistryDto registryDto, String siteUrl) {
        if (vendor.isUseGraphQL()) {
            log.info("Dispatching to GraphQL implementation for createFulfillmentWithTracking.");
            return createFulfillmentWithTrackingGraphQL(vendor, registryDto, siteUrl);
        } else {
            log.info("Dispatching to REST implementation for createFulfillmentWithTracking.");
            return createFulfillmentWithTrackingRest(vendor, registryDto, siteUrl);
        }
    }

    private void postFulfillmentEventRest(VendorDto vendor, String shopifyOrderId, String shopifyFulfillmentId, String eventStatus, String message) {
        if (vendor == null || shopifyOrderId == null || shopifyFulfillmentId == null || eventStatus == null) {
            log.error("Datos incompletos para enviar evento de fulfillment (REST). Shopify Order ID: {}, Fulfillment ID: {}", shopifyOrderId, shopifyFulfillmentId);
            return;
        }

        String url = baseUrl(vendor) + "/orders/" + shopifyOrderId + "/fulfillments/" + shopifyFulfillmentId + "/events.json";
        log.info("Enviando evento de fulfillment a Shopify (REST). URL: {}, Status: {}, Message: {}", url, eventStatus, message);

        // Construir el cuerpo de la solicitud
        JsonObject eventPayload = new JsonObject();
        JsonObject eventDetails = new JsonObject();
        eventDetails.addProperty("status", eventStatus);
        if (message != null && !message.isEmpty()) {
            eventDetails.addProperty("message", message);
        }
        // Formato ISO 8601, ej: "2025-07-11T09:00:00Z"
        eventDetails.addProperty("happened_at", Instant.now().toString());
        eventPayload.add("event", eventDetails);

        HttpHeaders headers = defaultHeaders(vendor);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(eventPayload.toString(), headers);

        try {
            log.debug("Payload para evento de fulfillment (REST): {}", eventPayload.toString());
            String response = restTemplate.postForObject(url, entity, String.class);
            log.info("Respuesta de Shopify al enviar evento de fulfillment (REST): {}", response);
        } catch (Exception e) {
            log.error("Error al enviar evento de fulfillment a Shopify (REST) para Order ID {}: {}", shopifyOrderId, e.getMessage(), e);
        }
    }

    private void postFulfillmentEventGraphQL(VendorDto vendor, OrderDto orderDto, String eventStatus, String message) {
        String fulfillmentGid = orderDto.getShopifyFulfillmentGid();
        if (fulfillmentGid == null) {
            log.error("No se puede enviar evento GraphQL sin fulfillment GID para orderKey {}", orderDto.getOrderKey().toString());
            return;
        }

        String url = baseUrl(vendor, true);
        String mutation = "mutation fulfillmentEventCreate($event: FulfillmentEventInput!) { " +
                          "fulfillmentEventCreate(fulfillmentEvent: $event) { " +
                          "fulfillmentEvent { id status } " +
                          "userErrors { field message } } }";

        // Convertir status de REST (ej. "in_transit") a Enum de GraphQL (ej. "IN_TRANSIT")
        String graphQLStatus = eventStatus.toUpperCase().replace("_", "");

        JsonObject eventInput = new JsonObject();
        eventInput.addProperty("fulfillmentId", fulfillmentGid);
        eventInput.addProperty("status", graphQLStatus);
        if (message != null && !message.isEmpty()) {
            eventInput.addProperty("message", message);
        }
        eventInput.addProperty("happenedAt", Instant.now().toString());

        JsonObject variables = new JsonObject();
        variables.add("event", eventInput);

        JsonObject payload = new JsonObject();
        payload.addProperty("query", mutation);
        payload.add("variables", variables);

        log.debug("Enviando evento de fulfillment via GraphQL. Payload: {}", payload.toString());
        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), defaultHeaders(vendor));

        try {
            String response = restTemplate.postForObject(url, entity, String.class);
            if (response == null) {
                log.error("GraphQL response for postFulfillmentEvent was null for fulfillment GID {}", fulfillmentGid);
                return;
            }
            JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
             if (responseJson.has("errors") || responseJson.getAsJsonObject("data").getAsJsonObject("fulfillmentEventCreate").getAsJsonArray("userErrors").size() > 0) {
                log.error("GraphQL errors for postFulfillmentEvent on fulfillment GID {}: {}", fulfillmentGid, response);
            } else {
                log.info("Evento de fulfillment enviado con éxito via GraphQL para fulfillment GID {}", fulfillmentGid);
            }
        } catch (Exception e) {
            log.error("Error during GraphQL postFulfillmentEvent for fulfillment GID {}", fulfillmentGid, e);
        }
    }

    public void postFulfillmentEvent(VendorDto vendor, OrderDto orderDto, String shopifyFulfillmentId, String eventStatus, String message) {
        if (vendor.isUseGraphQL()) {
            log.info("Dispatching to GraphQL implementation for postFulfillmentEvent.");
            postFulfillmentEventGraphQL(vendor, orderDto, eventStatus, message);
        } else {
            log.info("Dispatching to REST implementation for postFulfillmentEvent.");
            // La implementación REST usa el ID numérico de la orden, no el GID.
            postFulfillmentEventRest(vendor, orderDto.getId(), shopifyFulfillmentId, eventStatus, message);
        }
    }
}
