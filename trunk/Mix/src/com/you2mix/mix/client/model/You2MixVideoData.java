/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you2mix.mix.client.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.core.client.GWT;

/**
 * A client side data object representing a Sticky note.
 * 
 * @author knorton@google.com (Kelly Norton)
 */
@SuppressWarnings("serial")
public class You2MixVideoData implements Serializable {

  public interface NoteObserver {
    void onUpdate(You2MixVideoData note);
  }

  /**
   * The primary key which is always assigned by the server.
   */
  private String key;

  /**
   * The key of the Surface to which this note belongs.
   */
  private String surfaceKey;

  /**
   * The dimensions of the sticky note.
   */
  private int x, y, width, height;

  /**
   * The text content of the note.
   */
  
  private String videoKey;
  
  
  private String content;
  
  private You2MixVideo video;

  /**
   * The time of the most recent update. This value is always supplied by the
   * server.
   */
  private Date lastUpdatedAt;

  /**
   * The name of the author in a form that can be displayed in the Ui.
   */
  private String authorName;

  private String authorEmail;
  
  

  
 
  /**
   * An observer to receive callbacks whenever this {@link You2MixVideoData} is updated.
   */
  private transient NoteObserver observer;

  /**
   * Indicates whether a sticky is editable by the current author.
   */
  private transient boolean ownedByCurrentUser;

  /**
   * A constructor to be used on client-side only.
   * 
   * @param model
   * @param x
   * @param y
   * @param width
   * @param height
   */
  public You2MixVideoData(Model model, int x, int y, int width, int height) {
    assert GWT.isClient();
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    ownedByCurrentUser = true;
    this.video = new You2MixVideo( "rMaTIEKIuRI", 0, 30);
    
  }

  /**
   * A constructor to be used on server-side only.
   * 
   * @param key
   * @param x
   * @param y
   * @param width
   * @param height
   * @param content
   * @param lastUpdatedAt
   * @param authorName
   * @param ownedByCurrentUser
   */
  public You2MixVideoData(String key, int x, int y, int width, int height, String content,String youTubeId,
      Date lastUpdatedAt, String authorName, String authorEmail, You2MixVideo video) {
    assert !GWT.isClient();
    this.key = key;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.content = content;
    this.lastUpdatedAt = lastUpdatedAt;
    this.authorName = authorName;
    this.authorEmail = authorEmail;
    this.video = video;
  }

  /**
   * A default constructor to allow these objects to be serialized with GWT's
   * RPC.
   */
  @SuppressWarnings("unused")
  private You2MixVideoData() {
  }

 

public String getAuthorName() {
    return (ownedByCurrentUser) ? "You" : authorName;
  }

  public String getContent() {
    return content;
  }

  public int getHeight() {
    return height;
  }

  public String getKey() {
    return key;
  }

  public Date getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  /**
   * Gets the observer that is receiving notification when the note is modified.
   * 
   * @return
   */
  public NoteObserver getObserver() {
    return observer;
  }

  public String getSurfaceKey() {
    return surfaceKey;
  }

  public int getWidth() {
    return width;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  /**
   * Indicates whether this note is owned by the current user.
   * 
   * @return <code>true</code> if the note is owned by the current user,
   *         <code>false</code> otherwise
   */
  public boolean isOwnedByCurrentUser() {
    return ownedByCurrentUser;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Sets the observer that will receive notification when this note is
   * modified.
   * 
   * @param observer
   */
  public void setObserver(NoteObserver observer) {
    this.observer = observer;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }
  
  /**
   * @param video the video to set
   */
  public void setVideoKey(String videoKey) {
  	this.videoKey = videoKey;
  }

  /**
   * @return the video
   */
  
  
  

  /**
   * Initializes transient data structures in the object. This will be called
   * directly by the controlling model when the note is first received.
   * 
   * @param model
   *          the model that owns this {@link You2MixVideoData}
   */
  void initialize(Model model) {
    ownedByCurrentUser = model.getCurrentAuthor().getEmail()
        .equals(authorEmail);
    
  }

  /**
   * Invoked when the note has been saved to the server.
   * 
   * @param lastUpdatedAt
   *          the time that the server reported for the save
   * @return <code>this</code>, for chaining purposes
   */
  You2MixVideoData update(Date lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;

    return this;
  }

  /**
   * Invoked when the model receives notification from the server that this note
   * has been modified.
   * 
   * @param note
   *          a note containing up-to-date information about <code>this</code>
   * @return <code>this</code>, for chaining purposes
   */
  You2MixVideoData update(You2MixVideoData note) {
    if (!note.getLastUpdatedAt().equals(lastUpdatedAt)) {
      key = note.key;
      surfaceKey = note.surfaceKey;
      x = note.x;
      y = note.y;
      width = note.width;
      height = note.height;
      content = note.content;
      ownedByCurrentUser = note.ownedByCurrentUser;
      authorName = note.authorName;
      lastUpdatedAt = note.lastUpdatedAt;
      videoKey = note.videoKey;
      video = note.video;
      observer.onUpdate(this);
    }
    return this;
  }

  You2MixVideoData update(String key, Date lastUpdatedAt) {
    this.key = key;
    this.lastUpdatedAt = lastUpdatedAt;

    return this;
  }

/**
 * @param video the video to set
 */
public void setVideo(You2MixVideo video) {
	this.video = video;
}

/**
 * @return the video
 */
public You2MixVideo getVideo() {
	return video;
}






}
