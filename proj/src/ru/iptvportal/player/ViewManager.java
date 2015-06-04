package ru.iptvportal.player;

import android.view.animation.Animation;

public interface ViewManager {
	public enum AppViewState {INTERFACE,EPG,VIDEO,LOGIN;
	
	 public AppViewState getNext() {
	     return this.ordinal() < AppViewState.values().length - 2
	         ? AppViewState.values()[this.ordinal() + 1]
	         : INTERFACE;
	   }
	 public AppViewState getPrev() {
	     return this.ordinal() != INTERFACE.ordinal()
	         ? AppViewState.values()[this.ordinal() - 1]
	         : VIDEO;
	   }
	}
	public void onViewInterface(AppViewState from,Animation a);
	public void onViewEpg(AppViewState from,Animation a);
	public void onViewVideo(AppViewState from,Animation a);
	public void onViewLogin(AppViewState from,Animation a);
}
