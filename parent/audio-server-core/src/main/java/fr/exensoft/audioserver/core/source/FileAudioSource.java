package fr.exensoft.audioserver.core.source;

import java.io.File;
import java.util.LinkedList;

import fr.exensoft.audioserver.codec.decoder.FileAudioDecoder;
import fr.exensoft.audioserver.core.AudioSourceManager;

public class FileAudioSource implements AudioSource{
	
	private FileAudioDecoder mFileDecoder = null;
	
	private LinkedList<File> mFiles;
	
	private byte[] mBuffer = new byte[1024];
	
	//Gestionnaire de flux audio source à qui on envoie nos données
	private AudioSourceManager mAudioSourceManager;
	
	
	
	public FileAudioSource() {
		mFiles = new LinkedList<>();
	}
	
	public void addAudioFile(File file){
		mFiles.add(file);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAudioSourceManager(AudioSourceManager audioSourceManager) {
		mAudioSourceManager = audioSourceManager;
	}
	
	@Override
	public void process() {
		//Il faut ajouter des données dans le buffer
		if(!sendAudioBytes()){
			System.out.println("Vide");
			for(int i=0;i<mBuffer.length;i++){
				mBuffer[i] = (byte) ((i%2==0)?128:0);
			}
			mAudioSourceManager.newAudioSamples(mBuffer, mBuffer.length);
		}
	}
	
	public boolean sendAudioBytes(){
		if(mFileDecoder != null && mFileDecoder.isEnded()){
			mFileDecoder.close();
			mFileDecoder = null;
		}
		
		if(mFileDecoder == null){
			if(mFiles.isEmpty()){
				return false;
			}
			File file = mFiles.poll();
			System.out.println("Lecture de "+file);
			mFileDecoder = new FileAudioDecoder(file);
			mFileDecoder.init();
		}

		int length = mFileDecoder.readAudioData(mBuffer);
		if(length <= 0){
			mFileDecoder.close();
			mFileDecoder = null;
			return false;
		}
		mAudioSourceManager.newAudioSamples(mBuffer, length);
		return true;

	}
}
