package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.api.response.GCMRegIdsList;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;

/**
 * Servlet implementation class GCMNotification
 */
@WebServlet("/sendgcm")
public class GCMNotification extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String GOOGLE_SERVER_KEY = "AIzaSyAfaESLACPriKKwf9W3hfZ4v95JdBr9gUo";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GCMNotification() {
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
		Result result = null;
		String regId = request.getParameter("regid");
		String multipleRegIds = request.getParameter("multipleregids");
		String title = request.getParameter("title");
		String notiMessage = request.getParameter("message");
		String type = request.getParameter("type");
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /sendgcm");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

		if (title.trim().equals("") || notiMessage.trim().equals("")
				|| type.trim().equals("")) {
			System.out
					.println("Check request params. Title or type or message is empty");
			APIStatus error = new APIStatus(ERROR_CODE.BAD_REQUEST);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		if (regId != null) {
			try {
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message message = new Message.Builder()
						.timeToLive(14 * 24 * 60 * 60).delayWhileIdle(true)
						.addData("type", type).addData("title", title)
						.addData("message", notiMessage).build();
				result = sender.send(message, regId, 3);
				System.out.println("GcmNotification and result"
						+ result.toString());
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (multipleRegIds != null) {
			List<String> regIdList = new Gson().fromJson(multipleRegIds,
					GCMRegIdsList.class).gcmRegIdList;
			Set<String> set = new HashSet<String>();
			set.addAll(regIdList);
			regIdList.clear();
			regIdList.addAll(set);
			Sender sender = new Sender(GOOGLE_SERVER_KEY);
			Message message = new Message.Builder()
					.timeToLive(14 * 24 * 60 * 60).delayWhileIdle(true)
					.addData("type", type).addData("title", title)
					.addData("message", notiMessage).build();
			MulticastResult multicastResult = sender
					.send(message, regIdList, 3);
			System.out.println("GcmNotification and result"
					+ multicastResult.toString());
		}
		APIStatus error = new APIStatus(200,"Success");
		out.print(new Gson().toJson(error));
		out.flush();
	}
}
