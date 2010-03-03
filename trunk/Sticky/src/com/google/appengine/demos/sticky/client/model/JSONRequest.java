package com.google.appengine.demos.sticky.client.model;

/* Copyright (c) 2008 Google Inc.
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




public class JSONRequest {
  public static void get(String url, JSONRequestHandler handler) {
    String callbackName = "JSONCallback"+handler.hashCode();
    get( url+callbackName, callbackName, handler );
  }	
  public static void get(String url, String callbackName, JSONRequestHandler handler ) {
    createCallbackFunction( handler, callbackName );
    addScript(url);
  }
  public static native void addScript(String url) /*-{
    var scr = document.createElement("script");
    scr.setAttribute("language", "JavaScript");
    scr.setAttribute("src", url);
    document.getElementsByTagName("body")[0].appendChild(scr);
  }-*/;
  private native static void createCallbackFunction( JSONRequestHandler obj, String callbackName)/*-{
    tmpcallback = function(j) {
      obj.@com.google.appengine.demos.sticky.client.model.JSONRequestHandler::onRequestComplete(Lcom/google/gwt/core/client/JavaScriptObject;)(j);
    };
    eval( "window." + callbackName + "=tmpcallback" );
  }-*/;
}