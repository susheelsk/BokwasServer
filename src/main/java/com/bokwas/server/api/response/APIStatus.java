package com.bokwas.server.api.response;

@SuppressWarnings("unused")
public class APIStatus {

	private int statusCode;
	private String message;
	
	public enum ERROR_CODE {
		AUTH_MISSING,
		STANDARD_SERVER_ERROR,
		BAD_REQUEST,
		PERSON_NOT_FOUND,
		POST_NOT_FOUND,
		INACTIVE
	}

	public APIStatus(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
	
	public APIStatus(ERROR_CODE error) {
		switch(error) {
		case AUTH_MISSING:
			this.statusCode = 401;
			this.message = "No Authorization. Check access token.";
			break;
		case STANDARD_SERVER_ERROR:
			this.statusCode = 500;
			this.message = "Oops. Something went wrong!";
			break;
		case BAD_REQUEST:
			this.statusCode = 400;
			this.message = "Bad Request. Please check the input params.";
			break;
		case PERSON_NOT_FOUND:
			this.statusCode=404;
			this.message="Person not found";
			break;
		case POST_NOT_FOUND:
			this.statusCode=404;
			this.message = "Post not found";
			break;
		case INACTIVE:
			this.statusCode = 403;
			break;
		default:
			break;
		}
		
	}

}
