package com.bokwas.database;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class BokwasPosts implements Evaluator {

	boolean isBokwasPost = false;
	long since = 0;
	String fbid = "";
	int count = 0;
	String currentName;
	boolean isbokwas = false;

	public BokwasPosts(String id, long snce, boolean isbokwaspost) {
		this.fbid = id;
		Node person = BokwasNodeFactory.findPerson(id);
		if(isbokwaspost) {
			this.currentName = person.getProperty(Person.BOKWAS_NAME).toString();
		}else {
			this.currentName = "";
		}
		this.since = snce;
		this.isbokwas = isbokwaspost;

	}

	public Evaluation evaluate(Path p) {
		try {
			Posts post = new Posts(p.endNode());
			if (post.getPostedBy().equals(fbid) && post.getPostUpdatedTime() < since) {
				if (count == 10) {
					return Evaluation.INCLUDE_AND_PRUNE;
				}
				if (isbokwas && post.isBokwasPost() && post.getPostedByName().equals(currentName)) {
					count++;
					return Evaluation.INCLUDE_AND_CONTINUE;
				} else if (isbokwas == false && post.isBokwasPost() == false) {
					count++;
					return Evaluation.INCLUDE_AND_CONTINUE;
				}
			}

		} catch (Exception e) {
			return Evaluation.EXCLUDE_AND_CONTINUE;
		}
		return Evaluation.EXCLUDE_AND_CONTINUE;

	}
}