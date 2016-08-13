package fr.exensoft.audioserver.codec.encoder.mp3;

import java.util.List;
import java.util.Map;

import fr.exensoft.audioserver.codec.XugglerProfile;
import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.codec.manager.AbstractStreamEncoderFactory;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class MP3StreamEncoderFactory extends AbstractStreamEncoderFactory<XugglerProfile> {

	public MP3StreamEncoderFactory() {
		super("xuggler-mp3", "mp3", "audio/mpeg");
		
		XugglerProfile profile = new XugglerProfile("mp3-128k");
		profile.setBitrate(128);
		profile.setChannels(2);
		profile.setSampleRate(44100);
		addProfile(profile);
	}

	@Override
	public StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats,
			XugglerProfile profile) {
		return new MP3StreamEncoder(bufferManager, streamFormats, profile);
	}

	@Override
	public StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats,
			Map<String, String> configuration) {
		XugglerProfile profile = getProfileByIdentifier(configuration.get("profile"));
		if (profile == null) {
			profile = new XugglerProfile("");
		} else {
			profile = new XugglerProfile(profile);
		}

		return createStreamEncoder(bufferManager, streamFormats, profile);
	}

}
