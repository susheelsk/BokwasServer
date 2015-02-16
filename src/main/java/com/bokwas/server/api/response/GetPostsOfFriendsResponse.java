package com.bokwas.server.api.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;

import com.bokwas.database.BokwasNodeFactory;
import com.bokwas.database.Comments;
import com.bokwas.database.Person;
import com.bokwas.database.Posts;
import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class GetPostsOfFriendsResponse {
	private ArrayList<Post> posts = new ArrayList<Post>();
	private APIStatus status;

	public GetPostsOfFriendsResponse(HashMap<Posts, List<Comments>> map,
			APIStatus status) {
		for (Map.Entry<Posts, List<Comments>> entry : map.entrySet()) {
			Posts friendPost = entry.getKey();
			List<Comment> postCommentList = new ArrayList<Comment>();
			
			for (Comments comment : entry.getValue()) 
			{
				List<Like>commentlikes=new ArrayList<Like>();
				HashMap<String,String>likes=comment.getLikes();
				for(Map.Entry<String,String> entry1 : likes.entrySet())
				{
					String []result=entry1.getKey().split("_");
					commentlikes.add(new Like(result[0],result[1],entry1.getValue()));
				}
				postCommentList.add(new Comment(comment.getCommentId(), comment
						.getCommentTime(), comment.getCommentText(), comment
						.getCommentedBy(), commentlikes));
			}
			List<Like>postlikes=new ArrayList<Like>();
			HashMap<String,String>plikes=friendPost.getLikes();
			for(Map.Entry<String,String> entry2 : plikes.entrySet())
			{
				String []result=entry2.getKey().split("_");
				postlikes.add(new Like(result[0],result[1],entry2.getValue()));
			}
			String bokwasName="";
			String avatarid="";
			if(friendPost.isBokwasPost())
			{
			Node friend=BokwasNodeFactory.findPerson(friendPost.getPostedBy());
			if(friend!=null)
			{
			bokwasName=friend.getProperty("bokwasname").toString();
			avatarid=friend.getProperty("avatarid").toString();
			}
			}
			else
			{
				bokwasName=friendPost.getPostedByName();		
			}
			String photo_url="";
			if(friendPost.getType().equals("photo"))
			{
				photo_url=friendPost.getPhotoUrl();
			}
		
			posts.add(new Post(friendPost.getPostId(), friendPost.getPostTime(), friendPost
					.getPostText(), friendPost.getPostedBy(), friendPost
					.isBokwasPost(),bokwasName,avatarid,friendPost.getPostUpdatedTime(),friendPost.getFbPicture(),friendPost.getType(),photo_url,postlikes, postCommentList));
		}
		Collections.sort(posts, new PostComparator());
		this.status = status;
	}
	private class PostComparator implements
	Comparator<Post> {
public int compare(Post a,
		Post b) {
	Date dateA = new Date(Long.valueOf(a.updated_time));
	Date dateB = new Date(Long.valueOf(b.updated_time));
	return dateB.compareTo(dateA);
}
}

		private class Post {

		@Expose private String post_id;
		@Expose private long created_time;
		@Expose private String message;
		@Expose private String posted_by;
		//@Expose private String likes="";
		@Expose private boolean isBokwasPost;
		@Expose private String name="";
		@Expose  private String avatar_id;
		@Expose private String profile_picture;
		
		private long updated_time;
		@Expose private List<Comment> comments;
		@Expose private List<Like> likes;
		@Expose private String type;
		@Expose private String picture;

		public Post(String post_id, long created_time, String message,
				String posted_by, boolean isBokwasPost,String bokwasname, String avatarid,long update_time,String pic_link,String type,String photo_url, List<Like> likes, List<Comment> comments) {
			super();
			this.post_id = post_id;
			this.created_time = created_time;
			this.message = message;
			this.posted_by = posted_by;
			this.isBokwasPost = isBokwasPost;
			this.name=bokwasname;
			this.avatar_id=avatarid;
			this.profile_picture=pic_link;
			this.likes=likes;
			this.updated_time = update_time;
			this.comments = comments;
			this.type=type;
			if(type.equals("photo"))
				this.picture=photo_url.replace("130x130","320x320");
			
		}

	}

	private class Comment {

		private String comment_id;
		private long created_time;
		private String message;
		private String commented_by;
		private String bokwas_name;
		private String avatar_id;
		@Expose private List<Like> likes;

		public Comment(String comment_id, long l, String message,
				String commented_by, List<Like> likes) {
			super();
			this.comment_id = comment_id;
			this.created_time = l;
			this.message = message;
			this.commented_by = commented_by;
			
			this.likes=likes ;
			Person p=new Person(BokwasNodeFactory.findPerson(commented_by));
			this.bokwas_name=p.getBokwasName();
			this.avatar_id=p.getBokwasAvatarId();
		}
	}
	private class Like
	{
		private String fbid;
		private String name;
		private String avatar_id;
		public Like(String fbid,String avatarid,String name)
		{
			this.fbid=fbid;
			this.name=name;
			this.avatar_id=avatarid;
		}
		
	}

}
