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
import com.google.gson.GsonBuilder; // Asegurar que GsonBuilder esté importado si se usa, o quitar su uso.
import org.springframework.http.HttpStatus; // Importación faltante

// ... (otros imports permanecen igual)
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap; // Para el Map de respuesta
import java.util.Map; // Para el Map de respuesta

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
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // Formato ISO 8601
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

        // Map<String, Object> responseMap = new HashMap<>(); // Ya no se usa para la respuesta final

        try {
            log.info("Iniciando obtenerOrders para usuario: {} con created_at_min: {} y created_at_max: {}", user, createdAtMin, createdAtMax);

            UserDto usuario = usuarioService.obtenerUsuario(user);

            if (usuario == null) {
                log.warn("Usuario no encontrado: {}", user);
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "Usuario no encontrado")));
            }

            VendorDto vendor = vendorService.obtenerTiendaPorId(usuario.getTienda() != null ? usuario.getTienda().longValue() : null);
            if (vendor == null || vendor.getShopifyAccessToken() == null || vendor.getShopifyStoreUrl() == null) {
                log.warn("Credenciales de Shopify no configuradas para vendor: {}", (vendor != null ? vendor.getId() : "null"));
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "Credenciales no configuradas")));
            }

            String shopifyApiVersion = "2024-04"; // Stable API version
            String rawShopifyStoreUrl = vendor.getShopifyStoreUrl();
            String shopifyAccessToken = vendor.getShopifyAccessToken();

            log.info("Raw Shopify Store URL from DB: '{}'", rawShopifyStoreUrl);
            // Avoid logging full token in production if possible, or mask it. For debug:
            // log.info("Shopify Access Token (first 10 chars): '{}...'", shopifyAccessToken != null && shopifyAccessToken.length() > 10 ? shopifyAccessToken.substring(0, 10) : "TOKEN_TOO_SHORT_OR_NULL");


            // Normalize the store URL: ensure it starts with https:// and does not end with a slash before appending API path
            String normalizedStoreUrl = rawShopifyStoreUrl;
            if (normalizedStoreUrl == null || normalizedStoreUrl.trim().isEmpty()) {
                log.error("URL de tienda Shopify es null o vacía para vendor asociado al usuario: {}", user);
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "URL de tienda Shopify no configurada")));
            }

            if (!normalizedStoreUrl.startsWith("https://") && !normalizedStoreUrl.startsWith("http://")) {
                normalizedStoreUrl = "https://" + normalizedStoreUrl;
            } else if (normalizedStoreUrl.startsWith("http://")) {
                log.warn("Shopify Store URL starts with http://, forcing https:// for user: {}", user);
                normalizedStoreUrl = "https://" + normalizedStoreUrl.substring(7);
            }
            
            if (normalizedStoreUrl.endsWith("/")) {
                normalizedStoreUrl = normalizedStoreUrl.substring(0, normalizedStoreUrl.length() - 1);
            }

            String finalShopifyApiUrl = normalizedStoreUrl + "/admin/api/" + shopifyApiVersion + "/orders.json";
            log.info("Final constructed Shopify API URL: '{}'", finalShopifyApiUrl);

            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Shopify-Access-Token", shopifyAccessToken);
            
            String fieldsToRequest = "id,name,email,financial_status,created_at,currency,subtotal_price,total_shipping_price_set,shipping_address,billing_address,line_items,note,note_attributes,customer,shipping_lines,processed_at,updated_at,fulfillment_status,cancelled_at";
            // No incluimos explícitamente sub-campos de shipping_address o billing_address aquí,
            // ya que Shopify debería devolver el objeto completo si se solicita el objeto padre.
            // Si esto no funciona, el siguiente paso sería añadir los sub-campos como 'shipping_address.name', etc.

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(finalShopifyApiUrl)
                                             .queryParam("status", "any")
                                             .queryParam("fields", fieldsToRequest);

            if (createdAtMin != null && !createdAtMin.trim().isEmpty()) {
                builder.queryParam("created_at_min", createdAtMin);
            }
            if (createdAtMax != null && !createdAtMax.trim().isEmpty()) {
                builder.queryParam("created_at_max", createdAtMax);
            }
            // builder.queryParam("limit", 250);
            
            String requestUrl = builder.toUriString();
            log.info("Full request URL with params: {}", requestUrl);

            ResponseEntity<String> resp;
            try {
                resp = rest.exchange(
                        requestUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                log.error("Error en la llamada a Shopify API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                log.error("Exception details: ", e);
                return ResponseEntity.status(e.getStatusCode()).body(gson.toJson(new DBResponse(false, "Error al comunicarse con Shopify: " + e.getResponseBodyAsString())));
            }

            log.info("Shopify API Response Status: {}", resp.getStatusCode());
            // No loguear todo el body si puede ser muy grande, solo si es necesario para debug puntual.
            // log.info("Shopify API Response Body: {}", resp.getBody());

            JsonObject obj = JsonParser.parseString(resp.getBody()).getAsJsonObject();
            JsonArray ordersArray = obj.getAsJsonArray("orders");
            List<RegistryDto> registros = new ArrayList<>();

            if (ordersArray == null) {
                log.warn("La respuesta de Shopify no contiene el array 'orders' o es nulo.");
            } else {
                log.info("Número de pedidos recibidos de Shopify: {}", ordersArray.size());
                for (JsonElement element : ordersArray) {
                    JsonObject o = element.getAsJsonObject();
                    RegistryDto reg = new RegistryDto();
                    OrderDto order = new OrderDto();

                    order.setId(o.has("id") && !o.get("id").isJsonNull() ? o.get("id").getAsString() : null);
                    order.setName(o.has("name") && !o.get("name").isJsonNull() ? o.get("name").getAsString() : "");
                    order.setEmail(o.has("email") && !o.get("email").isJsonNull() ? o.get("email").getAsString() : "");
                    order.setFinancial_status(o.has("financial_status") && !o.get("financial_status").isJsonNull() ? o.get("financial_status").getAsString() : "");
                    order.setVendor(user);
                    order.setRisk_level(o.has("risk_level") && !o.get("risk_level").isJsonNull() ? o.get("risk_level").getAsString() : "N/A");
                    order.setSource(o.has("source_name") && !o.get("source_name").isJsonNull() ? o.get("source_name").getAsString() : "SHOPIFY");
                    order.setCurrency(o.has("currency") && !o.get("currency").isJsonNull() ? o.get("currency").getAsString() : "");
                    order.setSubtotal(o.has("subtotal_price") && !o.get("subtotal_price").isJsonNull() ? o.get("subtotal_price").getAsDouble() : 0.0);

                    double shippingCost = 0.0;
                    if (o.has("total_shipping_price_set") && o.get("total_shipping_price_set").isJsonObject()) {
                        JsonObject totalShippingPriceSet = o.getAsJsonObject("total_shipping_price_set");
                        if (totalShippingPriceSet.has("shop_money") && totalShippingPriceSet.get("shop_money").isJsonObject()) {
                            JsonObject shopMoney = totalShippingPriceSet.getAsJsonObject("shop_money");
                            if (shopMoney.has("amount") && !shopMoney.get("amount").isJsonNull()) {
                                 shippingCost = shopMoney.get("amount").getAsDouble();
                            }
                        }
                    }
                    order.setShipping(shippingCost);

                    String shippingMethod = "";
                    if (o.has("shipping_lines") && o.get("shipping_lines").isJsonArray()) {
                        JsonArray shippingLines = o.getAsJsonArray("shipping_lines");
                        if (shippingLines.size() > 0) {
                            JsonObject firstShippingLine = shippingLines.get(0).getAsJsonObject();
                            if (firstShippingLine.has("title") && !firstShippingLine.get("title").isJsonNull()) {
                                shippingMethod = firstShippingLine.get("title").getAsString();
                            }
                        }
                    }
                    order.setShipping_method(shippingMethod);

                    if (o.has("created_at") && !o.get("created_at").isJsonNull()) {
                        String createdAtStr = o.get("created_at").getAsString();
                        try {
                            order.setCreated_at(Date.from(OffsetDateTime.parse(createdAtStr).toInstant()));
                        } catch (DateTimeParseException dtpe) {
                            log.warn("No se pudo parsear la fecha created_at de Shopify: '{}' para el pedido {}. Usando fecha actual.", createdAtStr, order.getId());
                            order.setCreated_at(new Date());
                        }
                    } else {
                        order.setCreated_at(new Date());
                    }
                    reg.setOrder(order);

                    if (o.has("shipping_address") && o.get("shipping_address").isJsonObject()) {
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
                        ship.setProvince_name(sa.has("province_code") && !sa.get("province_code").isJsonNull() ? sa.get("province_code").getAsString() : (sa.has("province") && !sa.get("province").isJsonNull() ? sa.get("province").getAsString() : ""));
                        ship.setCountry(sa.has("country") && !sa.get("country").isJsonNull() ? sa.get("country").getAsString() : "");
                        ship.setPhone(sa.has("phone") && !sa.get("phone").isJsonNull() ? sa.get("phone").getAsString() : "");
                        reg.setShipping_address(ship);
                    } else {
                        log.warn("Pedido {} no tiene shipping_address. Se creará uno vacío.", order.getId() != null ? order.getId() : "ID no disponible");
                        reg.setShipping_address(new Shipping_addressDto());
                    }

                    if (o.has("billing_address") && o.get("billing_address").isJsonObject()) {
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
                        bill.setProvince_name(ba.has("province_code") && !ba.get("province_code").isJsonNull() ? ba.get("province_code").getAsString() : (ba.has("province") && !ba.get("province").isJsonNull() ? ba.get("province").getAsString() : ""));
                        bill.setCountry(ba.has("country") && !ba.get("country").isJsonNull() ? ba.get("country").getAsString() : "");
                        bill.setPhone(ba.has("phone") && !ba.get("phone").isJsonNull() ? ba.get("phone").getAsString() : "");
                        reg.setBilling_address(bill);
                    } else {
                        log.warn("Pedido {} no tiene billing_address. Se creará uno vacío.", order.getId() != null ? order.getId() : "ID no disponible");
                        reg.setBilling_address(new Billing_addressDto());
                    }
                    registros.add(reg);
                }
            }

            RegistroServiceInDto dto = new RegistroServiceInDto();
            dto.setRegistro(registros);
            dto.setIdVendor(user);
            dto.setTipoCarga(0);
            log.info("Devolviendo {} registros al frontend para el usuario {}. Payload: {}", registros.size(), user, gson.toJson(dto)); // Loguear el DTO que se envía
            return ResponseEntity.ok(gson.toJson(dto)); // Devolver directamente el DTO serializado

        } catch (Exception e) {
            log.error("Error general en obtenerOrders para usuario: {}. Fechas: min={}, max={}. Excepción: ", user, createdAtMin, createdAtMax, e);
            // Devolver un DBResponse en caso de error, como antes
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gson.toJson(new DBResponse(false, "Error interno del servidor al consultar Shopify: " + e.getMessage())));
        }
    }
}
