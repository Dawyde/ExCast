package fr.exensoft.audioserver.core.metadata;

import java.util.Queue;

import fr.exensoft.audioserver.core.AudioStreamManager;

public class StreamMetadata {
	public final static int MAX_STREAM_TITLES = 1;
	
	private AudioStreamManager mAudioStreamManager;
	
	private String mName;
	
	private Queue<StreamTitle> mStreamTitles;
	
	private StreamTitle mCurrentStreamTitle = null;
	
	private long mCurrentStep = 0;
	
	private long mNextStepCounter = 0;
	
	public StreamMetadata(AudioStreamManager audioStreamManager){
		mAudioStreamManager = audioStreamManager;
	}
	
	public void newStreamTitle(String title){
		StreamTitle streamTitle = new StreamTitle(title, mAudioStreamManager.getSourceByte(), System.currentTimeMillis()/1000);
		mNextStepCounter = streamTitle.getByteCounter();
		mStreamTitles.add(streamTitle);
		if(mStreamTitles.size() > MAX_STREAM_TITLES){
			mStreamTitles.poll();
		}
	}
	
	public long getCurrentStepCounter(){
		if(mNextStepCounter > 0){
			if(mNextStepCounter >= mAudioStreamManager.getCurrentByte()){
				mCurrentStep++;
				mNextStepCounter = 0;
				mCurrentStreamTitle = mStreamTitles.peek();
			}
		}
		
		return mCurrentStep;
	}
	
	public String getCurrentStreamTitle(){
		return mCurrentStreamTitle==null?"":mCurrentStreamTitle.getStreamTitle();
	}
}