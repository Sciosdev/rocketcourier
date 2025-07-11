package com.rocket.service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rocket.service.entity.VendorDto;
import com.rocket.service.entity.OrderDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ShopifySyncServiceTest {

    @Mock
    private RestTemplate restTemplate;


    private ShopifySyncService service;

    private VendorDto vendor;

    @BeforeEach
    void setup() {
        vendor = new VendorDto();
        vendor.setShopifyAccessToken("tkn");
        vendor.setShopifyStoreUrl("https://store.myshopify.com");
        vendor.setShopifyApiVersion("2024-04");

        service = new ShopifySyncService(restTemplate);

    }

    @Test
    void testCreateFulfillmentCallsShopify() {
        service.createFulfillment(vendor, "123");

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

    }

    @Test
    void testAddTrackingCallsShopify() {
        service.addTracking(vendor, "123", "555", "TRACK", "Test");

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

    }

    @Test
    void testPostFulfillmentEventCallsShopify() {
        service.postFulfillmentEvent(vendor, "123", "555", "delivered");

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

    }

    @Test
    void testFetchFulfillmentDataCallsShopify() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok("{\"fulfillment_orders\":[{\"id\":1,\"line_items\":[{\"id\":2,\"quantity\":1}]}]}"));

        service.fetchFulfillmentData(vendor, "123");

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testCreateFulfillmentWithTrackingCallsShopify() {
        OrderDto order = new OrderDto();
        order.setId("123");
        order.setFulfillmentOrderId("1");
        order.setFulfillmentLineItemId("2");
        order.setFulfillmentLineItemQty(1);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(org.springframework.http.ResponseEntity.ok("{\"fulfillment\":{\"id\":321}}"));

        String result = service.createFulfillmentWithTracking(vendor, order, "https://example.com");

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
        assertEquals("321", result);
        assertEquals("321", order.getFulfillmentId());
    }
}
