package com.mars.miuifilemanager.adapter;


public class FavoriteItem {
	public FileInfo fileInfo;
	public long id;
	public String location;
	public String title;
	
	public FavoriteItem(long id, String title, String location) {
		this.id = id;
		this.title = title;
		this.location = location;
	}

	public FavoriteItem(String title, String location) {
		this.title = title;
		this.location = location;
	}
}
