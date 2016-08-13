package fr.exensoft.audioserver.codec.decoder;


import fr.exensoft.audioserver.core.buffermanager.BufferManager;

public abstract class StreamDecoder {

	protected BufferManager mBufferManager;
	
	protected AudioSampleListener mAudioSampleListener;
	
	public StreamDecoder(BufferManager bufferManager){
		mBufferManager = bufferManager;
	}
	
	public void setAudioSampleListener(AudioSampleListener audioSampleListener){
		mAudioSampleListener = audioSampleListener;
	}
	
	public void setBitrate(int bitrate){
		
	}
	
	protected void newAudioSamples(byte[] buffer, int length){
		if(mAudioSampleListener != null){
			mAudioSampleListener.newAudioSamples(buffer, length);
		}
	}
	
	public abstract void decodeAudioData(byte[] buffer, int offset, int length);
	
	public abstract void init();
	
	public abstract void close();
}
