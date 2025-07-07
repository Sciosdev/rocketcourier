package com.rocket.service.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rocket.service.entity.Billing_addressDto;
import com.rocket.service.entity.OrderDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.Shipping_addressDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.model.DBResponse;
import com.rocket.service.model.RegistroServiceInDto;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.service.VendorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ShopifyController {

    @Autowired
    VendorService vendorService;

    @Autowired
    UsuarioService usuarioService;

    @RequestMapping(value = "/shopify/orders/{user}", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public ResponseEntity<String> obtenerOrders(
            @PathVariable String user,
            @RequestParam(value = "created_at_min", required = false) String createdAtMin,
            @RequestParam(value = "created_at_max", required = false) String createdAtMax) {
        Gson gson = new Gson();
        try {

            UserDto usuario = usuarioService.obtenerUsuario(user);

            if (usuario == null) {
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "Usuario no encontrado")));
            }
            VendorDto vendor = vendorService.obtenerTiendaPorId(usuario.getTienda() != null ? usuario.getTienda().longValue() : null);
            if (vendor == null || vendor.getShopifyAccessToken() == null || vendor.getShopifyStoreUrl() == null) {
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "Credenciales no configuradas")));
            }

            String shopifyApiVersion = "2024-04"; // Stable API version
            String rawShopifyStoreUrl = vendor.getShopifyStoreUrl();
            String shopifyAccessToken = vendor.getShopifyAccessToken();

            log.info("Shopify API Call Details for user: {}", user);
            log.info("Raw Shopify Store URL from DB: '{}'", rawShopifyStoreUrl);
            // Avoid logging full token in production if possible, or mask it. For debug:
            log.info("Shopify Access Token (first 10 chars): '{}...'", shopifyAccessToken != null && shopifyAccessToken.length() > 10 ? shopifyAccessToken.substring(0, 10) : "TOKEN_TOO_SHORT_OR_NULL");
            log.info("Attempting to use Shopify API Version: {}", shopifyApiVersion);
            log.info("Received createdAtMin: '{}', createdAtMax: '{}'", createdAtMin, createdAtMax);

            // Normalize the store URL: ensure it starts with https:// and does not end with a slash before appending API path
            String normalizedStoreUrl = rawShopifyStoreUrl;
            if (normalizedStoreUrl == null || normalizedStoreUrl.trim().isEmpty()) {
                log.error("Shopify Store URL is null or empty for vendor linked to user: {}", user);
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "URL de tienda Shopify no configurada")));
            }

            if (!normalizedStoreUrl.startsWith("https://") && !normalizedStoreUrl.startsWith("http://")) {
                normalizedStoreUrl = "https://" + normalizedStoreUrl;
            } else if (normalizedStoreUrl.startsWith("http://")) {
                // Optionally upgrade http to https, or log a warning.
                log.warn("Shopify Store URL starts with http://, forcing https:// for user: {}", user);
                normalizedStoreUrl = "https://" + normalizedStoreUrl.substring(7);
            }
            
            // Remove trailing slash if present, to prevent double slashes when appending path
            if (normalizedStoreUrl.endsWith("/")) {
                normalizedStoreUrl = normalizedStoreUrl.substring(0, normalizedStoreUrl.length() - 1);
            }

            String finalShopifyApiUrl = normalizedStoreUrl + "/admin/api/" + shopifyApiVersion + "/orders.json";
            log.info("Final constructed Shopify API URL: '{}'", finalShopifyApiUrl);

            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Shopify-Access-Token", shopifyAccessToken);
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(finalShopifyApiUrl);

            if (createdAtMin != null && !createdAtMin.trim().isEmpty()) {
                builder.queryParam("created_at_min", createdAtMin);
            }
            if (createdAtMax != null && !createdAtMax.trim().isEmpty()) {
                builder.queryParam("created_at_max", createdAtMax);
            }
            
            log.info("Full request URL with params: {}", builder.toUriString());

            ResponseEntity<String> resp = rest.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            JsonObject obj = JsonParser.parseString(resp.getBody()).getAsJsonObject();
            JsonArray orders = obj.getAsJsonArray("orders");
            List<RegistryDto> registros = new ArrayList<>();
            for (JsonElement e : orders) {
                JsonObject o = e.getAsJsonObject();
                RegistryDto reg = new RegistryDto();
                OrderDto order = new OrderDto();
                order.setId(o.get("id").getAsString());
                order.setName(o.get("name").getAsString());
                // Safely get optional fields
                order.setEmail(o.has("email") && !o.get("email").isJsonNull() ? o.get("email").getAsString() : "");
                order.setFinancial_status(o.has("financial_status") && !o.get("financial_status").isJsonNull() ? o.get("financial_status").getAsString() : "");
                order.setVendor(user);
                order.setRisk_level("N/A");
                order.setSource("SHOPIFY");
                order.setCurrency(o.has("currency") && !o.get("currency").isJsonNull() ? o.get("currency").getAsString() : "");
                order.setSubtotal(o.has("subtotal_price") && !o.get("subtotal_price").isJsonNull() ? o.get("subtotal_price").getAsDouble() : 0.0);
                
                double shipping = 0.0;
                if (o.has("total_shipping_price_set") && !o.get("total_shipping_price_set").isJsonNull()) {
                    JsonObject totalShippingPriceSet = o.getAsJsonObject("total_shipping_price_set");
                    if (totalShippingPriceSet.has("shop_money") && !totalShippingPriceSet.get("shop_money").isJsonNull()) {
                        JsonObject shopMoney = totalShippingPriceSet.getAsJsonObject("shop_money");
                        if (shopMoney.has("amount") && !shopMoney.get("amount").isJsonNull()) {
                             shipping = shopMoney.get("amount").getAsDouble();
                        }
                    }
                }
                order.setShipping(shipping);
                order.setShipping_method(""); // Consider if this can be fetched
                order.setCreated_at(new Date()); // This sets the creation date in *your* system, not from Shopify order
                reg.setOrder(order);

                if (o.has("shipping_address") && !o.get("shipping_address").isJsonNull()) {
                    JsonObject sa = o.getAsJsonObject("shipping_address");
                    Shipping_addressDto ship = new Shipping_addressDto();
                    ship.setName(sa.has("name") && !sa.get("name").isJsonNull() ? sa.get("name").getAsString() : "");
                    ship.setStreet(sa.has("address1") && !sa.get("address1").isJsonNull() ? sa.get("address1").getAsString() : "");
                    ship.setAddress1(sa.has("address1") && !sa.get("address1").isJsonNull() ? sa.get("address1").getAsString() : "");
                    ship.setAddress2(sa.has("address2") && !sa.get("address2").isJsonNull() ? sa.get("address2").getAsString() : "");
                    ship.setCompany(sa.has("company") && !sa.get("company").isJsonNull() ? sa.get("company").getAsString() : "");
                    ship.setCity(sa.has("city") && !sa.get("city").isJsonNull() ? sa.get("city").getAsString() : "");
                    ship.setZip(sa.has("zip") && !sa.get("zip").isJsonNull() ? sa.get("zip").getAsString() : "");
                    ship.setProvince(sa.has("province") && !sa.get("province").isJsonNull() ? sa.get("province").getAsString() : "");
                    ship.setProvince_name(ship.getProvince());
                    ship.setCountry(sa.has("country") && !sa.get("country").isJsonNull() ? sa.get("country").getAsString() : "");
                    ship.setPhone(sa.has("phone") && !sa.get("phone").isJsonNull() ? sa.get("phone").getAsString() : "");
                    reg.setShipping_address(ship);
                }

                if (o.has("billing_address") && !o.get("billing_address").isJsonNull()) {
                    JsonObject ba = o.getAsJsonObject("billing_address");
                    Billing_addressDto bill = new Billing_addressDto();
                    bill.setName(ba.has("name") && !ba.get("name").isJsonNull() ? ba.get("name").getAsString() : "");
                    bill.setStreet(ba.has("address1") && !ba.get("address1").isJsonNull() ? ba.get("address1").getAsString() : "");
                    bill.setAddress1(ba.has("address1") && !ba.get("address1").isJsonNull() ? ba.get("address1").getAsString() : "");
                    bill.setAddress2(ba.has("address2") && !ba.get("address2").isJsonNull() ? ba.get("address2").getAsString() : "");
                    bill.setCompany(ba.has("company") && !ba.get("company").isJsonNull() ? ba.get("company").getAsString() : "");
                    bill.setCity(ba.has("city") && !ba.get("city").isJsonNull() ? ba.get("city").getAsString() : "");
                    bill.setZip(ba.has("zip") && !ba.get("zip").isJsonNull() ? ba.get("zip").getAsString() : "");
                    bill.setProvince(ba.has("province") && !ba.get("province").isJsonNull() ? ba.get("province").getAsString() : "");
                    bill.setProvince_name(bill.getProvince());
                    bill.setCountry(ba.has("country") && !ba.get("country").isJsonNull() ? ba.get("country").getAsString() : "");
                    bill.setPhone(ba.has("phone") && !ba.get("phone").isJsonNull() ? ba.get("phone").getAsString() : "");
                    reg.setBilling_address(bill);
                }
                registros.add(reg);
            }
            RegistroServiceInDto dto = new RegistroServiceInDto();
            dto.setRegistro(registros);
            dto.setIdVendor(user);
            dto.setTipoCarga(0); // Consider if this should be dynamic or configurable
            return ResponseEntity.ok(gson.toJson(dto));
        } catch (Exception e) {
            log.error("Error al consultar Shopify para usuario: {}. Fechas: min={}, max={}. Excepción: ", user, createdAtMin, createdAtMax, e);
            return ResponseEntity.internalServerError().body(gson.toJson(new DBResponse(false, "Error consultando Shopify")));
        }
    }
}
