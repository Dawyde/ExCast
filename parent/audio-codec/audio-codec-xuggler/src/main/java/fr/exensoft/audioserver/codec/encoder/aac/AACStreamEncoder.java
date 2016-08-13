package fr.exensoft.audioserver.codec.encoder.aac;

import java.util.List;

import com.xuggle.xuggler.ICodec;

import fr.exensoft.audioserver.codec.XugglerProfile;
import fr.exensoft.audioserver.codec.encoder.XugglerStreamEncoder;
import fr.exensoft.audioserver.codec.format.IcecastEncoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class AACStreamEncoder extends XugglerStreamEncoder{

	private XugglerProfile mProfile;
	
	public AACStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, XugglerProfile profile) {
		super(bufferManager, streamFormats, ICodec.ID.CODEC_ID_AAC);
		mProfile = profile;
		
		mNumChannels = mProfile.getChannels();
		mSampleRate = mProfile.getSampleRate();
		mBitrate = mProfile.getBitrate();
	}
	
	@Override
	protected void createStreamFormatEncoders(List<StreamFormat> streamFormats) {
		for(StreamFormat streamFormat : streamFormats){
			if(streamFormat == StreamFormat.ICECAST){
				addStreamFormatEncoder(new IcecastEncoder("audio/aac"));
			}
		}
	}
}
