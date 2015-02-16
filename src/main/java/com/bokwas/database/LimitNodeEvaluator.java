package com.bokwas.database;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class LimitNodeEvaluator implements Evaluator {

    private int peopleCount;
    private int maxPeople;
    
    public LimitNodeEvaluator(int max) {
        maxPeople = max;
        this.peopleCount = 0;
        
    }

    public Evaluation evaluate(Path p) {
        if(peopleCount > maxPeople)
        {
        	return Evaluation.EXCLUDE_AND_PRUNE;
        }
        else
        {
        	peopleCount++;
            return Evaluation.INCLUDE_AND_CONTINUE;
        }

    }
}