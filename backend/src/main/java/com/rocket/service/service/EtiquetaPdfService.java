package com.rocket.service.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.rocket.service.entity.LoadDto;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.entity.VendorDto;
import com.rocket.service.entity.UserDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 *
 * @author Raul Eduardo Martinez Chavez <b>&lt;raul.zevahc@gmail.com&gt; </b>
 *
 */
@Service
public class EtiquetaPdfService {

	@Autowired
	CargaService cargaService;

	@Autowired
	UsuarioService usuarioService;

	@Autowired
	VendorService vendorService;

	public byte[] generaPdf(RegistryDto registro, String templatePath) throws DocumentException, IOException {

		LoadDto carga = cargaService.obtenerCargaPorId(registro.getIdCarga());
		UserDto usuario = usuarioService.consulta(carga.getIdVendor()).get(0);

		VendorDto tienda = vendorService.obtenerTiendaPorId(usuario.getTienda());

		InputStream resource = new ClassPathResource(templatePath).getInputStream();

		PdfReader reader;

		reader = new PdfReader(resource);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfStamper stamper = new PdfStamper(reader, baos);
		PdfContentByte cb = stamper.getOverContent(1);

		BarcodeQRCode barcodeQRCode = new BarcodeQRCode(registro.getOrder().getOrderKey().toHexString(), 130, 130,
				null);
		Image codeQrImage = barcodeQRCode.getImage();
		codeQrImage.setAbsolutePosition(72, 270);
		codeQrImage.setScaleToFitHeight(false);

		cb.addImage(codeQrImage);

		Image logo = null;
		if (tienda.getLogo() != null && tienda.getLogo().getData() != null)
			logo = Image.getInstance(tienda.getLogo().getData());
		else {
			URL noImage = new ClassPathResource("images/no-image.jpg").getURL();
			logo = Image.getInstance(noImage);
		}

		logo.setAbsolutePosition(75, 450);
		logo.scaleAbsolute(60, 60);

		cb.addImage(logo);

		AcroFields form = stamper.getAcroFields();

		form.setField("orderkey", registro.getOrder().getOrderKey().toHexString().toUpperCase());
		form.setField("nombre", tienda.getNombreTienda());
		form.setField("direccion", tienda.getEmail());
		form.setField("telefono", tienda.getTelefono());
		form.setField("nombre_dest", registro.getShipping_address().getName());
		form.setField("direccion_dest",
				registro.getShipping_address().getAddress1() + " " + registro.getShipping_address().getAddress2());
		form.setField("telefono_dest", registro.getShipping_address().getPhone());
		form.setField("comuna_dest", registro.getShipping_address().getCity());
		form.setField("ciudad_dest", registro.getShipping_address().getProvince_name());
		form.setField("pedido", registro.getOrder().getName());

		stamper.setFormFlattening(true);
		stamper.close();
		reader.close();

		byte[] result = baos.toByteArray();

		return result;
	}

}
