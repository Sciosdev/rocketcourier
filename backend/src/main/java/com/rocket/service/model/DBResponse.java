/**
 * 
 */
package com.rocket.service.model;

/**
 * @author Raúl Eduardo Martínez Chávez
 *
 */
public class DBResponse {
	
	private Boolean response;
	private String responseMessage;
	
	public DBResponse() {
		this.response = false;
		this.responseMessage = null;
	}
	
	public DBResponse(Boolean response, String responseMessage) {
		this.response = response;
		this.responseMessage = responseMessage;
	}
	
	/**
	 * @return the response
	 */
	public Boolean getResponse() {
		return response;
	}
	/**
	 * @param response the response to set
	 */
	public void setResponse(Boolean response) {
		this.response = response;
	}
	/**
	 * @return the responseMessage
	 */
	public String getResponseMessage() {
		return responseMessage;
	}
	/**
	 * @param responseMessage the responseMessage to set
	 */
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("DBResponse [response=%s, responseMessage=%s]", response, responseMessage);
	}
}
