package fr.exensoft.audioserver.core;

import java.util.LinkedList;
import java.util.List;

import fr.exensoft.audioserver.core.source.AudioSource;

/**
 * Gestion des sources audio
 */
public class AudioSourceManager implements Runnable{
	
	private boolean mIsRunning = false;
	
	private Thread mThread;
	
	private AudioStreamManager mAudioStreamManager;
	
	private List<AudioSource> mAudioSources = new LinkedList<>();
	
	private AudioSource mAudioSource = null;
	
	private long mBytesToBufferize;
	
	private long mMinBytes;
	
	private byte[] mBuffer = new byte[1024];
	
	public AudioSourceManager(AudioStreamManager audioStreamManager){
		mAudioStreamManager = audioStreamManager;
		mBytesToBufferize = mAudioStreamManager.getBytesPerSecond()*2;
		mMinBytes = mAudioStreamManager.getBytesPerSecond()/2;
		for(int i=0;i<mBuffer.length;i++){
			mBuffer[i] = (byte) ((i%2==0)?128:0);
		}
	}
	
	public void start(){
		mThread = new Thread(this);
		mThread.start();
	}
	
	public void newAudioSamples(byte[] buffer, int length){
		mAudioStreamManager.encodeAudio(buffer, length);
	}
	
	@Override
	public void run() {
		mIsRunning = true;
		while(mIsRunning){
			if(mAudioStreamManager.getBufferedBytes() < mBytesToBufferize){
				//Il faut ajouter des données dans le buffer
				if(mAudioSource != null){
					mAudioSource.process();
				}
				if(mAudioStreamManager.getBufferedBytes() < mMinBytes){
					System.out.println("Plus rien à encoder");
					addWhiteNoise(mAudioStreamManager.getBytesPerSecond());
				}
				
			}
			else{
				try{
					Thread.sleep(200);
				}
				catch(Exception e){}
			}
		}
	}
	
	public long getBytesDelay(){
		return mBytesToBufferize-mAudioStreamManager.getBufferedBytes();
	}
	
	public long getBytesPerSecond(){
		return mAudioStreamManager.getBytesPerSecond();
	}
	
	public void addWhiteNoise(long length){
		int data = 0;
		while(data < length){
			newAudioSamples(mBuffer, mBuffer.length);
			data += mBuffer.length;
		}
	}
	public synchronized void addAudioSource(AudioSource audioSource){
		audioSource.setAudioSourceManager(this);
		mAudioSources.add(audioSource);
		System.out.println("Changement de source "+audioSource.getClass().getName());
		mAudioSource = audioSource;
	}
	
	public synchronized void removeAudioSource(AudioSource audioSource){
		mAudioSources.remove(audioSource);
		//Si on supprime la source actuellement utilisée, il faut la remplacer
		if(mAudioSource == audioSource){
			mAudioSource = null;
			if(!mAudioSources.isEmpty()){
				mAudioSource = mAudioSources.get(0);
				System.out.println("Changement de source "+audioSource.getClass().getName());
			}
		}
	}

}
