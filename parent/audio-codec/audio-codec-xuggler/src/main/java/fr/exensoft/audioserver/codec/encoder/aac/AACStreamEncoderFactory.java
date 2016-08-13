package fr.exensoft.audioserver.codec.encoder.aac;

import java.util.List;
import java.util.Map;

import fr.exensoft.audioserver.codec.XugglerProfile;
import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.codec.manager.AbstractStreamEncoderFactory;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class AACStreamEncoderFactory extends AbstractStreamEncoderFactory<XugglerProfile> {

	public AACStreamEncoderFactory() {
		super("xuggler-aac", "aac", "audio/aac");
		
		XugglerProfile profile = new XugglerProfile("aac-128k");
		profile.setBitrate(128);
		profile.setChannels(2);
		profile.setSampleRate(44100);
		addProfile(profile);
		
		
	}

	@Override
	public StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, XugglerProfile profile) {
		return new AACStreamEncoder(bufferManager, streamFormats, profile);
	}

	@Override
	public StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, Map<String,String> configuration) {
		XugglerProfile profile = getProfileByIdentifier(configuration.get("profile"));
		if(profile == null){
			profile = new XugglerProfile("");
		}
		else{
			profile = new XugglerProfile(profile);
		}
		
		return createStreamEncoder(bufferManager, streamFormats, profile);
	}

}
