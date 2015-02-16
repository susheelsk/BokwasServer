package com.bokwas.server.backgroundtasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Comments;
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.bokwas.database.RelationType;
import com.bokwas.server.util.GCMSender;

public class AddLikesGCMTask implements Task {

	private Comments comment;
	private Posts post;
	private Person person; // guy who's liking the post

	public AddLikesGCMTask(Comments comment, Posts post, Person person) {
		super();
		this.comment = comment;
		this.post = post;
		this.person = person;
	}

	@Override
	public void execute() {
		System.out.println("Sending addLikesGCM");
		try (Transaction txOne = BokwasDB.getDatabase().beginTx()) {
			try {
				Map<String, String> params = new LinkedHashMap<>();
				if (comment == null) { // person has liked the post
					if (!post.getPostedBy().equals(person.getFbId())) {
						params.put("title", "Bokwas");
						params.put("type", "ADDLIKES_NOTI");
						params.put("message", person.getBokwasName()
								+ "  likes your post");
						params.put("likesPersonId", person.getFbId());
						params.put("likeTime", String.valueOf(System.currentTimeMillis()));
						params.put("likesPersonName", person.getBokwasName());
						params.put("postId", post.getPostId());
						params.put("avatarId", person.getBokwasAvatarId());
						String notificationId = UUID.randomUUID().toString();
						params.put("notificationId", notificationId);
						Person postPerson = new Person(
								BokwasNodeFactory.findPerson(post.getPostedBy()));
						if (postPerson != null && postPerson.getGcmRegId() != null
								&& !postPerson.getGcmRegId().equals("")) {
							Relationship rel=postPerson.getUnderlyingNode().createRelationshipTo(post.getUnderlyingNode(), RelationType.NOTIFIED);
							JSONObject notification_data=new JSONObject(params);
							JSONObject combined=new JSONObject();
							combined.put("notification_id",notificationId);
							combined.put("notification_data",notification_data);
							rel.setProperty("data", combined.toString());
							rel.setProperty("time",params.get("likeTime"));
							GCMSender sender = new GCMSender(
									postPerson.getGcmRegId(), params);
							sender.send();
						}
					}
				} else { // person has liked the comment
					if (!comment.getCommentedBy().equals(person.getFbId())) {
						params.put("title", "Bokwas");
						params.put("type", "ADDLIKES_NOTI");
						params.put("message", person.getBokwasName()
								+ "  liked your comment");
						params.put("likesPersonId", person.getFbId());
						params.put("likesPersonName", person.getBokwasName());
						params.put("postId", post.getPostId());
						params.put("likeTime", String.valueOf(System.currentTimeMillis()));
						params.put("likesCommentId", comment.getCommentId());
						params.put("avatarId", person.getBokwasAvatarId());
						Person commentPerson = new Person(
								BokwasNodeFactory.findPerson(comment
										.getCommentedBy()));
						String notificationId = UUID.randomUUID().toString();
						params.put("notificationId", notificationId);
						if (commentPerson != null
								&& commentPerson.getGcmRegId() != null
								&& !commentPerson.getGcmRegId().equals("")) {
							Relationship relComment= commentPerson.getUnderlyingNode().createRelationshipTo(comment.getUnderlyingNode(),RelationType.NOTIFIED);
							JSONObject notification_data=new JSONObject(params);
							JSONObject combined=new JSONObject();
							combined.put("notification_id",notificationId);
							combined.put("notification_data",notification_data);
							relComment.setProperty("data",combined.toString());
							relComment.setProperty("time", params.get("likeTime"));
							GCMSender sender = new GCMSender(
									commentPerson.getGcmRegId(), params);
							sender.send();
						}
					}
				}
				txOne.success();
			} catch (Exception e) {
				e.printStackTrace();
				txOne.failure();
			}
		}

	}

}
