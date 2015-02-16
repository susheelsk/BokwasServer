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
import com.bokwas.server.api.response.APIStatus;
import com.bokwas.server.api.response.APIStatus.ERROR_CODE;
import com.google.gson.Gson;

/**
 * Servlet implementation class UpdateProfileInfoApi
 */
@WebServlet("/updateprofileinfo")
public class UpdateProfileInfoApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateProfileInfoApi() {
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
		String personId = request.getParameter("person_id");
		String access_key = request.getParameter("access_key");
		String newName = request.getParameter("bokwas_name");
		String newAvatarId = request.getParameter("bokwas_avatar_id");
		PrintWriter out = response.getWriter();

		System.out.println("doPost called on /updateprofileinfo");
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {

			String paramName = (String) en.nextElement();
			System.out.println(paramName + " = "
					+ request.getParameter(paramName));

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
			Date profileUpdatedDate = new Date(person.getProfileUpdatedDate());
			int dateDiff = (int) getDateDiff(computationBeginDate, profileUpdatedDate, TimeUnit.DAYS);
			System.out.println("Date diff : "+dateDiff);
			if(dateDiff < 7) {
				System.out.println("Profile updated before 7 days");
				APIStatus error = new APIStatus(1100,"Profile was updated recently. You can change only after "+(7-dateDiff)+" days");
				out.print(new Gson().toJson(error));
				out.flush();
				return;
			}
			if(newName.trim().equalsIgnoreCase(person.getFbName()) || person.getFbName().contains(newName)){
				System.out.println("Matches with Facebook name");
				APIStatus error = new APIStatus(1100,"You cannot choose a name which matches your facebook name");
				out.print(new Gson().toJson(error));
				out.flush();
				return;
				
			}
			if(person.isNameTaken(newName)){
				
				System.out.println("Duplicate name exists in network");
				APIStatus error = new APIStatus(1100,"Sorry! One of your friends has already taken this name");
				out.print(new Gson().toJson(error));
				out.flush();
				return;
		
			}
			
			person.setBokwasName(newName);
			person.setBokwasAvatarId(newAvatarId);
			person.setProfileUpdatedDate(computationBeginDate.getTime());
			tx.success();
		}
		
		APIStatus error = new APIStatus(200,"Success");
		out.print(new Gson().toJson(error));
		out.flush();
		
		Date computationOverDate = new Date();
		System.out.println("Computation Time : "
				+ getDateDiff(computationOverDate, computationBeginDate,
						TimeUnit.SECONDS));
		System.out.println("Total Time : "
				+ getDateDiff(computationOverDate, requestDate,
						TimeUnit.SECONDS));
	}
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date1.getTime() - date2.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

}
