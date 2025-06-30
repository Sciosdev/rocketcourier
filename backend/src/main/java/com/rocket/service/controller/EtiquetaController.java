package com.rocket.service.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.itextpdf.text.DocumentException;
import com.rocket.service.entity.RegistryDto;
import com.rocket.service.service.EtiquetaPdfService;
import com.rocket.service.service.RegistroService;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController @Slf4j
public class EtiquetaController {

	@Autowired
	RegistroService registroService;

	@Autowired
	EtiquetaPdfService etiquetaPdfService;

	@RequestMapping(value = "/api/registro/{orderkey}/etiqueta", method = RequestMethod.GET, produces = {
			"application/pdf" })
	public ResponseEntity<byte[]> generatePdf(@PathVariable String orderkey) throws DocumentException, IOException {

		RegistryDto registro = registroService.buscarPorOrderKey(new ObjectId(orderkey));

		if (registro != null) {
			byte[] response;
			response = etiquetaPdfService.generaPdf(registro, "plantillas/etiqueta_3.0.pdf");

			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
		}
	}

	@RequestMapping(value = "/api/registro/etiqueta", method = RequestMethod.GET, produces = { "application/zip" })
	public ResponseEntity<byte[]> generateMultiplePdf(@RequestParam String[] orderkeys)
			throws DocumentException, IOException {

		ByteArrayOutputStream baos = null;

		baos = new ByteArrayOutputStream();

		ZipOutputStream zos = new ZipOutputStream(baos);

		for (String orderkey : orderkeys) {
			RegistryDto registro = registroService.buscarPorOrderKey(new ObjectId(orderkey));

			if (registro != null) {
				byte[] response;
				response = etiquetaPdfService.generaPdf(registro, "plantillas/etiqueta_3.0.pdf");
				ZipEntry entry = new ZipEntry(orderkey.toUpperCase() + ".pdf");

				entry.setSize(response.length);

				zos.putNextEntry(entry);
				zos.write(response);
				zos.closeEntry();
			}

		}

		try {
			zos.close();
		} catch (IOException e) {
			log.error( e.getMessage());
		}

		return new ResponseEntity<>(baos.toByteArray(), HttpStatus.OK);
	}
}
