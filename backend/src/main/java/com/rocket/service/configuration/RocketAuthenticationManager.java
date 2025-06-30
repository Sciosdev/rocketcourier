package com.rocket.service.configuration;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;


import com.rocket.service.entity.RolDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.service.RolService;
import com.rocket.service.service.UsuarioService;
import com.rocket.service.utils.PasswordStorage;
import com.rocket.service.utils.PasswordStorage.CannotPerformOperationException;
import com.rocket.service.utils.PasswordStorage.InvalidHashException;



@Component
public class RocketAuthenticationManager implements AuthenticationManager {

	@Autowired
	UsuarioService usuarioService;

	@Autowired
	RolService rolService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		List<UserDto> usuario = usuarioService.consulta(username);

		if (usuario.isEmpty() || !usuario.get(0).isActivo())
			throw new BadCredentialsException(String.format("Authentication failed for %s", username));

		UserDto user = usuario.get(0);

		boolean authenticated = validate(password, user.getPassword());

		if (authenticated) {

			List<RolDto> roles = rolService.consultaRol(user.getRol());
			List<GrantedAuthority> grantedAuthorities = null;
			grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(roles.get(0).getAccessLevel());
			
			user.setLastLogin(new Date());
			try {
				usuarioService.guardarUsuario(user);
			} catch (Exception e) {
				System.out.println("Error al actualizar el último login");
			}
						
			return new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities);

		} else
			throw new BadCredentialsException(String.format("Authentication failed for %s", username));

	}

	public boolean validate(String password, String hashedPassword) {
		try {
			return PasswordStorage.verifyPassword(password, hashedPassword);
		} catch (CannotPerformOperationException | InvalidHashException e) {
			return false;
		}
	}

}
