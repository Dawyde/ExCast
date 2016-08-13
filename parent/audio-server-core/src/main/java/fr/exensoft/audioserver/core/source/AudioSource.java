package fr.exensoft.audioserver.core.source;

import fr.exensoft.audioserver.core.AudioSourceManager;

public interface AudioSource {
	void process();
	
	void setAudioSourceManager(AudioSourceManager audioSourceManager);
	
	void close();
}
