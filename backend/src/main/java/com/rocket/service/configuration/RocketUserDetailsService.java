package com.rocket.service.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.rocket.service.entity.RolDto;
import com.rocket.service.entity.UserDto;
import com.rocket.service.model.RocketUserDetails;
import com.rocket.service.service.RolService;
import com.rocket.service.service.UsuarioService;

public class RocketUserDetailsService implements UserDetailsService {
	@Autowired
	UsuarioService usuarioService;

	@Autowired
	RolService rolService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		List<UserDto> usuario = usuarioService.consulta(username);

		if (usuario.isEmpty())
			throw new BadCredentialsException(String.format("Authentication failed for %s", username));

		UserDto user = usuario.get(0);
		List<RolDto> roles = rolService.consultaRol(user.getRol());
		List<GrantedAuthority> grantedAuthorities = null;
		grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(roles.get(0).getAccessLevel());

		return new RocketUserDetails(username, user.getName(), "", grantedAuthorities, null);
	}

}
