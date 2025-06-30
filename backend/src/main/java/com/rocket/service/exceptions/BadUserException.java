package com.rocket.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason="El usuario no es válido")
public class BadUserException extends RuntimeException {

	
}
