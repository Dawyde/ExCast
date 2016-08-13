package fr.exensoft.audioserver.codec.manager;

import fr.exensoft.audioserver.codec.decoder.StreamDecoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;

public abstract class AbstractStreamDecoderFactory extends AbstractCodecFactory {

	public AbstractStreamDecoderFactory(String identifier, String codec, String mimeType) {
		super(identifier, codec, mimeType);
	}

	public abstract StreamDecoder createDecoder(BufferManager bufferManager);
}
