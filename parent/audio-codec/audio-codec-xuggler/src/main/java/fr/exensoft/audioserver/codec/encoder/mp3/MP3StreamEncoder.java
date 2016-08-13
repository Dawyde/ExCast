package fr.exensoft.audioserver.codec.encoder.mp3;

import java.util.List;

import com.xuggle.xuggler.ICodec;

import fr.exensoft.audioserver.codec.XugglerProfile;
import fr.exensoft.audioserver.codec.encoder.XugglerStreamEncoder;
import fr.exensoft.audioserver.codec.format.IcecastEncoder;
import fr.exensoft.audioserver.codec.format.mp3.Mp3AduRtpEncoder;
import fr.exensoft.audioserver.codec.format.mp3.Mp3RtpEncoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class MP3StreamEncoder extends XugglerStreamEncoder{
	private XugglerProfile mProfile;

	public MP3StreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, XugglerProfile profile) {
		super(bufferManager, streamFormats, ICodec.ID.CODEC_ID_MP3);
		mProfile = profile;
		
		mNumChannels = mProfile.getChannels();
		mSampleRate = mProfile.getSampleRate();
		mBitrate = mProfile.getBitrate();
	}

	@Override
	protected void createStreamFormatEncoders(List<StreamFormat> streamFormats) {
		for(StreamFormat streamFormat : streamFormats){
			if(streamFormat == StreamFormat.INTERLEAVED_RTP){
				addStreamFormatEncoder(new Mp3RtpEncoder());
			}
			else if(streamFormat == StreamFormat.RTP){
				addStreamFormatEncoder(new Mp3AduRtpEncoder(mBufferManager));
			}
			else if(streamFormat == StreamFormat.ICECAST){
				addStreamFormatEncoder(new IcecastEncoder("audio/mpeg"));
			}
		}
	}
}
