package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
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
import com.google.gson.Gson;

/**
 * Servlet implementation class DeletePostApi
 */
@WebServlet("/deletepost")
public class DeletePostApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeletePostApi() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Date requestDate = new Date();
		String accessKey = request.getParameter("access_key");
		String postId = request.getParameter("post_id");
		String commentId = request.getParameter("comment_id");
		String personId = request.getParameter("person_id");
		PrintWriter out = response.getWriter();

		
		System.out.println("doPost called on /deletepost");
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
			nodePerson=BokwasNodeFactory.findPerson(personId);
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
		
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) 
		{
			if (postId != null) 
			{
				if (personId == null) 
				{
					System.out.println(personId);
					APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
					out.print(new Gson().toJson(error));
					out.flush();
					return;
				}
				
				Posts post = person.getPost(postId);
				
				if (commentId == null)
				{
					post.deletePost(postId);
				}
				else if (commentId != null)
				{
					post.deleteComment(commentId);
				}
			}			
			tx.success();
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
