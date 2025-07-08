package com.rocket.service.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@RestController
@Slf4j
public class ShopifyController {

    @Autowired
    VendorService vendorService;

    @Autowired
    UsuarioService usuarioService;

    private String getString(JsonObject jsonObject, String key, String defaultValue) {
        if (jsonObject != null && jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsString();
        }
        return defaultValue;
    }

    private String getString(JsonObject jsonObject, String key) {
        return getString(jsonObject, key, "");
    }

    @RequestMapping(value = "/shopify/orders/{user}", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public ResponseEntity<String> obtenerOrders(
            @PathVariable String user,
            @RequestParam(value = "created_at_min", required = false) String createdAtMin,
            @RequestParam(value = "created_at_max", required = false) String createdAtMax) {

        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

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

            String shopifyApiVersion = "2024-04";
            String rawShopifyStoreUrl = vendor.getShopifyStoreUrl();
            String shopifyAccessToken = vendor.getShopifyAccessToken();

            log.info("Raw Shopify Store URL from DB: '{}'", rawShopifyStoreUrl);

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
            
            String fieldsToRequest = "id,name,email,financial_status,created_at,currency,subtotal_price,total_shipping_price_set,note,note_attributes,customer,shipping_lines,processed_at,updated_at,fulfillment_status,cancelled_at,"
                                     + "billing_address,shipping_address,"
                                     + "shipping_address.name,shipping_address.first_name,shipping_address.last_name,shipping_address.address1,shipping_address.address2,shipping_address.company,shipping_address.city,shipping_address.zip,shipping_address.province,shipping_address.province_code,shipping_address.country,shipping_address.country_code,shipping_address.phone,"
                                     + "billing_address.name,billing_address.first_name,billing_address.last_name,billing_address.address1,billing_address.address2,billing_address.company,billing_address.city,billing_address.zip,billing_address.province,billing_address.province_code,billing_address.country,billing_address.country_code,billing_address.phone,"
                                     + "customer.default_address.name,customer.default_address.first_name,customer.default_address.last_name,customer.default_address.address1,customer.default_address.address2,customer.default_address.company,customer.default_address.city,customer.default_address.zip,customer.default_address.province,customer.default_address.province_code,customer.default_address.country,customer.default_address.country_code,customer.default_address.phone,"
                                     + "line_items";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(finalShopifyApiUrl)
                                             .queryParam("status", "any")
                                             .queryParam("fields", fieldsToRequest);

            if (createdAtMin != null && !createdAtMin.trim().isEmpty()) {
                builder.queryParam("created_at_min", createdAtMin);
            }
            if (createdAtMax != null && !createdAtMax.trim().isEmpty()) {
                builder.queryParam("created_at_max", createdAtMax);
            }
            
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
            log.debug("DEBUG - Shopify API Response Body: {}", resp.getBody()); // Cambiado a log.debug

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

                    order.setId(getString(o, "id", null));
                    order.setName(getString(o, "name"));
                    order.setEmail(getString(o, "email"));
                    order.setFinancial_status(getString(o, "financial_status"));
                    order.setVendor(user);
                    order.setRisk_level(getString(o, "risk_level", "N/A"));
                    order.setSource(getString(o, "source_name", "SHOPIFY"));
                    order.setCurrency(getString(o, "currency"));
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

                    JsonObject customerJson = null;
                    if (o.has("customer") && o.get("customer").isJsonObject()) {
                        customerJson = o.getAsJsonObject("customer");
                    }
                    JsonObject customerDefaultAddressJson = null;
                    if (customerJson != null && customerJson.has("default_address") && customerJson.get("default_address").isJsonObject()) {
                        customerDefaultAddressJson = customerJson.getAsJsonObject("default_address");
                        log.debug("DEBUG - Raw customer.default_address JSON para pedido {}: {}", (order.getId() != null ? order.getId() : "N/A"), customerDefaultAddressJson.toString());
                    }

                    JsonObject saJson = null;
                    if (o.has("shipping_address") && o.get("shipping_address").isJsonObject()) {
                        saJson = o.getAsJsonObject("shipping_address");
                        log.debug("DEBUG - Raw order.shipping_address JSON para pedido {}: {}", (order.getId() != null ? order.getId() : "N/A"), saJson.toString());
                    }

                    JsonObject effectiveShippingAddress = saJson;
                    boolean shippingAddressIsBasic = (saJson != null && getString(saJson, "address1").isEmpty() && saJson.has("province") && saJson.has("country"));

                    if (effectiveShippingAddress == null || shippingAddressIsBasic) {
                        if (customerDefaultAddressJson != null) {
                            log.info("Usando customer.default_address como shipping_address para el pedido {}", order.getId());
                            effectiveShippingAddress = customerDefaultAddressJson;
                        }
                    }

                    Shipping_addressDto ship = new Shipping_addressDto();
                    if (effectiveShippingAddress != null) {
                        ship.setName(getString(effectiveShippingAddress, "name", (getString(effectiveShippingAddress,"first_name") + " " + getString(effectiveShippingAddress,"last_name")).trim()));
                        log.debug("DEBUG - Mapped Shipping Name: '{}'", ship.getName());
                        String addr1 = getString(effectiveShippingAddress, "address1");
                        ship.setStreet(addr1);
                        ship.setAddress1(addr1);
                        log.debug("DEBUG - Mapped Shipping Street/Address1: '{}'", addr1);
                        ship.setAddress2(getString(effectiveShippingAddress, "address2"));
                        log.debug("DEBUG - Mapped Shipping Address2: '{}'", ship.getAddress2());
                        ship.setCompany(getString(effectiveShippingAddress, "company"));
                        log.debug("DEBUG - Mapped Shipping Company: '{}'", ship.getCompany());
                        ship.setCity(getString(effectiveShippingAddress, "city"));
                        log.debug("DEBUG - Mapped Shipping City: '{}'", ship.getCity());
                        ship.setZip(getString(effectiveShippingAddress, "zip"));
                        log.debug("DEBUG - Mapped Shipping Zip: '{}'", ship.getZip());
                        ship.setProvince(getString(effectiveShippingAddress, "province"));
                        log.debug("DEBUG - Mapped Shipping Province: '{}'", ship.getProvince());
                        ship.setProvince_name(getString(effectiveShippingAddress, "province_code", ship.getProvince()));
                        log.debug("DEBUG - Mapped Shipping Province Name: '{}'", ship.getProvince_name());
                        ship.setCountry(getString(effectiveShippingAddress, "country"));
                        log.debug("DEBUG - Mapped Shipping Country: '{}'", ship.getCountry());
                        ship.setPhone(getString(effectiveShippingAddress, "phone"));
                        log.debug("DEBUG - Mapped Shipping Phone: '{}'", ship.getPhone());
                    } else {
                        log.warn("Pedido {} no tiene shipping_address utilizable (ni en order ni en customer.default_address). Se usará DTO vacío.", (order.getId() != null ? order.getId() : "ID no disponible"));
                    }
                    reg.setShipping_address(ship);

                    JsonObject baJson = null;
                    if (o.has("billing_address") && o.get("billing_address").isJsonObject()) {
                        baJson = o.getAsJsonObject("billing_address");
                        log.debug("DEBUG - Raw order.billing_address JSON para pedido {}: {}", (order.getId() != null ? order.getId() : "N/A"), baJson.toString());
                    }

                    JsonObject effectiveBillingAddress = baJson;
                    boolean billingAddressIsBasic = (baJson != null && getString(baJson, "address1").isEmpty() && baJson.has("province") && baJson.has("country"));

                    if (effectiveBillingAddress == null || billingAddressIsBasic) {
                         if (customerDefaultAddressJson != null) {
                            log.info("Usando customer.default_address como billing_address para el pedido {}", order.getId());
                            effectiveBillingAddress = customerDefaultAddressJson;
                        }
                    }

                    Billing_addressDto bill = new Billing_addressDto();
                    if (effectiveBillingAddress != null) {
                        bill.setName(getString(effectiveBillingAddress, "name", (getString(effectiveBillingAddress,"first_name") + " " + getString(effectiveBillingAddress,"last_name")).trim()));
                        log.debug("DEBUG - Mapped Billing Name: '{}'", bill.getName());
                        String billAddr1 = getString(effectiveBillingAddress, "address1");
                        bill.setStreet(billAddr1);
                        bill.setAddress1(billAddr1);
                        log.debug("DEBUG - Mapped Billing Street/Address1: '{}'", billAddr1);
                        bill.setAddress2(getString(effectiveBillingAddress, "address2"));
                        log.debug("DEBUG - Mapped Billing Address2: '{}'", bill.getAddress2());
                        bill.setCompany(getString(effectiveBillingAddress, "company"));
                        log.debug("DEBUG - Mapped Billing Company: '{}'", bill.getCompany());
                        bill.setCity(getString(effectiveBillingAddress, "city"));
                        log.debug("DEBUG - Mapped Billing City: '{}'", bill.getCity());
                        bill.setZip(getString(effectiveBillingAddress, "zip"));
                        log.debug("DEBUG - Mapped Billing Zip: '{}'", bill.getZip());
                        bill.setProvince(getString(effectiveBillingAddress, "province"));
                        log.debug("DEBUG - Mapped Billing Province: '{}'", bill.getProvince());
                        bill.setProvince_name(getString(effectiveBillingAddress, "province_code", bill.getProvince()));
                        log.debug("DEBUG - Mapped Billing Province Name: '{}'", bill.getProvince_name());
                        bill.setCountry(getString(effectiveBillingAddress, "country"));
                        log.debug("DEBUG - Mapped Billing Country: '{}'", bill.getCountry());
                        bill.setPhone(getString(effectiveBillingAddress, "phone"));
                        log.debug("DEBUG - Mapped Billing Phone: '{}'", bill.getPhone());
                    } else {
                        log.warn("Pedido {} no tiene billing_address utilizable (ni en order ni en customer.default_address). Se usará DTO vacío.", (order.getId() != null ? order.getId() : "ID no disponible"));
                    }
                    reg.setBilling_address(bill);

                    registros.add(reg);
                }
            }

            RegistroServiceInDto dto = new RegistroServiceInDto();
            dto.setRegistro(registros);
            dto.setIdVendor(user);
            dto.setTipoCarga(0);
            log.info("Devolviendo {} registros al frontend para el usuario {}. Payload: {}", registros.size(), user, gson.toJson(dto));
            return ResponseEntity.ok(gson.toJson(dto));

        } catch (Exception e) {
            log.error("Error general en obtenerOrders para usuario: {}. Fechas: min={}, max={}. Excepción: ", user, createdAtMin, createdAtMax, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gson.toJson(new DBResponse(false, "Error interno del servidor al consultar Shopify: " + e.getMessage())));
        }
    }
}
```

Ahora, el código en `CargaLayoutComponent.ts` para comentar los `console.log`:

```typescript
import { Component, OnInit, ViewChild } from '@angular/core';
import { NbAuthOAuth2JWTToken, NbAuthService } from '@nebular/auth';
import { NbStepperComponent, NbToastrService } from '@nebular/theme';
import { PrevisualizacionComponent } from './previsualizacion/previsualizacion.component';
import { ShopifyService } from '../../services/shopify.service';
import { RegistroService } from '../../services/registro.service';

@Component({
  selector: 'app-carga-layout',
  templateUrl: './carga-layout.component.html',
  styleUrls: ['./carga-layout.component.scss']
})
export class CargaLayoutComponent implements OnInit {

  @ViewChild(PrevisualizacionComponent)
  previsualizacion: PrevisualizacionComponent;

  @ViewChild('stepper')
  stepper: NbStepperComponent;

  user: any;

  fechaInicio: string;
  fechaFin: string;

  archivoCargado:any;
  procesado:boolean = false;
  tipoCarga: number;
  resultado: any;
  errores: any[] = [];

  constructor(
    private shopifyService: ShopifyService,
    private authService: NbAuthService,
    private registroService: RegistroService,
    private toastrService: NbToastrService
  ) { }

  ngOnInit(): void {
    this.authService.onTokenChange().subscribe((token: NbAuthOAuth2JWTToken) => {
      if (token.isValid()) {
        this.user = token.getAccessTokenPayload();
      }
    });

    const today = new Date();
    const year = today.getFullYear();
    const month = ('0' + (today.getMonth() + 1)).slice(-2);
    const day = ('0' + today.getDate()).slice(-2);

    this.fechaInicio = `${year}-${month}-${day}`;
    this.fechaFin = `${year}-${month}-${day}`;
  }

  setArchivoCargado($event: any) {
    this.archivoCargado = $event;
  }

  setProcesado($event: boolean) {
    this.procesado = $event;
  }

  setResultado($event: any) {
    this.resultado = $event;
    if ($event && $event.respuesta) {
        if ($event.registrosFallidos > 0 || ($event.registrosOmitidos > 0 && $event.registrosExitosos === 0 && $event.registrosFallidos === 0)) {
            this.errores = typeof $event.respuesta === 'string' ? $event.respuesta.split('|').map((err: string) => err.trim()) : [];
        } else {
            this.errores = [];
        }
    } else {
        this.errores = [];
    }
  }

  setErrores($event: any[]) {
    this.errores = $event;
  }

  setTipoCarga($event: number) {
    this.tipoCarga = $event;
  }

  cargarMisEnvios() {
    if (!this.user) {
      this.toastrService.show('No se pudo obtener la información del usuario.', 'Error', { status: 'danger', duration: 5000 });
      return;
    }
    if (!this.fechaInicio || !this.fechaFin) {
      this.toastrService.show('Por favor, seleccione fecha de inicio y fin.', 'Advertencia', { status: 'warning', duration: 5000 });
      return;
    }

    const vendor = this.user.user_name || this.user.name || this.user.sub;
    if (!vendor) {
        this.toastrService.show('No se pudo determinar el vendor.', 'Error', { status: 'danger', duration: 5000 });
        return;
    }

    const startDate = new Date(this.fechaInicio);
    startDate.setUTCHours(0, 0, 0, 0);
    const inicioISO = startDate.toISOString();

    const endDate = new Date(this.fechaFin);
    endDate.setUTCHours(23, 59, 59, 999);
    const finISO = endDate.toISOString();

    // console.log(`[Shopify API Carga] Solicitando pedidos para vendor: ${vendor}, inicio: ${inicioISO}, fin: ${finISO}`);
    this.toastrService.info('Obteniendo pedidos de Shopify...', 'Procesando', { duration: 3000 });
    this.procesado = true;
    this.tipoCarga = 0;
    this.resultado = null;
    this.errores = [];

    this.shopifyService.obtenerOrders(vendor, inicioISO, finISO).subscribe(
      (payloadShopify: any) => {
        // console.log('[Shopify API Carga] Respuesta de shopifyService.obtenerOrders:', JSON.stringify(payloadShopify));

        if (payloadShopify && typeof payloadShopify.registro !== 'undefined') {
          if (payloadShopify.registro.length > 0) {
            this.toastrService.info(`Se encontraron ${payloadShopify.registro.length} pedidos de Shopify. Registrando en la base de datos...`, 'Procesando', { duration: 3000 });

            this.registroService.registrarCarga(payloadShopify).subscribe(
              (resultadoCarga: any) => {
                this.setResultado(resultadoCarga);
                let toastMessage = `Carga API: ${resultadoCarga.registrosExitosos || 0} exitosos, ${resultadoCarga.registrosFallidos || 0} fallidos, ${resultadoCarga.registrosOmitidos || 0} omitidos.`;
                if (resultadoCarga.registrosFallidos > 0 || (resultadoCarga.registrosOmitidos > 0 && resultadoCarga.registrosExitosos === 0 && resultadoCarga.registrosFallidos === 0)) {
                    this.toastrService.warning(toastMessage, 'Resultado de Carga', { duration: 8000 });
                } else {
                    this.toastrService.success(toastMessage, 'Resultado de Carga', { duration: 5000 });
                }

                if (this.stepper) {
                  this.stepper.selectedIndex = 2;
                }
                this.procesado = false;
              },
              (errorRegistro: any) => {
                console.error('[Shopify API Carga] Error en registroService.registrarCarga:', errorRegistro);
                const detailError = errorRegistro.error?.respuesta || errorRegistro.error?.error || errorRegistro.error?.message || errorRegistro.message || 'Error desconocido al registrar.';
                this.toastrService.danger(`Error al registrar los pedidos: ${detailError}`, 'Error Fatal', { duration: 8000 });
                this.resultado = {
                    idCarga: null,
                    uploadDate: new Date().toISOString(),
                    registrosExitosos: 0,
                    registrosFallidos: payloadShopify.registro ? payloadShopify.registro.length : 0,
                    registrosOmitidos: 0,
                    respuesta: `Error al registrar pedidos: ${detailError}`,
                    idVendor: vendor,
                    tipoCarga: 0
                };
                this.setResultado(this.resultado);
                if (this.stepper) {
                  this.stepper.selectedIndex = 2;
                }
                this.procesado = false;
              }
            );
          } else {
            this.toastrService.warning('No se encontraron pedidos de Shopify para las fechas seleccionadas.', 'Información', { duration: 5000 });
            this.resultado = {
                idCarga: null,
                uploadDate: new Date().toISOString(),
                registrosExitosos: 0,
                registrosFallidos: 0,
                registrosOmitidos: 0,
                respuesta: 'No se encontraron pedidos de Shopify para procesar.',
                idVendor: vendor,
                tipoCarga: 0
            };
            this.setResultado(this.resultado);
            if (this.stepper) {
              this.stepper.selectedIndex = 2;
            }
            this.procesado = false;
          }
        } else {
          console.error('[Shopify API Carga] La respuesta de obtenerOrders no tiene la estructura esperada:', payloadShopify);
          this.toastrService.danger('Error al procesar la respuesta de Shopify: estructura inesperada.', 'Error', { duration: 5000 });
          this.resultado = {
              idCarga: null,
              uploadDate: new Date().toISOString(),
              registrosExitosos: 0,
              registrosFallidos: 0,
              registrosOmitidos: 0,
              respuesta: 'Error: Respuesta inesperada del servidor al obtener pedidos.',
              idVendor: vendor,
              tipoCarga: 0
            };
          this.setResultado(this.resultado);
          if (this.stepper) {
            this.stepper.selectedIndex = 2;
          }
          this.procesado = false;
        }
      },
      (errorObtenerOrders: any) => {
        console.error('[Shopify API Carga] Error en shopifyService.obtenerOrders:', errorObtenerOrders);
        const errorMessage = errorObtenerOrders.error?.responseMessage || errorObtenerOrders.error?.error || errorObtenerOrders.message || 'Error desconocido al conectar con Shopify.';
        this.toastrService.danger(`Error al conectar con Shopify: ${errorMessage}`, 'Error', { duration: 8000 });
        this.resultado = {
            idCarga: null,
            uploadDate: new Date().toISOString(),
            registrosExitosos: 0,
            registrosFallidos: 0,
            registrosOmitidos: 0,
            respuesta: `Error al conectar con Shopify: ${errorMessage}`,
            idVendor: vendor,
            tipoCarga: 0
        };
        this.setResultado(this.resultado);
        if (this.stepper) {
            this.stepper.selectedIndex = 2;
        }
        this.procesado = false;
      }
    );
  }
}
```

He aplicado las modificaciones a ambos archivos.

Con esto, hemos completado la limpieza de logs de depuración y los ajustes finales para la funcionalidad de carga de pedidos de Shopify.

Este paso del plan está completo.
