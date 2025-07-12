package com.rocket.service.service;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.rocket.service.entity.OrderDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.model.ShopifyFulfillmentData;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;

@ExtendWith(MockitoExtension.class)
class ShopifySyncServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ShopifySyncService service;
    private VendorDto vendor;
    private RegistryDto registryDto;
    private OrderDto orderDto;

    private final String DEFAULT_SITE_URL = "https://test.rocket.com";

    @BeforeEach
    void setup() {
        vendor = new VendorDto();
        vendor.setShopifyAccessToken("test_token");
        vendor.setShopifyStoreUrl("https://test-store.myshopify.com");
        vendor.setShopifyApiVersion("2025-07"); // Matching the API version in user's example

        orderDto = new OrderDto();
        orderDto.setId("shopifyOrder123"); // Shopify Order ID
        orderDto.setOrderKey(new ObjectId()); // Internal order key
        orderDto.setFulfillmentOrderId("10978443329828");
        orderDto.setFulfillmentLineItemId("31904189251876");
        orderDto.setFulfillmentLineItemQty(9);

        registryDto = new RegistryDto();
        registryDto.setOrder(orderDto);

        service = new ShopifySyncService(restTemplate);
    }

    private String buildExpectedShopifyUrl() {
        return "https://test-store.myshopify.com/admin/api/2025-07/fulfillments.json";
    }

    @Test
    void createFulfillmentWithTracking_success() {
        String expectedFulfillmentId = "5773843562788";
        String mockShopifyResponse = "{\"fulfillment\":{\"id\":" + expectedFulfillmentId + ",\"status\":\"success\"}}";

        when(restTemplate.postForObject(eq(buildExpectedShopifyUrl()), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockShopifyResponse);

        String actualFulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);

        assertEquals(expectedFulfillmentId, actualFulfillmentId);

        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(buildExpectedShopifyUrl()), httpEntityCaptor.capture(), eq(String.class));

        HttpEntity capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity.getBody());
        String payload = capturedEntity.getBody().toString();

        // Basic payload checks
        JsonObject parsedPayload = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
        JsonObject fulfillmentNode = parsedPayload.getAsJsonObject("fulfillment");
        assertNotNull(fulfillmentNode);
        assertEquals(true, fulfillmentNode.get("notify_customer").getAsBoolean());

        JsonObject trackingInfo = fulfillmentNode.getAsJsonObject("tracking_info");
        assertNotNull(trackingInfo);
        assertEquals(orderDto.getOrderKey().toString(), trackingInfo.get("number").getAsString());
        assertEquals("Rocket Courier", trackingInfo.get("company").getAsString());
        assertEquals(DEFAULT_SITE_URL + "/intranet/inicio?orderKey=" + orderDto.getOrderKey().toString(), trackingInfo.get("url").getAsString());

        JsonObject lineItemsOrder = fulfillmentNode.getAsJsonArray("line_items_by_fulfillment_order").get(0).getAsJsonObject();
        assertEquals(Long.parseLong(orderDto.getFulfillmentOrderId()), lineItemsOrder.get("fulfillment_order_id").getAsLong());
        JsonObject lineItem = lineItemsOrder.getAsJsonArray("fulfillment_order_line_items").get(0).getAsJsonObject();
        assertEquals(Long.parseLong(orderDto.getFulfillmentLineItemId()), lineItem.get("id").getAsLong());
        assertEquals(orderDto.getFulfillmentLineItemQty().intValue(), lineItem.get("quantity").getAsInt());
    }

    @Test
    void createFulfillmentWithTracking_nullVendor_returnsNull() {
        String fulfillmentId = service.createFulfillmentWithTracking(null, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_nullRegistry_returnsNull() {
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, null, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_nullOrderDto_returnsNull() {
        registryDto.setOrder(null);
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_nullSiteUrl_returnsNull() {
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, null);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_missingShopifyOrderId_returnsNull() {
        orderDto.setId(null);
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
         verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_missingOrderKey_returnsNull() {
        orderDto.setOrderKey(null);
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_missingFulfillmentOrderId_returnsNull() {
        orderDto.setFulfillmentOrderId(null);
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_missingLineItemId_returnsNull() {
        orderDto.setFulfillmentLineItemId(null);
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_missingQuantity_returnsNull() {
        orderDto.setFulfillmentLineItemQty(null);
        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void createFulfillmentWithTracking_shopifyReturnsNoFulfillmentId_returnsNull() {
        String mockShopifyResponse = "{\"fulfillment\":{\"status\":\"success\"}}"; // No 'id' field
        when(restTemplate.postForObject(eq(buildExpectedShopifyUrl()), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockShopifyResponse);

        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
    }

    @Test
    void createFulfillmentWithTracking_shopifyReturnsMalformedJson_returnsNull() {
        String mockShopifyResponse = "{\"fulfillment\":{\"id\":123,}}"; // Malformed JSON
        when(restTemplate.postForObject(eq(buildExpectedShopifyUrl()), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockShopifyResponse);

        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
    }

    @Test
    void createFulfillmentWithTracking_shopifyReturnsError_returnsNull() {
        when(restTemplate.postForObject(eq(buildExpectedShopifyUrl()), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Shopify error"));

        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
    }

    @Test
    void createFulfillmentWithTracking_shopifyReturnsNullResponse_returnsNull() {
        when(restTemplate.postForObject(eq(buildExpectedShopifyUrl()), any(HttpEntity.class), eq(String.class)))
            .thenReturn(null);

        String fulfillmentId = service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);
        assertNull(fulfillmentId);
    }


    // --- Existing tests, ensure they are still valid or adapt if necessary ---
    @Test
    void testCreateFulfillmentCallsShopify() {
        // This test might be for a different createFulfillment method,
        // ensure it's still relevant or remove/adapt.
        // For now, assuming it tests the older simple fulfillment creation.
        String olderApiUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/123/fulfillments.json";
        service.createFulfillment(vendor, "123"); // Assuming this is the older method
        verify(restTemplate).postForEntity(eq(olderApiUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testAddTrackingCallsShopify() {
        String olderApiUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/123/fulfillments/555/update_tracking.json";
        service.addTracking(vendor, "123", "555", "TRACK", "Test");
        verify(restTemplate).postForEntity(eq(olderApiUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testPostFulfillmentEventCallsShopify() {
        String olderApiUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/123/fulfillments/555/events.json";
        service.postFulfillmentEvent(vendor, "123", "555", "delivered");
        verify(restTemplate).postForEntity(eq(olderApiUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void fetchFulfillmentData_dispatchesToRest_whenFlagIsFalse() {
        vendor.setUseGraphQL(false);
        String apiUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/123/fulfillment_orders.json";
        when(restTemplate.exchange(eq(apiUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(ResponseEntity.ok("{\"fulfillment_orders\":[{\"id\":1001,\"line_items\":[{\"id\":2002,\"quantity\":1}]}]}"));

        service.fetchFulfillmentData(vendor, "123", orderDto);

        verify(restTemplate).exchange(eq(apiUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void fetchFulfillmentData_dispatchesToGraphQL_whenFlagIsTrue() {
        vendor.setUseGraphQL(true);
        String graphqlUrl = "https://test-store.myshopify.com/admin/api/2025-07/graphql.json";
        String mockResponse = "{\"data\":{\"order\":{\"fulfillmentOrders\":{\"edges\":[{\"node\":{\"id\":\"gid://shopify/FulfillmentOrder/1001\",\"lineItems\":{\"edges\":[{\"node\":{\"id\":\"gid://shopify/FulfillmentOrderLineItem/2002\",\"quantity\":1}}]}}}]}}}}";
        when(restTemplate.postForObject(eq(graphqlUrl), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockResponse);

        service.fetchFulfillmentData(vendor, "123", orderDto);

        verify(restTemplate).postForObject(eq(graphqlUrl), any(HttpEntity.class), eq(String.class));
        assertEquals("gid://shopify/FulfillmentOrder/1001", orderDto.getShopifyFulfillmentOrderGid());
        assertEquals("gid://shopify/FulfillmentOrderLineItem/2002", orderDto.getShopifyLineItemGid());
    }

    @Test
    void createFulfillmentWithTracking_dispatchesToRest_whenFlagIsFalse() {
        vendor.setUseGraphQL(false);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn("{\"fulfillment\":{\"id\":5773843562788,\"status\":\"success\"}}");

        service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);

        verify(restTemplate).postForObject(eq(buildExpectedShopifyUrl()), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void createFulfillmentWithTracking_dispatchesToGraphQL_whenFlagIsTrue() {
        vendor.setUseGraphQL(true);
        // Prereqs for GraphQL call
        orderDto.setShopifyFulfillmentOrderGid("gid://shopify/FulfillmentOrder/10978443329828");
        orderDto.setShopifyLineItemGid("gid://shopify/FulfillmentOrderLineItem/31904189251876");

        String graphqlUrl = "https://test-store.myshopify.com/admin/api/2025-07/graphql.json";
        String mockResponse = "{\"data\":{\"fulfillmentCreateV2\":{\"fulfillment\":{\"id\":\"gid://shopify/Fulfillment/5773843562788\"},\"userErrors\":[]}}}";
        when(restTemplate.postForObject(eq(graphqlUrl), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockResponse);

        service.createFulfillmentWithTracking(vendor, registryDto, DEFAULT_SITE_URL);

        verify(restTemplate).postForObject(eq(graphqlUrl), any(HttpEntity.class), eq(String.class));
        assertEquals("gid://shopify/Fulfillment/5773843562788", orderDto.getShopifyFulfillmentGid());
    }

    @Test
    void postFulfillmentEvent_dispatchesToRest_whenFlagIsFalse() {
        vendor.setUseGraphQL(false);
        String expectedUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/shopifyOrder123/fulfillments/fulfillment123/events.json";

        service.postFulfillmentEvent(vendor, orderDto, "fulfillment123", "delivered", "msg");

        verify(restTemplate).postForObject(eq(expectedUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void postFulfillmentEvent_dispatchesToGraphQL_whenFlagIsTrue() {
        vendor.setUseGraphQL(true);
        orderDto.setShopifyFulfillmentGid("gid://shopify/Fulfillment/12345");
        String graphqlUrl = "https://test-store.myshopify.com/admin/api/2025-07/graphql.json";
        String mockResponse = "{\"data\":{\"fulfillmentEventCreate\":{\"fulfillmentEvent\":{\"id\":\"gid://shopify/FulfillmentEvent/1\"},\"userErrors\":[]}}}";
        when(restTemplate.postForObject(eq(graphqlUrl), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockResponse);

        service.postFulfillmentEvent(vendor, orderDto, "12345", "delivered", "msg");

        verify(restTemplate).postForObject(eq(graphqlUrl), any(HttpEntity.class), eq(String.class));
    }


    // --- Existing tests, adapted or kept for regression ---

    @Test
    void postFulfillmentEvent_success() {
        String shopifyOrderId = "shopifyOrder123";
        String fulfillmentId = "fulfillment123";
        String eventStatus = "delivered";
        String message = "Your package has been delivered.";
        String expectedUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/" + shopifyOrderId + "/fulfillments/" + fulfillmentId + "/events.json";

        service.postFulfillmentEvent(vendor, shopifyOrderId, fulfillmentId, eventStatus, message);

        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(expectedUrl), httpEntityCaptor.capture(), eq(String.class));

        HttpEntity capturedEntity = httpEntityCaptor.getValue();
        assertNotNull(capturedEntity.getBody());
        String payload = capturedEntity.getBody().toString();

        JsonObject parsedPayload = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
        JsonObject eventNode = parsedPayload.getAsJsonObject("event");
        assertNotNull(eventNode);
        assertEquals(eventStatus, eventNode.get("status").getAsString());
        assertEquals(message, eventNode.get("message").getAsString());
        assertNotNull(eventNode.get("happened_at").getAsString());
    }

    @Test
    void postFulfillmentEvent_nullMessage_success() {
        String shopifyOrderId = "shopifyOrder123";
        String fulfillmentId = "fulfillment123";
        String eventStatus = "in_transit";
        String expectedUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/" + shopifyOrderId + "/fulfillments/" + fulfillmentId + "/events.json";

        service.postFulfillmentEvent(vendor, shopifyOrderId, fulfillmentId, eventStatus, null);

        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(expectedUrl), httpEntityCaptor.capture(), eq(String.class));

        HttpEntity capturedEntity = httpEntityCaptor.getValue();
        String payload = capturedEntity.getBody().toString();

        JsonObject parsedPayload = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
        JsonObject eventNode = parsedPayload.getAsJsonObject("event");
        assertNotNull(eventNode);
        assertEquals(eventStatus, eventNode.get("status").getAsString());
        assertNull(eventNode.get("message")); // Message should not be present in payload
    }

    @Test
    void postFulfillmentEvent_apiError_doesNotThrow() {
        String shopifyOrderId = "shopifyOrder123";
        String fulfillmentId = "fulfillment123";
        String eventStatus = "failure";
        String expectedUrl = "https://test-store.myshopify.com/admin/api/2025-07/orders/" + shopifyOrderId + "/fulfillments/" + fulfillmentId + "/events.json";

        when(restTemplate.postForObject(eq(expectedUrl), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Shopify Server Error"));

        // Should not throw an exception, as it's caught and logged internally
        service.postFulfillmentEvent(vendor, shopifyOrderId, fulfillmentId, eventStatus, "Critical failure");

        // Verify that the call was still attempted
        verify(restTemplate).postForObject(eq(expectedUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void postFulfillmentEvent_missingFulfillmentId_doesNotCallApi() {
        service.postFulfillmentEvent(vendor, "shopifyOrder123", null, "delivered", "message");
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }
}
