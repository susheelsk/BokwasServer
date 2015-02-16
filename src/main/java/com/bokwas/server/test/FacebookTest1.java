package com.bokwas.server.test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.Person;
import com.bokwas.facebook.FBClient;
import com.bokwas.facebook.FQLPost;
import com.restfb.Connection;
import com.restfb.types.User;

/**
 * Servlet implementation class FacebookTest1
 */
@WebServlet("/fbtest1")
public class FacebookTest1 extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FacebookTest1() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/**
		 * @1. Create a node for the user and add a dummy bokwasName, and
		 *     bokwasAvatarId along with fbName and fbId;
		 * @2. Get me/friends and create a node for each of them and then link
		 *     them to the user
		 */

		FBClient client = new FBClient(
				"CAAEAyEFFs5IBAI7m55yoU2QXWKElHLVbp1ZBMr7txr0FBZCQVV7sCJRHWYHWieHWKhimDqpSnUJeDYqw1iPxCMmrZBTlMWby5QFOs6HC3Y4Lk86c7JI0lIMVEBUriNIFh3Nc38i0PYjBilr18uvvRL56Rk6pZCI3EuPWkIJh5aWEZCLZC8F2Fx1uoZCwtfHaA8ZD");
		Connection<User> friendList = null;
		// Connection<Post> feed = null;
		List<FQLPost> posts = null;
		User user = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			user = client.getClient().fetchObject("me", User.class);
			friendList = client.getFriends();
			// feed = client.getPosts();
			String query = "SELECT post_id,created_time,actor_id,message FROM stream WHERE source_id IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND type IN (46) LIMIT 50";
			posts = client.getClient().executeFqlQuery(query, FQLPost.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Date date = new Date();
		System.out.println("Request Time : " + dateFormat.format(date));

		Person selfPerson = addSelfToDb(user, friendList);

		addFriendsToDb(selfPerson, friendList);

		addPostsToFriends(selfPerson, friendList, posts);

		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			System.out.println("Friends added :" + selfPerson.getNrOfFriends());
			tx.success();
		}

		date = new Date();
		System.out.println("Response Time : " + dateFormat.format(date));

	}

	private void addPostsToFriends(Person selfPerson,
			Connection<User> friendList, List<FQLPost> posts) {
//		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
//			for (FQLPost post : posts) {
//				if (post.getActor_id() != null || post.getMessage() != null
//						|| post.getPost_id() != null) {
//					Person friend = selfPerson.getFriend(post.getActor_id());
//					Posts fbPost = new Posts(post.getMessage(),
//							Long.valueOf(post.getCreated_time()),
//							post.getPost_id(), friend.getFbId(), false);
//					friend.addPost(fbPost);
//				}
//			}
//			List<Posts> friendPosts = selfPerson.getFriendsFacebookFeed();
//			for (Posts post : friendPosts) {
//				System.out.println();
//				System.out.println("From : " + post.getPostedBy());
//				System.out.println("Message: " + post.getPostText());
//			}
//			tx.success();
//		}
	}

	private Person addSelfToDb(User user, Connection<User> friendList) {/*
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			String name = user.getName();
			String fbId = user.getId();
			String bokwasName = "Evil Philanthropist";
			String bokwasAvatarId = "13";

			Node selfNode = BokwasNodeFactory.createSelfNode(fbId, bokwasName,
					bokwasAvatarId, name, bokwasAvatarId);
			System.out.println("Self Node created");
			System.out.println("Name : " + selfNode.getProperty(Person.FB_NAME)
					+ ", BokwasName: "
					+ selfNode.getProperty(Person.BOKWAS_NAME));
			Person person = new Person(selfNode);
			tx.success();
			return person;
		}

	*/
		return null;
		}

	private void addFriendsToDb(Person person, Connection<User> friendList) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			for (User friend : friendList.getData()) {
				if (friend.getId() != null) {
				//	person.addFriend(friendPerson);
				}
			}
			tx.success();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

}
