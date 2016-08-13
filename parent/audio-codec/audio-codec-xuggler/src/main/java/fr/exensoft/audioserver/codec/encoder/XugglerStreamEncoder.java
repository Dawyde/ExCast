package fr.exensoft.audioserver.codec.encoder;

import java.util.List;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IAudioSamples.Format;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IStreamCoder.Direction;

import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public abstract class XugglerStreamEncoder extends StreamEncoder{

	protected IStreamCoder mAudioCoder = null;
	
	protected IPacket mOutputPacket;
	
	protected IAudioSamples mAudioSamples;
	
	protected ICodec.ID mCodec;
	
	protected int mNumSamples = 1024;
	
	protected int mOutputOffset = 0;
	
	protected int mOutputTotal = 0;

	protected int mOffset = 0;
	
	protected int mTotalLength;

	protected long mStartOffset = 0;
	
	protected long mEndOffset = 0;
	
	protected byte[] mBuffer = new byte[2048];
	
	public XugglerStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, ICodec.ID codec) {
		super(bufferManager, streamFormats);
		mCodec = codec;
	}
	
	
	protected void configure(){
		mAudioCoder.setChannels(mNumChannels);
		mAudioCoder.setSampleRate(mSampleRate);
		mAudioCoder.setBitRate(mBitrate*1000);
		mAudioCoder.setSampleFormat(Format.FMT_S16);
	}
	
	@Override
	public void init(){
		mAudioCoder = IStreamCoder.make(Direction.ENCODING, mCodec);
		
		configure();
		int r = mAudioCoder.open(null, null);
		if(r<0){

			System.exit(0);
		}
		mOutputPacket = IPacket.make(1024);
		mAudioSamples = IAudioSamples.make(mNumSamples, mNumChannels, Format.FMT_S16);
		mTotalLength = 4096;
	}


	@Override
	public void close() {
		mAudioCoder.close();
		mAudioSamples.delete();
	}
	
	@Override
	public void encodeAudioData(byte[] buffer, int length, long startOffset, long endOffset) {
		int l;
		int offset = 0;
		while(length > 0){
			l = Math.min(length, mTotalLength-mOffset);
			mAudioSamples.put(buffer, offset, mOffset, l);
			mOffset += l;
			length -= l;
			offset += l;
			mEndOffset += l;
			if(mOffset >= mTotalLength){
				mAudioSamples.setComplete(true, mNumSamples, mAudioCoder.getSampleRate(), mAudioCoder.getChannels(), Format.FMT_S16, Global.NO_PTS);
				encodeSamples(mAudioSamples, mNumSamples);
			}
		}
	}
	
	public int read(byte[] buffer, int offset, int length){
		int toRead = Math.min(length, mOutputTotal-mOutputOffset);
		if(toRead <= 0){
			return 0;
		}
		mOutputPacket.get(mOutputOffset, buffer, offset, toRead);
		mOutputOffset += toRead;
		return toRead;
	}
	
	protected void encodeSamples(IAudioSamples audioSamples, int numSamples){
		mOffset = 0;
		while(mOffset < numSamples){
			mOffset += mAudioCoder.encodeAudio(mOutputPacket, audioSamples, mOffset);
			if(mOutputPacket.isComplete()){
				mOutputOffset = 0;
				mOutputTotal = mOutputPacket.getSize();
				read(mBuffer, 0, mOutputTotal);
				newPacketReady(mBuffer, mOutputTotal, mStartOffset, mEndOffset);
				mStartOffset = mEndOffset;
			}
		}
		mOffset = 0;
	}

	public int available() {
		return mOutputTotal-mOutputOffset;
	}
}
