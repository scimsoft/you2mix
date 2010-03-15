package com.you2mix.mix.client;

import java.util.ArrayList;

import com.bramosystems.oss.player.core.client.PlayException;
import com.bramosystems.oss.player.core.client.skin.CustomPlayerControl;
import com.bramosystems.oss.player.core.event.client.PlayStateEvent;
import com.bramosystems.oss.player.core.event.client.PlayStateHandler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.you2mix.mix.client.You2MixAnimatedPadPanel.VideoMixPositionObserver;
import com.you2mix.mix.client.model.You2MixChromelessPlayer;
import com.you2mix.mix.client.model.You2MixVideo;
import com.you2mix.mix.client.model.You2MixChromelessPlayer.You2MixPlayTimeListener;

public class You2MixPreviewView extends FlowPanel implements VideoMixPositionObserver, You2MixPlayTimeListener, PlayStateHandler {

	private static final String PREVIEW_PLAYER_FOREGROUND_INDEX = "100000";
	private static final String PREVIEW_PLAYER_BACKGROUND_INDEX = "99999";

	private ArrayList<You2MixVideo> mixdata;
	private ArrayList<You2MixChromelessPlayer> previewPlayersList;
	private ArrayList<FlowPanel> previewPlayerPanelsList;
	private int currentPlayingPreviewPlayerIndex = 0;
	private double currentPlayerEndTime;

	public You2MixPreviewView(SurfaceView surfaceView) {

		surfaceView.setObserver(this);
	}

	private void createPreviewPlayer(You2MixVideo videoData, Boolean isFirst) {
		Label title = new Label();
		title.setStyleName("preview-title");
		title.setText("Preview");

		FlowPanel previewPlayerpanel = new FlowPanel();
		previewPlayerpanel.setSize("310", "350");
		previewPlayerpanel.setStyleName("preview-panel");
		previewPlayerpanel.add(title);
		You2MixChromelessPlayer previewPlayer;
		previewPlayer = You2MixMediaPlayer.createPreviewPlayerWidget(getYouTubeURLStringFromYouTubeID(videoData.getYouTubeID()), videoData.getStartTime(), "300", "300");
		previewPlayer.addPlayStateHandler(this);
		if (isFirst)
			this.currentPlayerEndTime = videoData.getEndTime();
		previewPlayersList.add(previewPlayer);

		previewPlayerpanel.add(previewPlayer);
		Style player1style = previewPlayerpanel.getElement().getStyle();
		player1style.setProperty("position", "absolute");
		player1style.setPropertyPx("left", 950);

		player1style.setPropertyPx("top", 60);
		if (isFirst) {
			player1style.setProperty("zIndex", PREVIEW_PLAYER_FOREGROUND_INDEX);
		} else {
			player1style.setProperty("zIndex", PREVIEW_PLAYER_BACKGROUND_INDEX);
		}

		CustomPlayerControl cpc = new CustomPlayerControl(previewPlayer);
		previewPlayerpanel.add(cpc);
		previewPlayerPanelsList.add(previewPlayerpanel);
		add(previewPlayerpanel);
		previewPlayer.isCued = false;
		previewPlayer.setYou2MixPlayTimeListener(this);

	}

	private void switchToNextPreviewPLayer() {
		Style currentPreviewPanel;
		previewPlayersList.get(currentPlayingPreviewPlayerIndex).stopMedia();
		currentPreviewPanel = previewPlayerPanelsList.get(currentPlayingPreviewPlayerIndex).getElement().getStyle();
		currentPreviewPanel.setProperty("zIndex", PREVIEW_PLAYER_BACKGROUND_INDEX);

		You2MixChromelessPlayer currentPlayer = previewPlayersList.get(getNextVideo());
		currentPreviewPanel = previewPlayerPanelsList.get(currentPlayingPreviewPlayerIndex).getElement().getStyle();
		currentPreviewPanel.setProperty("zIndex", PREVIEW_PLAYER_FOREGROUND_INDEX);
		currentPlayerEndTime = mixdata.get(currentPlayingPreviewPlayerIndex).getEndTime();
		try {
			currentPlayer.playMedia();
		} catch (PlayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getYouTubeURLStringFromYouTubeID(String youTubeID) {
		return "http://www.youtube.com/v/" + youTubeID + "&version=2&autoplay=0&enablejsapi=1";

	}

	private int getNextVideo() {
		if (currentPlayingPreviewPlayerIndex + 1 < mixdata.size()) {
			currentPlayingPreviewPlayerIndex++;

		} else {
			currentPlayingPreviewPlayerIndex = 0;

		}

		return currentPlayingPreviewPlayerIndex;
	}

	@Override
	public void onUpdateMixPositions(ArrayList<You2MixVideo> videoDataList) {
		this.mixdata = videoDataList;
		previewPlayersList.clear();
		previewPlayerPanelsList.clear();
		preparePreviewPlayers(videoDataList);
	}

	@Override
	public void onInitialMixPositionsLoaded(ArrayList<You2MixVideo> videoDataList) {
		this.mixdata = videoDataList;
		preparePreviewPlayers(videoDataList);
	}

	private void preparePreviewPlayers(ArrayList<You2MixVideo> videoDataList) {
		previewPlayersList = new ArrayList<You2MixChromelessPlayer>();
		previewPlayerPanelsList = new ArrayList<FlowPanel>();
		for (int videoNumber = 0; videoNumber < videoDataList.size(); videoNumber++) {
			createPreviewPlayer(videoDataList.get(videoNumber), videoNumber == 0);
		}

	}

	@Override
	public void onPlayStateChanged(PlayStateEvent event) {
		You2MixChromelessPlayer handledPreviewPlayer = (You2MixChromelessPlayer) event.getSource();
		if (event.getPlayState() == PlayStateEvent.State.Started && !handledPreviewPlayer.isCued) {
			handledPreviewPlayer.setPlayPosition((double)(handledPreviewPlayer.getStartTime()));
			handledPreviewPlayer.pauseMedia();
			handledPreviewPlayer.isCued = true;
		}
		
	}

	@Override
	public void onCurrentPlayTimeChange(Double currentPlayTime) {
		if (currentPlayTime / 1000 > currentPlayerEndTime) {
			switchToNextPreviewPLayer();

		}

	}
}
