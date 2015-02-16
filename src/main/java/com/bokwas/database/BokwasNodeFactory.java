package com.bokwas.database;

import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.UniqueFactory;

public class BokwasNodeFactory {

	public static Node createFriendNode(final String friendFbId,
			final String friendName) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(
					BokwasDB.getDatabase(), Person.PERSON_INDEX) {
				@Override
				protected void initialize(Node created,
						Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label(Person.PERSON_Label));
					created.setProperty(Person.FB_ID,
							properties.get(Person.FB_ID));
				}
			};
			
			Node node = factory.getOrCreate(Person.FB_ID, friendFbId);
			node.setProperty(Person.FB_NAME, friendName);
			node.setProperty(Person.BOKWAS_NAME, "");
			node.setProperty(Person.BOKWAS_AVATAR_ID, "");
			tx.success();
			return node;
		}
	}

	public static Node createSelfNode(final String friendFbId,
			final String bokwasName, final String bokwasAvatarId,
			final String friendName, final String gender,final String gcmRegId, final String key) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(
					BokwasDB.getDatabase(), Person.PERSON_INDEX) {
				@Override
				protected void initialize(Node created,
						Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label(Person.PERSON_Label));
					created.setProperty(Person.FB_ID,
							properties.get(Person.FB_ID));
				}
			};
			Node node = factory.getOrCreate(Person.FB_ID, friendFbId);
			node.setProperty(Person.FB_NAME, friendName);
			node.setProperty(Person.BOKWAS_AVATAR_ID, bokwasAvatarId);
			node.setProperty(Person.BOKWAS_NAME, bokwasName);
			node.setProperty(Person.UNIQUE_KEY, key);
			node.setProperty(Person.GCM_REG_ID, gcmRegId);
			node.setProperty(Person.GENDER, gender);
			node.setProperty(Person.PROFILE_UPDATE_DATE, System.currentTimeMillis());
			tx.success();
			return node;
		}
	}

	public static Node findPerson(String fbId) {
		Node foundUser = null;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			Index<Node> nodeIndex = BokwasDB.getDatabase().index()
					.forNodes(Person.PERSON_INDEX);
			foundUser = nodeIndex.get(Person.FB_ID, fbId).getSingle();
			tx.success();
		}
		return foundUser;
	}
	public static Node findPost(String postid) {
		Node foundUser = null;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			Index<Node> nodeIndex = BokwasDB.getDatabase().index()
					.forNodes(Posts.POSTS_INDEX);
			foundUser = nodeIndex.get(Posts.POST_ID, postid).getSingle();
			tx.success();
		}
		return foundUser;
	}
	public static Node findNotification(String fbid) {
		Node foundUser = null;
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			Index<Node> nodeIndex = BokwasDB.getDatabase().index()
					.forNodes(Notification.NOTIFICATION_INDEX);
			foundUser = nodeIndex.get(Notification.NOTIFICATION_INDEX, fbid).getSingle();
			tx.success();
		}
		return foundUser;
	}

}
