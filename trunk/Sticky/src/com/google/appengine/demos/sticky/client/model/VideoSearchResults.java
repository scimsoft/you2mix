package com.google.appengine.demos.sticky.client.model;

import java.util.ArrayList;

public class VideoSearchResults {
	
	private ArrayList<VideoSearchResult> results;
	
	public VideoSearchResults(){
		setResults(new ArrayList<VideoSearchResult>());
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(ArrayList<VideoSearchResult> results) {
		this.results = results;
	}

	/**
	 * @return the results
	 */
	public ArrayList<VideoSearchResult> getResults() {
		return results;
	}

	public void addSearchResult(VideoSearchResult searchResult){
		results.add(searchResult);
	}



	public class VideoSearchResult{
		private String title;
		private String youTubeID;
		private String videoLink;
		private String description;
	
	public VideoSearchResult(String title, String videoHref,String descritpion) {
		
		this.setTitle(title);
		this.setVideoLink(videoHref);
		this.setYouTubeID(videoHref.subSequence(32, 43).toString());
		this.description = descritpion;
		System.out.println("youTubeID: " + youTubeID);
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param youTubeID the youTubeID to set
	 */
	public void setYouTubeID(String youTubeID) {
		this.youTubeID = youTubeID;
	}

	/**
	 * @return the youTubeID
	 */
	public String getYouTubeID() {
		return youTubeID;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param videoLink the videoLink to set
	 */
	public void setVideoLink(String videoLink) {
		this.videoLink = videoLink;
	}

	/**
	 * @return the videoLink
	 */
	public String getVideoLink() {
		return videoLink;
	}
	}



	public void addSearchResult(String title, String hrefLink, String description) {
		
		
		addSearchResult(new VideoSearchResult(title, hrefLink, description));
		
	}
}
