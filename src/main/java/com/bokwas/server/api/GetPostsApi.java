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
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.bokwas.database.RelationType;
import com.bokwas.facebook.FBClient;
import com.bokwas.facebook.FBPosts;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.api.response.PostResponse;
import com.google.gson.Gson;
import com.restfb.Connection;
import com.restfb.types.User;

/**
 * Servlet implementation class GetPostsApi
 */
@WebServlet("/getposts")
public class GetPostsApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String access_key = "";
	
	/*
	 * 
	 * @see HttpServlet#HttpServlet()
	 */
	public GetPostsApi() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Date requestDate = new Date();
		String accessToken = request.getParameter("access_token");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /getPosts");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

		if (accessToken == null || accessToken.trim().equals("")) {
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		FBClient client = new FBClient(accessToken);
		Connection<FBPosts> postObject = null;
		User user = null;

		List<FBPosts> posts = new ArrayList<FBPosts>();
		Person selfPerson = null;
		int postsFetched = 0;
		try {
			
			user = client.getClient().fetchObject("me", User.class);
			HashMap<String, Integer> dbPosts = new HashMap<String, Integer>();
			try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
				Node persn = BokwasNodeFactory.findPerson(user.getId());
				selfPerson = new Person(persn);
				dbPosts = selfPerson.getPostIds();
				this.access_key=selfPerson.getSecretKey();
				tx.success();
			}
			
			boolean stopfetching = false;
			String url = "https://graph.facebook.com/v2.0/me/home?fields=link,picture,from.fields(name,about,picture.type(large),picture.fields(url)),message,story,status_type,type,updated_time&access_token="
					+ accessToken + "&limit=50";
			while (postsFetched <= 10 && stopfetching == false) {
				postObject = client.getmorePosts(url);
				for (FBPosts post : postObject.getData()) {

					if (!dbPosts.containsKey(post.getId().split("_")[1])
							&& postsFetched < 15) 
					{
						String status_type= post.getStatusType();
						if (post.getStatusType() != null &&(status_type.equals("mobile_status_update")||status_type.equals("added_photos")
								||status_type.equals("shared_story"))
								&& post.getFrom().getAbout() == null
								&&(post.getType().equals("status")
								||(post.getType().equals("photo"))&&post.getLinkCount()==1)){
								posts.add(post);
								postsFetched += 1;
						}
					}
					else
					{
						stopfetching=true;
						break;
					}
				}
				if (postObject.hasNext() && postsFetched < 15) {
					url = postObject.getNextPageUrl();
				} else {
					postsFetched = 16;
				}
			}

			addPostsToSelf(selfPerson, posts, user);
			} catch (Exception e) {
			e.printStackTrace();
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		Date computationBeginDate = new Date();
		try {
			HashMap<Posts, List<Comments>> friendPosts = getFriendsPosts(selfPerson);

			response.setContentType("application/json");
			String data = getJsonData(friendPosts,access_key);
			out.print(data);
			out.flush();

			Date computationOverDate = new Date();
			System.out.println("Computation Time : "
					+ getDateDiff(computationOverDate, computationBeginDate,
							TimeUnit.SECONDS));
			System.out.println("Total Time : "
					+ getDateDiff(computationOverDate, requestDate,
							TimeUnit.SECONDS));
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

			response = new PostResponse(friendPosts, accessKey,
					new APIStatus(200, "Success"));
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

	private void addPostsToSelf(Person selfPerson, List<FBPosts> posts,
			User user) throws Exception {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) 
		{
			for (FBPosts post : posts) 
			{
						String message = "";
						if (post.getMessage() == null) 
						{
							if (post.getStory() != null) 
							{
								message = post.getStory();
							}
						} 
						else  if(post.getMessage()!=null && post.getStory() != null)
						{
							message = post.getMessage() + post.getStory();
						}
						else
						{
							message=post.getMessage();
						}
					
						String url = post.getFrom().getPicture()
								.getPictureLink().getPicLink();
						String[] postId = post.getId().split("_");
						
						Posts fbPost=null;
						if(post.getType().equals("status"))
						{
							fbPost = new Posts(message, post.getCreatedTime()
								.getTime(), postId[1], postId[0], post
								.getFrom().getName(),"", url, false);
						}
						else if(post.getType().equals("photo")){
						
							fbPost = new Posts(message, post.getCreatedTime()
									.getTime(), postId[1], postId[0], post
									.getFrom().getName(),"", url,post.getPhoto(), false);
						}
						// to connect fb post of the user on bokwas
						Node postedByNode =BokwasNodeFactory.findPerson(postId[0]);
							if(postedByNode !=null ){
								Person postPerson = new Person(postedByNode);
								if(postPerson.getPost(postId[1])==null){
									postedByNode.createRelationshipTo(fbPost.getUnderlyingNode(), RelationType.SUBSCRIBE_TO);
								}
						}
						
						if(fbPost!=null&&message!=null&message.equals("")==false)
						{
						selfPerson.addPost(fbPost);
						}
			}
			tx.success();
		}
	}

}
