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

import org.json.JSONArray;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Person;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.api.response.GetNotificationResponse;
import com.bokwas.server.util.GeneralUtils;
import com.google.gson.Gson;

/**
 * Servlet implementation class GetNotification
 */
@WebServlet("/getnotification")
public class GetNotification extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetNotification() {
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
		String accessKey = request.getParameter("access_key");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		System.out.println("doPost called on /getnotification");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}
		
		if(personId==null||personId.equals("")) {
			System.out.println("Bad Request");
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}
		
		if(accessKey==null||accessKey.equals("")) {
			System.out.println("Bad Request");
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		Person selfPerson;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			Node personNode = BokwasNodeFactory.findPerson(personId);
			if (personNode != null) {
				selfPerson = new Person(personNode);
			} else {
				System.out.println("Person not Found");
				APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
				out.print(new Gson().toJson(error));
				out.flush();
				return;
			}
			boolean isValid = false;
			if (selfPerson != null && accessKey != null
					&& accessKey.equals(selfPerson.getSecretKey())) {
				isValid = true;
			}
			if(!isValid) {
				APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
				out.print(new Gson().toJson(error));
				out.flush();
				return;
			}
			JSONArray notifArray = selfPerson.getNotifications();
			String responseJSON = getJsonData(notifArray,personId);
			out.print(responseJSON);
			out.flush();
			selfPerson.deleteNotifications();

			Date computationOverDate = new Date();
			System.out.println("Total Time : "
					+ GeneralUtils.getDateDiff(computationOverDate, requestDate,
							TimeUnit.SECONDS));
			tx.success();
		}
	}

	private String getJsonData(JSONArray notifications,String personId) {
		GetNotificationResponse response = new GetNotificationResponse(notifications,personId, new APIStatus(200, "SUCCESS"));
		return new Gson().toJson(response);
	}

}
