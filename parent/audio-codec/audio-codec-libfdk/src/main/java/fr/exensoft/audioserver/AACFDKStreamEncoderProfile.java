package fr.exensoft.audioserver;

import fr.exensoft.audioserver.codec.manager.AbstractCodecProfile;

public class AACFDKStreamEncoderProfile extends AbstractCodecProfile {
	
	protected int mAot;
	
	protected int mChannelMode;
	
	protected int mBitrate;
	
	protected int mSampleRate;
	
	protected int mBitrateMode;
	
	protected int mTransmux;
	
	protected int mChannelOrder;
	
	protected int mSbrMode;
	
	protected boolean mAfterBurner;

	public AACFDKStreamEncoderProfile(String profileName) {
		super(profileName);
	}
	
	public AACFDKStreamEncoderProfile(AACFDKStreamEncoderProfile original){
		super(original.mProfileName);
		
		mAot = original.mAot;
		mChannelMode = original.mChannelMode;
		mBitrate = original.mBitrate;
		mSampleRate = original.mSampleRate;
		mBitrateMode = original.mBitrateMode;
		mTransmux = original.mTransmux;
		mChannelOrder = original.mChannelOrder;
		mSbrMode = original.mSbrMode;
		mAfterBurner = original.mAfterBurner;
	}

	
	public void setAot(int aot){
		mAot = aot;
	}
	
	public void setChannelMode(int channelMode){
		mChannelMode = channelMode;
	}
	
	public void setBitrate(int bitrate){
		mBitrate = bitrate;
	}
	
	public void setSampleRate(int sampleRate){
		mSampleRate = sampleRate;
	}
	
	public void setBitrateMode(int bitrateMode){
		mBitrateMode = bitrateMode;
	}
	
	public void setTransmux(int transmux){
		mTransmux = transmux;
	}
	
	public void setChannelOrder(int channelOrder){
		mChannelOrder = channelOrder;
	}
	
	public void setSbrMode(int sbrMode){
		mSbrMode = sbrMode;
	}
	
	public void setAfterBurner(boolean afterBurner){
		mAfterBurner = afterBurner;
	}
	
	public int getAot(){
		return mAot;
	}
	
	public int getChannelMode(){
		return mChannelMode;
	}
	
	public int getBitrate(){
		return mBitrate;
	}
	
	public int getSampleRate(){
		return mSampleRate;
	}
	
	public int getBitrateMode(){
		return mBitrateMode;
	}
	
	public int getTransmux(){
		return mTransmux;
	}
	
	public int getChannelOrder(){
		return mChannelOrder;
	}
	
	public int getSbrMode(){
		return mSbrMode;
	}
	
	public boolean getAfterBurner(){
		return mAfterBurner;
	}
}
