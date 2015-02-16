package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
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
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.api.response.AddPostResponse;
import com.google.gson.Gson;

/**
 * Servlet implementation class AddPostApi
 */
@WebServlet("/addpost")
public class AddPostApi extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddPostApi() {
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
		String accessKey = request.getParameter("access_key");
		String postId = UUID.randomUUID().toString();
		String personId = request.getParameter("person_id");
		String postText = URLDecoder.decode(request.getParameter("post_text"),
				"UTF-32");
		
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /addpost");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ URLDecoder.decode(request.getParameter(paramName),
							"UTF-32"));

		}
		boolean isValid = false;
		Person p1 = new Person(BokwasNodeFactory.findPerson(personId));
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			if (p1 != null && accessKey != null
					&& accessKey.equals(p1.getSecretKey())) {
				isValid = true;
			}
			tx.success();
		}

		if (!isValid) {
			System.out.println("Access Token null");
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		if (postId == null || postId.trim().equals("") || personId == null
				|| personId.trim().equals("") || postText == null) {
			APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		try {
			try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
				Node nodePerson = BokwasNodeFactory.findPerson(personId);
				Person person = new Person(nodePerson);
				Posts p=new Posts(postText, System.currentTimeMillis(),
						postId, personId,person.getBokwasName(),person.getBokwasAvatarId(),"", true);
				person.addBokwasPost(p);
				tx.success();
			}
		} catch (Exception e) {
			e.printStackTrace();
			APIStatus error = new APIStatus(ERROR_CODE.STANDARD_SERVER_ERROR);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		AddPostResponse addCommentResponse = new AddPostResponse(postId,
				new APIStatus(200, "Success"));
		out.print(new Gson().toJson(addCommentResponse));
		out.flush();
		Date responseDate = new Date();
		System.out.println("Total Time : "
				+ getDateDiff(responseDate, requestDate, TimeUnit.SECONDS));
	}

	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date1.getTime() - date2.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

}
