package fr.exensoft.audioserver.core;

import java.util.LinkedList;
import java.util.List;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.buffers.AudioSampleBuffer;
import fr.exensoft.audioserver.core.source.AudioSource;
import fr.exensoft.audioserver.core.stream.AudioStream;

public class AudioStreamManager implements Runnable{
	
	public final static int SAMPLE_BUFFER_LENGTH = 1024;
	
	/**
	 * Identifieur du Flux audio
	 */
	private String mIdentifier;
	
	/**
	 * Compteur de bits actuel
	 */
	private long mCurrentByteCounter = 0;
	
	/**
	 * Compteur de bits au niveau de l'encodeur
	 */
	private long mSourceByteCounter = 0;
	
	
	/**
	 * Nombre d'échantillon par seconde
	 */
	private long mSampleRate = 44100;
	
	/**
	 * Nombre de canaux
	 */
	private int mChannelCount = 2;
	
	/**
	 * Nombre d'octets pour un échantillon
	 */
	private int mBytesPerSample = 2;
	
	
	/**
	 * Date en milliseconde de démarage
	 */
	private long mStartTime;
	
	/**
	 * Nombre de bytes de samples lus pour 1 seconde
	 */
	private long mBytesPerSecond;
	
	/**
	 * Si le stream est en cours de lecture actuellement
	 */
	private boolean mIsRunning = false;
	
	/**
	 * Gestionnaire de source audio
	 */
	private AudioSourceManager mAudioSourceManager;
	
	private Thread mThread;
	
	private BufferManager mBufferManager;
	
	private BaseBufferManager<AudioSampleBuffer> mAudioSampleBufferManager;
	
	private AudioServer mAudioServer;
	
	/**
	 * Flux audio
	 */
	private List<AudioStream> mAudioStreams;
	
	public AudioStreamManager(AudioServer audioServer, String identifier){
		mIdentifier = identifier;
		mBytesPerSecond = mSampleRate*mChannelCount*mBytesPerSample;
		mCurrentByteCounter = 0;
		mSourceByteCounter = 0;
		mAudioServer = audioServer;
		mAudioStreams = new LinkedList<AudioStream>();
		mBufferManager = mAudioServer.getBufferManager();
		mBufferManager.registerBufferManager(AudioSampleBuffer.class, new AudioSampleBuffer.AudioSampleBufferFactory(SAMPLE_BUFFER_LENGTH));
		mAudioSourceManager = new AudioSourceManager(this);

		mAudioSampleBufferManager = mBufferManager.getBaseBufferManager(AudioSampleBuffer.class);
	}
	
	public void addAudioStream(AudioStream audioStream){
		mAudioStreams.add(audioStream);
	}

	public long getBytesPerSecond(){
		return mBytesPerSecond;
	}
	
	public String getIdentifier(){
		return mIdentifier;
	}
	
	public AudioStream getAudioStream(String identifier){
		return mAudioStreams.stream()
			.filter(stream->stream.getIdentifier().equals(identifier))
			.findFirst()
			.orElse(null);
	}
	
	public void start(){
		mIsRunning = true;
		mStartTime = System.currentTimeMillis();
		mThread = new Thread(this);
		mThread.start();
		for(AudioStream audioStream : mAudioStreams){
			audioStream.start();
		}
		mAudioSourceManager.start();
	}
	
	public void encodeAudio(byte[] audioSamples, int length){
		//On prend un packet de buffer
		AudioSampleBuffer buffer = mAudioSampleBufferManager.getPacket();
		buffer.write(audioSamples, 0, length);
		buffer.setLength(length);
		//On s'occupe du compteur de bytes
		buffer.setStartByte(mSourceByteCounter);
		mSourceByteCounter+= length;
		buffer.setEndByte(mSourceByteCounter);
		
		//On envoie les paquets à l'encodage
		buffer.acquire();
		for(AudioStream audioStream : mAudioStreams){
			audioStream.encodeAudioSample(buffer);
		}
		buffer.release();
		if(buffer.getUsage()<=0){
			buffer.reset();
			mAudioSampleBufferManager.releasePacket(buffer);
		}
	}

	@Override
	public void run() {
		long lastTime = System.currentTimeMillis();
		while(mIsRunning){
			long currentTime = System.currentTimeMillis();
			mCurrentByteCounter = (currentTime-mStartTime)*mBytesPerSecond/1000;
			if (currentTime - lastTime > 40)
			{
				for(AudioStream audioStream : mAudioStreams){
					audioStream.sendAudioSamples(mCurrentByteCounter);
				}
				lastTime = currentTime;
			}
			else{
				try{
					Thread.sleep(40- (currentTime-lastTime));
				}
				catch(InterruptedException e){
					
				}
			}
		}
	}

	public BufferManager getBufferManager(){
		return mBufferManager;
	}

	public long getBufferedBytes(){
		return mSourceByteCounter - mCurrentByteCounter;
	}
	
	public void addAudioSource(AudioSource audioSource){
		mAudioSourceManager.addAudioSource(audioSource);
	}
	
	public void removeAudioSource(AudioSource audioSource){
		mAudioSourceManager.removeAudioSource(audioSource);
	}

	public void close() {
		// TODO Fermer les flux
	}

	public long getCurrentByte() {
		return mCurrentByteCounter;
	}
	
	public long getSourceByte() {
		return mSourceByteCounter;
	}
}
