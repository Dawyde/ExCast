package fr.exensoft.audioserver.codec.manager;

public abstract class AbstractCodecProfile {
	protected String mProfileName;
	
	public AbstractCodecProfile(String profileName){
		mProfileName = profileName;
	}
	
	public String getName(){
		return mProfileName;
	}
}
