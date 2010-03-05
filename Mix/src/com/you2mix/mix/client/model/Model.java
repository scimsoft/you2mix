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

package com.you2mix.mix.client.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.you2mix.mix.client.model.Service.CreateObjectResult;
import com.you2mix.mix.client.model.Service.UserInfoResult;

/**
 * Encapsulates the entire application data controller for the application. The
 * model controls all RPC to the server and is responsible for keeping client
 * side copies of data synchronized with the server.
 * 
 * @author knorton@google.com (Kelly Norton)
 */
public class Model {
	/**
	 * The period to use, in millisconds, for polling for updates to notes on
	 * the currently selected surface.
	 */
	private static final int GET_NOTES_POLLING_INTERVAL = 10000 /* ms. */;

	/**
	 * The period to use, in milliseconds, for polling for updates to the list
	 * of surfaces that the author is participating in.
	 */
	private static final int GET_SURFACES_POLLING_INTERVAL = 20000 /* ms. */;

	/**
	 * An rpc proxy for making calls to the server.
	 */
	private final ServiceAsync api;

	/**
	 * The currently selected surface. This should never be null.
	 */
	private Surface selectedSurface;

	/**
	 * The currently logged in author.
	 */
	private final Author author;

	/**
	 * The list of the observers monitoring the model for data related events.
	 */
	private final List<DataObserver> dataObservers = new ArrayList<DataObserver>();

	/**
	 * The observer that is receiving status events.
	 */
	private final StatusObserver statusObserver;

	private final List<VideoSearchObserver> videoSearchObservers = new ArrayList<VideoSearchObserver>();

	/**
	 * A url that can be used to log the current user out.
	 */
	private final String logoutUrl;

	/**
	 * A task queue to manage all writes to the server.
	 */
	private final TaskQueue taskQueue = new TaskQueue();

	/**
	 * Manages the initial loading of notes associated with the selected surface
	 * and polls repeatedly for changes.
	 */
	private final VideoCanvasLoader noteLoader = new VideoCanvasLoader(this, GET_NOTES_POLLING_INTERVAL);

	/**
	 * Manages the initial loading of the list of surfaces for an author and
	 * continues polling repeatedly for updates.
	 */
	private final SurfaceLoader surfaceLoader = new SurfaceLoader(this, GET_SURFACES_POLLING_INTERVAL);

	/**
	 * Indicates whether the RPC end point is currently responding.
	 */
	private boolean offline;

	public interface VideoSearchObserver {
		void onStartSearch();

		void onStreamReceived(VideoSearchResults results);
	}

	/**
	 * An observer interface to deliver all data change events.
	 */
	public interface DataObserver {

		/**
		 * Called when a new {@link VideoCanvas} is created.
		 * 
		 * @param note
		 *            the note that was created
		 */
		void onNoteCreated(VideoCanvas note);

		/**
		 * Called when a new {@link Surface} is created.
		 * 
		 * @param surface
		 *            the surface that was created
		 */
		void onSurfaceCreated(Surface surface);

		// void onVideoCreated(Video video);

		/**
		 * Called when an initial list of {@link VideoCanvas}s is returned from the
		 * server.
		 * 
		 * @param notes
		 *            all the {@link VideoCanvas}s on the currently selected
		 *            {@link Surface}.
		 */
		void onSurfaceNotesReceived(VideoCanvas[] notes);

		/**
		 * Called when the selected {@link Surface} changes.
		 * 
		 * @param nowSelected
		 *            the surface that is now selected
		 * @param wasSelected
		 *            the surface that was previously selected
		 */
		void onSurfaceSelected(Surface nowSelected, Surface wasSelected);

		/**
		 * Called when the initial list of {@link Surface}s is returned from the
		 * server.
		 * 
		 * @param surfaces
		 *            all the surfaces where the current author is a member
		 */
		void onSurfacesReceived(Surface[] surfaces);

	}

	/**
	 * An observer interface used to get callbacks during the initial load of
	 * the {@link Model}.
	 */
	public interface LoadObserver {

		/**
		 * Invoked when the {@link Model} loads successfully.
		 * 
		 * @param model
		 *            the newly loaded model
		 */
		void onModelLoaded(Model model);

		/**
		 * Invoked when the model fails to load.
		 */
		void onModelLoadFailed();
	}

	/**
	 * An observer interface that provides callbacks useful for giving the user
	 * feedback about calls to the server.
	 */
	public interface StatusObserver {
		/**
		 * Invoked when RPC calls begin to succeed again after a failure was
		 * reported.
		 */
		void onServerCameBack();

		/**
		 * Invoked when RPC calls to the server are failing.
		 */
		void onServerWentAway();

		/**
		 * Invoked when current task has finished. This is often used to stop
		 * displaying status Ui that was made visible in the
		 * {@link StatusObserver#onTaskStarted(String)} callback.
		 */
		void onTaskFinished();

		/**
		 * Invoked when a task that requires user feedback starts.
		 * 
		 * @param description
		 *            a description of the task that is starting
		 */
		void onTaskStarted(String description);
	}

	/**
	 * A simple callback for reporting success to the caller asynchronously.
	 * This is used for call sites where the caller needs to know the result of
	 * an RPC.
	 */
	public static interface SuccessCallback {
		void onResponse(boolean success);
	}

	/**
	 * A task that manages the call to the server to add an author to a surface.
	 */
	private class AddAuthorToSurfaceTask extends Task implements AsyncCallback<Service.AddAuthorToSurfaceResult> {
		private final Surface surface;

		private final String email;

		private final SuccessCallback callback;

		public AddAuthorToSurfaceTask(Surface surface, String email, SuccessCallback callback) {
			this.surface = surface;
			this.email = email;
			this.callback = callback;
		}

		public void onFailure(Throwable caught) {
			getQueue().taskFailed(this, caught instanceof Service.AccessDeniedException);
		}

		public void onSuccess(Service.AddAuthorToSurfaceResult result) {
			final boolean success = result != null;
			callback.onResponse(success);
			if (success) {
				surface.update(Model.this, result.getAuthorName(), result.getUpdatedAt());
			}
			getQueue().taskSucceeded(this);
		}

		@Override
		void execute() {
			api.addAuthorToSurface(surface.getKey(), email, this);
		}
	}

	/**
	 * A task that manages the call to the server to create a new note.
	 */
	private class CreateNoteTask extends Task implements AsyncCallback<CreateObjectResult> {
		private final VideoCanvas note;

		private final Surface surface;

		public CreateNoteTask(Surface surface, VideoCanvas note) {
			this.note = note;
			this.surface = surface;
		}

		@Override
		public void execute() {
			api.createNote(surface.getKey(), note.getX(), note.getY(), note.getWidth(), note.getHeight(), note.getVideo(), this);
		}

		public void onFailure(Throwable caught) {
			getQueue().taskFailed(this, caught instanceof Service.AccessDeniedException);
		}

		public void onSuccess(CreateObjectResult result) {
			if (surface == selectedSurface) {
				noteLoader.cacheNote(result.getKey(), note);
			}
			note.update(result.getKey(), result.getUpdateTime());
			getQueue().taskSucceeded(this);
		}
	}

	/**
	 * A task to manages the call to the server to create a new surface.
	 */
	private class CreateSurfaceTask extends Task implements AsyncCallback<Service.CreateObjectResult> {
		private final Surface surface;

		public CreateSurfaceTask(Surface surface) {
			this.surface = surface;
		}

		public void execute() {
			api.createSurface(surface.getTitle(), this);
		}

		public void onFailure(Throwable caught) {
			getQueue().taskFailed(this, caught instanceof Service.AccessDeniedException);
		}

		public void onSuccess(Service.CreateObjectResult result) {
			surfaceLoader.cacheSurface(result.getKey(), surface);
			surface.update(result.getKey(), result.getUpdateTime());
			getQueue().taskSucceeded(this);
		}
	}

	/**
	 * Encapsulates a linked list node that is used by {@link TaskQueue} to keep
	 * an ordered list of pending {@link Task}s.
	 */
	private static class Node {
		private final Task task;

		private Node next;

		Node(Task task) {
			this.task = task;
		}

		void execute(TaskQueue queue) {
			task.execute(queue);
		}
	}

	/**
	 * Encapsulates a task for writing data to the server. The tasks are managed
	 * by the {@link TaskQueue} and are auto-retried on failure.
	 */
	private abstract static class Task {
		private TaskQueue queue;

		abstract void execute();

		void execute(TaskQueue queue) {
			this.queue = queue;
			execute();
		}

		TaskQueue getQueue() {
			return queue;
		}
	}

	/**
	 * Provides a mechanism to perform write tasks sequentially and retry tasks
	 * that fail.
	 */
	private class TaskQueue extends RetryTimer {

		private Node head, tail;

		public void post(Task task) {
			final Node node = new Node(task);
			if (isIdle()) {
				head = tail = node;
				executeHead();
			} else {
				enqueueTail(node);
			}
		}

		private void enqueueTail(Node node) {
			assert head != null && tail != null;
			assert node != null;
			tail = tail.next = node;
		}

		private void executeHead() {
			head.execute(this);
		}

		private void executeNext() {
			head = head.next;
			if (head != null) {
				executeHead();
			} else {
				tail = null;
			}
		}

		private boolean isIdle() {
			return head == null;
		}

		private void taskFailed(Task task, boolean fatal) {
			assert task == head.task;

			// Report a failure to the Model.
			onServerFailed(fatal);

			// Schedule a retry.
			retryLater();
		}

		private void taskSucceeded(Task task) {
			assert task == head.task;
			// Report a success to the Model.
			onServerSucceeded();

			// Reset the retry counter.
			resetRetryCount();

			// Move on to the next task.
			executeNext();
		}

		@Override
		protected void retry() {
			// Retry running the head task.
			executeHead();
		}
	}

	/**
	 * A {@link Task} that manages the call to the server to update the contents
	 * of a {@link VideoCanvas}.
	 */
	private class UpdateNoteContentTask extends Task implements AsyncCallback<Date> {
		private final String content;

		private final VideoCanvas note;

		public UpdateNoteContentTask(VideoCanvas note, String content) {
			this.note = note;
			this.content = content;
			this.note.setVideo(note.getVideo());
		}

		public void execute() {
			note.setContent(content);
			api.changeNoteContent(note.getKey(), content, this);
		}

		public void onFailure(Throwable caught) {
			getQueue().taskFailed(this, caught instanceof Service.AccessDeniedException);
		}

		public void onSuccess(Date lastUpdatedAt) {
			note.update(lastUpdatedAt);
			getQueue().taskSucceeded(this);
		}
	}

	private class UpdateNoteVideoTask extends Task implements AsyncCallback<Date> {
		private final Video video;

		private final VideoCanvas note;

		public UpdateNoteVideoTask(VideoCanvas note, Video video) {
			this.note = note;
			this.video = video;

		}

		public void execute() {
			note.setVideo(video);
			api.changeNoteVideo(note.getKey(), video, this);
		}

		public void onFailure(Throwable caught) {
			getQueue().taskFailed(this, caught instanceof Service.AccessDeniedException);
		}

		public void onSuccess(Date lastUpdatedAt) {
			note.update(lastUpdatedAt);
			note.getObserver().onUpdate(note);
			getQueue().taskSucceeded(this);

		}
	}

	private class UpdateVideoTask extends Task implements AsyncCallback<Date> {

		private Video video;

		public UpdateVideoTask(Video video) {
			this.video = video;
		}

		@Override
		void execute() {
			api.changeVideo(video.getKey(), video, this);

		}

		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSuccess(Date result) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * A {@link Task} that manages the call to the server to update the contents
	 * of a {@link Note}.
	 */

	/**
	 * A {@link Task} that manages the call to the server to update the position
	 * of a {@link VideoCanvas}.
	 */
	private class UpdateNotePositionTask extends Task implements AsyncCallback<Date> {
		private final VideoCanvas note;

		private final int x, y, width, height;

		public UpdateNotePositionTask(VideoCanvas note, int x, int y, int w, int h) {
			this.note = note;
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
		}

		public void execute() {
			note.setX(x);
			note.setY(y);
			note.setWidth(width);
			note.setHeight(height);
			api.changeNotePosition(note.getKey(), x, y, width, height, this);
		}

		public void onFailure(Throwable caught) {
			getQueue().taskFailed(this, caught instanceof Service.AccessDeniedException);
		}

		public void onSuccess(Date result) {
			note.update(result);
			getQueue().taskSucceeded(this);
		}
	}

	

	private Model(Author author, Surface selectedSurface, String logoutUrl, ServiceAsync api, StatusObserver statusObserver) {
		this.author = author;
		this.api = api;
		this.logoutUrl = logoutUrl;
		this.statusObserver = statusObserver;

		selectedSurface.initialize(this);
		selectSurface(selectedSurface);

		surfaceLoader.start();
	}

	/**
	 * Provides an asynchronous factory for loading a {@link Model}.
	 * 
	 * @param loadObserver
	 *            a callback to receive load events
	 * @param statusObserver
	 *            a callback to receive status events
	 */
	public static void load(final LoadObserver loadObserver, final StatusObserver statusObserver) {
		final ServiceAsync api = GWT.create(Service.class);
		api.getUserInfo(new AsyncCallback<Service.UserInfoResult>() {
			public void onFailure(Throwable caught) {
				loadObserver.onModelLoadFailed();
				
			}

			public void onSuccess(UserInfoResult result) {
				loadObserver.onModelLoaded(new Model(result.getAuthor(), result.getSurface(), result.getLogoutUrl(), api, statusObserver));
			}
		});
	}

	native static void forceApplicationReload() /*-{
		$wnd.location.reload();
	}-*/;

	/**
	 * Add an {@link Author} as a member of a particular {@link Surface} and
	 * persist that change to the server.
	 * 
	 * @param surface
	 *            the surface to which the author will be added
	 * @param email
	 *            the email address of the person to add
	 * @param callback
	 *            a callback to report success/failure to the caller
	 */
	public void addAuthorToSurface(Surface surface, String email, SuccessCallback callback) {
		taskQueue.post(new AddAuthorToSurfaceTask(surface, email, callback));
	}

	/**
	 * Subsscribes a {@link DataObserver} to receive data related events from
	 * this {@link Model}.
	 * 
	 * @param observer
	 */
	public void addDataObserver(DataObserver observer) {
		dataObservers.add(observer);
	}

	public void addStreamObserver(VideoSearchObserver searchObserver) {
		videoSearchObservers.add(searchObserver);

	}

	/**
	 * Creates a note with no content at a particular location on the
	 * {@link Surface} and persists that change to the server.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void createNote(int x, int y, int width, int height) {
		final VideoCanvas note = new VideoCanvas(this, x, y, width, height);
		notifyNoteCreated(note);
		taskQueue.post(new CreateNoteTask(getSelectedSurface(), note));
	}

	/**
	 * Creates a {@link Surface} with the specified title and persists that
	 * change to the server.
	 * 
	 * @param title
	 */
	public void createSurface(String title) {
		final Surface surface = new Surface(this, title);
		notifySurfaceCreated(surface);
		taskQueue.post(new CreateSurfaceTask(surface));
		selectSurface(surface);
	}

	public void getYouTubeSearchResults(String searchString) {
		final VideoSearchResults finalResult = new VideoSearchResults();
		StringBuffer jsonURL = new StringBuffer("http://gdata.youtube.com/feeds/api/videos?q=");
		jsonURL.append(searchString);
		jsonURL.append("&max-results=5&alt=json-in-script&callback=");
		JSONRequest.get(jsonURL.toString(), new JSONRequestHandler() {

			@Override
			public void onRequestComplete(JavaScriptObject json) {
				System.out.println("ok2");
				JSONObject jso = new JSONObject(json);
				JSONArray ary = jso.get("feed").isObject().get("entry").isArray();
				// JSONValue value = JSONParser.parse(json.toSource());
				for (int i = 0; i < ary.size(); ++i) {
					String title = ary.get(i).isObject().get("title").isObject().get("$t").toString();
					System.out.println("Title: " + title);

					String contentText = ary.get(i).isObject().get("content").isObject().get("$t").toString();
					System.out.println("Content: " + contentText);

					JSONArray jsonValue = ary.get(i).isObject().get("link").isArray();
					String hrefLink = jsonValue.get(0).isObject().get("href").toString();
					System.out.println("ID: " + hrefLink);
					finalResult.addSearchResult(title, hrefLink, contentText);

				}
				notifyStreamObserver(finalResult);

			}

		});

	}

	/**
	 * Gets the currently logged in author.
	 * 
	 * @return
	 */
	public Author getCurrentAuthor() {
		return author;
	}

	/**
	 * Gets a url that can be used to log out the current user.
	 * 
	 * @return
	 */
	public String getLogoutUrl() {
		return logoutUrl;
	}

	/**
	 * Gets the currently selected surface.
	 * 
	 * @return
	 */
	public Surface getSelectedSurface() {
		return selectedSurface;
	}

	/**
	 * Selects the specified {@link Surface}. The newly selected surface should
	 * be made visible in the Ui.
	 * 
	 * @param surface
	 *            the surface to display
	 */
	public void selectSurface(Surface surface) {
		if (surface == selectedSurface) {
			return;
		}

		final Surface wasSelected = selectedSurface;
		selectedSurface = surface;

		noteLoader.reset();
		// videoLoader.start();

		notifySurfaceSelected(surface, wasSelected);
	}

	/**
	 * Updates the contents of a {@link VideoCanvas} and persists the change to the
	 * server.
	 * 
	 * @param note
	 * @param content
	 */
	public void updateNoteContent(final VideoCanvas note, String content) {
		taskQueue.post(new UpdateNoteContentTask(note, content));
	}

	public void updateNoteVideo(final VideoCanvas note, Video video) {
		taskQueue.post(new UpdateNoteVideoTask(note, video));

	}

	public void updateVideo(Video video) {
		taskQueue.post(new UpdateVideoTask(video));
	}

	/**
	 * Updates the position of a {@link VideoCanvas} and persists the change to the
	 * server.
	 * 
	 * @param note
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void updateNotePosition(final VideoCanvas note, int x, int y, int width, int height) {
		taskQueue.post(new UpdateNotePositionTask(note, x, y, width, height));
	}

	private void notifySurfaceSelected(Surface nowSelected, Surface wasSelected) {
		for (int i = 0, n = dataObservers.size(); i < n; ++i) {
			dataObservers.get(i).onSurfaceSelected(nowSelected, wasSelected);
		}
	}

	public ServiceAsync getService() {
		return api;
	}

	StatusObserver getStatusObserver() {
		return statusObserver;
	}

	void notifyNoteCreated(VideoCanvas note) {
		for (int i = 0, n = dataObservers.size(); i < n; ++i) {
			dataObservers.get(i).onNoteCreated(note);
		}
	}

	void notifySurfaceCreated(Surface surface) {
		for (int i = 0, n = dataObservers.size(); i < n; ++i) {
			dataObservers.get(i).onSurfaceCreated(surface);
		}
	}

	void notifySurfaceNotesReceived(VideoCanvas[] notes) {
		for (int i = 0, n = dataObservers.size(); i < n; ++i) {
			dataObservers.get(i).onSurfaceNotesReceived(notes);
		}
	}

	void notifySurfacesReceived(Surface[] surfaces) {
		for (int i = 0, n = dataObservers.size(); i < n; ++i) {
			dataObservers.get(i).onSurfacesReceived(surfaces);
		}
	}

	void notifyStreamObserver(VideoSearchResults results) {
		for (int i = 0, n = videoSearchObservers.size(); i < n; ++i) {
			videoSearchObservers.get(i).onStreamReceived(results);
		}
	}

	/**
	 * Invoked by tasks and loaders when RPC invocations begin to fail.
	 */
	void onServerFailed(boolean fatal) {
		if (fatal) {
			forceApplicationReload();
			return;
		}

		if (!offline) {
			statusObserver.onServerWentAway();
			offline = true;
		}
	}

	/**
	 * Invoked by tasks and loaders when RPC invocations succeed.
	 */
	void onServerSucceeded() {
		if (offline) {
			statusObserver.onServerCameBack();
			offline = false;
		}
	}

	public void onStartSearch() {
		notifySearchStart();
	}

	private void notifySearchStart() {
		for (int i = 0, n = videoSearchObservers.size(); i < n; ++i) {
			videoSearchObservers.get(i).onStartSearch();
		}

	}

}
