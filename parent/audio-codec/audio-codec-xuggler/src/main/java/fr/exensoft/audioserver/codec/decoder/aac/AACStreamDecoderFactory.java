package fr.exensoft.audioserver.codec.decoder.aac;

import fr.exensoft.audioserver.codec.decoder.StreamDecoder;
import fr.exensoft.audioserver.codec.manager.AbstractStreamDecoderFactory;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;

public class AACStreamDecoderFactory extends AbstractStreamDecoderFactory {

	public AACStreamDecoderFactory() {
		super("xuggler-aac", "aac", "audio/aac");
	}

	@Override
	public StreamDecoder createDecoder(BufferManager bufferManager) {
		return new AACStreamDecoder(bufferManager);
	}

}
