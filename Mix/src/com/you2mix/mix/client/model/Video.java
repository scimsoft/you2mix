package com.you2mix.mix.client.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;

@SuppressWarnings("serial")
@PersistenceCapable
public class Video  implements Serializable {

	private String key;
	
	private String youTubeID;
	
	private int startTime;
	
	private int endTime;
	
	private transient VideoObserver observer;
	
	@SuppressWarnings("unused")
	private Video(){}
	
	public Video( String youTubeId, int startTime, int endTime){
		
		this.youTubeID=youTubeId;
		this.startTime=startTime;
		this.endTime=endTime;
		 
	}

	public void setYouTubeID(String youTubeID) {
		this.youTubeID = youTubeID;
	}

	public String getYouTubeID() {
		return youTubeID;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getEndTime() {
		return endTime;
	}
	
	
	

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	
	void initialize(Model model) {
	    
	    
	  }
	
	
	
	Video update(Model model, Video video) {
	    if (!video.getLastUpdatedAt().equals(lastUpdatedAt)) {
	      youTubeID = video.youTubeID;
	      startTime = video.startTime;
	      endTime = video.endTime;
	      observer.onVideoUpdate(video);
	    }
	    return this;
	  }

	private Date lastUpdatedAt;
	public Date getLastUpdatedAt() {
    return lastUpdatedAt;
  }

	
	 /**
	 * @param observer the observer to set
	 */
	public void setObserver(VideoObserver observer) {
		this.observer = observer;
	}

	/**
	 * @return the observer
	 */
	public VideoObserver getObserver() {
		return observer;
	}
	public interface VideoObserver{
		 void onVideoUpdate(Video video);
	 }
	
}
