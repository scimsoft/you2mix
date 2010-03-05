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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.you2mix.mix.client.model.Service.GetVideoResult;

/**
 * Controls all aspects of loading the set of {@link Surface}s associated with
 * the current author. This class takes care of performing (and possibly
 * retrying) a query for the initial set of Notes and then continues polling the
 * server for updates.
 * 
 * @author knorton@google.com (Kelly Norton)
 */
class You2MixVideoLoader extends Timer implements AsyncCallback<GetVideoResult> {

  /**
   * Controls the initial laod of surfaces from the server and will retry on
   * failure.
   */
  private class InitialLoader extends RetryTimer implements
      AsyncCallback<GetVideoResult> {

    /**
     * Constructs and starts an initial load.
     */
    public InitialLoader() {
      start();
    }

    public void onFailure(Throwable caught) {
      model.onServerFailed(caught instanceof Service.AccessDeniedException);

      retryLater();
    }

    public void onSuccess(GetVideoResult result) {
      model.onServerSucceeded();

      startPolling();

      assert result != null;

      timestamp = result.getTimestamp();
      final You2MixVideo video = result.getVideo();
      
        video.initialize(model);
        videoCache.put(video.getKey(), video);
      
     
    }

    private void start() {
      model.getService().getVideos(null /* timestamp */, this);
    }

    @Override
    protected void retry() {
      start();
    }
  }


  /**
   * The model being controlled by this loader.
   */
  private final Model model;

  /**
   * A cache of {@link Surface}s that have been loaded. Used to ensure that
   * there is only one instance of each surface.
   */
  private final Map<String, You2MixVideo> videoCache = new HashMap<String, You2MixVideo>();

  /**
   * The polling period.
   */
  private final int interval;

  /**
   * The most recent timestamp returned by the server.
   */
  private String timestamp;

  /**
   * Constructs a new loader for the given model.
   * 
   * @param model
   *          the model this loader will control
   * @param interval
   *          the polling interval
   */
  public You2MixVideoLoader(Model model, int interval) {
    this.model = model;
    this.interval = interval;
  }

  public void onFailure(Throwable caught) {
    model.onServerFailed(caught instanceof Service.AccessDeniedException);
  }

  public void onSuccess(GetVideoResult result) {
    model.onServerSucceeded();
    if (result != null) {
      timestamp = result.getTimestamp();
      final You2MixVideo video = result.getVideo();
      if(video!=null){
        final You2MixVideo existing = videoCache.get(video.getKey());
        if (existing == null) {
          video.initialize(model);
          videoCache.put(video.getKey(), video);
         
        } else {
          existing.update(model, video);
        }
      }
    }
  }

  @Override
  public void run() {
    model.getService().getVideos(timestamp, this);
  }

  /**
   * Starts the loader by issuing an initial load. When that load completes, the
   * loader will start polling.
   */
  public void start() {
    new InitialLoader();
  }

  private void startPolling() {
    scheduleRepeating(interval);
  }

  /**
   * Invoked by {@link Model} when it creates a {@link Surface}.
   * 
   * @param key
   *          the key for the newly saved surface
   * @param video
   *          a reference to the surface
   */
  void cacheSurface(String key, You2MixVideo video) {
    assert key != null;
    videoCache.put(key, video);
  }
}
