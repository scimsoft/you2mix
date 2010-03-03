package com.google.appengine.demos.sticky.client.model;

import com.google.gwt.core.client.JavaScriptObject;

public interface JSONRequestHandler 
{
	public void onRequestComplete( JavaScriptObject json );
}