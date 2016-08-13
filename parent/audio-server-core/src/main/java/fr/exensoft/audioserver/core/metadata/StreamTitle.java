package fr.exensoft.audioserver.core.metadata;

public class StreamTitle {
	private String mStreamTitle;
	
	private long mByteCounter;
	
	private long mTime;
	
	public StreamTitle(String streamTitle, long byteCounter, long time){
		mStreamTitle = streamTitle;
		mByteCounter = byteCounter;
		mTime = time;
	}

	public String getStreamTitle(){
		return mStreamTitle;
	}
	
	public long getByteCounter(){
		return mByteCounter;
	}
	
	public long getTime(){
		return mTime;
	}
}
