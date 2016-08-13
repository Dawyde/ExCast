package fr.exensoft.audioserver.core.stream.format;


import fr.exensoft.audioserver.format.encoder.StreamFormat;

public abstract class AudioStreamFormat {
	
	public AudioStreamFormat(){
	}
	
	public abstract StreamFormat getFormat();

	public abstract void init();
	
	public abstract void close();
	
	public abstract void sendAudioSamples(long currentBytePosition);
}
