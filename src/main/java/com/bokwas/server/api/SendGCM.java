package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
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
import com.bokwas.database.PrivateMessage;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.backgroundtasks.Task;
import com.bokwas.server.backgroundtasks.TaskManager;
import com.bokwas.server.util.GCMSender;
import com.bokwas.server.util.GeneralUtils;
import com.google.gson.Gson;

/**
 * Servlet implementation class SendGCM
 */
@WebServlet("/sendgcm")
public class SendGCM extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SendGCM() {
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
		String receiverId = request.getParameter("receiver_id");
		String accessKey = request.getParameter("access_key");
		String messageText = request.getParameter("message");
		String messageId = UUID.randomUUID().toString();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		System.out.println("doPost called on /sendgcm");
		Enumeration<String> en = request.getParameterNames();

		final Map<String, String> params = new LinkedHashMap<>();
		;

		while (en.hasMoreElements()) {
			String paramName = (String) en.nextElement();
			if (paramName.equals("person_id") || paramName.equals("access_key")
					|| paramName.equals("receiver_id")) {
				// doNothing, if someone is reading, please change the condition
				// appropriately. I was simply lazy to do so
			} else {
				params.put(paramName, URLDecoder.decode(
						request.getParameter(paramName), "UTF-8"));
			}
			System.out.println(paramName
					+ " = "
					+ URLDecoder.decode(request.getParameter(paramName),
							"UTF-8"));
		}
		params.put("fromId", personId);
		params.put("time", String.valueOf(System.currentTimeMillis()));
		params.put("type", "PRIVATE_MESSAGE_NOTI");
		params.put("messageId", messageId);

		Person selfPerson, receiverPerson;
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

			Node receiverNode = BokwasNodeFactory.findPerson(receiverId);
			if (receiverNode != null) {
				receiverPerson = new Person(receiverNode);
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
			if (!isValid) {
				APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
				out.print(new Gson().toJson(error));
				out.flush();
				return;
			}
			final String gcmRegId = receiverPerson.getGcmRegId();
			PrivateMessage message = new PrivateMessage(messageText, System.currentTimeMillis(), messageId, personId);
			receiverPerson.addPrivateMessage(message);
			tx.success();
			
			if (gcmRegId == null || gcmRegId.equals("")) {
				APIStatus error = new APIStatus(404,
						"Person's gcmRegId missing");
				out.print(new Gson().toJson(error));
				out.flush();
				return;
			}
			System.out.println("Sending Gcm...");
			APIStatus error = new APIStatus(200, "Success!");
			out.print(new Gson().toJson(error));
			out.flush();
			TaskManager.getInstance().addTask(new Task() {
				
				@Override
				public void execute() {
					GCMSender gcmSender = new GCMSender(gcmRegId, params);
					gcmSender.send();
					System.out.println("Sent");
				}
			});
			
		}

		Date computationOverDate = new Date();
		System.out.println("Total Time : "
				+ GeneralUtils.getDateDiff(computationOverDate, requestDate,
						TimeUnit.SECONDS));
	}

}
