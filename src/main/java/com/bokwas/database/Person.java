package com.bokwas.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorUtil;

/**
 * 
 * @author sk
 * @Properties bokwasName, bokwasAvatarId, facebookName, facebookId
 */
public class Person {

	/**
	 * Properties of Person : bokwasName, bokwasAvatarId, facebookName,
	 * facebookId,
	 */

	public static final String FB_NAME = "fbname";
	public static final String BOKWAS_NAME = "bokwasname";
	public static final String FB_ID = "fbid";
	public static final String BOKWAS_AVATAR_ID = "avatarid";
	public static final String GENDER = "gender";

	public static final String PERSON_INDEX = "Person";
	public static final String PERSON_Label = "persons";
	public static final String UNIQUE_KEY = "secretkey";
	public static final String GCM_REG_ID = "gcmregid";
	public static final String PROFILE_UPDATE_DATE = "profileupdatedate";
	public HashMap<String, Integer> friends = new HashMap<String, Integer>();
	public HashMap<String, Integer> posts = new HashMap<String, Integer>();

	private final Node underlyingNode;

	public Person(Node userNode) {
		this.underlyingNode = userNode;
	}

	public String getSecretKey() {
		return (String) underlyingNode.getProperty(UNIQUE_KEY);
	}

	public String getFbName() {
		return (String) underlyingNode.getProperty(FB_NAME);
	}

	public String getGender() {
		try {
			return underlyingNode.getProperty(GENDER).toString();
		} catch (Exception e) {
			return "male";
		}
	}

	public String getGcmRegId() {
		if (underlyingNode.hasProperty(GCM_REG_ID)) {
			return (String) underlyingNode.getProperty(GCM_REG_ID);
		} else {
			return "";
		}
	}

	public String getBokwasName() {
		return (String) underlyingNode.getProperty(BOKWAS_NAME);
	}
	
	public long getProfileUpdatedDate() {
		if(underlyingNode.hasProperty(PROFILE_UPDATE_DATE)) {
			return (long) underlyingNode.getProperty(PROFILE_UPDATE_DATE);
		}
		return 0;
	}
	
	public void setProfileUpdatedDate(long profileUpdatedTimestamp) {
		underlyingNode.setProperty(PROFILE_UPDATE_DATE, profileUpdatedTimestamp);
	}

	public String getFbId() {
		return (String) underlyingNode.getProperty(FB_ID);
	}

	public String getBokwasAvatarId() {
		return (String) underlyingNode.getProperty(BOKWAS_AVATAR_ID);
	}

	public void setFbName(String fbName) {
		underlyingNode.setProperty(FB_NAME, fbName);
	}

	public void setGcmRegId(String gcmRegId) {
		underlyingNode.setProperty(GCM_REG_ID, gcmRegId);
	}

	public void setFbId(String fbId) {
		underlyingNode.setProperty(FB_ID, fbId);
	}

	public void setBokwasName(String bokwasName) {
		underlyingNode.setProperty(BOKWAS_NAME, bokwasName);
	}

	public void setBokwasAvatarId(String bokwasId) {
		underlyingNode.setProperty(BOKWAS_AVATAR_ID, bokwasId);

	}

	public Node getUnderlyingNode() {
		return underlyingNode;
	}

	public boolean isBokwasUser() {
		if (getBokwasName() == null || getBokwasName().trim().equals("")) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return underlyingNode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Person
				&& underlyingNode.equals(((Person) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Person [ FB_NAME : " + getFbName() + " , BOKWAS_NAME : "
				+ getBokwasName() + "]";
	}

	public void addPost(Posts post) {
		underlyingNode.createRelationshipTo(post.getUnderlyingNode(),
				RelationType.SUBSCRIBE_TO);
		// MAGIC Code. DO-NOT DELETE THE COMMENT
		/*
		 * String
		 * query="MATCH (person:persons{ fbid:'"+fbid+"' }),(post:posts{ postid:'"
		 * +postid+"' })" +"MERGE (person)-[r:SUBSCRIBE_TO]->(post) RETURN r";
		 * ExecutionEngine engine=BokwasDB.getEngine(); engine.execute(query);
		 */
	}

	public void addPrivateMessage(PrivateMessage message) {
		underlyingNode.createRelationshipTo(message.getUnderlyingNode(),
				RelationType.PRIVATE_MESSAGE_OF);
	}

	
	
	public List<PrivateMessage> getPrivateMessages() {
		TraversalDescription travDesc = BokwasDB.getDatabase()
				.traversalDescription().breadthFirst()
				.relationships(RelationType.PRIVATE_MESSAGE_OF)
				.uniqueness(Uniqueness.NODE_GLOBAL)
				.evaluator(Evaluators.toDepth(1)).sort(new Comparator<Path>() {

					@Override
					public int compare(Path p1, Path p2) {
						Date d1 = new Date((long) p1.endNode().getProperty(PrivateMessage.MESSAGE_TIME));
						Date d2 = new Date((long) p2.endNode().getProperty(PrivateMessage.MESSAGE_TIME));
						return d1.compareTo(d2);
					}
				}).evaluator(Evaluators.excludeStartPosition());

		return IteratorUtil.asList(createMessagesFromPath(travDesc
				.traverse(underlyingNode)));
	}
	
	public JSONArray getNotifications(){
		JSONArray jarr=new JSONArray();
		Iterable<Relationship> relations = underlyingNode.getRelationships(RelationType.NOTIFIED);
		for(Relationship notifications:relations){
			JSONObject dataArray=new JSONObject(notifications.getProperty("data").toString());
			jarr.put(dataArray);
		}
		return jarr;
	}
	
	public boolean isNameTaken(String newName){
		Iterable<Person>friends=getFriends();
		for(Person p:friends){
			if(p.getBokwasName().equalsIgnoreCase(newName)){
				return true;
			}
		}
		return false;
	}
	public void deleteNotifications(){
		Iterable<Relationship> relations = underlyingNode.getRelationships(RelationType.NOTIFIED);
		List<NotificationItem> notifs=new ArrayList<NotificationItem>();
		for(Relationship notifications:relations){
			notifs.add(new NotificationItem(notifications));
		}
		System.out.println(notifs.size());
		
		if (notifs.size() > 15) {
			Collections.sort(notifs, new Comparator<NotificationItem>() {

				@Override
				public int compare(NotificationItem o1, NotificationItem o2) {
					Date d1 = new Date(Long.parseLong(o1.getTime()));
					Date d2 = new Date(Long.parseLong(o2.getTime()));
					return d1.compareTo(d2);
				}
			});
			System.out.println(notifs.size());
				int size=notifs.size()-15;
			for(int k=0;k<size;k++){
				NotificationItem notificationItem=notifs.get(k);
				notificationItem.getEdge().delete();
			}

		}
		
	}
	public void deleteAllPrivateMessages() {
		Iterable<Relationship> readMessages=underlyingNode.getRelationships(Direction.BOTH, RelationType.PRIVATE_MESSAGE_OF);
		for(Relationship message : readMessages) {
			Node messageNode=message.getOtherNode(underlyingNode);
			message.delete();
			messageNode.delete();
		}
	}
	
	private IterableWrapper<PrivateMessage, Path> createMessagesFromPath(
			Traverser iterableToWrap) {
		return new IterableWrapper<PrivateMessage, Path>(iterableToWrap) {
			@Override
			protected PrivateMessage underlyingObjectToObject(Path path) {
				PrivateMessage msg=new PrivateMessage(path.endNode());
				return msg;
			}
		};
	}

	public void addNotification(Notification notification) {
		underlyingNode.createRelationshipTo(notification.getUnderlyingNode(),
				RelationType.NOTIFY);
	}

	public void addBokwasPost(Posts p) {
		/*
		 * String
		 * query="MATCH (person:persons{ fbid:'"+fbid+"' }),(post:posts{ postid:'"
		 * +postid+"' })" +"MERGE (person)-[r:BOKWAS_POST]->(post) RETURN r";
		 * ExecutionEngine engine=BokwasDB.getEngine(); engine.execute(query);
		 */
		underlyingNode.createRelationshipTo(p.getUnderlyingNode(),
				RelationType.SUBSCRIBE_TO);
		Iterable<Person> friends = getFriends();
		for (Person me : friends) {
			me.underlyingNode.createRelationshipTo(p.getUnderlyingNode(),
					RelationType.SUBSCRIBE_TO);
		}
	}

	public boolean IsAlreadyFriend(Person person, Node friendNode) {
		Iterable<Relationship> r = person.underlyingNode.getRelationships();
		for (Relationship check : r) {
			if (check.getOtherNode(person.underlyingNode).equals(friendNode))
				return true;
		}
		return false;
	}

	public void addFriend(Person self, Person otherPerson) {
		if (!this.equals(otherPerson)) {

			self.underlyingNode.createRelationshipTo(
					otherPerson.underlyingNode, RelationType.FRIENDS_WITH);
		}/*
		 * if (!this.equals(otherPerson)) { String
		 * query="MATCH (person:Person { fbid:'"
		 * +self.getFbId()+"' }),(friend:Person{ fbid:'"
		 * +otherPerson.getFbId()+"' })"
		 * +"MERGE (person)-[r:FRIENDS_WITH]->(friend) RETURN r";
		 * ExecutionEngine engine=BokwasDB.getEngine(); engine.execute(query); }
		 */
	}

	public int getNrOfFriends() {
		return IteratorUtil.count(getFriends());
	}

	public Iterable<Posts> getPostsSince(long since) {

		TraversalDescription travDesc = BokwasDB.getDatabase()
				.traversalDescription();
		travDesc = travDesc.breadthFirst()
				.relationships(RelationType.SUBSCRIBE_TO)
				.uniqueness(Uniqueness.NODE_PATH)
				.evaluator(new PagingHelper(since))
				.evaluator(Evaluators.toDepth(1)).sort(new Comparator<Path>() {

					@Override
					public int compare(Path p1, Path p2) {
						Date d1 = new Date((long) p1.endNode().getProperty(
								Posts.UPDATED_TIME));
						Date d2 = new Date((long) p2.endNode().getProperty(
								Posts.UPDATED_TIME));
						return d1.compareTo(d2);
					}
				}).evaluator(Evaluators.excludeStartPosition());
		return IteratorUtil.asList(createPostsFromPath(travDesc
				.traverse(underlyingNode)));
	}

	public Iterable<Posts> getPosts() {
		TraversalDescription travDesc = BokwasDB.getDatabase()
				.traversalDescription();
		travDesc = travDesc.breadthFirst()
				.relationships(RelationType.SUBSCRIBE_TO)
				.uniqueness(Uniqueness.NODE_PATH)
				.evaluator(Evaluators.toDepth(1)).sort(new Comparator<Path>() {

					@Override
					public int compare(Path p1, Path p2) {
						Date d1 = new Date((long) p1.endNode().getProperty(
								Posts.UPDATED_TIME));
						Date d2 = new Date((long) p2.endNode().getProperty(
								Posts.UPDATED_TIME));
						return d1.compareTo(d2);
					}
				}).evaluator(Evaluators.excludeStartPosition());
		return IteratorUtil.asList(createPostsFromPath(travDesc
				.traverse(underlyingNode)));
	}

	public Iterable<Posts> getPosts(int numMostRecentPosts) {
		TraversalDescription travDesc = BokwasDB.getDatabase()
				.traversalDescription().breadthFirst()
				.relationships(RelationType.SUBSCRIBE_TO)
				.uniqueness(Uniqueness.NODE_PATH)
				// .evaluator(new LimitNodeEvaluator(numMostRecentPosts))
				.evaluator(Evaluators.toDepth(1)).sort(new Comparator<Path>() {

					@Override
					public int compare(Path p1, Path p2) {

						Date d1 = new Date((long) p1.endNode().getProperty(
								Posts.UPDATED_TIME));
						Date d2 = new Date((long) p2.endNode().getProperty(
								Posts.UPDATED_TIME));
						return d1.compareTo(d2);
					}
				})
				// .evaluator(new LimitNodeEvaluator(numMostRecentPosts))
				.evaluator(Evaluators.excludeStartPosition());

		return IteratorUtil.asList(createPostsFromPath(travDesc
				.traverse(underlyingNode)));
	}

	public List<Posts> getBokwasPosts(String postPerson, String since,
			String isbokwaspost) {
		long sinc = Long.parseLong(since);
		boolean isbokwas = false;
		if (isbokwaspost.equals("true")) {
			isbokwas = true;
		} else {
			isbokwas = false;
		}
		TraversalDescription travDesc = BokwasDB.getDatabase()
				.traversalDescription().breadthFirst()
				.relationships(RelationType.SUBSCRIBE_TO)
				.uniqueness(Uniqueness.NODE_PATH)
				.evaluator(new BokwasPosts(postPerson, sinc, isbokwas))
				// .evaluator(new LimitNodeEvaluator(numMostRecentPosts))
				.evaluator(Evaluators.toDepth(1)).sort(new Comparator<Path>() {
					@Override
					public int compare(Path p1, Path p2) {
						Date d1 = new Date((long) p1.endNode().getProperty(
								Posts.UPDATED_TIME));
						Date d2 = new Date((long) p2.endNode().getProperty(
								Posts.UPDATED_TIME));
						return d1.compareTo(d2);
					}
				}).evaluator(Evaluators.excludeStartPosition());

		return IteratorUtil.asList(createPostsFromPath(travDesc
				.traverse(underlyingNode)));
	}

	public Posts getPost(String postId) {
		Node postNode=BokwasNodeFactory.findPost(postId);
		/*
		 * String query="START post=node:Post(postid='"+postId+"') RETURN post";
		 * ExecutionEngine e=BokwasDB.getEngine(); ExecutionResult
		 * r=e.execute(query); Iterator<Node> post=r.columnAs("post");
		 */
		// List<Posts> selfPosts = IteratorUtil.asList(getPosts());
		if(postNode !=null){
			return new Posts(postNode);
		}
		return null;
	}

	public Iterable<Person> getFriends() {
		return getFriendsByDepth(1);
	}

	public Person getFriend(String fbId) {
		Node friendNode=BokwasNodeFactory.findPerson(fbId);
		if(friendNode != null){
			return new Person(friendNode);
		}
			
//		Iterable<Person> friendIterable = getFriends();
//		Iterator<Person> friendIterator = friendIterable.iterator();
//		while (friendIterator.hasNext()) {
//			Person friend = friendIterator.next();
//			if (friend.getFbId().equals(fbId)) {
//				return friend;
//			}
//		}
		return null;
	}

	public HashMap<Posts, List<Comments>> getFriendsPostsWithComments() {
		List<Posts> posts = IteratorUtil.asList(getPosts((int) 30));
		/*
		 * String query="MATCH ({ fbid: '"+self.getFbId()+
		 * "' })-[:SUBSCRIBE_TO|:BOKWAS_POST]->(posts) return posts ORDER BY posts.postupdatetime"
		 * ; ExecutionEngine engine=BokwasDB.getEngine(); ExecutionResult
		 * result=engine.execute(query); Iterator<Node> n_column =
		 * result.columnAs( "posts" ); List<Posts>posts=new ArrayList<Posts>();
		 * for(Node post:IteratorUtil.asIterable( n_column )) { if(post!=null)
		 * posts.add(new Posts(post)); }
		 */
		if (posts != null && posts.size() > 31) {
			posts = posts.subList(posts.size() - 31, posts.size());
		}
		HashMap<Posts, List<Comments>> map = new HashMap<Posts, List<Comments>>();
		for (Posts post : posts) {
			List<Comments> comments = post.getCommentsAsList();
			map.put(post, comments);
		}
		return map;
	}

	public HashMap<Posts, List<Comments>> getFriendsPostSince(String since) {
		List<Posts> posts = IteratorUtil.asList(getPostsSince(Long
				.parseLong(since)));
		HashMap<Posts, List<Comments>> map = new HashMap<Posts, List<Comments>>();
		for (Posts post : posts) {
			List<Comments> comments = post.getCommentsAsList();
			map.put(post, comments);
		}
		return map;
	}

	private List<Person> getFriendsByDepth(int depth) {
		TraversalDescription travDesc = BokwasDB.getDatabase()
				.traversalDescription().breadthFirst()
				.relationships(RelationType.FRIENDS_WITH)
				.uniqueness(Uniqueness.NODE_GLOBAL)
				.evaluator(Evaluators.toDepth(depth))
				.evaluator(Evaluators.excludeStartPosition());

		return IteratorUtil.asList(createPersonsFromPath(travDesc
				.traverse(underlyingNode)));
	}

	public HashMap<String, Integer> friendlist() {
		getFriends();
		return friends;
		/*
		 * List<String>ppl_id=new ArrayList<String>(); for(Person p:ppl) {
		 * ppl_id.add(p.getFbId()); } return ppl_id;
		 */
	}

	private IterableWrapper<Person, Path> createPersonsFromPath(
			Traverser iterableToWrap) {
		return new IterableWrapper<Person, Path>(iterableToWrap) {
			@Override
			protected Person underlyingObjectToObject(Path path) {
				Person p = new Person(path.endNode());
				friends.put(p.getFbId(), 1);
				return p;
			}
		};
	}

	public void getBokwasPostsAndConnect() {
		String query = "MATCH ({ fbid: '"
				+ getFbId()
				+ "' })-[:FRIENDS_WITH]->(friends)-[:SUBSCRIBE_TO]->(p) where p.isbokwaspost=true and p.postedby=friends.fbid return p ORDER BY p.postupdatetime";
		ExecutionEngine engine = BokwasDB.getEngine();
		ExecutionResult result = engine.execute(query);
		Iterator<Node> n_column = result.columnAs("p");
		for (Node post : IteratorUtil.asIterable(n_column)) {
			if (post != null)
				underlyingNode.createRelationshipTo(post,
						RelationType.SUBSCRIBE_TO);
		}
		return;

	}

	public HashMap<String, Integer> getPostIds() {
		getPosts();
		return this.posts;
	}

	private IterableWrapper<Posts, Path> createPostsFromPath(
			Traverser iterableToWrap) {
		return new IterableWrapper<Posts, Path>(iterableToWrap) {
			@Override
			protected Posts underlyingObjectToObject(Path path) {
				Posts po = new Posts(path.endNode());
				posts.put(po.getPostId(), 1);
				return po;
			}
		};
	}
}
