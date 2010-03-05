package com.you2mix.mix.client;

import com.bramosystems.oss.player.core.client.LoadException;
import com.bramosystems.oss.player.core.client.skin.CustomPlayerControl;
import com.bramosystems.oss.player.core.event.client.PlayStateEvent;
import com.bramosystems.oss.player.core.event.client.PlayStateHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.you2mix.mix.client.SurfaceView.Images;
import com.you2mix.mix.client.model.Model;
import com.you2mix.mix.client.model.You2MixVideoData;
import com.you2mix.mix.client.model.You2MixVideo;

public class You2MixVideoView extends SimplePanel implements You2MixVideo.VideoObserver, PlayStateHandler {

	protected final TextBox youTubeIdBox;
	final You2MixChromelessPlayer noteVideo;
	private final You2MixVideoData note;

	final You2MixVideo video;
	private boolean isCued;
	private Model model;

	private TextBox startTime;
	private TextBox endTime;

	public You2MixVideoView(final Model model, final You2MixVideoData note) {
		this.model = model;
		this.note = note;
		this.video = note.getVideo();
		video.setObserver(this);

		String youTubeIDString = video.getYouTubeID();
		StringBuffer urlString = new StringBuffer("http://www.youtube.com/v/");
		urlString.append(youTubeIDString);

		/*
		 * The player Widget added a play state listener to treat the playback
		 * from startime
		 */
		noteVideo = You2MixMediaPlayer.createPlayerWidget(urlString.toString(),video.getStartTime(), "170", "170");
		noteVideo.addPlayStateHandler(this);

		/*
		 * Panel containing: TextBox to show and edit YouTubeID Has a
		 * keyboardlistener attached Button to open searchVideoView
		 */
		ComplexPanel youTubeIDPanel = new HorizontalPanel();

		youTubeIdBox = new TextBox();
		initYouTubeIdBox(youTubeIDString);
		youTubeIDPanel.add(youTubeIdBox);
		youTubeIDPanel.add(createSearchButton());

		/*
		 * Controls of the video TODO: create new control panel
		 */
		CustomPlayerControl cpc = new CustomPlayerControl(noteVideo);

		/*
		 * Panel with text boxes to show and edit start end time
		 */
		HorizontalPanel timerPanel = getTimingPanel();

		/*
		 * The Panel containing all the sub panels
		 */

		FlowPanel videoViewPanel = new FlowPanel();
		videoViewPanel.add(youTubeIDPanel);
		videoViewPanel.add(noteVideo);
		videoViewPanel.add(cpc);
		videoViewPanel.add(timerPanel);

		add(videoViewPanel);
		setVideoStartTime(Integer.toString(video.getStartTime()));
		noteVideo.pauseMedia();

	}

	public void setVideoStartTime(String stime) {
		if (stime != null) {
			startTime.setValue(stime);
		}

	}

	private void setYouTubeIdBoxValue(String youTubeIDString) {
		youTubeIdBox.setValue(youTubeIDString);
	}

	protected void loadNewVideo() throws LoadException {
		StringBuffer videoUrl = new StringBuffer("http://www.youtube.com/v/");
		String youTubeTextBoxValue = note.getVideo().getYouTubeID();
		videoUrl.append(youTubeTextBoxValue);
		setYouTubeIdBoxValue(youTubeTextBoxValue);
		noteVideo.loadMedia(videoUrl.toString());

	}

	@Override
	public void onVideoUpdate(You2MixVideo video) {
		note.setVideo(video);
		youTubeIdBox.setText(video.getYouTubeID());
		startTime.setValue(Integer.toString(video.getStartTime()));
		endTime.setValue(Integer.toString(video.getEndTime()));
	}

	@Override
	public void onPlayStateChanged(PlayStateEvent event) {
		if (event.getPlayState() == PlayStateEvent.State.Started) {
			if (!isCued) {
				// noteVideo.stopMedia();
				noteVideo.setPlayPosition(Double.parseDouble(startTime.getValue()));
				isCued = true;
			}
		}

	}

	private PushButton createSearchButton() {
		final Images images = GWT.create(Images.class);
		return Buttons.createPushButtonWithImageStates(images.surfaceListAddSurfaceButtonUp().createImage(), images.surfaceListAddSurfaceButtonHv()
				.createImage(), "surface-list-add", new ClickHandler() {
			public void onClick(ClickEvent event) {
				model.onStartSearch();
			}
		});
	}

	private void initYouTubeIdBox(String youTubeIDString) {
		youTubeIdBox.setStyleName("note-YouTubeID");
		setYouTubeIdBoxValue(youTubeIDString);
		youTubeIdBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				switch (event.getCharCode()) {
				case KeyCodes.KEY_ENTER:
					try {
						video.setYouTubeID(youTubeIdBox.getText());
						loadNewVideo();
					} catch (LoadException e) {
						e.printStackTrace();
					}
					model.updateNoteVideo(note, video);
					break;
				case KeyCodes.KEY_ESCAPE:
					break;
				}

			}
		});

	}

	private HorizontalPanel getTimingPanel() {
		HorizontalPanel timerPanel = new HorizontalPanel();
		timerPanel.setStyleName("video-timer-panel");

		Label startTimeLabel = new Label("Start:");
		startTimeLabel.setStyleName("video-timer-label");
		startTime = new TextBox();
		startTime.setValue("0");
		startTime.setStyleName("video-start-box");
		startTime.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				int newStartTime = Integer.parseInt(startTime.getValue()== ""?"0":startTime.getValue());
				video.setStartTime(newStartTime);
				model.updateNoteVideo(note, video);
				isCued = false;
				noteVideo.setStartTime(newStartTime);
				

			}
		});

		Label endTimeLabel = new Label("End:");
		endTimeLabel.setStyleName("video-timer-label");
		endTime = new TextBox();
		endTime.setValue("0");
		endTime.setStyleName("video-start-box");

		timerPanel.add(startTimeLabel);
		timerPanel.add(startTime);
		timerPanel.add(endTimeLabel);
		timerPanel.add(endTime);
		return timerPanel;
	}
}