package com.example.test2;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EPGData {
	private Date _start;
	private Date _stop;
	private String _title;
	private String _url="";
	
	public EPGData (Date start,Date stop,String title){_start=start;_stop=stop;_title=title;}
	public String getTitle(){return _title;}
	public String getStopTime(){
		SimpleDateFormat sdf= new SimpleDateFormat("HH:mm");
		return sdf.format(_stop);
	}
	public String getStopDayTime(){
		SimpleDateFormat sdf= new SimpleDateFormat("dd.MM HH:mm");
		return sdf.format(_stop);
	}
	public String getStartTime(){
		SimpleDateFormat sdf= new SimpleDateFormat("HH:mm");
		return sdf.format(_start);
	}
	public String getStartDayTime(){
		SimpleDateFormat sdf= new SimpleDateFormat("dd.MM HH:mm");
		return sdf.format(_start);
	}
	public Date getStop(){return _stop;}
	public Date getStart(){return _start;}
	public String getUrl() {
		return _url;
	}
	public void setUrl(String _url) {
		this._url = _url;
	}
}
