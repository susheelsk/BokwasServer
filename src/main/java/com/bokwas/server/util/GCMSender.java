package com.bokwas.server.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

public class GCMSender {

	private String regId;
	private List<String> regIdList;
	private Map<String, String> params;
	private static final String GOOGLE_SERVER_KEY = "AIzaSyAfaESLACPriKKwf9W3hfZ4v95JdBr9gUo";

	public GCMSender(String regId, Map<String, String> params) {
		this.regId = regId;
		this.params = params;
	}

	public GCMSender(List<String> regIdList, Map<String, String> params) {
		this.regIdList = regIdList;
		this.params = params;
	}

	public void send() {
		System.out.println("Sending GCM");
		Result result = null;
		Builder messageBuilder = new Message.Builder().timeToLive(
				14 * 24 * 60 * 60).delayWhileIdle(true);
		for (Map.Entry<String, String> entry : params.entrySet()) {
			messageBuilder.addData(entry.getKey(), (String) entry.getValue());
		}
		Message message = messageBuilder.build();
		if (regId != null) {
			try {
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				result = sender.send(message, regId, 3);
				System.out.println("Sent GCM");
				System.out.println("GcmNotification and result"
						+ result.toString());
			} catch (IOException ioe) {
				System.out.println(ioe.getLocalizedMessage());
				ioe.printStackTrace();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		} else if (regIdList != null) {
			try {
				Set<String> set = new HashSet<String>();
				set.addAll(regIdList);
				regIdList.clear();
				regIdList.addAll(set);
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				MulticastResult multicastResult = sender
						.send(message, regIdList, 3);
				System.out.println("Sent GCM");
				System.out.println("GcmNotification and result"
						+ multicastResult.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
