package com.google.appengine.demos.sticky.client;

import com.bramosystems.oss.player.core.client.AbstractMediaPlayer;
import com.bramosystems.oss.player.core.client.ConfigParameter;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.core.client.TransparencyMode;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;

public abstract class You2MixMediaPlayer {

	public static AbstractMediaPlayer createPlayerWidget(String urlString, String width, String height) {
		ChromelessPlayer videoWidget = null;
		try {
			PlayerParameters parameters = new PlayerParameters();
			parameters.setLoadRelatedVideos(false);
			parameters.setFullScreenEnabled(false);
			parameters.setAutoplay(false);
			videoWidget = new ChromelessPlayer(urlString.toString(),
					parameters, width, height);
			videoWidget.setConfigParameter(ConfigParameter.TransparencyMode,
					TransparencyMode.TRANSPARENT);
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
