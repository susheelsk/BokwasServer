package com.bokwas.server.backgroundtasks;

import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.Person;
import com.bokwas.server.util.GCMSender;

public class ServerNotificationGCMTask implements Task {
	private Person receiver;
	private String message;

	public ServerNotificationGCMTask(Person person, String message) {
		this.receiver = person;
		this.message = message;
	}

	@Override
	public void execute() {
		try (Transaction txOne = BokwasDB.getDatabase().beginTx()) {
			System.out.println("in SendServerNotifications::execute()");
			Map<String, String> params = new LinkedHashMap<>();
			params.put("title", "Bokwas");
			params.put("type", "GENERIC_MESSAGE");
			params.put("message", message);
			GCMSender sender = new GCMSender(receiver.getGcmRegId(), params);
			sender.send();
			txOne.success();
		}
	}
}
