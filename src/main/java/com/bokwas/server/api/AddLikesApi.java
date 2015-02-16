package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.backgroundtasks.AddLikesGCMTask;
import com.bokwas.server.backgroundtasks.TaskManager;
import com.google.gson.Gson;

/**
 * Servlet implementation class AddLikesApi
 */
@WebServlet("/addlikes")
public class AddLikesApi extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddLikesApi() {
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

		String postId = request.getParameter("post_id");
		String commentId = request.getParameter("comment_id");
		String personId = request.getParameter("person_id");
		String postPersonId = request.getParameter("post_person_id");
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /addlikes");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

		Person person;
		Node nodePerson;
		boolean isValid = false;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			nodePerson = BokwasNodeFactory.findPerson(personId);
			person = new Person(nodePerson);
			if (person != null && accessKey != null
					&& accessKey.equals(person.getSecretKey())) {
				isValid = true;
			}
			tx.success();
		}
		if (!isValid) {
			System.out.println("Bad Request");
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		if (commentId.trim().equals("") || commentId == null) {
			commentId = null;
		}

		Comments comment = null;
		Posts post = null;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			if (postId != null && commentId == null) {
				if (postPersonId == null) {
					APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
					out.print(new Gson().toJson(error));
					out.flush();
					return;
				}
				// Node postPersonNode = BokwasNodeFactory.findPerson(personId);
				// Person postPerson = new Person(postPersonNode);
				post = person.getPost(postId);
				if(post == null){
					APIStatus error = new APIStatus(ERROR_CODE.POST_NOT_FOUND);
					out.print(new Gson().toJson(error));
					out.flush();
					return;
				}
				HashMap<String, String> likes = post.getLikes();
				post.setPostUpdatedTime(System.currentTimeMillis());
				if (likes.containsKey(personId+"_"+person.getBokwasAvatarId())) {
					post.removeLikes(personId);
				} else {
					post.setLikes(nodePerson);
					AddLikesGCMTask task = new AddLikesGCMTask(null, post,
							person);
					TaskManager.getInstance().addTask(task);
				}
				tx.success();
			} else if (commentId != null) {
				// Node postPersonNode = BokwasNodeFactory
				// .findPerson(postPersonId);
				// Person postPerson = new Person(postPersonNode);
				post = person.getPost(postId);
				comment = post.getComment(commentId);
				HashMap<String, String> commentlikes = comment.getLikes();
				if (commentlikes.containsKey(personId)) {
					comment.removeLikes(personId);
				} else {
					comment.addLikes(nodePerson);
					AddLikesGCMTask task = new AddLikesGCMTask(comment, post,
							person);
					TaskManager.getInstance().addTask(task);
				}
				
				tx.success();
			}
			
		}

		APIStatus status = new APIStatus(200, "Success");
		out.print(new Gson().toJson(status));
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
