package com.bokwas.facebook;

import com.restfb.Facebook;

public class FQLPost {

	@Facebook
	String post_id;
	
	@Facebook
	String created_time;
	
	@Facebook
	String actor_id;
	
	@Facebook
	String message;

	@Facebook
	String description;
	
	public String getPost_id() {
		return post_id;
	}

	public String getCreated_time() {
		return created_time;
	}

	public String getActor_id() {
		return actor_id;
	}

	public String getMessage() {
		return message;
	}
	public String getDescription() {
		return description;
	}

	@Override
	  public String toString() {
	    return String.format("%s (%s)", post_id, message);
	  }

}
