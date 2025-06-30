package com.rocket.service.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import com.rocket.service.model.DBResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SecurityTokenManagementController {

	@Autowired @Qualifier("tokenServices")
	private ResourceServerTokenServices resourceServerTokenServices;

	@Autowired @Qualifier("mtokenStore")
	private TokenStore tokenStore;

	@RequestMapping(value = "/security/revoke_token", method = RequestMethod.DELETE,produces=MediaType.APPLICATION_JSON_VALUE)
	public DBResponse revoke_token(Principal principal, HttpServletRequest request) {
		DBResponse response = new DBResponse();

		try{
			OAuth2RefreshToken token = null;
			 final String authorizationHeaderValue = request.getHeader("refresh_token");
			    if (authorizationHeaderValue != null) {
			      String token_str = authorizationHeaderValue;
			       token = tokenStore.readRefreshToken(token_str);
			    }
			if(token != null)
			{
				tokenStore.removeRefreshToken(token);
			}
			
			response.setResponse(true);
			response.setResponseMessage(String.format("The token [%s] was removed", token.getValue()));
		}catch(Exception e)
		{
			response.setResponse(false);
			response.setResponseMessage(e.getMessage());
		}
		
		return response;
	}
	protected String getClientId(Principal principal) {
		Authentication client = (Authentication) principal;
		if (!client.isAuthenticated()) {
			throw new InsufficientAuthenticationException("The client is not authenticated.");
		}
		String clientId = client.getName();
		if (client instanceof OAuth2Authentication) {
			// Might be a client and user combined authentication
			clientId = ((OAuth2Authentication) client).getOAuth2Request().getClientId();
		}
		
		System.out.println(clientId);
		return clientId;
	}
}
