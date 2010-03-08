package com.you2mix.mix.client.model;

import com.bramosystems.oss.player.core.client.LoadException;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;

public class PreviewPlayer extends ChromelessPlayer {

	private double startTime;

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public PreviewPlayer(String videoURL, String width, String height) throws PluginNotFoundException, PluginVersionException {
		super(videoURL, width, height);
		
	}
	
	public void loadMedia(String mediaURL) throws LoadException{
		if(impl!=null)impl.loadVideoByUrl(mediaURL, this.startTime);
	}

	public void loadMedia(String youTubeURLStringFromYouTubeID, double startTime)throws LoadException {
		this.startTime=startTime;
		loadMedia(youTubeURLStringFromYouTubeID);
		
	}

	public void stopMedia(){
		
		pauseMedia();
		impl.seekTo(startTime, true);
	}
}
