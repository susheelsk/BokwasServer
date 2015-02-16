package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Comments;
import com.bokwas.database.GetUniqueKey;
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.bokwas.database.RelationType;
import com.bokwas.facebook.FBClient;
import com.bokwas.facebook.FBPosts;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.api.response.PostResponse;
import com.bokwas.server.backgroundtasks.ServerNotificationGCMTask;
import com.bokwas.server.backgroundtasks.TaskManager;
import com.google.gson.Gson;
import com.restfb.Connection;
import com.restfb.types.User;

/**
 * Servlet implementation class LoginRequestApi
 */
@WebServlet("/login")
public class LoginRequestApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String access_key = "";
	private boolean isNewUser = false;
	private String access_token = "";
	HashMap<String, Integer> friends = null;
	private int friendLimit = 3; // change to 4 or 5 if prod or 2 if dev

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginRequestApi() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Date requestDate = new Date();
		String accessToken = request.getParameter("access_token");
		String bokwasName = request.getParameter("bokwas_name");
		String bokwasAvatarId = request.getParameter("bokwas_avatar_id");
		String gcmRegId = request.getParameter("gcmregid");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		System.out.println("doPost called on /login");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = " + request.getParameter(paramName));

		}

		if (accessToken == null || accessToken.trim().equals("")) {
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}
		if (bokwasName == null || bokwasAvatarId == null || bokwasAvatarId.trim().equals("")) {
			APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}
		try {
			if (Integer.parseInt(bokwasAvatarId) > 0) {

			}
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}
		access_token = accessToken;

		FBClient client = new FBClient(accessToken);

		Connection<User> friendList = null;
		User user = null;
		Connection<FBPosts> postObject = null;
		List<FBPosts> posts = new ArrayList<FBPosts>();
		Person selfPerson = null;
		int postsFetched = 0;
		try {
			// CREATING A USER
			user = client.getClient().fetchObject("me", User.class);
			friendList = client.getFriends();
			HashMap<String, Integer> dbPosts = new HashMap<String, Integer>();
			try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
				Node persn = BokwasNodeFactory.findPerson(user.getId());
				if (persn == null) {
					for (User friend : friendList.getData()) {
						if (bokwasName.equalsIgnoreCase(friend.getName()) || user.getName().contains(bokwasName.trim())) {
							System.out.println("Duplicate name exists in network");
							APIStatus error = new APIStatus(1100, "Sorry! One of your friends has already taken this name");
							out.print(new Gson().toJson(error));
							out.flush();
							return;
						}
					}
					if (user.getName().equalsIgnoreCase(bokwasName) || user.getName().contains(bokwasName)) {
						System.out.println("Matches with Facebook name");
						APIStatus error = new APIStatus(1100, "You cannot choose a name which matches your facebook name");
						out.print(new Gson().toJson(error));
						out.flush();
						return;
					}

					selfPerson = addSelfToDb(user, friendList, bokwasName, bokwasAvatarId, gcmRegId);
					isNewUser = true;
				} else {
					selfPerson = new Person(persn);
					selfPerson.setBokwasName(bokwasName);
					selfPerson.setBokwasAvatarId(bokwasAvatarId);
					this.access_key = selfPerson.getSecretKey();
					if (gcmRegId != null) {
						if (gcmRegId.equals("") == false) {
							selfPerson.setGcmRegId(gcmRegId);
						}
					}
				}
				dbPosts = selfPerson.getPostIds();
				tx.success();
			}
			try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
				addFriendsToDb(selfPerson, friendList);
//				int friends = selfPerson.friendlist().size();
//				if (friends < friendLimit) {
//					System.out.println("Not enough friends");
//					APIStatus error = new APIStatus(403, "You don't have enough friends on Bokwas yet. You need " + (friendLimit - friends)
//							+ " more friends to enable this app! We will notify you as soon as you they have joined bokwas.");
//					out.print(new Gson().toJson(error));
//					out.flush();
//					return;
//				}
				tx.success();
			}

			// ADD FRIENDS AND POSTS
			boolean stopfetching = false;
			// List<BatchResponse>batchresponses=client.getPosts();
			// String from=batchresponses.get(0).getBody();
			// JSONObject cat=new JSONObject(from);
			// JSONArray jarr=cat.getJSONArray("data");
			// for(int x=0;x<jarr.length();x++)
			// {
			// JSONObject temp=jarr.getJSONObject(x);
			// System.out.println(temp.get("id"));
			// System.out.println(temp.getJSONObject("from").optString("category"));
			//
			// }
			String url = "https://graph.facebook.com/v2.0/me/home?fields=link,picture,from.fields(name,about,picture.type(large),picture.fields(url)),message,story,status_type,type,updated_time&access_token="
					+ access_token + "&limit=50";
			while (postsFetched <= 15 && stopfetching == false) {
				postObject = client.getmorePosts(url);
				for (FBPosts post : postObject.getData()) {

					if (!dbPosts.containsKey(post.getId().split("_")[1]) && postsFetched < 15) {
						String status_type = post.getStatusType();
						if (post.getStatusType() != null
								&& (status_type.equals("mobile_status_update") || status_type.equals("added_photos") || status_type
										.equals("shared_story")) && post.getFrom().getAbout() == null
								&& (post.getType().equals("status") || (post.getType().equals("photo")) && post.getLinkCount() == 1)) {
							posts.add(post);
							postsFetched += 1;
						}
					} else {
						stopfetching = true;
						break;
					}
				}
				if (postObject.hasNext() && postsFetched < 15) {
					url = postObject.getNextPageUrl();
				} else {
					postsFetched = 16;
				}
			}

			try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
				addPostsToSelf(selfPerson, posts, user);

				if (isNewUser && friendList.getData() != null) {
					selfPerson.getBokwasPostsAndConnect();
				}
				tx.success();
			}

		} catch (Exception e) {
			e.printStackTrace();
			APIStatus error = new APIStatus(ERROR_CODE.STANDARD_SERVER_ERROR);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		Date computationBeginDate = new Date();
		try {
			HashMap<Posts, List<Comments>> friendPosts = getFriendsPosts(selfPerson);

			response.setContentType("application/json");
			String data = getJsonData(friendPosts, access_key);
			out.print(data);
			out.flush();

			Date computationOverDate = new Date();
			System.out.println("Computation Time : " + getDateDiff(computationOverDate, computationBeginDate, TimeUnit.SECONDS));
			System.out.println("Total Time : " + getDateDiff(computationOverDate, requestDate, TimeUnit.SECONDS));
		} catch (Exception e) {
			e.printStackTrace();
			APIStatus error = new APIStatus(ERROR_CODE.STANDARD_SERVER_ERROR);
			out.print(new Gson().toJson(error));
			out.flush();
		}
	}

	private String getJsonData(HashMap<Posts, List<Comments>> friendPosts, String accessKey) {
		PostResponse response;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

			response = new PostResponse(friendPosts, accessKey, new APIStatus(200, "Success"));
			tx.success();
		}
		Gson gson = new Gson();
		return gson.toJson(response);
	}

	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date1.getTime() - date2.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

	private HashMap<Posts, List<Comments>> getFriendsPosts(Person selfPerson) {
		HashMap<Posts, List<Comments>> friendPosts = new HashMap<Posts, List<Comments>>();
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			friendPosts = selfPerson.getFriendsPostsWithComments();
			tx.success();
		}
		return friendPosts;
	}

	private void addPostsToSelf(Person selfPerson, List<FBPosts> posts, User user) throws Exception {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			for (FBPosts post : posts) {
				String message = "";
				if (post.getMessage() == null) {
					if (post.getStory() != null) {
						message = post.getStory();
					}
				} else if (post.getMessage() != null && post.getStory() != null) {
					message = post.getMessage() + post.getStory();
				} else {
					message = post.getMessage();
				}

				String url = post.getFrom().getPicture().getPictureLink().getPicLink();
				String[] postId = post.getId().split("_");
				String postedBy = postId[0];

				Posts fbPost = null;
				if (post.getType().equals("status")) {
					fbPost = new Posts(message, post.getCreatedTime().getTime(), postId[1], postId[0], post.getFrom().getName(), "", url, false);
				} else if (post.getType().equals("photo")) {

					fbPost = new Posts(message, post.getCreatedTime().getTime(), postId[1], postId[0], post.getFrom().getName(), "", url,
							post.getPhoto(), false);
				}
				// to connect fb post of the user on bokwas
				Node postedByNode = BokwasNodeFactory.findPerson(postedBy);
				if (postedByNode != null) {
					Person postPerson = new Person(postedByNode);
					if (postPerson.getPost(postId[1]) == null) {
						postedByNode.createRelationshipTo(fbPost.getUnderlyingNode(), RelationType.SUBSCRIBE_TO);
					}
				}
				if (fbPost != null && message != null & message.equals("") == false) {
					selfPerson.addPost(fbPost);
				}
			}
			tx.success();
		}
	}

	private Person addSelfToDb(User user, Connection<User> friendList, String bokwasName, String bokwasAvatarId, String gcmRegId) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			String name = user.getName();
			String fbId = user.getId();

			String key = GetUniqueKey.getKey(fbId);
			this.access_key = key;

			if (gcmRegId == null) {
				gcmRegId = "";
			}

			Node selfNode = BokwasNodeFactory.createSelfNode(fbId, bokwasName, bokwasAvatarId, name, user.getGender(), gcmRegId, key);
			Person person = new Person(selfNode);
			tx.success();
			return person;
		}

	}

	private void addFriendsToDb(Person person, Connection<User> friendList) {

		if (friendList.getData() == null)
			return;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			friends = person.friendlist();

			for (User friend : friendList.getData()) {

				if (friend != null && friends.containsKey(friend.getId()) == false) {
					Node friendNode = BokwasNodeFactory.findPerson(friend.getId());
					if (friendNode != null) {

						Person friendPerson = new Person(friendNode);
						System.out.println("new friend:" + friendPerson.getFbName() + friendPerson.getFbId() + " --" + friends.size());
						person.addFriend(person, friendPerson);
						tx.success();
						/**
						 * The code present after this point can be moved to a
						 * backgroud job
						 */
						int friendsConnections = friendPerson.friendlist().size();
						if (friendsConnections == friendLimit && isNewUser) {
							ServerNotificationGCMTask serverTask = new ServerNotificationGCMTask(friendPerson,
									"Hurray! Your app is now enabled,keep calm and do bokwas");
							TaskManager.getInstance().addTask(serverTask);
						} else if (friendsConnections < friendLimit && isNewUser) {
							ServerNotificationGCMTask serverTask = new ServerNotificationGCMTask(friendPerson,
									"Hurray,one more friend joined bokwas, you need just " + (friendLimit - friendsConnections)
											+ " friends to enable app. Invite now");
							TaskManager.getInstance().addTask(serverTask);
						}else if(friendsConnections > friendLimit) {
							ServerNotificationGCMTask serverTask = new ServerNotificationGCMTask(friendPerson,
									"One of your friends just joined bokwas!");
							TaskManager.getInstance().addTask(serverTask);
						}

					}
				}
			}
			tx.success();
		}
	}

}
