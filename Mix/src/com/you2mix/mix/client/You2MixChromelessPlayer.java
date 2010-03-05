package com.you2mix.mix.client;

import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;

public class You2MixChromelessPlayer extends ChromelessPlayer {

	

	private int startTime;

	public You2MixChromelessPlayer(String videoURL,PlayerParameters playerParameters,int startTime, String width, String height)
			throws PluginNotFoundException, PluginVersionException {		
		super(videoURL, playerParameters, width, height);
		this.startTime = startTime;		
	}
	
	public void stopMedia(){
		
		pauseMedia();
		impl.seekTo(Double.parseDouble(Integer.toString(startTime)), true);
	}
	
	public void setStartTime(int startTime){
		this.startTime=startTime;
	}
}
