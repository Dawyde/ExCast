package fr.exensoft.audioserver.core.stream;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.buffers.AudioSampleBuffer;
import fr.exensoft.audioserver.core.buffers.BytePacket;
import fr.exensoft.audioserver.core.buffers.RTPPacket;
import fr.exensoft.audioserver.core.stream.format.AudioStreamFormat;
import fr.exensoft.audioserver.core.stream.format.IcecastStreamFormat;
import fr.exensoft.audioserver.core.stream.format.RTPInterleavedStreamFormat;
import fr.exensoft.audioserver.core.stream.format.RTPUnicastStreamFormat;
import fr.exensoft.audioserver.format.encoder.IcecastFormatEncoder;
import fr.exensoft.audioserver.format.encoder.RTPFormatEncoder;
import fr.exensoft.audioserver.format.encoder.StreamFormat;
import fr.exensoft.audioserver.format.encoder.StreamFormatEncoder;

public class AudioStream implements Runnable{
	
	private AudioStreamManager mAudioStreamManager;
	
	private String mIdentifier;
	
	private StreamEncoder mStreamEncoder;
	
	private Map<StreamFormat, AudioStreamFormat> mAudioStreamFormat;
	
	private List<AudioStreamFormat> mAudioStreamFormatList;
	
	private BaseBufferManager<AudioSampleBuffer> mAudioSampleBufferManager;
	/**
	 * Samples à encoder
	 */
	private BlockingQueue<AudioSampleBuffer> mSamplesToEncode;
	
	private boolean mIsRunning = false;
	
	private Thread mThread;
	
	
	public AudioStream(AudioStreamManager audioStreamManager, String identifier, StreamEncoder streamEncoder){
		mAudioStreamManager = audioStreamManager;
		mIdentifier = identifier;
		mStreamEncoder = streamEncoder;
		mAudioSampleBufferManager = audioStreamManager.getBufferManager().getBaseBufferManager(AudioSampleBuffer.class);
		mSamplesToEncode = new SynchronousQueue<>();
		
		mAudioStreamFormat = new HashMap<>();
		//On ajoute les flux
		for(StreamFormat format : streamEncoder.getStreamFormats()){
			//On regarde si on a bien un encodeur pour ce format
			StreamFormatEncoder encoder = streamEncoder.getStreamFormatEncoder(format);
			if(encoder == null){
				continue;
			}

			if(format == StreamFormat.RTP){
				mAudioStreamFormat.put(format, new RTPUnicastStreamFormat(audioStreamManager, (RTPFormatEncoder) encoder));
			}
			else if(format == StreamFormat.INTERLEAVED_RTP){
				mAudioStreamFormat.put(format, new RTPInterleavedStreamFormat(audioStreamManager, (RTPFormatEncoder) encoder));
			}
			else if(format == StreamFormat.ICECAST){
				mAudioStreamFormat.put(format, new IcecastStreamFormat(audioStreamManager, (IcecastFormatEncoder) encoder));
			}
		}
		mAudioStreamFormatList = new LinkedList<>(mAudioStreamFormat.values());
		
	}
	
	public AudioStreamFormat getStreamFormatEncoder(StreamFormat streamFormat){
		return mAudioStreamFormat.get(streamFormat);
	}
	
	public void start(){
		mStreamEncoder.init();
		for(AudioStreamFormat audioStreamFormat : mAudioStreamFormat.values()){
			audioStreamFormat.init();
		}
		mIsRunning = true;
		mThread = new Thread(this);
		mThread.start();
	}


	public void encodeAudioSample(AudioSampleBuffer audioSample){
		audioSample.acquire();
		try {
			mSamplesToEncode.put(audioSample);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] data = new byte[2048];
	@Override
	public void run(){
		try {
			AudioSampleBuffer buffer;
			while(mIsRunning){
				//On récupère des données à encoder
				buffer = mSamplesToEncode.take();
				
				//On encode les données
				mStreamEncoder.encodeAudioData(buffer.getBuffer(), buffer.getLength(), buffer.getStartByte(), buffer.getEndByte());
				//On libère les données
				buffer.release();
				if(buffer.getUsage()<=0){
					buffer.reset();
					mAudioSampleBufferManager.releasePacket(buffer);
				}
				
			}
		} catch (Exception e) {
			mIsRunning = false;
			e.printStackTrace();
			System.out.println("Arrêt du thread");
		}
	}

	public void sendAudioSamples(long currentBytePosition){
		for(AudioStreamFormat audioStreamFormat : mAudioStreamFormatList){
			audioStreamFormat.sendAudioSamples(currentBytePosition);
		}
	}

	public String getIdentifier() {
		return mIdentifier;
	}

	
}
