package fr.exensoft.audioserver;

import fr.exensoft.audioserver.codec.manager.CodecPlugin;
import fr.exensoft.audioserver.codec.manager.CodecManager;

public class LibFDKCodecPlugin implements CodecPlugin{


	@Override
	public void registerCodecs(CodecManager manager) {
		
		manager.addEncoder(new AACFDKEStreamEncoderFactory());
		
	}

	@Override
	public String getName() {
		return "LibFDK Wrapper";
	}

}
