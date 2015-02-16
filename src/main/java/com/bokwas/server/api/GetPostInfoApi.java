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
import com.bokwas.server.api.response.SinglePostResponse;
import com.google.gson.Gson;

/**
 * Servlet implementation class GetPostInfoApi
 */
@WebServlet("/postinfo")
public class GetPostInfoApi extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetPostInfoApi() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String accessKey = request.getParameter("access_key");
		String postId = request.getParameter("post_id");
		String personId = request.getParameter("person_id");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		System.out.println("doPost called on /postinfo");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

		Person selfPerson;
		Posts post = null;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

			Node personNode = BokwasNodeFactory.findPerson(personId);
			if (personNode != null) {
				selfPerson = new Person(personNode);
			} else {
				System.out.println("Post not Found");
				APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
				out.print(new Gson().toJson(error));
				out.flush();
				return;

			}
			Node postNode = BokwasNodeFactory.findPost(postId);
			
			if (postNode == null || accessKey == null) {
				if( !accessKey.equals(selfPerson.getSecretKey()))
						{
				System.out.println("Post not Found");
				APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
				out.print(new Gson().toJson(error));
				out.flush();
				return;
						}
			}
			post = new Posts(postNode);
			HashMap<Posts, List<Comments>> singlepost = new HashMap<Posts, List<Comments>>();
			singlepost.put(post, post.getCommentsAsList());
			String data = getJsonData(singlepost, accessKey);
			out.print(data);
			out.flush();

			tx.success();

		}

	}

	private String getJsonData(HashMap<Posts, List<Comments>> singlepost,
			String accessKey) {
		SinglePostResponse response;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

			response = new SinglePostResponse(singlepost);
			tx.success();
		}
		Gson gson = new Gson();
		return gson.toJson(response);
	}

}
