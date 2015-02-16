package com.bokwas.server.api.response;

import java.util.HashMap;

import com.google.gson.annotations.Expose;

public class PersonInfoResponse {
	@Expose
	private HashMap<String,String> person_details;
	@Expose	
	private APIStatus status;

	public PersonInfoResponse(HashMap<String,String> me,APIStatus status) {
		this.person_details=me;
		this.status=status;
	}
}
