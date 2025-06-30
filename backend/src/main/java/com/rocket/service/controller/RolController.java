package com.rocket.service.controller;

import java.util.List;

import com.google.gson.Gson;
import com.rocket.service.entity.RolDto;
import com.rocket.service.model.DBResponse;
import com.rocket.service.service.RolService;
import com.rocket.service.utils.RoleName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RolController {

    @Autowired
    RolService service;

    @RequestMapping(value = "rol/{rolName}", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public ResponseEntity<String> getRol(@PathVariable String rolName) {

        List<RolDto> roles = service.consultaRol(rolName);
        Gson gson = new Gson();

        if(roles.isEmpty()){
            DBResponse response = new DBResponse(false, "No se encontro el rol");
            return new ResponseEntity<>(gson.toJson(response), HttpStatus.BAD_REQUEST);
        }

        RolDto rol = roles.get(0);

        rol.setAccessLevel(null);
        rol.setStatusChange(null);
        rol.setStatusView(null);

        return new ResponseEntity<>(gson.toJson(rol), HttpStatus.OK);
    }

    @RequestMapping(value = "rol/", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
    public ResponseEntity<String> getRol() {

        List<RolDto> roles = service.consultaRol();

        if (roles.isEmpty())
            return null;

        RolDto root = new RolDto();

        roles.forEach(rol -> {
            rol.setAccessLevel(null);
            rol.setStatusChange(null);
            rol.setStatusView(null);

            if (rol.getRol().equals(RoleName.ROOT.getValue())) {
                root.setId(rol.getId());
                root.setRol(rol.getRol());
                root.setAccessLevel(rol.getAccessLevel());
                root.setStatusView(rol.getStatusView());
                root.setStatusChange(rol.getStatusChange());
                root.setVendorAssignment(rol.isVendorAssignment());
            }
        });

        if (root.getId() != null || !root.getId().isEmpty())
            roles.remove(root);

        Gson gson = new Gson();
        return new ResponseEntity<>(gson.toJson(roles), HttpStatus.OK);
    }

}
