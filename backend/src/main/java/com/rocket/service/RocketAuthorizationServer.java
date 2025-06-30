package com.rocket.service;

import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import com.rocket.service.configuration.RocketAuthenticationManager;
import com.rocket.service.configuration.RocketTokenEnhancer;

@Configuration
@EnableAuthorizationServer
public class RocketAuthorizationServer extends AuthorizationServerConfigurerAdapter {

	@Autowired
	@Qualifier("mtokenStore")
	private TokenStore tokenStore;

	@Autowired
	private AccessTokenConverter accessTokenConverter;

	@Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;


	@Bean
	@Primary
	public ResourceServerTokenServices tokenServices() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setSupportRefreshToken(true);
		tokenServices.setTokenStore(this.tokenStore);
		return tokenServices;
	}

	@Bean
	public TokenEnhancer tokenEnhancer() {
		return new RocketTokenEnhancer();
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), (JwtAccessTokenConverter) accessTokenConverter));

		endpoints.tokenStore(tokenStore).authenticationManager(authenticationManager)
				.userDetailsService(userDetailsService).tokenEnhancer(tokenEnhancerChain);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer securityConfigurer) throws Exception {
		securityConfigurer.checkTokenAccess("isAuthenticated()");
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

		clients.inMemory().withClient("foo").secret(passwordEncoder.encode("foosecret"))
				.resourceIds(RocketResourceServer.RESOURCE_ID)
				.authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit", "check_token")
				.authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT").scopes("read", "write", "trust")
				.accessTokenValiditySeconds(30).refreshTokenValiditySeconds(43200).and()

				.withClient("rocketApp").secret(passwordEncoder.encode("mE8_vt2852d_f@B"))
				.resourceIds(RocketResourceServer.RESOURCE_ID)
				.authorizedGrantTypes("password", "refresh_token", "check_token")
				.authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT").scopes("read", "write", "trust")
				.accessTokenValiditySeconds(1800).refreshTokenValiditySeconds(43200);
	}
}
