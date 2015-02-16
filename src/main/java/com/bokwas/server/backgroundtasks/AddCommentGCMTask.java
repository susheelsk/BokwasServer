package com.bokwas.server.backgroundtasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.bokwas.database.BokwasDB;
import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Comments;
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.bokwas.database.RelationType;
import com.bokwas.server.util.GCMSender;

public class AddCommentGCMTask implements Task {

	private Comments comment;
	private Posts post;
	private Person person;
	private Person postPerson;
	private List<Person> followers = new ArrayList<Person>();

	public AddCommentGCMTask(Comments comment, Posts post, Person person) {
		this.comment = comment;
		this.post = post;
		this.person = person;
	}

	@Override
	public void execute() {
		System.out.println("Sending addCommentsGCM");
		try (Transaction txOne = BokwasDB.getDatabase().beginTx()) {
			System.out.println("in AddCommentGCMTask::execute()");
			Map<String, String> params = new LinkedHashMap<>();
			params.put("title", "Bokwas");
			params.put("type", "ADDCOMMENT_NOTI");
			params.put("message", person.getBokwasName()
					+ "  commented on your post");
			params.put("postId", post.getPostId());
			params.put("commentText", comment.getCommentText());
			params.put("commentId", comment.getCommentId());
			params.put("commentTime", String.valueOf(comment.getCommentTime()));
			params.put("commentPersonBokwasName", person.getBokwasName());
			params.put("commentPersonId", person.getFbId());
			params.put("avatarId", person.getBokwasAvatarId());
			String notificationId = UUID.randomUUID().toString();
			params.put("notificationId", notificationId);

			Node postperson = BokwasNodeFactory.findPerson(post.getPostedBy());
			if (postperson != null) {
				postPerson = new Person(postperson);
				if (postPerson.getFbId().equals(person.getFbId()) == false) {
					JSONObject notification_data = new JSONObject(params);
					JSONObject combined = new JSONObject();
					combined.put("notification_id", notificationId);
					combined.put("notification_data", notification_data);
					Relationship rel= postPerson.getUnderlyingNode().createRelationshipTo(post.getUnderlyingNode(),RelationType.NOTIFIED);
					rel.setProperty("time", params.get("commentTime"));
					rel.setProperty("data", combined.toString());
					GCMSender sender = new GCMSender(postPerson.getGcmRegId(),
							params);
					sender.send();
				}
			}
			List<String> regIds;
			regIds = getListRegIds(post);
			Set<String> set = new HashSet<String>();
			set.addAll(regIds);
			regIds.clear();
			regIds.addAll(set);
			if (person.getGcmRegId() != null
					&& regIds.contains(person.getGcmRegId())) {
				regIds.remove(person.getGcmRegId());
			}
			if (comment == null) {
				return;
			}
			System.out.println(person.getBokwasName());
			if (regIds != null && regIds.size() > 0) {
				params = new LinkedHashMap<>();
				params.put("title", "Bokwas");
				params.put("type", "ADDCOMMENT_NOTI");
				try {
					if (person.getFbId().equals(postPerson.getFbId())&& post.isBokwasPost()) {
						String gender = "";
						if (person.getGender().equals("male"))
							gender = "his";
						else
							gender = "her";
						params.put("message", person.getBokwasName()
								+ " also commented on " + gender + " post");

					} else{
						params.put("message", person.getBokwasName()
								+ " commented on " + post.getPostedByName() + "'s post");
					}
				} catch (Exception e) {
					params.put("message", person.getBokwasName()
							+ " commented on " + post.getPostedByName() + "'s post");

				}
				params.put("postId", post.getPostId());
				params.put("commentText", comment.getCommentText());
				params.put("commentId", comment.getCommentId());
				params.put("commentTime",
						String.valueOf(comment.getCommentTime()));
				params.put("commentPersonBokwasName", person.getBokwasName());
				params.put("commentPersonId", person.getFbId());
				params.put("notificationId", notificationId);

				params.put("avatarId", person.getBokwasAvatarId());
				for (Person follower : followers) {
					Relationship rel=follower.getUnderlyingNode().createRelationshipTo(post.getUnderlyingNode(), RelationType.NOTIFIED);
					JSONObject notification_data = new JSONObject(params);
					JSONObject combined = new JSONObject();
					combined.put("notification_id", notificationId);
					combined.put("notification_data", notification_data);
					rel.setProperty("time", params.get("commentTime"));
					rel.setProperty("data", combined.toString());
				}
				GCMSender sender2 = new GCMSender(regIds, params);
				sender2.send();
			}
			txOne.success();
		}
	}

	private List<String> getListRegIds(Posts post) {
		Set<String> followerSet = new HashSet<String>();
		
		if (post != null) {
			List<Comments> comments = post.getCommentsAsList();
			postPerson = new Person(BokwasNodeFactory.findPerson(post
					.getPostedBy()));
			List<String> regIds = new ArrayList<String>();
			for (Comments comment : comments) {
				String commentedBy = comment.getCommentedBy();
				Person commentPerson = new Person(
						BokwasNodeFactory.findPerson(commentedBy));
				if (commentPerson.getGcmRegId() != null
						&& !commentPerson.getGcmRegId().trim().equals("")) {
					if (commentPerson.getFbId().equals(person.getFbId()) == false) {
						if(followerSet.add(commentPerson.getFbId())){
						System.out.println("Notification sent to"
								+ commentPerson.getBokwasName());
						followers.add(commentPerson);
					}
						}
					regIds.add(commentPerson.getGcmRegId());
				}
			}
			return regIds;
		} else {
		}
		return null;

	}

}
