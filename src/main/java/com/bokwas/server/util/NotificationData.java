package com.bokwas.server.util;

import com.google.gson.annotations.Expose;

public class NotificationData {
	
@Expose
private String title;
@Expose
private String notificationId;

@Expose
private String type;
@Expose
private String message;
@Expose
private String likesPersonId;
@Expose
private String likesPersonName;
@Expose
private String postId;
@Expose
private String avatarId;
@Expose
private String commentText;
@Expose
private String commentId;
@Expose
private String commentTime;
@Expose
private String commentPersonBokwasName;
@Expose
private String commentPersonId;

public String getNotificationId()
{
	return notificationId;
}



}
