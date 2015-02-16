package com.bokwas.server.api.response;

@SuppressWarnings("unused")
public class AddPostResponse {
	private String postId;
	private APIStatus status;

	public AddPostResponse(String postId, APIStatus status) {
		super();
		this.postId = postId;
		this.status = status;
	}
}
