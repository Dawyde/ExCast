package fr.exensoft.audioserver.codec;

import fr.exensoft.audioserver.codec.manager.AbstractCodecProfile;

public class XugglerProfile extends AbstractCodecProfile {


	public XugglerProfile(XugglerProfile profile) {
		super(profile.mProfileName);
		
		mSampleRate = profile.mSampleRate;
		mBitrate = profile.mBitrate;
		mChannels = profile.mChannels;
	}
	
	public XugglerProfile(String name){
		super(name);
	}
	
	private int mSampleRate;

	private int mBitrate;

	private int mChannels;

	public int getSampleRate() {
		return mSampleRate;
	}

	public int getBitrate() {
		return mBitrate;
	}

	public int getChannels() {
		return mChannels;
	}

	public void setSampleRate(int sampleRate) {
		mSampleRate = sampleRate;
	}

	public void setBitrate(int bitrate) {
		mBitrate = bitrate;
	}

	public void setChannels(int channels) {
		mChannels = channels;
	}
}
