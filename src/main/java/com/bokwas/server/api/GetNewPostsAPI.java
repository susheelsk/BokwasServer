package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.api.response.PostResponse;
import com.google.gson.Gson;

/**
 * Servlet implementation class GetPostsApi
 */
@WebServlet("/getnewposts")
public class GetNewPostsAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/*
	 * 
	 * @see HttpServlet#HttpServlet()
	 */
	public GetNewPostsAPI() {
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
		String personId = request.getParameter("person_id");
		String access_key = request.getParameter("access_key");
		String since = request.getParameter("since");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /getnewPosts");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

	
		Person person;
		try(Transaction tx = BokwasDB.getDatabase().beginTx()){
			Node personNode = BokwasNodeFactory.findPerson(personId);
			if(personNode==null){
					APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
					out.print(new Gson().toJson(error));
					out.flush();
					return;
				
			}
			person = new Person(personNode);
			tx.success();
		}
		
			HashMap<Posts, List<Comments>> friendPosts = getFriendsPosts(person,since);
			response.setContentType("application/json");
			String data = getJsonData(friendPosts,access_key);
			out.print(data);
			out.flush();

	
}
	private String getJsonData(HashMap<Posts, List<Comments>> friendPosts, String access_key) {
		PostResponse response;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			response = new PostResponse(friendPosts, access_key,
					new APIStatus(200, "Success"));
			tx.success();
		}
		Gson gson = new Gson();
		return gson.toJson(response);
	}


	private HashMap<Posts, List<Comments>> getFriendsPosts(Person selfPerson,String since) {
		HashMap<Posts, List<Comments>> friendPosts = new HashMap<Posts, List<Comments>>();
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			friendPosts = selfPerson.getFriendsPostSince(since);
			tx.success();
		}
		return friendPosts;
	}
	
}
