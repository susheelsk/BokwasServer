/*
 * Copyright (c) 2010-2013 Mark Allen.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.bokwas.facebook;
import static com.restfb.util.DateUtils.toDateFromLongFormat;
import java.util.Date;

import com.restfb.Facebook;
import com.restfb.types.NamedFacebookType;

public class FBPosts extends NamedFacebookType {
	private static final long serialVersionUID = 3L;

  @Facebook
  private CategorizedType from;
  @Facebook
  private String link;
  @Facebook
  private String message;
  @Facebook
  private String story;
  @Facebook
  private CategorizedType paging;

  @Facebook("created_time")
  private String createdTime;

  @Facebook("updated_time")
  private String updatedTime;

  
  @Facebook("status_type")
  private String statusType;

  @Facebook("type")
  private String type;
  @Facebook("picture")
  private String picture;
public int getLinkCount()
{
	if(picture.contains("?"))
	{
		return -1;
	}
	String count="";
	try{
	String []tmp=link.split("&");
	if(tmp[tmp.length-1].split("=")[0].startsWith("relevant")==false)
	{
		return -1;
	}
	 count=tmp[tmp.length-1].split("=")[1];
	}catch(Exception e)
	{
		return 0;
	}
	return Integer.parseInt(count);
}
 public String getPhoto()
 {
	 return picture;
 }
 
  public CategorizedType getFrom() {
    return from;
  }
  public CategorizedType getPaging()
  {
	  return paging;
  }

  public String getMessage() {
    return message;
  }

  public String getType() {
    return type;
  }

  public String getStory() {
    return story;
  }


  public Date getCreatedTime() {
    return toDateFromLongFormat(createdTime);
  }

  public Date getUpdatedTime() {
    return toDateFromLongFormat(updatedTime);
  }

  public String getStatusType() {
    return statusType;
  }


}