package fr.exensoft.audioserver.codec;

import fr.exensoft.audioserver.codec.decoder.aac.AACStreamDecoderFactory;
import fr.exensoft.audioserver.codec.decoder.mp3.MP3StreamDecoderFactory;
import fr.exensoft.audioserver.codec.encoder.aac.AACStreamEncoderFactory;
import fr.exensoft.audioserver.codec.encoder.mp3.MP3StreamEncoderFactory;
import fr.exensoft.audioserver.codec.manager.CodecManager;
import fr.exensoft.audioserver.codec.manager.CodecPlugin;

public class XugglerCodecPlugin implements CodecPlugin {

	@Override
	public String getName() {
		return "Xuggler";
	}

	@Override
	public void registerCodecs(CodecManager manager) {
		//Décodeurs 
		manager.addDecoder(new AACStreamDecoderFactory());
		manager.addDecoder(new MP3StreamDecoderFactory());
		
		//Encodeurs
		manager.addEncoder(new AACStreamEncoderFactory());
		manager.addEncoder(new MP3StreamEncoderFactory());
	}

}
