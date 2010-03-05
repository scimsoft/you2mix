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

import com.bramosystems.oss.player.core.client.LoadException;
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
import com.you2mix.mix.client.model.VideoCanvas;
import com.you2mix.mix.client.model.Surface;
import com.you2mix.mix.client.model.VideoSearchResults;

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

		@Resource("search_large.png")
		AbstractImagePrototype searchViewSearchButton();
	}

	/**
	 * A widget for displaying a single {@link VideoCanvas}.
	 */
	public class VideoCanvasView extends SimplePanel implements VideoCanvas.NoteObserver, MouseUpHandler, MouseDownHandler, MouseMoveHandler,
			ValueChangeHandler<String> {
		private final VideoCanvas note;

		private final DivElement titleElement;

		private final TextArea content = new TextArea();

		private boolean dragging;

		private int dragOffsetX, dragOffsetY;

		final VideoView videoView;

		/**
		 * @param note
		 *            the note to render
		 */
		public VideoCanvasView(VideoCanvas note) {
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
			videoView = new VideoView(model, note);
			setWidget(videoView);

			// youTubeId.setValue(note.getVideoKey().getYouTubeID());
			addDomHandler(this, MouseDownEvent.getType());
			addDomHandler(this, MouseMoveEvent.getType());
			addDomHandler(this, MouseUpEvent.getType());

		}

		public void onMouseDown(MouseDownEvent event) {
			SurfaceView.this.select(this);
			searchView.setCurrentnote(note);
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
				model.updateNotePosition(note, getAbsoluteLeft(), getAbsoluteTop(), note.getWidth(), note.getHeight());
			}
		}

		public void onUpdate(VideoCanvas note) {
			videoView.youTubeIdBox.setText(note.getVideo().getYouTubeID());
			videoView.video.setYouTubeID(note.getVideo().getYouTubeID());
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
			//setPadPixelPosition(this,note.getX(), note.getY());
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

	}

	private final Model model;

	private VideoCanvasView selectedNoteView;

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
		searchView = new SearchVideoView(model, this);

	}

	public void onNoteCreated(VideoCanvas note) {
		final VideoCanvasView view = new VideoCanvasView(note);
		add(view);
		select(view);
	}

	public void onSurfaceCreated(Surface group) {
	}

	public void onSurfaceNotesReceived(VideoCanvas[] notes) {
		removeAllNotes();
		for (int i = 0, n = notes.length; i < n; ++i) {
			add(new VideoCanvasView(notes[i]));
			System.out.println("video: id " + notes[i].getVideo().getYouTubeID());
			System.out.println("video: start " + notes[i].getVideo().getStartTime());
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

	private void select(VideoCanvasView noteView) {
		assert noteView != null;
		if (selectedNoteView != noteView) {
			noteView.select();
			selectedNoteView = noteView;
		}

	}

	public void addSearchView() {
		addSuper(searchView);
	}

	public void removeSearchView() {
		removeSuper(searchView);
	}

	@Override
	public void onStreamReceived(VideoSearchResults results) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartSearch() {
		if (!searchView.isAttached()) {
			addSuper(searchView);
		}

	}

}