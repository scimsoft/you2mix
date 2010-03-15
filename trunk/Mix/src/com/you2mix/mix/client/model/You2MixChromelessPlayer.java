package com.you2mix.mix.client.model;

import com.bramosystems.oss.player.core.client.LoadException;
import com.bramosystems.oss.player.core.client.PlayException;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;
import com.google.gwt.user.client.Timer;

public class You2MixChromelessPlayer extends ChromelessPlayer {

	public interface You2MixPlayTimeListener{		
		void onCurrentPlayTimeChange(Double currentPlayTime);		
	}
	

	private int startTime;
	public int getStartTime() {
		return startTime;
	}

	private Timer playTime;
	private You2MixPlayTimeListener playTimeListener;
	public boolean isCued;
	private String videoUrl;
		

	public You2MixChromelessPlayer(String videoURL,PlayerParameters playerParameters,int startTime, String width, String height)
			throws PluginNotFoundException, PluginVersionException, LoadException {		
		super(videoURL, playerParameters, width, height);
		this.setStartTime(startTime);
		this.videoUrl = videoURL;
		
		playTime = new Timer() {
			@Override
			public void run() {				
				playTimeListener.onCurrentPlayTimeChange(getPlayPosition());				
			}
		};
		playTime.scheduleRepeating(1000);
	}
	
	
	public void stopMedia(){
		playTime.cancel();
		pauseMedia();
		impl.seekTo((double)startTime, true);
	}
	
	public void pauseMedia(){
		playTime.cancel();
		super.pauseMedia();
	}
	
	public void playMedia()throws PlayException{
		playTime.scheduleRepeating(1000);
		super.playMedia();
	}
	
	public void setStartTime(int startTime){
		this.startTime=startTime;
	}
	
	public void setYou2MixPlayTimeListener(You2MixPlayTimeListener listener){
		playTimeListener = listener;
	}

	
	
	
	
}
