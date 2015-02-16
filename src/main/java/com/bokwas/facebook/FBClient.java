package com.bokwas.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import org.json.JSONObject;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;
public class FBClient {

	private FacebookClient facebookClient;

	public FBClient(String accessToken) {
		this.facebookClient = new DefaultFacebookClient(accessToken);
	}

	public FacebookClient getClient() {
		return facebookClient;
	}

	public Connection<User> getFriends() throws Exception {
		Connection<User> myFriends = null;
		if (facebookClient != null) {
			try {
				myFriends = facebookClient.fetchConnection("v2.0/me/friends",
						User.class);
				return myFriends;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		return null;
	}

	/*public List<BatchResponse> getPosts() throws Exception {
		if (facebookClient != null) {
			try {
				System.out.println("hello");
				String params="link,to,picture,from.fields(name,about,picture.type(large),picture.fields(url)),message,story,status_type,type,updated_time";
			//	myFeed = facebookClient.fetchConnection("v2.0/me/home", FBPosts.class,Parameter.with("fields",params),Parameter.with("limit","50"));
				BatchRequest meRequest = new BatchRequestBuilder("me/home").parameters(Parameter.with("fields", "from")).build();
				BatchRequest picRequest = new BatchRequestBuilder("me/home").parameters(Parameter.with("fields",params)).build();
				List<BatchResponse> batchResponses =facebookClient.executeBatch(meRequest,picRequest);
//				BatchResponse meResponse = batchResponses.get(0);
//				BatchResponse picResponse = batchResponses.get(1);
//				categories=meResponse.getBody();
//				picResponse.getBody();
//				
				
				return batchResponses;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		return null;
	}*/
	public Connection<FBPosts> getmorePosts(String url) throws Exception {
		Connection<FBPosts> myFeed=null;
		if (facebookClient != null) {
			try {
				myFeed=facebookClient.fetchConnectionPage(url,FBPosts.class);
				}catch(Exception e) {
					e.printStackTrace();
				}
		}
		return myFeed;		
			
	}
	

	
	public boolean isAccessTokenValid() {

		try {
			if (getFriends() != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public boolean isAccessTokenValid(String accessToken) {
		String url = "https://graph.facebook.com/?access_token=" + accessToken;

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");

			// add request header

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	
			JSONObject jsonObject = new JSONObject(response.toString());
			String error = (String) new JSONObject(
					(String) jsonObject.get("error")).get("code");
			if (error.equals("100")) {
				return true;
			} else {
				return false;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}



}
