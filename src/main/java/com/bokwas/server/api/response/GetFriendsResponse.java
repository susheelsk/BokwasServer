package com.bokwas.server.api.response;

import java.util.ArrayList;
import java.util.List;

import com.bokwas.database.Person;
import com.google.gson.annotations.Expose;

public class GetFriendsResponse {
	List<Friends>bokwasfriends=new ArrayList<Friends>();
	public GetFriendsResponse(List<Person>friends,APIStatus status)
	{
		
		for(Person friend:friends)
		{
		bokwasfriends.add(new Friends(friend.getBokwasName(),friend.getBokwasAvatarId(),friend.getFbName(),friend.getFbId()));	
		}
	}
	private class Friends
	{
		
		@Expose
		private String bokwas_name;
		@Expose
		private String avatar_id;
		@Expose
		private String fbname;
		@Expose
		private String fbid;
		
		public Friends(String bokwasName,String avatarid,String facebook_name,String fbid)
		{
			
			this.bokwas_name=bokwasName;
			this.avatar_id=avatarid;
			this.fbname=facebook_name;
			this.fbid=fbid;
		}
		
	}
	
}
