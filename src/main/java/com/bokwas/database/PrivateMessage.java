package com.bokwas.database;

import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.UniqueFactory;

public class PrivateMessage {

	static final String MESSAGE_TEXT = "messagetext";
	static final String MESSAGE_TIME = "messagetime";
	static final String MESSAGE_ID = "messageid";
	static final String MESSAGE_FROM = "messagefrom";
	private final Node underlyingNode;

	public PrivateMessage(Node userNode) {
		this.underlyingNode = userNode;
	}

	public PrivateMessage(String messageText, long messageTime,
			String messageId, String messageFromId) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(
					BokwasDB.getDatabase(), "Comment") {
				@Override
				protected void initialize(Node created,
						Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label("comments"));
					created.setProperty(MESSAGE_ID, properties.get(MESSAGE_ID));
				}
			};
			Node node = factory.getOrCreate(MESSAGE_ID, messageId);
			node.setProperty(MESSAGE_TEXT, messageText);
			node.setProperty(MESSAGE_TIME, messageTime);
			node.setProperty(MESSAGE_FROM, messageFromId);

			tx.success();
			this.underlyingNode = node;
		}
	}
	
	public String getMessageText() {
		return (String) underlyingNode.getProperty(MESSAGE_TEXT);
	}

	public long getMessageTime() {
		return (long) underlyingNode.getProperty(MESSAGE_TIME);
	}

	public String getMessageId() {
		return (String) underlyingNode.getProperty(MESSAGE_ID);
	}

	public String getMessageFrom() {
		return (String) underlyingNode.getProperty(MESSAGE_FROM);
	}

	public Node getUnderlyingNode() {
		return underlyingNode;
	}
	
	public void setMessageText(String messageText) {
		underlyingNode.setProperty(MESSAGE_TEXT, messageText);
	}
	
	public void setMessageTime(long messageTime) {
		underlyingNode.setProperty(MESSAGE_TIME, messageTime);
	}
	
	public void messageFromId(String messageFromId) {
		underlyingNode.setProperty(MESSAGE_FROM, messageFromId);
	}
	
	public void messageId(String messageId) {
		underlyingNode.setProperty(MESSAGE_ID, messageId);
	}
}
