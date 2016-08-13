package fr.exensoft.audioserver.codec.decoder;

public interface AudioSampleListener {
	void newAudioSamples(byte[] buffer, int length);
}
