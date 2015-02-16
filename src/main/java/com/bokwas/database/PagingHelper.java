package com.bokwas.database;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class PagingHelper implements Evaluator {

    private long get_since;
    private int total;

    public PagingHelper(long since) {
        get_since = since;
        total=0;
    }


    public Evaluation evaluate(Path p) {
    try{
    	
        //prune if we have found the required number already
    	if(total > 15)
    	{
    		return Evaluation.EXCLUDE_AND_PRUNE;
        	
    	}
    
    	if(new Posts(p.endNode()).getPostUpdatedTime() < get_since )
        	{
    		total++;
        	return Evaluation.INCLUDE_AND_CONTINUE;
        	}
        		
    }
    catch(Exception e)
    {
        return Evaluation.EXCLUDE_AND_CONTINUE;
         	
    }
       return Evaluation.EXCLUDE_AND_CONTINUE;
    }
}