package com.bokwas.server.api.response;

import java.util.ArrayList;
import java.util.List;

import com.bokwas.database.PrivateMessage;
import com.google.gson.annotations.Expose;

public class GetPrivateMessagesResponse {
	List<Message> messages = new ArrayList<Message>();
	APIStatus status;

	public GetPrivateMessagesResponse(List<PrivateMessage> messages, APIStatus status) {

		for (PrivateMessage message : messages) {
			this.messages.add(new Message(message.getMessageId(), message.getMessageText(), message.getMessageFrom(), String.valueOf(message
					.getMessageTime())));
		}
		this.status = status;
	}

	private class Message {
		@Expose
		private String messageId;
		@Expose
		private String messageText;
		@Expose
		private String messageFromId;
		@Expose
		private String messageTime;

		public Message(String messageId, String messageText, String messageFromId, String messageTime) {
			super();
			this.messageId = messageId;
			this.messageText = messageText;
			this.messageFromId = messageFromId;
			this.messageTime = messageTime;
		}

	}
}
