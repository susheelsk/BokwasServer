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

import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Comments;
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.api.response.AddCommentResponse;
import com.bokwas.server.backgroundtasks.AddCommentGCMTask;
import com.bokwas.server.backgroundtasks.TaskManager;
import com.google.gson.Gson;

/**
 * Servlet implementation class AddCommentApi
 */
@WebServlet("/addcomment")
public class AddCommentApi extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddCommentApi() {
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
		// String accessToken = request.getParameter("access_token");
		String postId = request.getParameter("post_id");
		String personId = request.getParameter("person_id");
		String commentText = URLDecoder.decode(
				request.getParameter("comment_text"), "UTF-8");
		String accessKey = request.getParameter("access_key");
		String commentId = UUID.randomUUID().toString();
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /addcomment");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ URLDecoder.decode(request.getParameter(paramName),"UTF-8"));

		}

		// FBClient client = new FBClient(accessToken);
		Person person;
		boolean isValid = false;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

			person = new Person(BokwasNodeFactory.findPerson(personId));
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
		if (postId == null || postId.trim().equals("") || commentText == null) {
			APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		try {
			Comments comment;
			Posts post;
			try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
				post = person.getPost(postId);
				if (post == null) {
					APIStatus error = new APIStatus(
							400,
							"Post not found. Please check post_id and send person_id of the person who posted.");
					out.print(new Gson().toJson(error));
					out.flush();
					return;
				}
				comment = new Comments(commentText, System.currentTimeMillis(),
						commentId, personId);
				post.addComment(comment);
				post.setPostUpdatedTime(System.currentTimeMillis());
				// List<String> regIds;
				//
				// regIds = getListRegIds(post);
				// Set<String> set = new HashSet<String>();
				// set.addAll(regIds);
				// regIds.clear();
				// regIds.addAll(set);
				// if (person.getGcmRegId() != null
				// && regIds.contains(person.getGcmRegId())) {
				// regIds.remove(person.getGcmRegId());
				// }
				//
				// if (regIds != null && regIds.size() > 0) {
				// System.out.println("Sending GCM");
				// new Thread(new SendGCMTask("Bokwas", "generic",
				// person.getBokwasName()
				// + "  commented on your post", regIds,
				// GeneralUtils.getURL(request))).start();
				// }
				tx.success();
			}
			AddCommentGCMTask task = new AddCommentGCMTask(comment, post,
					person);
			TaskManager.getInstance().addTask(task);
		} catch (Exception e) {
			e.printStackTrace();
			APIStatus error = new APIStatus(ERROR_CODE.STANDARD_SERVER_ERROR);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		AddCommentResponse addCommentResponse = new AddCommentResponse(
				commentId, new APIStatus(200, "Success"));
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
