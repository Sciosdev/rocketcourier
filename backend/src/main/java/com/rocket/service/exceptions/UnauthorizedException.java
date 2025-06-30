package com.rocket.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="You don't have enoght permissions")
public class UnauthorizedException extends RuntimeException {

	private static final long serialVersionUID = -3884466259450448653L;
	
}
