package fr.exensoft.audioserver.codec.encoder;

import java.util.LinkedList;
import java.util.List;

import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;
import fr.exensoft.audioserver.format.encoder.StreamFormatEncoder;

public abstract class StreamEncoder {
	
	protected BufferManager mBufferManager;
	
	protected List<StreamFormatEncoder> mStreamFormatEncoders = new LinkedList<>();
	
	protected List<StreamFormat> mStreamFormats;
	
	protected int mNumChannels = 2;

	protected int mBitrate = 128;
	
	protected int mSampleRate = 44100;
	
	public StreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats){
		mBufferManager = bufferManager;
		mStreamFormats = streamFormats;
		createStreamFormatEncoders(streamFormats);
	}
	
	public List<StreamFormat> getStreamFormats(){
		return mStreamFormats;
	}
	
	public abstract void encodeAudioData(byte[] buffer, int length, long startByte, long endByte);
	
	public void addStreamFormatEncoder(StreamFormatEncoder streamFormatEncoder){
		mStreamFormatEncoders.add(streamFormatEncoder);
	}
	
	public StreamFormatEncoder getStreamFormatEncoder(StreamFormat streamFormat){
		return mStreamFormatEncoders.stream()
				.filter(encoder->encoder.getFormat()==streamFormat)
				.findFirst()
				.orElse(null);
	}
	
	protected void newPacketReady(byte[] buffer, int length, long startByte, long endByte){
		for(StreamFormatEncoder streamFormatEncoder : mStreamFormatEncoders){
			streamFormatEncoder.encodeAudioData(buffer, length, startByte, endByte);
		}
	}
	
	public void setChannel(int channel){
		mNumChannels = channel;
	}
	
	public void setSampleRate(int sampleRate){
		mSampleRate = sampleRate;
	}
	
	public void setBitrate(int bitrate){
		mBitrate = bitrate;
	}
	
	protected abstract void createStreamFormatEncoders(List<StreamFormat> streamFormats);
	
	public abstract void init();
	
	public abstract void close();
}
