package com.bokwas.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Person;
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.GetFriendsResponse;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.google.gson.Gson;

/**
 * Servlet implementation class GetFriendsApi
 */
@WebServlet("/getfriends")
public class GetFriendsApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetFriendsApi() {
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
		String personId = request.getParameter("person_id");
		String access_key = request.getParameter("access_key");
		
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /getfriends");
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
		
		if(access_key==null||access_key.equals("")) {
			System.out.println("Bad Request");
			APIStatus error = new APIStatus(ERROR_CODE.AUTH_MISSING);
			out.print(new Gson().toJson(error));
			out.flush();
			return;
		}

		Person person;
		Node nodePerson;
		boolean isValid = false;
		Date computationBeginDate = new Date();

		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
		
			nodePerson = BokwasNodeFactory.findPerson(personId);
			person = new Person(nodePerson);
			if (person != null && access_key != null
					&& access_key.equals(person.getSecretKey())) {
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
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			List<Person>friends=IteratorUtil.asList(person.getFriends());
			response.setContentType("application/json");
			String data = getJsonData(friends);
			out.print(data);
			out.flush();

			Date computationOverDate = new Date();
			System.out.println("Computation Time : "
					+ getDateDiff(computationOverDate,
							computationBeginDate, TimeUnit.SECONDS));
			System.out.println("Total Time : "
					+ getDateDiff(computationOverDate, requestDate,
							TimeUnit.SECONDS));

			tx.success();
		}
	}
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date1.getTime() - date2.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

	


	private String getJsonData(List<Person> friends) {
		GetFriendsResponse response;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			response = new GetFriendsResponse(friends, new APIStatus(
					200, "Success"));
			tx.success();
		}
		Gson gson = new Gson();
		return gson.toJson(response);
	}

}
