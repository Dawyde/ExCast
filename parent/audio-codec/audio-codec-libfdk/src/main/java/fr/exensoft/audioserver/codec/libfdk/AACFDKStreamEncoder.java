package fr.exensoft.audioserver.codec.libfdk;

import java.util.List;

import fr.exensoft.audioserver.AACFDKStreamEncoderProfile;
import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.codec.format.IcecastEncoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class AACFDKStreamEncoder extends StreamEncoder {

	private LibFDKWrapper mWrapper;

	private byte[] mInputBuffer = new byte[4096];
	private int mInputOffset = 0;
	private int mInputLength = 4096;
	private long mStartByte = 0;
	private long mEndByte = 0;
	
	private AACFDKStreamEncoderProfile mProfile;

	private byte[] mOutputBuffer = new byte[4096];
	public AACFDKStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, AACFDKStreamEncoderProfile streamEncoderProfile) {
		super(bufferManager, streamFormats);
		mProfile = streamEncoderProfile;
	}

	@Override
	public void encodeAudioData(byte[] buffer, int length, long startByte, long endByte) {
		int available = length;
		int encoded_length;
		int offset = 0;
		mEndByte = endByte;
		while(available > 0){
			//Si la taille à encoder est inférieur ou égale à la taille restante dans le buffer, on encode tout
			if(available <= mInputLength-mInputOffset){
				encoded_length = available;
			}
			else{//Sinon on va fractionner
				encoded_length = mInputLength-mInputOffset;
			}
			System.arraycopy(buffer, offset, mInputBuffer, mInputOffset, encoded_length);
			mInputOffset += encoded_length;
			offset += encoded_length;
			available -= encoded_length;
			if(mInputOffset >= mInputLength){
				mEndByte += offset;
				encodeSamples();
			}
		}
		
	}
	
	protected void encodeSamples(){
		//System.out.println("--> "+mInputOffset);
		int len = mWrapper.encodeAudio(mOutputBuffer, mInputBuffer, mInputOffset);
		if(len > 0){
			newPacketReady(mOutputBuffer, len, mStartByte, mEndByte);
			mStartByte = mEndByte;
		}
		mInputOffset = 0;
	}
	@Override
	public void init() {
		mWrapper = new LibFDKWrapper();
		try {
			mWrapper.open();
			
			mWrapper.setChannelMode(mProfile.getChannelMode());
			mWrapper.setSampleRate(mProfile.getSampleRate());
			mWrapper.setAOT(mProfile.getAot());
			mWrapper.setAfterBurner(mProfile.getAfterBurner());
			mWrapper.setChannelOrder(mProfile.getChannelOrder());
			mWrapper.setTransmux(mProfile.getTransmux());
			mWrapper.setBitrate(mProfile.getBitrate()*1000);

			mWrapper.init();
			
			mSampleRate = mWrapper.getSampleRate();
			mBitrate = mWrapper.getBitrate()/1000;
			mNumChannels = mWrapper.getChannelMode();
		} catch (NativeFDKException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public void close() {
		try {
			mWrapper.close();
		} catch (NativeFDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
