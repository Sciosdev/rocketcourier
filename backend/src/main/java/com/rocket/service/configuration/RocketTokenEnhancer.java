package com.rocket.service.configuration;

import java.util.HashMap;
import java.util.Map;

import com.rocket.service.model.RocketUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class RocketTokenEnhancer implements TokenEnhancer {

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		Map<String, Object> additionalInfo = new HashMap<>();
		
		RocketUserDetails user = (RocketUserDetails) userDetailsService.loadUserByUsername(authentication.getName());
		
		additionalInfo.put("fullname", user.getName());
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
		return accessToken;
	}
}