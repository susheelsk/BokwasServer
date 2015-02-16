package com.bokwas.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.UniqueFactory;
public class Notification {
	static final String FBID = "fbid";
	static final String NOTIFICATION_DATA = "notificationdata";
	static final String NOTIFICATION_TIME = "notificationtime";
	static final String NOTIFICATION_INDEX = "Notification";
	private String dbnotifications="";	

	
	private final Node underlyingNode;
	
	public Notification(Node node) {
		this.underlyingNode = node;
	}
	
	public Notification(String fbid) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(
					BokwasDB.getDatabase(), "Notification") {
				@Override
				protected void initialize(Node created,
						Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label("notification"));
					created.setProperty(FBID, properties.get(FBID));
				}
			};
			Node node = factory.getOrCreate(FBID, fbid);
			tx.success();
		this.underlyingNode = node;
		}
	}
	public void setNotificationData(String data)
	{
		  DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		   Date date = new Date();
		   String yourDate = dateFormat.format(date);
		   try{
		   String notifications=underlyingNode.getProperty(yourDate).toString();
		   notifications=notifications+","+data;
		   underlyingNode.setProperty(yourDate, notifications);
		   }
		   catch(Exception e)
		   {
			   underlyingNode.setProperty(yourDate,(Object)data);
				     
		   }
	}
	public void getNotificationData()
	{
		
	//		String notifications = getMoreNotifications(0);
//		String tmp="{\"notifications\": ["+notifications+"]}";
//		JSONObject see=new JSONObject(tmp);
//		JSONArray arr=see.getJSONArray("notifications");
//		
//			return arr;
	}
	@SuppressWarnings("unused")
	private String getMoreNotifications(int i)
	{
		///i=0 beginning from today
		if(i>0)
			return "";
		else if(i<-7) //i=-7, for past one week
		{
			if(dbnotifications.equals(""))
				return dbnotifications;
			return dbnotifications.substring(0,dbnotifications.length()-1);
		}
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, i);
		String yourDate = dateFormat.format(cal.getTime());
		try {
			dbnotifications+=underlyingNode.getProperty(yourDate).toString()+",";
			String tmp="{\"notifications\": ["+dbnotifications.substring(0,dbnotifications.length()-1)+"]}";
			JSONObject see=new JSONObject(tmp);
			JSONArray arr=see.getJSONArray("notifications");
				if(arr.length()<10&&i>-7)
					{
						return getMoreNotifications(--i);
					}
				else
				{
					return dbnotifications.substring(0,dbnotifications.length()-1);
				}
			}
			catch (Exception e) {
				return getMoreNotifications(--i);
			}
	}
	public Node getUnderlyingNode() {
		return underlyingNode;
	}
	
	@Override
	public int hashCode() {
		return underlyingNode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Comments
				&& underlyingNode.equals(((Comments) o).getUnderlyingNode());
	}

	
}
