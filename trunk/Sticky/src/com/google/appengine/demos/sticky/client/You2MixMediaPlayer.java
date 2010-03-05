package com.google.appengine.demos.sticky.client;

import com.bramosystems.oss.player.core.client.ConfigParameter;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.core.client.TransparencyMode;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;

public abstract class You2MixMediaPlayer {

	public static You2MixChromelessPlayer createPlayerWidget(String urlString, String width, String height) {
		You2MixChromelessPlayer playerWidget = createPlayerWidget(urlString, 0, width, height);
		return playerWidget;
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

}
