package com.you2mix.mix.client.model;

import com.google.gwt.core.client.JavaScriptObject;

public interface JSONRequestHandler 
{
	public void onRequestComplete( JavaScriptObject json );
}