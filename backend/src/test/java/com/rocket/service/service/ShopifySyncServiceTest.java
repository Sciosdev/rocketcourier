package com.rocket.service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.rocket.service.entity.VendorDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
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
        verify(restTemplate).postForEntity(any(String.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testAddTrackingCallsShopify() {
        service.addTracking(vendor, "123", "555", "TRACK", "Test");
        verify(restTemplate).postForEntity(any(String.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testPostFulfillmentEventCallsShopify() {
        service.postFulfillmentEvent(vendor, "123", "555", "delivered");
        verify(restTemplate).postForEntity(any(String.class), any(HttpEntity.class), any(Class.class));
    }
}
