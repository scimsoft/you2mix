package com.google.appengine.demos.sticky.client;

import java.util.ArrayList;

import com.bramosystems.oss.player.core.client.skin.CustomPlayerControl;
import com.google.appengine.demos.sticky.client.SurfaceView.Images;
import com.google.appengine.demos.sticky.client.model.Model;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.appengine.demos.sticky.client.model.Video;
import com.google.appengine.demos.sticky.client.model.VideoSearchResults;
import com.google.appengine.demos.sticky.client.model.Model.VideoSearchObserver;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SearchVideoView extends FlowPanel implements
		VideoSearchObserver,MouseUpHandler, MouseDownHandler, MouseMoveHandler {

	private StackPanel resultsPanel;
	private final TextBox searchTextBox;
	private Note currentnote;
	private Model model;
	private SurfaceView surfaceView;
	DivElement titleElement;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	

	public SearchVideoView(final Model model, SurfaceView surfaceView) {
		this.model = model;
		this.surfaceView = surfaceView;
		this.searchTextBox = new TextBox();
		
		
		final Element elem = getElement();
		
		model.addStreamObserver(this);
		
		setStyleName("search-panel");		
		elem.getStyle().setProperty("position", "absolute");
		elem.getStyle().setPropertyPx("left", 100);
		elem.getStyle().setPropertyPx("top", 200);
		elem.getStyle().setProperty("zIndex", "10000");
		
		
		addTitlePanel();
		
		addSearchBoxPanel();
		
		resultsPanel = new StackPanel();
		add(resultsPanel);
		
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseMoveEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
	}

	private void addSearchBoxPanel() {
		final Images images = GWT.create(Images.class);
		HorizontalPanel searchBoxPanel = new HorizontalPanel();
		searchBoxPanel.setStyleName("search-box-panel");
		
		searchTextBox.setStyleName("search-box");
		searchTextBox.addKeyPressHandler(createSearchBoxKeyHandler(model));
		
		searchBoxPanel.add(searchTextBox);
		
		searchBoxPanel.add(createSearchButton(images));
		
		add(searchBoxPanel);
	}

	private void addTitlePanel() {
		titleElement = getElement().appendChild(Document.get().createDivElement());
		titleElement.setClassName("search-view-title");
		titleElement.setInnerText("Search YouTube Videos");
	}

	private KeyPressHandler createSearchBoxKeyHandler(final Model model) {
		KeyPressHandler searchBoxKeyHandler = new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				switch (event.getCharCode()) {
				case KeyCodes.KEY_ENTER:
					model.getYouTubeSearchResults(searchTextBox.getText());
					break;
				case KeyCodes.KEY_ESCAPE:
					break;
				}
			}
		};
		return searchBoxKeyHandler;
	}

	private PushButton createSearchButton(final Images images) {
		PushButton searchButton = Buttons.createPushButtonWithImageStates(images
				.searchViewSearchButton().createImage(), "search-button",
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						model.getYouTubeSearchResults(searchTextBox.getText());

					}

				});
		searchButton.setStyleName("search-button");
		return searchButton;
	}
	
	
	
	public void setPixelPosition(int x, int y) {
		final Style style = getElement().getStyle();
		style.setPropertyPx("left", x);
		style.setPropertyPx("top", y);
	}

	@Override
	public void onStreamReceived(VideoSearchResults results) {
		removeSearchResults();
		ArrayList<VideoSearchResult> videos = results.getResults();
		for (int i = 0; i < videos.size(); i++) {
			final VideoSearchResult videoSearchResult = videos.get(i);
			
			StringBuffer urlString = new StringBuffer("http://www.youtube.com/v/");			
			urlString.append(videoSearchResult.getYouTubeID());
			You2MixChromelessPlayer playerWidget = You2MixMediaPlayer.createPlayerWidget(urlString.toString(), "170", "170");
			

			

			
			VerticalPanel contentYButton = new VerticalPanel();
			
			TextArea descriptionArea = new TextArea();
			descriptionArea.setStyleName("content-area");
			descriptionArea.setValue(videoSearchResult.getDescription());
			
			Button addButton = createAddButton(videoSearchResult);
			contentYButton.add(descriptionArea);
			contentYButton.add(addButton);

			HorizontalPanel videoYDescriptionPane = new HorizontalPanel();
			videoYDescriptionPane.add(playerWidget);
			videoYDescriptionPane.setCellWidth(playerWidget, "170");
			videoYDescriptionPane.add(contentYButton);
			videoYDescriptionPane.setCellWidth(descriptionArea, "210");
			videoYDescriptionPane.setCellHorizontalAlignment(descriptionArea,HorizontalPanel.ALIGN_RIGHT);
			
			FlowPanel singleResultPane = new FlowPanel();
			singleResultPane.add(videoYDescriptionPane);
			CustomPlayerControl cpc = new CustomPlayerControl(playerWidget);
			singleResultPane.add(cpc);
			
			
			resultsPanel.add(singleResultPane, videoSearchResult.getTitle(),true);

		}

	}

	private Button createAddButton(final VideoSearchResult videoSearchResult) {
		Button addButton = new Button("add", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				model.updateNoteVideo(currentnote, new Video(videoSearchResult.getYouTubeID(), 0, 0));
				surfaceView.removeSearchView();
			}
		});
		return addButton;
	}

	private void removeSearchResults() {
		resultsPanel.clear();
	}

	/**
	 * @param currentnote
	 *            the currentnote to set
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

	@Override
	public void onStartSearch() {
		removeSearchResults();

	}
	
public void onMouseDown(MouseDownEvent event) {
		

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
			
		}
	}

}
