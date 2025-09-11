package com.rocket.service.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.mapper.RegistroMapper;
import com.rocket.service.model.RegistroServiceOutDto;
import com.rocket.service.repository.RegistroRepository;
import com.rocket.service.utils.OrderSource;

import org.bson.types.ObjectId;
import org.jboss.logging.Logger; // o import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class RegistroService {

	private final static Logger logger = Logger.getLogger(RegistroService.class); // o private static final Logger logger = LoggerFactory.getLogger(RegistroService.class);

	@Autowired
	RegistroRepository repoRegis;

	@Autowired
	EstatusService estatusService;

	@Autowired
	UsuarioService usuarioService;

	@Autowired
	CargaService cargaService;

	@Autowired
	VendorService vendorService;

	private MongoOperations mongoOperations;

	@Autowired
	public RegistroService(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	public RegistryDto guardar(RegistryDto regis) {
		return repoRegis.save(regis);
	}

	public List<RegistryDto> listaRegistro() {
		return repoRegis.findAll();
	}

        public RegistryDto buscarPorOrderKey(ObjectId orderKey) {
                return repoRegis.findByOrderKey(orderKey);
        }

        public boolean eliminarRegistro(ObjectId orderKey) {
                RegistryDto registro = repoRegis.findByOrderKey(orderKey);
                if (registro != null) {
                        repoRegis.delete(registro);
                        return true;
                }
                return false;
        }

	public List<RegistryDto> consultaRegistroCargaEstatus(Long idCarga, Integer idEstatus, String courier) {
		List<RegistryDto> registros = new ArrayList<>();
		if (courier == null || courier.isEmpty()) {
			registros = mongoOperations.find(
					Query.query(Criteria.where("idCarga").is(idCarga).and("idEstatus").is(idEstatus)),
					RegistryDto.class);
		} else {
			registros = mongoOperations.find(Query.query(Criteria.where("idCarga").is(idCarga).and("idEstatus")
					.is(idEstatus).and("scheduled.idCourier").is(courier)), RegistryDto.class);
		}

		return registros;
	}

	public List<RegistryDto> consultaRegistrosCourier(String courier) {
		List<RegistryDto> registros = new ArrayList<>();
		if (courier != null && !courier.isEmpty()) {
			registros = mongoOperations.find(Query.query(Criteria.where("scheduled.idCourier").is(courier)),
					RegistryDto.class);
		}
		return registros;
	}

	public List<String> consultaComunas() {

		List<String> comunasDuplicates = new ArrayList<>();
		List<String> comunasNoDuplicates = new ArrayList<>();

		List<String> comunas = mongoOperations.findDistinct("shipping_address.city", RegistryDto.class, String.class);

		comunas.forEach(comuna -> {
			comunasDuplicates.add(comuna.trim().toUpperCase());
		});

		comunasNoDuplicates = new ArrayList<>(new HashSet<>(comunasDuplicates));

		return comunasNoDuplicates;
	}

	public List<RegistryDto> consultaRegistroPorCourierYEstatus(String courier, Integer idEstatus) {

		List<RegistryDto> registros = new ArrayList<>();

		if (courier != null) {
			registros = mongoOperations.find(
					Query.query(Criteria.where("scheduled.idCourier").is(courier).and("idEstatus").is(idEstatus)),
					RegistryDto.class);
		} else {
			registros = mongoOperations.find(
					Query.query(Criteria.where("idEstatus").is(idEstatus)),
					RegistryDto.class);
		}

		return registros;
	}

	public List<RegistryDto> consultaRegistroPorCourierEstatusYComuna(String courier, Integer idEstatus,
			String comuna) {

		List<RegistryDto> registros = mongoOperations.find(Query.query(Criteria.where("scheduled.idCourier").is(courier)
				.and("idEstatus").is(idEstatus).and("shipping_address.city").is(comuna)), RegistryDto.class);

		return registros;
	}

	public List<RegistryDto> consultaRegistroPorCourier(String courier) {

		List<RegistryDto> registros = mongoOperations
				.find(Query.query(Criteria.where("scheduled.idCourier").is(courier)), RegistryDto.class);

		return registros;
	}

	public RegistryDto consultaRegistroPorCourierYOrderkey(String courier, String orderKey) {

		ObjectId order = new ObjectId(orderKey);
		List<RegistryDto> registros = mongoOperations
				.find(Query.query(Criteria.where("scheduled.idCourier").is(courier).and("order.orderKey").is(order)),
						RegistryDto.class);

		if (registros.isEmpty())
			return null;
		else
			return registros.get(0);
	}

        public List<RegistroServiceOutDto> consultaRegistro(Date fDate, Date tDate, List<String> customer,
                        Integer idEstatus,
                        String courier) {

		Calendar toDate = Calendar.getInstance();
		toDate.setTime(tDate);
		toDate.set(Calendar.HOUR_OF_DAY, 23);
		toDate.set(Calendar.MINUTE, 59);
		toDate.set(Calendar.SECOND, 59);

		Calendar fromDate = Calendar.getInstance();
		fromDate.setTime(fDate);
		fromDate.set(Calendar.HOUR_OF_DAY, 0);
		fromDate.set(Calendar.MINUTE, 0);
		fromDate.set(Calendar.SECOND, 0);

		logger.debugf("Consulta de registros: Desde %s Hasta %s, Estatus: %d, Courier: %s, Customer: %s",
            fromDate.getTime(), toDate.getTime(), idEstatus, courier, customer);


		List<RegistroServiceOutDto> registros = new ArrayList<>();

		if (customer != null && !customer.isEmpty()) {
			List<LoadDto> cargas = mongoOperations.find(Query.query(Criteria.where("uploadDate").gte(fromDate.getTime())
					.lte(toDate.getTime()).and("idVendor").in(customer)), LoadDto.class);
			cargas.forEach(carga -> {
				List<RegistryDto> temp = consultaRegistroCargaEstatus(carga.getIdCarga(), idEstatus, courier);
				temp.forEach(registro -> {
					registros.add(RegistroMapper.mapRegistroCargaToRegistroOut(registro, carga, usuarioService,
							vendorService, estatusService));
				});
			});
		} else {
			List<LoadDto> cargas = mongoOperations.find(
					Query.query(Criteria.where("uploadDate").gte(fromDate.getTime()).lte(toDate.getTime())),
					LoadDto.class);
			cargas.forEach(carga -> {
				List<RegistryDto> temp = consultaRegistroCargaEstatus(carga.getIdCarga(), idEstatus, courier);
				temp.forEach(registro -> {
					registros.add(RegistroMapper.mapRegistroCargaToRegistroOut(registro, carga, usuarioService,
							vendorService, estatusService));
				});
			});
		}

                return registros;
        }

        public void normalize(RegistryDto regis) {
                if (regis == null || regis.getShipping_address() == null) {
                        return;
                }

                String zip = regis.getShipping_address().getZip();
                if (zip == null || zip.replaceAll("[-'\"\\s]", "").isEmpty()) {
                        regis.getShipping_address().setZip(null);
                }

                String street = regis.getShipping_address().getStreet();
                String address1 = regis.getShipping_address().getAddress1();
                if ((street == null || street.trim().isEmpty()) && address1 != null && !address1.trim().isEmpty()) {
                        regis.getShipping_address().setStreet(address1);
                }
        }

        public String validacion(RegistryDto regis) {
                StringBuilder mensaje = new StringBuilder();

        if (regis.getOrder() == null) {
            mensaje.append("Datos del pedido no pueden ser nulos. ");
        } else {
            if (regis.getOrder().getName() == null || regis.getOrder().getName().trim().isEmpty()) {
                mensaje.append("Order Name: es un campo requerido. ");
            }
            if (regis.getOrder().getFinancial_status() == null || regis.getOrder().getFinancial_status().trim().isEmpty()) {
                mensaje.append("Financial_status: es un campo requerido. ");
            }
        }

        if (regis.getShipping_address() == null) {
            mensaje.append("Shipping Address: no puede ser nulo. ");
        } else {
            if (regis.getShipping_address().getName() == null || regis.getShipping_address().getName().trim().isEmpty()) {
                mensaje.append("Shipping Name: es un campo requerido. ");
            }
            if ((regis.getShipping_address().getStreet() == null || regis.getShipping_address().getStreet().trim().isEmpty())
                    && (regis.getShipping_address().getAddress1() == null || regis.getShipping_address().getAddress1().trim().isEmpty())) {
                mensaje.append("Shipping Street o Address1: al menos uno de los campos debe estar presente. ");
            }
            if (regis.getShipping_address().getCity() == null || regis.getShipping_address().getCity().trim().isEmpty()) {
                mensaje.append("Shipping City: es un campo requerido. ");
            }
            if (regis.getShipping_address().getProvince() == null || regis.getShipping_address().getProvince().trim().isEmpty()) {
                mensaje.append("Shipping Province: es un campo requerido. ");
            }
            if (regis.getShipping_address().getZip() != null && regis.getShipping_address().getZip().trim().isEmpty()) {
                mensaje.append("Shipping Zip: es opcional, pero si se proporciona no debe estar vacío. ");
            }
            if (regis.getShipping_address().getPhone() == null || regis.getShipping_address().getPhone().trim().isEmpty()) {
                mensaje.append("Shipping Phone: es un campo requerido. ");
            }
            if (regis.getShipping_address().getCountry() == null || regis.getShipping_address().getCountry().trim().isEmpty()) {
                mensaje.append("Shipping Country: es un campo requerido. ");
            }
        }
		return mensaje.toString().trim();
	}

    public boolean existePedidoShopify(String shopifyOrderId, String idVendorDeCarga) {
        Query query = new Query();
        // Comparamos el ID del pedido de Shopify (almacenado en order.id)
        // y el idVendor (almacenado en order.vendor, que se llena con el idVendor de la carga).
        query.addCriteria(Criteria.where("order.id").is(shopifyOrderId)
                              .and("order.vendor").is(idVendorDeCarga)
                              .and("order.source").is(OrderSource.SHOPIFY.getValue()));
        logger.debugf("Verificando si existe pedido Shopify con ID: %s y Vendor: %s", shopifyOrderId, idVendorDeCarga);
        boolean exists = mongoOperations.exists(query, RegistryDto.class);
        logger.debugf("Pedido Shopify con ID: %s y Vendor: %s ¿existe?: %s", shopifyOrderId, idVendorDeCarga, exists);
        return exists;
    }
}
