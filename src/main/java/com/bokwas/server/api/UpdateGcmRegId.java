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

import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Person;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.bokwas.server.util.GeneralUtils;
import com.google.gson.Gson;

/**
 * Servlet implementation class UpdateGcmRegId
 */
@WebServlet("/addgcmregid")
public class UpdateGcmRegId extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UpdateGcmRegId() {
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
		String gcmRegId = request.getParameter("gcmregid");
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /addgcmregid");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

		Person selfPerson = null;
		boolean isValid = false;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {

			try {
				selfPerson = new Person(BokwasNodeFactory.findPerson(personId));
				if (selfPerson != null && accessKey != null
						&& accessKey.equals(selfPerson.getSecretKey())) {
					isValid = true;
				}

				if (!isValid || selfPerson == null) {
					System.out.println("No Auth");
					APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
					out.print(new Gson().toJson(error));
					out.flush();
					return;
				}

				selfPerson.setGcmRegId(gcmRegId);
				tx.success();
				APIStatus error = new APIStatus(200, "Success");
				out.print(new Gson().toJson(error));
				out.flush();
				
				Date computationOverDate = new Date();
				System.out.println("Total Time : "
						+ GeneralUtils.getDateDiff(computationOverDate, requestDate,
								TimeUnit.SECONDS));
			} catch (Exception e) {
				e.printStackTrace();
				tx.failure();
			}
		}
	}

}
