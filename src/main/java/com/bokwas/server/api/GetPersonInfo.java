package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
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
import com.bokwas.server.api.response.PersonInfoResponse;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.google.gson.Gson;

/**
 * Servlet implementation class GetPersonInfo
 */
@WebServlet("/getpersoninfo")
public class GetPersonInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetPersonInfo() {
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
		String personid = request.getParameter("person_id");
		PrintWriter out = response.getWriter();
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

		}

		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			Node personNode = BokwasNodeFactory.findPerson(personid);
			Person self = null;
			if (personNode != null) {
				self = new Person(personNode);
				/*int friends = self.friendlist().size();
				if (friends < 5) {
					APIStatus error = new APIStatus(
							403,
							"You don't have enough friends on Bokwas yet. You need "
									+ (5 - friends)
									+ " more friends to enable this app! We will notify you as soon as you they have joined bokwas.");
					out.print(new Gson().toJson(error));
					out.flush();
					return;
				}*/
				HashMap<String, String> mydetails = new HashMap<String, String>();
				mydetails.put("bokwas_name", self.getBokwasName());
				mydetails.put("bokwas_avatar_id", self.getBokwasAvatarId());
				response.setContentType("application/json");
				System.out.println(self.getBokwasName());
				String data = getJsonData(mydetails);
				out.print(data);
				out.flush();

			} else {
				APIStatus error = new APIStatus(ERROR_CODE.PERSON_NOT_FOUND);
				out.print(new Gson().toJson(error));
				out.flush();
			}
			tx.success();
		}
	}

	private String getJsonData(HashMap<String, String> myinfo) {
		PersonInfoResponse personresponse;
		personresponse = new PersonInfoResponse(myinfo, new APIStatus(200,
				"Success"));
		Gson gson = new Gson();
		return gson.toJson(personresponse);
	}

}
