package com.bokwas.database;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class ReturnComment implements Evaluator {

    private String commentId;

    public ReturnComment(String comment) {
        commentId=comment;
    }

    public Evaluation evaluate(Path p) {
    try{
        //prune if we have found the required number already
        if(new Comments(p.endNode()).getCommentId().equals(commentId)) return Evaluation.INCLUDE_AND_PRUNE;
    }
    catch(Exception e)
    {
        return Evaluation.EXCLUDE_AND_CONTINUE;
         	
    }
       return Evaluation.EXCLUDE_AND_CONTINUE;
    }
}