package com.rocket.service.configuration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

public class RocketClientDetailsService implements ClientDetailsService {

	@Override
	public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {

	    String clientSecret = "";

		Set<String> scope = Collections.emptySet();

		Set<String> resourceIds = Collections.emptySet();
		Set<String> authorizedGrantTypes = Collections.emptySet();

		List<GrantedAuthority> authorities = Collections.emptyList();
		Integer accessTokenValiditySeconds = 0;
		Integer refreshTokenValiditySeconds = 0;
		if (clientId.equals("foo")) {
			scope.add("read");
			scope.add("write");
			scope.add("trust");
			
			
			resourceIds.add("rocket_bpm");
			
			authorizedGrantTypes.add("password");
			authorizedGrantTypes.add("authorization_code");
			authorizedGrantTypes.add("refresh_token");
			authorizedGrantTypes.add("implicit");
			authorizedGrantTypes.add("check_token");
			
			
			authorities.add(new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT"));
			authorities.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
			
			accessTokenValiditySeconds = 600;
			refreshTokenValiditySeconds = 6000;
			
			clientSecret = "foosecret";
			
		}
		BaseClientDetails detalle = new BaseClientDetails();
		detalle.setClientId(clientId);
		detalle.setScope(scope);
		detalle.setResourceIds(resourceIds);
		detalle.setAuthorizedGrantTypes(authorizedGrantTypes);
		detalle.setAuthorities(authorities);
		detalle.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
		detalle.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
		detalle.setClientSecret(clientSecret);
		return detalle;
	}

}
