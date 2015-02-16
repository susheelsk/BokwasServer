package com.bokwas.server.api.response;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;

import com.bokwas.database.Notification;
import com.bokwas.server.util.NotificationData;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class GetNotificationResponse {
	private ArrayList<Notifications> notification = new ArrayList<Notifications>();
	private APIStatus status;
	
	public GetNotificationResponse(JSONArray notificationList,String fbid,
			APIStatus status) {
		super();
		for(int i=0;i<notificationList.length();i++)
		{
			JSONObject tmpobj=(JSONObject)notificationList.getJSONObject(i);
			notification.add(new Notifications(tmpobj.getString("notification_id"),tmpobj.getJSONObject("notification_data").toString()));
		}
		this.status = status;
	}

	private class Notifications {
		@Expose
		private String notification_id;
		private String notification_data;
		
		public Notifications(String notification_id, String notification_data) {
			super();
			this.notification_id = notification_id;
			this.notification_data = notification_data;
		
		}
	}
}
