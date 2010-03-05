package com.you2mix.mix.client.model;

import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;

public class You2MixMiniPLayer extends ChromelessPlayer {

	public You2MixMiniPLayer(String videoURL, PlayerParameters playerParameters,
			String width, String height) throws PluginNotFoundException,
			PluginVersionException {
		//panel.add()
		super(videoURL, playerParameters, width, height);
		
	}

}
