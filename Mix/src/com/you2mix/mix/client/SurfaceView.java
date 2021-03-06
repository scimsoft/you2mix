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

package com.you2mix.mix.client;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.you2mix.mix.client.model.Model;
import com.you2mix.mix.client.model.Note;
import com.you2mix.mix.client.model.Surface;
import com.you2mix.mix.client.model.You2MixVideoSearchResults;

/**
 * A widget to display the collection of notes that are on a particular
 * {@link Surface}.
 * 
 * @author knorton@google.com (Kelly Norton)
 */
@SuppressWarnings("deprecation")
public class SurfaceView extends You2MixAnimatedPadPanel implements Model.DataObserver, Model.VideoSearchObserver {

	public interface Images extends ImageBundle {
		@Resource("surface-list-add-hv.gif")
		AbstractImagePrototype surfaceListAddSurfaceButtonHv();

		@Resource("surface-list-add-up.gif")
		AbstractImagePrototype surfaceListAddSurfaceButtonUp();

		@Resource("search-icon-up.gif")
		AbstractImagePrototype searchViewSearchButtonUp();
		
		@Resource("search-icon-hv.gif")
		AbstractImagePrototype searchViewSearchButtonHv();
		
		@Resource("panel-close-up.gif")
		AbstractImagePrototype closePanelButtonUp();
		
		@Resource("panel-close-hv.gif")
		AbstractImagePrototype closePanelButtonHv();
	}

	/**
	 * A widget for displaying a single {@link Note}.
	 */
	public class You2MixVideoDataView extends SimplePanel implements Note.NoteObserver, MouseUpHandler, MouseDownHandler, MouseMoveHandler,
			ValueChangeHandler<String> {
		private final Note videoData;

		public Note getVideoData() {
			return videoData;
		}

		private final DivElement titleElement;

		private final TextArea content = new TextArea();

		private boolean dragging;

		private int dragOffsetX, dragOffsetY;

		final You2MixVideoView videoView;

		private boolean isNewVideo;

		/**
		 * @param note
		 *            the note to render
		 */
		public You2MixVideoDataView(Note note) {
			this.videoData = note;
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
			videoView = new You2MixVideoView(model, note);
			setWidget(videoView);

			// youTubeId.setValue(note.getVideoKey().getYouTubeID());
			addDomHandler(this, MouseDownEvent.getType());
			addDomHandler(this, MouseMoveEvent.getType());
			addDomHandler(this, MouseUpEvent.getType());

		}

		public void onMouseDown(MouseDownEvent event) {
			SurfaceView.this.select(this);
			searchView.setCurrentnote(videoData);
			if (!videoData.isOwnedByCurrentUser()) {
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
				setPixelPosition(event.getX() + getAbsoluteLeft() - dragOffsetX, event.getY() + getAbsoluteTop() - dragOffsetY);
				event.preventDefault();
			}
		}

		public void onMouseUp(MouseUpEvent event) {
			if (dragging) {
				dragging = false;
				DOM.releaseCapture(getElement());
				event.preventDefault();
				setPadPixelPosition(this,event.getX() + getAbsoluteLeft() - dragOffsetX, event.getY() + getAbsoluteTop() - dragOffsetY);
				model.updateNotePosition(videoData, getAbsoluteLeft(), getAbsoluteTop(), videoData.getWidth(), videoData.getHeight());
			}
		}

		public void onUpdate(Note note) {
			videoView.onVideoUpdate(note.getVideo(),isNewVideo);			
			render();
		}

		public void onValueChange(ValueChangeEvent<String> event) {
			model.updateNoteContent(videoData, event.getValue());
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
			//setPadPixelPosition(this,note.getX(), note.getY());
			setPixelSize(videoData.getWidth(), videoData.getHeight());

			titleElement.setInnerHTML(videoData.getAuthorName());

			final String noteContent = videoData.getContent();
			content.setText((noteContent == null) ? "" : noteContent);

			content.setReadOnly(!videoData.isOwnedByCurrentUser());

		}

		private void select() {
			getElement().getStyle().setProperty("zIndex", "" + nextZIndex());
		}

		public void setNewVideo(boolean b) {
			this.isNewVideo = b;
			
		}

		/**
		 * @param videoObject
		 *            the videoObject to set
		 */

	}

	private final Model model;

	private You2MixVideoDataView selectedNoteView;

	private int zIndex = 1;

	private final You2MixSearchVideoView searchView;

	

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
		searchView = new You2MixSearchVideoView(model, this);
		
		
		
	}

	public void onNoteCreated(Note note) {
		final You2MixVideoDataView view = new You2MixVideoDataView(note);
		add(view);
		select(view);
	}

	public void onSurfaceCreated(Surface group) {
	}

	public void onSurfaceNotesReceived(Note[] notes) {
		removeAllNotes();
		for (int i = 0, n = notes.length; i < n; ++i) {
			add(new You2MixVideoDataView(notes[i]));
		}
		You2MixPreviewView previewView = new You2MixPreviewView(this);
		addSuper(previewView);
		observer.onInitialMixPositionsLoaded(getVideoList());
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

	private void select(You2MixVideoDataView noteView) {
		assert noteView != null;
		if (getSelectedNoteView() != noteView) {
			noteView.select();
			setSelectedNoteView(noteView);
		}

	}

	public void addSearchView() {
		addSuper(searchView);
	}

	public void removeSearchView() {
		removeSuper(searchView);
	}

	@Override
	public void onStreamReceived(You2MixVideoSearchResults results) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartSearch() {
		if (!searchView.isAttached()) {
			addSuper(searchView);
		}

	}

	/**
	 * @param selectedNoteView the selectedNoteView to set
	 */
	public void setSelectedNoteView(You2MixVideoDataView selectedNoteView) {
		this.selectedNoteView = selectedNoteView;
	}

	/**
	 * @return the selectedNoteView
	 */
	public You2MixVideoDataView getSelectedNoteView() {
		return selectedNoteView;
	}

}
