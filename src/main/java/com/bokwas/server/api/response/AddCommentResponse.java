package com.bokwas.server.api.response;

@SuppressWarnings("unused")
public class AddCommentResponse {
	private String commentId;
	private APIStatus status;

	public AddCommentResponse(String commentId, APIStatus status) {
		super();
		this.commentId = commentId;
		this.status = status;
	}

}
