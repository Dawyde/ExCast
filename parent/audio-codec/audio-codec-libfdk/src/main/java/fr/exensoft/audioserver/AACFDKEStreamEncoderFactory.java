package fr.exensoft.audioserver;

import java.util.List;
import java.util.Map;

import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.codec.libfdk.AACFDKStreamEncoder;
import fr.exensoft.audioserver.codec.manager.AbstractStreamEncoderFactory;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class AACFDKEStreamEncoderFactory extends AbstractStreamEncoderFactory<AACFDKStreamEncoderProfile>{

	public AACFDKEStreamEncoderFactory() {
		super("libfdk-aac", "aac", "audio/aac");
		
		//On va créer les différents profiles
		AACFDKStreamEncoderProfile profile = new AACFDKStreamEncoderProfile("he-aac-mp4-128k");
		profile.setAot(5);
		profile.setBitrate(128);
		profile.setSampleRate(44100);
		profile.setTransmux(2);
		profile.setChannelOrder(1);
		profile.setChannelMode(2);
		profile.setAfterBurner(true);
		addProfile(profile);
		
		
		
	}

	@Override
	public StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, AACFDKStreamEncoderProfile profile) {
		return new AACFDKStreamEncoder(bufferManager, streamFormats, profile);
	}

	@Override
	public StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, Map<String, String> configuration) {
		AACFDKStreamEncoderProfile profile = getProfileByIdentifier(configuration.get("profile"));
		if(profile == null){
			profile = new AACFDKStreamEncoderProfile("");
		}
		else{
			profile = new AACFDKStreamEncoderProfile(profile);
		}
		
		return createStreamEncoder(bufferManager, streamFormats, profile);
	}


}
