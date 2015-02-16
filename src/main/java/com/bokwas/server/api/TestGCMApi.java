package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
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
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.backgroundtasks.Task;
import com.bokwas.server.backgroundtasks.TaskManager;
import com.bokwas.server.util.GCMSender;
import com.bokwas.server.util.GeneralUtils;
import com.google.gson.Gson;

/**
 * Servlet implementation class TestGCMApi
 */
@WebServlet("/testgcm")
public class TestGCMApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestGCMApi() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Date requestDate = new Date();
		String receiverId = request.getParameter("receiver_id");
		String accessKey = request.getParameter("access_key");
		String messageText = request.getParameter("message");
		String title = request.getParameter("title");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		System.out.println("doPost called on /testgcm");
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
		params.put("time", String.valueOf(System.currentTimeMillis()));
		params.put("type", "GENERIC_MESSAGE");
		params.put("message", messageText);
		params.put("title", title);

		Person receiverPerson;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

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
			if (receiverPerson != null && accessKey != null
					&& accessKey.equals(receiverPerson.getSecretKey())) {
				isValid = true;
			}
			if (!isValid) {
				APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
				out.print(new Gson().toJson(error));
				out.flush();
				return;
			}
			final String gcmRegId = receiverPerson.getGcmRegId();
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
