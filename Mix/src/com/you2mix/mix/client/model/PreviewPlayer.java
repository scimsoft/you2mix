package com.you2mix.mix.client.model;

import com.bramosystems.oss.player.core.client.LoadException;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.core.event.client.PlayStateEvent;
import com.bramosystems.oss.player.core.event.client.PlayStateHandler;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;
import com.bramosystems.oss.player.youtube.client.YouTubePlayer;

public class PreviewPlayer extends YouTubePlayer implements PlayStateHandler{

	private double startTime;
	private PlayStateHandler statehandler;
	public boolean isCued;

	public void setStatehandler(PlayStateHandler statehandler) {
		this.statehandler = statehandler;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public PreviewPlayer(String videoURL,int startTime, String width, String height) throws PluginNotFoundException, PluginVersionException {
		super(videoURL, width, height);
		setStatehandler(this);
		this.startTime = (double)startTime;
		
	}
	
	public void loadMedia(String mediaURL) throws LoadException{
		if(impl!=null)impl.loadVideoByUrl(mediaURL, this.startTime);
	}

	public void loadMedia(String youTubeURLStringFromYouTubeID, int startTime)throws LoadException {
		this.startTime=(double)startTime;
		loadMedia(youTubeURLStringFromYouTubeID);
		
	}

	public void stopMedia(){		
		impl.seekTo(startTime, true);
		pauseMedia();
		isCued = true;
		
	}
	@Override
	public void onPlayStateChanged(PlayStateEvent event) {
		if (event.getPlayState() == PlayStateEvent.State.Started && !isCued) {
			System.out.println("Stopped previewplayer class");
			((PreviewPlayer) event.getSource()).stopMedia();
		}
	}
//	@Override
//    protected String getNormalizedVideoAppURL(String videoURL, PlayerParameters playerParameters) {
//        playerParameters.setPlayerAPIId(apiId);        
//        return "http://www.youtube.com/v/"+videoURL+"&version=2&enablejsapi=1&playerapiid=" +
//                playerParameters.getPlayerAPIId();
//    }
}
