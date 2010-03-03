/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.sticky.client;

import java.util.ArrayList;

import com.bramosystems.oss.player.core.client.AbstractMediaPlayer;
import com.bramosystems.oss.player.core.client.ConfigParameter;
import com.bramosystems.oss.player.core.client.LoadException;
import com.bramosystems.oss.player.core.client.PluginNotFoundException;
import com.bramosystems.oss.player.core.client.PluginVersionException;
import com.bramosystems.oss.player.core.client.TransparencyMode;
import com.bramosystems.oss.player.core.client.skin.CustomPlayerControl;
import com.bramosystems.oss.player.youtube.client.ChromelessPlayer;
import com.bramosystems.oss.player.youtube.client.PlayerParameters;
import com.google.appengine.demos.sticky.client.model.Model;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.appengine.demos.sticky.client.model.Surface;
import com.google.appengine.demos.sticky.client.model.Video;
import com.google.appengine.demos.sticky.client.model.VideoSearchResults;
import com.google.appengine.demos.sticky.client.model.Model.VideoSearchStreamObserver;
import com.google.appengine.demos.sticky.client.model.VideoSearchResults.VideoSearchResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 * A widget to display the collection of notes that are on a particular
 * {@link Surface}.
 * 
 * @author knorton@google.com (Kelly Norton)
 */
@SuppressWarnings("deprecation")
public class SurfaceView extends FlowPanel implements Model.DataObserver,Model.VideoSearchStreamObserver {

	public interface Images extends ImageBundle {
		@Resource("surface-list-add-hv.gif")
		AbstractImagePrototype surfaceListAddSurfaceButtonHv();

		@Resource("surface-list-add-up.gif")
		AbstractImagePrototype surfaceListAddSurfaceButtonUp();
		
		@Resource("search_large.png")
		AbstractImagePrototype searchViewSearchButton();
	}

	/**
	 * A widget for displaying a single {@link Note}.
	 */
	public class NoteView extends SimplePanel implements Note.Observer,
			MouseUpHandler, MouseDownHandler, MouseMoveHandler,
			ValueChangeHandler<String> {
		private final Note note;

		private final DivElement titleElement;

		private final TextArea content = new TextArea();

		private boolean dragging;

		private int dragOffsetX, dragOffsetY;

		final VideoView videoView;
		/**
		 * @param note
		 *            the note to render
		 */
		public NoteView(Note note) {
			this.note = note;
			setStyleName("note");
			note.setObserver(this);
			// Build simple DOM Structure.
			final Element elem = getElement();
			elem.getStyle().setProperty("position", "absolute");
			titleElement = elem.appendChild(Document.get().createDivElement());
			titleElement.setClassName("note-title");

			content.setStyleName("note-content");
			content.addValueChangeHandler(this);

			render();
			videoView = new VideoView(note);
			setWidget(videoView);
			// youTubeId.setValue(note.getVideoKey().getYouTubeID());
			addDomHandler(this, MouseDownEvent.getType());
			addDomHandler(this, MouseMoveEvent.getType());
			addDomHandler(this, MouseUpEvent.getType());

		}

		public void onMouseDown(MouseDownEvent event) {
			SurfaceView.this.select(this);
			if (!note.isOwnedByCurrentUser()) {
				return;
			}

			final EventTarget target = event.getNativeEvent().getEventTarget();
			assert Element.is(target);
			if (!Element.is(target)) {
				return;
			}

			if (titleElement.isOrHasChild(Element.as(target))) {
				dragging = true;
				final Element elem = getElement().cast();
				dragOffsetX = event.getX();
				dragOffsetY = event.getY();
				DOM.setCapture(elem);
				event.preventDefault();
			}

		}

		public void onMouseMove(MouseMoveEvent event) {
			if (dragging) {
				setPixelPosition(
						event.getX() + getAbsoluteLeft() - dragOffsetX, event
								.getY()
								+ getAbsoluteTop() - dragOffsetY);
				event.preventDefault();
			}
		}

		public void onMouseUp(MouseUpEvent event) {
			if (dragging) {
				dragging = false;
				DOM.releaseCapture(getElement());
				event.preventDefault();
				model.updateNotePosition(note, getAbsoluteLeft(),
						getAbsoluteTop(), note.getWidth(), note.getHeight());
			}
		}

		public void onUpdate(Note note) {
			videoView.youTubeIdBox.setText(note.getVideo().getYouTubeID());
			try {
				videoView.loadNewVideo();
			} catch (LoadException e) {
				// TODO Auto-generated catch block
				System.out.println(e.toString());
			}
			render();
		}

		public void onValueChange(ValueChangeEvent<String> event) {
			model.updateNoteContent(note, event.getValue());
		}

		public void setPixelPosition(int x, int y) {
			final Style style = getElement().getStyle();
			style.setPropertyPx("left", x);
			style.setPropertyPx("top", y);
		}

		public void setPixelSize(int width, int height) {
			content.setPixelSize(width, height);
		}

		private void render() {
			setPixelPosition(note.getX(), note.getY());

			setPixelSize(note.getWidth(), note.getHeight());

			titleElement.setInnerHTML(note.getAuthorName());

			final String noteContent = note.getContent();
			content.setText((noteContent == null) ? "" : noteContent);

			content.setReadOnly(!note.isOwnedByCurrentUser());
		
		}

		private void select() {
			getElement().getStyle().setProperty("zIndex", "" + nextZIndex());
		}

		

		/**
		 * @param videoObject
		 *            the videoObject to set
		 */
		public class VideoView extends SimplePanel implements Video.VideoObserver{
			
			protected final TextBox youTubeIdBox;
			final AbstractMediaPlayer noteVideo;
			private final Note note;
			
			public VideoView(final Note note)  {
				this.note = note;
				final Video video = note.getVideo();
				video.setObserver(this);
				String youTubeIDString = video.getYouTubeID();
				StringBuffer urlString = new StringBuffer(
						"http://www.youtube.com/v/");
				urlString.append(youTubeIDString);

				noteVideo = createPlayerWidget(
						urlString.toString(), "170", "170");

				youTubeIdBox = new TextBox();
				youTubeIdBox.setStyleName("note-YouTubeID");
				youTubeIdBox.setName("YouTube ID:");
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
				final Images images = GWT.create(Images.class);

				ComplexPanel youTubeIDPanel = new HorizontalPanel();
				youTubeIDPanel.add(youTubeIdBox);
				youTubeIDPanel.add(Buttons.createPushButtonWithImageStates(
						images.surfaceListAddSurfaceButtonUp().createImage(),
						images.surfaceListAddSurfaceButtonHv().createImage(),
						"surface-list-add", new ClickHandler() {
							public void onClick(ClickEvent event) {
								searchView.setCurrentnote(note);
								model.onStartSearch();
								
							}
						}));

				setYouTubeIdBoxValue(youTubeIDString);
				CustomPlayerControl cpc = new CustomPlayerControl(noteVideo);
				FlowPanel fp = new FlowPanel();
				fp.add(youTubeIDPanel);
				fp.add(noteVideo);
				fp.add(cpc);
				add(fp);
			}

			private void setYouTubeIdBoxValue(String youTubeIDString) {
				youTubeIdBox.setValue(youTubeIDString);
			}

			
			

			protected void loadNewVideo()
					throws LoadException {
				StringBuffer videoUrl = new StringBuffer(
						"http://www.youtube.com/v/");
				String youTubeTextBoxValue = note.getVideo().getYouTubeID();
				videoUrl.append(youTubeTextBoxValue);
				setYouTubeIdBoxValue(youTubeTextBoxValue);
				noteVideo.loadMedia(videoUrl.toString());

			}

			@Override
			public void onVideoUpdate(Video video) {
				note.setVideo(video);
				youTubeIdBox.setText(video.getYouTubeID());
				try {
					loadNewVideo();
				} catch (LoadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			

			

			
		}

	}

	private final Model model;

	private NoteView selectedNoteView;

	private int zIndex = 1;
	
	private final SearchVideoView searchView;

	/**
	 * @param model
	 *            the model to which the Ui will bind itself
	 */
	public SurfaceView(Model model) {
		this.model = model;
		final Element elem = getElement();
		elem.setId("surface");
		elem.getStyle().setProperty("position", "absolute");
		model.addDataObserver(this);
		model.addStreamObserver(this);
		searchView = new SearchVideoView();

	}

	public void onNoteCreated(Note note) {
		final NoteView view = new NoteView(note);
		add(view);
		select(view);
	}

	public void onSurfaceCreated(Surface group) {
	}

	public void onSurfaceNotesReceived(Note[] notes) {
		removeAllNotes();
		for (int i = 0, n = notes.length; i < n; ++i) {
			add(new NoteView(notes[i]));
		}
	}

	public void onSurfaceSelected(Surface nowSelected, Surface wasSelected) {
	}

	public void onSurfacesReceived(Surface[] surfaces) {
	}

	private int nextZIndex() {
		return zIndex++;
	}

	private void removeAllNotes() {
		final WidgetCollection kids = getChildren();
		while (kids.size() > 0) {
			remove(kids.size() - 1);
		}
	}

	private void select(NoteView noteView) {
		assert noteView != null;
		if (selectedNoteView != noteView) {
			noteView.select();
			selectedNoteView = noteView;
		}

	}
	
	public void addSearchView(){
		add(searchView);
	}
	
	public void  removeSearchView(){
		remove(searchView);
	}

	private AbstractMediaPlayer createPlayerWidget(String urlString,
			String width, String height) {
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

	public class SearchVideoView extends FlowPanel implements VideoSearchStreamObserver {

		private StackPanel resultsPanel;
		private final TextBox searchTextBox;
		private Note currentnote;
		

		public SearchVideoView() {
			
			final Images images = GWT.create(Images.class);
			setStyleName("search-panel");
			final Element elem = getElement();
			elem.getStyle().setProperty("position", "absolute");
			elem.getStyle().setPropertyPx("left", 100);
			elem.getStyle().setPropertyPx("top", 200);
			DivElement titleElement = elem.appendChild(Document.get().createDivElement());
			titleElement.setClassName("search-view-title");
			titleElement.setInnerText("Search YouTube Videos");
			searchTextBox = new TextBox();
			searchTextBox.setStyleName("search-box");
			searchTextBox.setName("YouTube ID:");
			searchTextBox.addKeyPressHandler(new KeyPressHandler() {
				public void onKeyPress(KeyPressEvent event) {
					switch (event.getCharCode()) {
					case KeyCodes.KEY_ENTER:
						model.getYouTubeSearchResults(searchTextBox.getText());
						break;
					case KeyCodes.KEY_ESCAPE:
						break;
					}
				}
			});
			HorizontalPanel searchBoxPanel = new HorizontalPanel();
			searchBoxPanel.add(searchTextBox);
			searchBoxPanel.add(Buttons.createPushButtonWithImageStates(images.searchViewSearchButton().createImage(), "search-button", new ClickHandler(){

				@Override
				public void onClick(ClickEvent event) {
					model.getYouTubeSearchResults(searchTextBox.getText());
					
				}
				
			}));
			add(searchBoxPanel);
			resultsPanel = new StackPanel();
			add(resultsPanel);
			getElement().getStyle().setProperty("zIndex", "" + nextZIndex());
			model.addStreamObserver(this);
		}

		@Override
		public void onStreamReceived(VideoSearchResults results) {
			removeSearchResults();
			ArrayList<VideoSearchResult> videos = results.getResults();
			for (int i = 0; i < videos.size(); i++) {
				
				StringBuffer urlString = new StringBuffer(
						"http://www.youtube.com/v/");
				
				final VideoSearchResult videoSearchResult = videos.get(i);
				urlString.append(videoSearchResult.getYouTubeID());
				
				
				AbstractMediaPlayer playerWidget = createPlayerWidget(
						urlString.toString(), "170", "170");
				System.out.println(urlString.toString());
				
				
				FlowPanel singleResultPane = new FlowPanel();
				
				
				
				HorizontalPanel videoYDescriptionPane = new HorizontalPanel();
				VerticalPanel contentYButton = new VerticalPanel();
				TextArea content = new TextArea();
				content.setStyleName("content-area");
				content.setText(videoSearchResult.getDescription());
				contentYButton.add(content);
				Button addButton = new Button("add", new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {						
						model.updateNoteVideo(currentnote, new Video(videoSearchResult.getYouTubeID(),0,0));
						
						removeSearchView();
					}
				});
				contentYButton.add(addButton);
				
				videoYDescriptionPane.add(playerWidget);
				videoYDescriptionPane.setCellWidth(playerWidget, "170");
				videoYDescriptionPane.add(contentYButton);
				videoYDescriptionPane.setCellWidth(content, "210");
				videoYDescriptionPane.setCellHorizontalAlignment(content, HorizontalPanel.ALIGN_RIGHT);
				singleResultPane.add(videoYDescriptionPane);
				CustomPlayerControl cpc = new CustomPlayerControl(playerWidget);
				singleResultPane.add(cpc);
				resultsPanel.add(singleResultPane, videoSearchResult.getTitle());

			}

		}

		@Override
		public void onStartSearch() {			
			removeSearchResults();
			
		}

		private void removeSearchResults() {
			//for (int searchResult = 0; searchResult < resultsPanel.getWidgetCount(); searchResult++) {
			//	resultsPanel.remove(searchResult);
			//}
			resultsPanel.clear();
		}

		/**
		 * @param currentnote the currentnote to set
		 */
		public void setCurrentnote(Note currentnote) {
			this.currentnote = currentnote;
		}

		/**
		 * @return the currentnote
		 */
		public Note getCurrentnote() {
			return currentnote;
		}

	}

	@Override
	public void onStartSearch() {
		if (!searchView.isAttached()){
		add(searchView);
		}
		
	}

	@Override
	public void onStreamReceived(VideoSearchResults results) {
		// TODO Auto-generated method stub
		
	}

}
