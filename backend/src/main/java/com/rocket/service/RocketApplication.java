package com.rocket.service;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@ImportResource({ "classpath:rocket.cfg.xml" })
@SpringBootApplication
public class RocketApplication {

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey("-----BEGIN RSA PRIVATE KEY-----\r\n"
				+ "MIIEogIBAAKCAQEA1WqJChQjHBoBDLX033W8hQmqu204dguBqzum2AtYcop5xwuG\r\n"
				+ "BysaDPUx+SoI0MDWCT1IqPlM9rShvSWs0050U8bKZItF937v+fjWbJSjlTNZBYPT\r\n"
				+ "E+d0BSay+NLMxI+cFhKuhPhj2nbci5dwqx5+s08BQrytdn17Y813dEwNUljfHUr7\r\n"
				+ "u8OMH1ppocgN01aCe0QWQL+4aH8xm0/M9klVsXaylTX5tG55tW7Pq6QIFez9OrSC\r\n"
				+ "H2qRodzwloOs5RLJ6gYbsi+V+BBniA7QsyZo1Bxh0KoT542ySYTWHFb7OMge9Qfw\r\n"
				+ "jHwZGDLGHKXL6OmNrcLDNpTUFiPCZndlYC4WgQIDAQABAoIBABe8bptxW+pPNhis\r\n"
				+ "0BxeYuZFCfkoj6QJaZWa39adhGvQ3PR3YqO/dnlEeBdnRm25VNTYPRGWEKpLssVv\r\n"
				+ "wEYMLsH2lSITn2eEdbpOo8KV0Y/RkzAa1kobbvr3r+Yew+mEs259C238llaA68yW\r\n"
				+ "vlA55wuYWOKMS5qnICccfhGwN7cVuVSFHung4qLcCaN0YxBIk7lLy+B/z+yRwA7w\r\n"
				+ "HPLjeFqHklO1vV5YFXE1K7Q9gJotpgFOEluvRsYOQUzzNbjM6GuOSD3zv0gqLFDX\r\n"
				+ "pWv4AeNNrSH1SSHyGk8cU/mwRG//9DpbCDMedA8vw7uyhcASlwbPa0WpcUO0BdlG\r\n"
				+ "6RPtLLECgYEA6dVUpE5nHQcvrNrqjubZfsAnXtqD+adcyE7ARPh3JIUyQbWsdSkl\r\n"
				+ "+BNzQez3NSmD4i9O8E1+XzkflD3T+/MQyODJqisFgB3DTS2YiGHHXlzzdul+8XwN\r\n"
				+ "bWClF7ZzcWrUmSpdgE5wYE6mP07v74UUYZHADD1LYaqzAjrK6dKkmC0CgYEA6aW4\r\n"
				+ "ejuH5W1rBviYo74bBouKHRDCPsswyf/QeYV6+iM+Z7JVZRKFGSviBgVv/8jk3fhS\r\n"
				+ "uKNCVWnc0ScuqqiC/0yBCu4bC5C0sbnjZTx+esXY3E4qN9X7HWz6d9anuNuZiJcr\r\n"
				+ "LI7fUnTFowZjK1ttPpq2iJNW5qqdH++o40WreCUCgYBUEXiu4ek8jHrdgI9X9TM4\r\n"
				+ "2MjO120fcsZu1bFWmqXrIWAiM7jfHn4iJIwJpbJzEIvz4+JTjujiP11VJK10Jb1h\r\n"
				+ "G0Alx/XgS9MNilkOn2jYSj7V/7i+BUWmj1qLnfL/UdNBhfO7z3ejiGNI245z5Lqh\r\n"
				+ "IY+UVu0lHgPhx1Vwn2TBuQKBgD8yAamd/G3DoJUsHCz9uFWW3G5sH/3X+4RHWt3j\r\n"
				+ "ipWpfrGRfKNv1OwwAFxckCSA89ZN2iHylnh/v6gajva9yWDAEo8gQ9Rm+ViwzJ6P\r\n"
				+ "NC6E4NkFk0my6M7WsRBE0OyhCO/240iUBDdLOkGT8rLBcdTjB8f9Ah41u3Xt3qJK\r\n"
				+ "L8y5AoGAZyFbQoqrWzjVqx0l+fchOGYlAr9ElIzf8RASCJbwAWha0b6SCTJaUM6b\r\n"
				+ "z5aHf0aLcjNJAkmpz3ICEu5notM0yU0whfjWSn1TTr2djhtRsdy2kXdJ4Dc004Dz\r\n"
				+ "JyVyT5ya1X4SMLu1JrhQZ+iosKZqzQlin947ugKxkfMR8Kphvpc=\r\n"
				+ "-----END RSA PRIVATE KEY-----");
		converter.setVerifierKey("-----BEGIN PUBLIC KEY-----\r\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1WqJChQjHBoBDLX033W8\r\n"
				+ "hQmqu204dguBqzum2AtYcop5xwuGBysaDPUx+SoI0MDWCT1IqPlM9rShvSWs0050\r\n"
				+ "U8bKZItF937v+fjWbJSjlTNZBYPTE+d0BSay+NLMxI+cFhKuhPhj2nbci5dwqx5+\r\n"
				+ "s08BQrytdn17Y813dEwNUljfHUr7u8OMH1ppocgN01aCe0QWQL+4aH8xm0/M9klV\r\n"
				+ "sXaylTX5tG55tW7Pq6QIFez9OrSCH2qRodzwloOs5RLJ6gYbsi+V+BBniA7QsyZo\r\n"
				+ "1Bxh0KoT542ySYTWHFb7OMge9QfwjHwZGDLGHKXL6OmNrcLDNpTUFiPCZndlYC4W\r\n"
				+ "gQIDAQAB\r\n"
				+ "-----END PUBLIC KEY-----");
		return converter;
	}

	@Bean(name = "mtokenStore")
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return new Jackson2ObjectMapperBuilderCustomizer() {
			@Override
			public void customize(Jackson2ObjectMapperBuilder builder) {
				Locale locale = new Locale("es", "MX");
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);
				builder.dateFormat(format);
			}
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public static void main(String[] args) {
		SpringApplication.run(RocketApplication.class, args);
	}
	
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		
		CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
		config.addAllowedMethod(HttpMethod.PUT);
		
		source.registerCorsConfiguration("/**", config );
		return source;
	}
}
