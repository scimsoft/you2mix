package com.you2mix.mix.client;

import java.util.ArrayList;

import com.bramosystems.oss.player.core.client.LoadException;
import com.bramosystems.oss.player.core.client.PlayException;
import com.bramosystems.oss.player.core.client.skin.CustomPlayerControl;
import com.bramosystems.oss.player.core.event.client.PlayerStateEvent;
import com.bramosystems.oss.player.core.event.client.PlayerStateHandler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.you2mix.mix.client.You2MixAnimatedPadPanel.VideoMixPositionObserver;
import com.you2mix.mix.client.model.Model;
import com.you2mix.mix.client.model.PreviewPlayer;
import com.you2mix.mix.client.model.You2MixVideo;

public class You2MixPreviewView extends FlowPanel implements VideoMixPositionObserver {

	private static final String PREVIEW_PLAYER_FOREGROUND_INDEX = "100000";
	private static final String PREVIEW_PLAYER_BACKGROUND_INDEX = "99999";

	private PreviewPlayer previewPlayer;
	private FlowPanel previewPlayerpanel;
	private FlowPanel previewPlayerpanel2;
	private PreviewPlayer previewPlayer2;
	private ArrayList<You2MixVideo> mixdata;

	private Timer mixTimer;
	private double firstPlayerEndTime;
	protected double secondPlayerEndTime;
	private Style player1style;
	private Style player2style;
	private int currentVideo = -1;

	public You2MixPreviewView(SurfaceView surfaceView) {
		mixTimer = new Timer() {
			@Override
			public void run() {
				if (previewPlayer.getPlayPosition() / 1000 > firstPlayerEndTime) {
					switchToSecondPlayer();

				}
				if (previewPlayer2.getPlayPosition() / 1000 > secondPlayerEndTime) {
					switchToFirstPlayer();
				}
			}
		};

		Label title = new Label();
		title.setStyleName("preview-title");
		title.setText("Preview");

		Label title2 = new Label();
		title2.setStyleName("preview-title");
		title2.setText("Preview");

		previewPlayerpanel = new FlowPanel();
		previewPlayerpanel.setSize("310", "350");
		previewPlayerpanel.setStyleName("preview-panel");
		previewPlayerpanel.add(title);

		previewPlayer = You2MixMediaPlayer.createPreviewPlayerWidget("", "300", "300");

		previewPlayerpanel.add(previewPlayer);
		player1style = previewPlayerpanel.getElement().getStyle();
		player1style.setProperty("position", "absolute");
		player1style.setPropertyPx("left", 950);
		player1style.setPropertyPx("top", 60);

		player1style.setProperty("zIndex", PREVIEW_PLAYER_FOREGROUND_INDEX);
		CustomPlayerControl cpc = new CustomPlayerControl(previewPlayer);
		previewPlayerpanel.add(cpc);

		add(previewPlayerpanel);

		previewPlayerpanel2 = new FlowPanel();
		previewPlayerpanel2.setSize("310", "350");
		previewPlayerpanel2.setStyleName("preview-panel");
		previewPlayerpanel2.add(title2);

		previewPlayer2 = You2MixMediaPlayer.createPreviewPlayerWidget("", "300", "300");
		previewPlayerpanel2.add(previewPlayer2);
		player2style = previewPlayerpanel2.getElement().getStyle();

		player2style.setProperty("position", "absolute");
		player2style.setPropertyPx("left", 950);
		player2style.setPropertyPx("top", 60);
		player2style.setProperty("zIndex", PREVIEW_PLAYER_BACKGROUND_INDEX);
		CustomPlayerControl cpc2 = new CustomPlayerControl(previewPlayer2);
		previewPlayerpanel2.add(cpc2);
		add(previewPlayerpanel2);
		mixTimer.scheduleRepeating(500);
		surfaceView.setObserver(this);
	}

	private void switchToSecondPlayer() {
		previewPlayer.stopMedia();
		player1style.setProperty("zIndex", PREVIEW_PLAYER_BACKGROUND_INDEX);
		player2style.setProperty("zIndex", PREVIEW_PLAYER_FOREGROUND_INDEX);
		try {
			previewPlayer2.playMedia();
		} catch (PlayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		preparefirstPlayer(getNextVideo());
	}

	private void switchToFirstPlayer() {
		previewPlayer2.stopMedia();
		player2style.setProperty("zIndex", PREVIEW_PLAYER_BACKGROUND_INDEX);
		player1style.setProperty("zIndex", PREVIEW_PLAYER_FOREGROUND_INDEX);
		try {
			previewPlayer.playMedia();
		} catch (PlayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		prepareSecondPlayer(getNextVideo());
	}

	private void prepareVideos() {
		if (mixdata.size() > 1) {
			You2MixVideo firstVideo = getNextVideo();
			You2MixVideo secondVideo = getNextVideo();

			preparefirstPlayer(firstVideo);

			prepareSecondPlayer(secondVideo);
		}

	}

	private boolean prepareSecondPlayer(You2MixVideo secondVideo) {
		if (secondVideo != null) {
			try {
				previewPlayer2.loadMedia(You2MixMediaPlayer.getYouTubeURLStringFromYouTubeID(secondVideo.getYouTubeID()), Double.parseDouble(Integer
						.toString(secondVideo.getStartTime())));

			} catch (LoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			previewPlayer2.stopMedia();
			secondPlayerEndTime = Double.parseDouble(Integer.toString(secondVideo.getEndTime()));
			return true;
		}
		return false;
	}

	private boolean preparefirstPlayer(You2MixVideo firstVideo) {
		if (firstVideo != null) {
			try {
				previewPlayer.loadMedia(You2MixMediaPlayer.getYouTubeURLStringFromYouTubeID(firstVideo.getYouTubeID()), Double.parseDouble(Integer
						.toString(firstVideo.getStartTime())));

			} catch (LoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			previewPlayer.stopMedia();
			firstPlayerEndTime = Double.parseDouble(Integer.toString(firstVideo.getEndTime()));
			return true;
		}
		return false;
	}

	private You2MixVideo getNextVideo() {
		if(currentVideo +1 < mixdata.size()){
			currentVideo++;

		} else {
			currentVideo = 0;

		}

		return mixdata.get(currentVideo);
	}

	@Override
	public void onUpdateMixPositions(ArrayList<You2MixVideo> videoDataList) {
		this.mixdata = videoDataList;
		currentVideo = -1;
		prepareVideos();

	}

}
