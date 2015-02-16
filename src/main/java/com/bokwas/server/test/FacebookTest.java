package com.bokwas.server.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.UniqueFactory;

import com.bokwas.database.BokwasDB;
import com.bokwas.facebook.FBClient;
import com.bokwas.facebook.FBPosts;
import com.restfb.Connection;
import com.restfb.types.User;

/**
 * Servlet implementation class FacebookTest
 */
@WebServlet("/fbtest")
public class FacebookTest extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FacebookTest() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unused")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		FBClient client = new FBClient(
				"CAAEAyEFFs5IBACz0DbgsKGfyMG4uQcn5wDt6nvnyPMXZAI6tcYyHVZAEZAzX25lSdFQAZAKVpSdWTfoHIzpjK9RXPcuTUyDviAIR534DFuZBUe5R9c3SvuNZCme3Czm8tg7y3njnNnYTN9eg1RIh9mJypbkt0WoaCFAOsM0r4ZCCqczqC9esLNZBJ0iifc3ijlYZD");
		Connection<User> friendList = null;
		Connection<FBPosts> feed = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println("Request Time : " + dateFormat.format(date));
		try {
			friendList = client.getFriends();
	//		feed = client.getPosts();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (feed != null) {
			logFeedDetails(feed);
		} else {
			System.out.println("feed null");
		}

		if (friendList != null) {
			addToDatabase(friendList);
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html>"
					+ "<head><title>Facebook Test</title></head>"
					+ "<body><h1>Facebook Test</h1>This is just a test page<br/>"
					+ "FriendSize : " + friendList.getData().size()
					// + "</br>FeedSize :  " + feed.getData().size() +
					+ "</body>" + "</html>");
		} else {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html>"
					+ "<head><title>Facebook Test</title></head>"
					+ "<body><h1>Facebook Test</h1>This is just a test page<br/>"
					+ "Something went wrong.");
		}
		printAllUsers();
		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		date = new Date();
		System.out.println("Response Time : " + dateFormat.format(date));
		BokwasDB.getDatabase().shutdown();
	}

	private void logFeedDetails(Connection<FBPosts> feed) {
		for (FBPosts post : feed.getData()) {
			System.out.println("PostId : " + post.getId());
			System.out.println("PostMessage : " + post.getMessage());
			System.out.println("PostFrom : " + post.getFrom().getName()
					+ " Id : " + post.getFrom().getId());
			System.out.println();
		}
	}

	private void addToDatabase(Connection<User> friendList) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			for (User friend : friendList.getData()) {
				if (friend.getId() != null) {
					getOrCreateUserWithUniqueFactory(friend.getId(),
							friend.getName());
					// createAndIndexUser(friend.getId(), friend.getName());
				}
			}
			tx.success();
			findUser("622206695");
			tx.success();
		}
	}

	private Node getOrCreateUserWithUniqueFactory(final String userId,
			final String name) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(
					BokwasDB.getDatabase(), "users") {
				@Override
				protected void initialize(Node created,
						Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label("User"));
					created.setProperty("userId", properties.get("userId"));
				}
			};
			Node node = factory.getOrCreate("userId", userId);
			node.setProperty("name", name);
			tx.success();
			return node;
		}

	}

	private void printAllUsers() {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			Index<Node> nodeIndex = BokwasDB.getDatabase().index()
					.forNodes("users");
			IndexHits<Node> allUsers = nodeIndex.query("*:*");
			System.out.println("Size of nodes : " + allUsers.size());
			while (allUsers.hasNext()) {
				Node currentNode = allUsers.next();
				System.out.println("UserId : "
						+ currentNode.getProperty("userId") + " ; Name : "
						+ currentNode.getProperty("name"));

			}
		}
	}

	private void findUser(String userName) {
		Index<Node> nodeIndex = BokwasDB.getDatabase().index()
				.forNodes("users");
		Node foundUser = nodeIndex.get("userId", userName).getSingle();
		if (foundUser != null) {
			System.out.println("The Name of user " + userName + " is "
					+ foundUser.getProperty("name"));
		} else {
			System.out.println("No User found");
		}
	}

//	private static Node createAndIndexUser(final String userId,
//			final String name) {
//		if (userId.equals("622206695")) {
//			System.out.println("Adding Saiesh");
//		}
//		Node node = BokwasDB.getDatabase().createNode();
//		node.setProperty("userId", userId);
//		node.setProperty("name", name);
//		Index<Node> nodeIndex = BokwasDB.getDatabase().index()
//				.forNodes("nodes");
//		nodeIndex.add(node, "userId", userId);
//		return node;
//	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

}
