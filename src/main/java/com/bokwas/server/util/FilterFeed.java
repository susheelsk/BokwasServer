package com.bokwas.server.util;
import static com.restfb.util.DateUtils.toDateFromLongFormat;

import java.util.Date;

import com.restfb.Facebook;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.NamedFacebookType;
public class FilterFeed extends NamedFacebookType {
  @Facebook
  private CategorizedFacebookType from;

  @Facebook
  private String message;

  @Facebook
  private String picture;

  @Facebook
  private String link;

  @Facebook
  private String caption;

  @Facebook
  private String description;

  @Facebook
  private String source;

  @Facebook
  private String type;

  @Facebook
  private NamedFacebookType application;

  @Facebook
  private String icon;

  @Facebook
  private String attribution;



  @Facebook("created_time")
  private String createdTime;

  @Facebook("updated_time")
  private String updatedTime;

  @Facebook("object_id")
  private String objectId;

  @Facebook("status_type")
  private String statusType;
  private static final long serialVersionUID = 3L;


  public CategorizedFacebookType getFrom() {
    return from;
  }

  /**
   * The message.
   * 
   * @return The message.
   */
  public String getMessage() {
    return message;
  }

  /**
   * If available, a link to the picture included with this post.
   * 
   * @return If available, a link to the picture included with this post.
   */
  public String getPicture() {
    return picture;
  }

  /**
   * The link attached to this post.
   * 
   * @return The link attached to this post.
   */
  public String getLink() {
    return link;
  }

  /**
   * The caption of the link (appears beneath the link name).
   * 
   * @return The caption of the link (appears beneath the link name).
   */
  public String getCaption() {
    return caption;
  }

  public String getDescription() {
    return description;
  }

  public String getSource() {
    return source;
  }

  public String getIcon() {
    return icon;
  }
  public String getType() {
    return type;
  }

  public NamedFacebookType getApplication() {
    return application;
  }

  public Date getCreatedTime() {
    return toDateFromLongFormat(createdTime);
  }

  /**
   * The time of the last comment on this post.
   * 
   * @return The time of the last comment on this post.
   */
  public Date getUpdatedTime() {
    return toDateFromLongFormat(updatedTime);
  }

  /**
   * The Facebook object id for an uploaded photo or video.
   * 
   * @return The Facebook object id for an uploaded photo or video.
   * @since 1.6.5
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * The {@code status_type} of post this is, for example {@code "approved_friend"}.
   * 
   * @return The {@code status_type} of post this is, for example {@code "approved_friend"}.
   * @since 1.6.12
   */
  public String getStatusType() {
    return statusType;
  }


}