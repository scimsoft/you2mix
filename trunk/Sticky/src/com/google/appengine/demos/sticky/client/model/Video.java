package com.google.appengine.demos.sticky.client.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;

import com.bramosystems.oss.player.core.client.ConfigParameter;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.core.client.TransparencyMode;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;
import com.bramosystems.oss.player.youtube.client.YouTubePlayer;

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
	
	
	public YouTubePlayer getVideoWidget() {

		StringBuffer videoUrl = new StringBuffer("http://www.youtube.com/v/");
		
		videoUrl.append(youTubeID);		
		YouTubePlayer swfWidget = null; 
		try {
			PlayerParameters p = new PlayerParameters();
		    p.setLoadRelatedVideos(false);
		    p.setFullScreenEnabled(false);
		    p.setAutoplay(false);
		    
			swfWidget = new ChromelessPlayer(videoUrl.toString(),p, "170", "170");
			swfWidget.setConfigParameter(ConfigParameter.TransparencyMode, TransparencyMode.TRANSPARENT);
			
			swfWidget.showLogger(false);
			
		} catch (PluginNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PluginVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return swfWidget;
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
