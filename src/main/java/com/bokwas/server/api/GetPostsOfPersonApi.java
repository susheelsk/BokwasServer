package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
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

import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Comments;
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.GetPostsOfFriendsResponse;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.google.gson.Gson;

/**
 * Servlet implementation class GetPostsOfPersonApi
 */
@WebServlet("/getpostsperson")
public class GetPostsOfPersonApi extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetPostsOfPersonApi() {
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
		String personId = request.getParameter("person_id");
		String postpersonId = request.getParameter("post_person_id");

		String accessKey = request.getParameter("access_key");
		String Since = request.getParameter("since");
		String isbokwaspost = request.getParameter("isbokwaspost");
		// String friendId = request.getParameter("friend_id");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /getpostsperson");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

		boolean isValid = false;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

			Person person = new Person(BokwasNodeFactory.findPerson(personId));
			if (person != null && accessKey != null
					&& accessKey.equals(person.getSecretKey()) && Since != null) {
				isValid = true;
			}
			tx.success();
		}
		if (!isValid) {
			System.out.println("Bad Request");
			APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

				Date computationBeginDate = new Date();
				Person selfPerson = new Person(
						BokwasNodeFactory.findPerson(personId));
				List<Posts> postsList = null;
				postsList = selfPerson.getBokwasPosts(postpersonId, Since,
						isbokwaspost);
				HashMap<Posts, List<Comments>> friendPosts = getFriendsPosts(postsList);
				response.setContentType("application/json");
				String data = getJsonData(friendPosts);
				out.print(data);
				out.flush();

				Date computationOverDate = new Date();
				System.out.println("Computation Time : "
						+ getDateDiff(computationOverDate, computationBeginDate,
								TimeUnit.SECONDS));
				System.out.println("Total Time : "
						+ getDateDiff(computationOverDate, requestDate,
								TimeUnit.SECONDS));
				tx.success();

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Server Error");
				APIStatus error = new APIStatus(ERROR_CODE.STANDARD_SERVER_ERROR);
				out.print(new Gson().toJson(error));
				out.flush();
				return;
			}
		
	}

	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date1.getTime() - date2.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

	private String getJsonData(HashMap<Posts, List<Comments>> friendPosts) {
		GetPostsOfFriendsResponse response;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			response = new GetPostsOfFriendsResponse(friendPosts,
					new APIStatus(200, "Success"));
			tx.success();
		}
		Gson gson = new Gson();
		return gson.toJson(response);
	}

	private HashMap<Posts, List<Comments>> getFriendsPosts(List<Posts> posts) {
		HashMap<Posts, List<Comments>> friendPosts = new HashMap<Posts, List<Comments>>();
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			for (Posts post : posts) {
				friendPosts.put(post, post.getCommentsAsList());
			}
			tx.success();
		}
		return friendPosts;
	}

}
