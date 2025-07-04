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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ShopifyController {

    @Autowired
    VendorService vendorService;

    @Autowired
    UsuarioService usuarioService;

    @RequestMapping(value = "/shopify/orders/{user}", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public ResponseEntity<String> obtenerOrders(@PathVariable String user) {
        Gson gson = new Gson();
        try {
            UserDto  usuario = usuarioService.obtenerUsuario(user);
            if (usuario == null) {
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "Usuario no encontrado")));
            }
            VendorDto vendor = vendorService.obtenerTiendaPorId(usuario.getTienda() != null ? usuario.getTienda().longValue() : null);
            if (vendor == null || vendor.getShopifyAccessToken() == null || vendor.getSitio() == null) {
                return ResponseEntity.badRequest().body(gson.toJson(new DBResponse(false, "Credenciales no configuradas")));
            }

            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Shopify-Access-Token", vendor.getShopifyAccessToken());
            ResponseEntity<String> resp = rest.exchange(
                    "https://" + vendor.getSitio() + "/admin/api/2023-07/orders.json",
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
                order.setEmail(o.get("email").getAsString());
                order.setFinancial_status(o.get("financial_status").getAsString());
                order.setVendor(user);
                order.setRisk_level("N/A");
                order.setSource("SHOPIFY");
                order.setCurrency(o.get("currency").getAsString());
                order.setSubtotal(o.get("subtotal_price").getAsDouble());
                order.setShipping(o.get("total_shipping_price_set").getAsJsonObject().get("shop_money").getAsJsonObject().get("amount").getAsDouble());
                order.setShipping_method("");
                order.setCreated_at(new Date());
                reg.setOrder(order);

                if (o.has("shipping_address") && !o.get("shipping_address").isJsonNull()) {
                    JsonObject sa = o.getAsJsonObject("shipping_address");
                    Shipping_addressDto ship = new Shipping_addressDto();
                    ship.setName(sa.get("name").getAsString());
                    ship.setStreet(sa.get("address1").getAsString());
                    ship.setAddress1(sa.get("address1").getAsString());
                    ship.setAddress2(sa.has("address2") && !sa.get("address2").isJsonNull() ? sa.get("address2").getAsString() : "");
                    ship.setCompany(sa.has("company") && !sa.get("company").isJsonNull() ? sa.get("company").getAsString() : "");
                    ship.setCity(sa.get("city").getAsString());
                    ship.setZip(sa.get("zip").getAsString());
                    ship.setProvince(sa.has("province") && !sa.get("province").isJsonNull() ? sa.get("province").getAsString() : "");
                    ship.setProvince_name(ship.getProvince());
                    ship.setCountry(sa.get("country").getAsString());
                    ship.setPhone(sa.has("phone") && !sa.get("phone").isJsonNull() ? sa.get("phone").getAsString() : "");
                    reg.setShipping_address(ship);
                }

                if (o.has("billing_address") && !o.get("billing_address").isJsonNull()) {
                    JsonObject ba = o.getAsJsonObject("billing_address");
                    Billing_addressDto bill = new Billing_addressDto();
                    bill.setName(ba.get("name").getAsString());
                    bill.setStreet(ba.get("address1").getAsString());
                    bill.setAddress1(ba.get("address1").getAsString());
                    bill.setAddress2(ba.has("address2") && !ba.get("address2").isJsonNull() ? ba.get("address2").getAsString() : "");
                    bill.setCompany(ba.has("company") && !ba.get("company").isJsonNull() ? ba.get("company").getAsString() : "");
                    bill.setCity(ba.get("city").getAsString());
                    bill.setZip(ba.get("zip").getAsString());
                    bill.setProvince(ba.has("province") && !ba.get("province").isJsonNull() ? ba.get("province").getAsString() : "");
                    bill.setProvince_name(bill.getProvince());
                    bill.setCountry(ba.get("country").getAsString());
                    bill.setPhone(ba.has("phone") && !ba.get("phone").isJsonNull() ? ba.get("phone").getAsString() : "");
                    reg.setBilling_address(bill);
                }
                registros.add(reg);
            }
            RegistroServiceInDto dto = new RegistroServiceInDto();
            dto.setRegistro(registros);
            dto.setIdVendor(user);
            dto.setTipoCarga(0);
            return ResponseEntity.ok(gson.toJson(dto));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(gson.toJson(new DBResponse(false, "Error consultando Shopify")));
        }
    }
}
