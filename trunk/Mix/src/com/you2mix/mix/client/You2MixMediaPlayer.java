package com.you2mix.mix.client;

import com.bramosystems.oss.player.core.client.ConfigParameter;
import com.bramosystems.oss.player.core.client.LoadException;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.core.client.TransparencyMode;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;
import com.you2mix.mix.client.model.You2MixChromelessPlayer;

public abstract class You2MixMediaPlayer {

	public static ChromelessPlayer createPlayerWidget(String urlString, String width, String height) {
		ChromelessPlayer videoWidget = null;
		try {
			PlayerParameters parameters = new PlayerParameters();
			parameters.setLoadRelatedVideos(false);
			parameters.setFullScreenEnabled(false);
			parameters.setAutoplay(false);
			videoWidget = new ChromelessPlayer(urlString.toString(),  parameters,width, height);
			videoWidget.setConfigParameter(ConfigParameter.TransparencyMode, TransparencyMode.TRANSPARENT);
			videoWidget.showLogger(false);

		} catch (PluginNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PluginVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return videoWidget;
	}
	
	public static You2MixChromelessPlayer createPreviewPlayerWidget(String urlString,int startTime, String width, String height) {
		You2MixChromelessPlayer videoWidget = null;
		
			PlayerParameters parameters = new PlayerParameters();
			parameters.setLoadRelatedVideos(false);
			parameters.setFullScreenEnabled(false);
			parameters.setAutoplay(false);
			try {
				videoWidget = new You2MixChromelessPlayer(urlString.toString(),parameters, startTime, width, height);
			} catch (PluginNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PluginVersionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			videoWidget.setConfigParameter(ConfigParameter.TransparencyMode, TransparencyMode.TRANSPARENT);
			videoWidget.showLogger(true);

		
		return videoWidget;
	}

	public static You2MixChromelessPlayer createPlayerWidget(String urlString, int startTime, String width, String height) {

		You2MixChromelessPlayer videoWidget = null;
		try {
			PlayerParameters parameters = new PlayerParameters();
			parameters.setLoadRelatedVideos(false);
			parameters.setFullScreenEnabled(false);
			parameters.setAutoplay(false);
			parameters.setStartTime(startTime);
			
			videoWidget = new You2MixChromelessPlayer(urlString.toString(),  parameters,startTime,width, height);
			videoWidget.setConfigParameter(ConfigParameter.TransparencyMode, TransparencyMode.TRANSPARENT);
			videoWidget.showLogger(true);

		} catch (PluginNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PluginVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return videoWidget;
	}
	
	
//	public static String getYouTubeURLStringFromYouTubeID(String youTubeID){
//		StringBuffer urlString = new StringBuffer("http://www.youtube.com/v/");			
//		urlString.append(youTubeID);
//		return urlString.toString();
//	}

}
