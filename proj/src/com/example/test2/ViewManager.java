package com.example.test2;

public interface ViewManager {
	public enum AppViewState {INTERFACE,EPG,VIDEO;
	 public AppViewState getNext() {
	     return this.ordinal() < AppViewState.values().length - 1
	         ? AppViewState.values()[this.ordinal() + 1]
	         : INTERFACE;
	   }
	 public AppViewState getPrev() {
	     return this.ordinal() != INTERFACE.ordinal()
	         ? AppViewState.values()[this.ordinal() - 1]
	         : VIDEO;
	   }
	}
	public void onViewInterface(AppViewState from);
	public void onViewEpg(AppViewState from);
	public void onViewVideo(AppViewState from);
	
}
