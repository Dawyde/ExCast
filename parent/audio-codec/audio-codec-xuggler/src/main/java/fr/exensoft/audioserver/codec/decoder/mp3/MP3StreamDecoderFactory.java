package fr.exensoft.audioserver.codec.decoder.mp3;

import fr.exensoft.audioserver.codec.decoder.StreamDecoder;
import fr.exensoft.audioserver.codec.manager.AbstractStreamDecoderFactory;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;

public class MP3StreamDecoderFactory extends AbstractStreamDecoderFactory {

	public MP3StreamDecoderFactory() {
		super("xuggler-mp3", "mp3", "audio/mp3");
	}

	@Override
	public StreamDecoder createDecoder(BufferManager bufferManager) {
		return new MP3StreamDecoder(bufferManager);
	}

}
