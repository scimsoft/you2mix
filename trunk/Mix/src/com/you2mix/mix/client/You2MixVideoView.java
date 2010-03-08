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
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.you2mix.mix.client.SurfaceView.Images;
import com.you2mix.mix.client.model.Model;
import com.you2mix.mix.client.model.You2MixChromelessPlayer;
import com.you2mix.mix.client.model.You2MixVideo;
import com.you2mix.mix.client.model.Note;

public class You2MixVideoView extends SimplePanel implements You2MixChromelessPlayer.You2MixPlayTimeListener, PlayStateHandler {

	protected final TextBox youTubeIdBox;
	final You2MixChromelessPlayer videoPLayerObject;
	private final Note note;

	final You2MixVideo video;
	private boolean isCued;
	private Model model;

	private TextBox startTime;
	private TextBox endTime;

	public You2MixVideoView(final Model model, final Note note) {
		this.model = model;
		this.note = note;
		this.video = note.getVideo();

		String youTubeIDString = video.getYouTubeID();
		StringBuffer urlString = new StringBuffer("http://www.youtube.com/v/");
		urlString.append(youTubeIDString);

		/*
		 * The player Widget added a play state listener to treat the playback
		 * from startime
		 */
		videoPLayerObject = You2MixMediaPlayer.createPlayerWidget(urlString.toString(), video.getStartTime(), "170", "170");
		videoPLayerObject.addPlayStateHandler(this);
		videoPLayerObject.setYou2MixPlayTimeListener(this);

		/*
		 * Panel containing: TextBox to show and edit YouTubeID Has a
		 * keyboardlistener attached Button to open searchVideoView
		 */
		ComplexPanel youTubeIDPanel = new HorizontalPanel();
		youTubeIDPanel.setStyleName("youtube-id-search-panel");

		youTubeIdBox = new TextBox();
		initYouTubeIdBox(youTubeIDString);
		youTubeIDPanel.add(youTubeIdBox);
		youTubeIDPanel.add(createSearchButton());

		/*
		 * Controls of the video TODO: create new control panel
		 */
		CustomPlayerControl cpc = new CustomPlayerControl(videoPLayerObject);

		/*
		 * Panel with text boxes to show and edit start end time
		 */
		HorizontalPanel timerPanel = getTimingPanel();

		/*
		 * The Panel containing all the sub panels
		 */

		FlowPanel videoViewPanel = new FlowPanel();
		videoViewPanel.add(youTubeIDPanel);
		videoViewPanel.add(videoPLayerObject);
		videoViewPanel.add(cpc);
		videoViewPanel.add(timerPanel);

		add(videoViewPanel);
		setVideoStartTime(Integer.toString(video.getStartTime()));
		setVideoEndTime(Integer.toString(video.getEndTime()));
		videoPLayerObject.pauseMedia();

	}

	public void setVideoStartTime(String stime) {
		if (stime != null) {
			startTime.setValue(stime);
		}

	}
	public void setVideoEndTime(String stime) {
		if (stime != null) {
			endTime.setValue(stime);
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
		videoPLayerObject.loadMedia(videoUrl.toString());
		videoPLayerObject.setYou2MixPlayTimeListener(this);

	}

	public void onVideoUpdate(You2MixVideo video, boolean isNewVideo) {
		note.setVideo(video);
		this.video.setYouTubeID(video.getYouTubeID());
		youTubeIdBox.setText(video.getYouTubeID());
		startTime.setValue(Integer.toString(video.getStartTime()));
		endTime.setValue(Integer.toString(video.getEndTime()));
		if (isNewVideo) {
			try {
				loadNewVideo();
				
			} catch (LoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isNewVideo = false;
		}else{
			videoPLayerObject.setPlayPosition(Double.parseDouble(startTime.getValue()));
		}
	}

	@Override
	public void onPlayStateChanged(PlayStateEvent event) {
		if (event.getPlayState() == PlayStateEvent.State.Started) {
			if (!isCued) {
				// noteVideo.stopMedia();
				videoPLayerObject.setPlayPosition(Double.parseDouble(startTime.getValue()));
				videoPLayerObject.pauseMedia();
				isCued = true;

			}
		}

	}

	private PushButton createSearchButton() {
		final Images images = GWT.create(Images.class);
		return Buttons.createPushButtonWithImageStates(images.searchViewSearchButtonUp().createImage(), images.searchViewSearchButtonHv()
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
		startTimeLabel.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				startTime.setText(Double.toString(videoPLayerObject.getPlayPosition()/1000));
				
			}
		});
		startTime = new TextBox();
		startTime.setValue("0");
		startTime.setStyleName("video-start-box");
		startTime.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				int newStartTime = Integer.parseInt(startTime.getValue() == "" ? "0" : startTime.getValue());
				video.setStartTime(newStartTime);
				model.updateNoteVideo(note, video);
				videoPLayerObject.setStartTime(newStartTime);
				isCued = false;

			}
		});

		Label endTimeLabel = new Label("End:");
		endTimeLabel.setStyleName("video-timer-label");
		endTimeLabel.addMouseDownHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				endTime.setText(Double.toString(videoPLayerObject.getPlayPosition()/1000));
				
			}
		});
		endTime = new TextBox();
		endTime.setValue("0");
		endTime.setStyleName("video-start-box");
		endTime.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				int newEndTime = Integer.parseInt(endTime.getValue() == "" ? "0" : endTime.getValue());
				video.setEndTime(newEndTime);
				model.updateNoteVideo(note, video);
				isCued = false;
			}
		});
		timerPanel.add(startTimeLabel);
		timerPanel.add(startTime);
		timerPanel.add(endTimeLabel);
		timerPanel.add(endTime);
		return timerPanel;
	}

	@Override
	public void onCurrentPlayTimeChange(Double currentPlayTime) {
		System.out.println("Current time" + currentPlayTime.toString());
		Double videoEndTime = Double.parseDouble(Integer.toString(video.getEndTime()))*1000;
		if (videoEndTime != 0 && (currentPlayTime >= videoEndTime)) {
			videoPLayerObject.stopMedia();
		}

	}
}