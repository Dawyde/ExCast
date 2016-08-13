package fr.exensoft.audioserver.core.source;

public class SourceDescriptor {

	private String mAudioFormat;

	private String mSourceFormat;
	
	private int mBitrate = 128;
	
	public SourceDescriptor(String sourceFormat, String audioFormat){
		mAudioFormat = audioFormat;
		mSourceFormat = sourceFormat;
	}
	
	public void setBitrate(int bitrate){
		mBitrate = bitrate;
	}
	
	public int getBitrate(){
		return mBitrate;
	}
	
	public String getAudioFormat(){
		return mAudioFormat;
	}
	
	public String getSourceFormat(){
		return mSourceFormat;
	}

}
