package ru.iptvportal.player;

import java.util.ArrayList;

public class ProfilesData {
	class Profile{
		private int _id;
		private String _name="";
		private String _title="";
		private boolean _pass_req=false;
		private int _age_limit=0;
		private String _password=null;
		public int getId() {
			return _id;
		}
		public void setId(int _id) {
			this._id = _id;
		}
		public String getTitle() {
			return _title;
		}
		public void setTitle(String _title) {
			this._title = _title;
		}
		public boolean isPassReq() {
			return _pass_req;
		}
		public void setPassReq(boolean _pass_req) {
			this._pass_req = _pass_req;
		}
		public int getAgeLimit() {
			return _age_limit;
		}
		public void setAgeLimit(int _age_limit) {
			this._age_limit = _age_limit;
		}
		public String getPassword() {
			return _password;
		}
		public void setPassword(String password) {
			_password = password;
		}
	};

	private ArrayList<Profile> _profiles;
	
	public ProfilesData()
	{
		_profiles=new ArrayList<Profile>();
	}
	
	public void addProfile(int id,String name,String title,boolean pass_req,int age_limit,String pass)
	{
		Profile p=new Profile();
		p.setId(id);
		p._name=name;
		p.setTitle(title);
		p.setPassReq(pass_req);
		p.setAgeLimit(age_limit);
		p.setPassword(pass);
		_profiles.add(p);
	}
	public ArrayList<Profile> getProfiles() {return _profiles;}
	public Profile getProfile(int num)
	{
		if (num>=0 && num<_profiles.size())
			return _profiles.get(num);
		else
			return new Profile();
	}
}
