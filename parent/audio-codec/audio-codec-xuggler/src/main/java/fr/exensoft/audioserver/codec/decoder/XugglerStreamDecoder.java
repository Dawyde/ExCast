package fr.exensoft.audioserver.codec.decoder;

import java.nio.ByteBuffer;

import com.xuggle.ferry.IBuffer;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IAudioSamples.Format;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IStreamCoder.Direction;

import fr.exensoft.audioserver.core.buffermanager.BufferManager;

public class XugglerStreamDecoder extends StreamDecoder{
	
	public final static int BUFFER_LEN = 4096;

	private IStreamCoder mAudioCoder = null;
	
	private ICodec.ID mCodec;
	
	private ByteBuffer mByteBuffer;
	
	private IPacket mInputPacket;
	
	private IBuffer mIBuffer;
	
	private IAudioSamples mAudioSamples;
	
	private int mNumSamples = 1024;
	
	private int mNumChannels = 2;
	
	private int mBiterate = 128;
	
	private byte[] mBuffer = new byte[4096];
	
	public XugglerStreamDecoder(BufferManager bufferManager, ICodec.ID codec) {
		super(bufferManager);
		mCodec = codec;
	}

	@Override
	public void init(){
		mAudioCoder = IStreamCoder.make(Direction.DECODING, mCodec);
		mAudioCoder.setChannels(2);
		mAudioCoder.setSampleRate(44100);
		mAudioCoder.setBitRate(mBiterate*1000);
		mAudioCoder.setDefaultAudioFrameSize(2);
		mAudioCoder.setSampleFormat(Format.FMT_S16);
		//mAudioCoder.setFrameRate(IRational.make(0, 0));
		
		//mAudioCoder.setBitRate(320000);
		//mAudioCoder.set
		mAudioCoder.open(null, null);
		
		mByteBuffer = ByteBuffer.allocate(BUFFER_LEN);
		mIBuffer = null;
		mInputPacket = IPacket.make(BUFFER_LEN);
		
		mAudioSamples = IAudioSamples.make(mNumSamples, mNumChannels, Format.FMT_S16);
	}

	@Override
	public void setBitrate(int biterate){
		mBiterate = biterate;
	}
	
	@Override
	public void close() {
		mAudioCoder.close();
		mAudioSamples.delete();
		if(mIBuffer != null){
			mIBuffer.delete();
		}
		mInputPacket.delete();
	}
	
	@Override
	public void decodeAudioData(byte[] buffer, int offset, int length) {
		mByteBuffer.put(buffer, offset, length);
		decodeSamples(length);
	}
	
	private void decodeSamples(int length){
		//http://www.programcreek.com/java-api-examples/index.php?source_dir=myLib-master/myLib.old/myLib.xuggle.test/src/test/java/com/ttProject/xuggle/test/MakePictureFromRGB24Test.java
		if(mIBuffer != null) {
			mIBuffer.delete();
		}
		mIBuffer = IBuffer.make(null, mByteBuffer.array(), 0, length);
		mInputPacket.setData(mIBuffer);
		mInputPacket.setKeyPacket(true);
		mInputPacket.setTimeBase(IRational.make(1, 1000)); 
		mInputPacket.setComplete(true, length);
		mInputPacket.setFlags(0);
		
		
		int offset = 0;
		while(offset < length){
			int decodedBytes = mAudioCoder.decodeAudio(mAudioSamples, mInputPacket, offset);
			offset += decodedBytes;
			if(mAudioSamples.isComplete()){
				int remaining = mAudioSamples.getSize();
				int o = 0;
				int l;
				while(remaining > 0){
					l = (remaining>1024)?1024:remaining;
					mAudioSamples.get(o, mBuffer, 0, l);
					remaining -= l;
					o += l;
					newAudioSamples(mBuffer, l);
				}
			}
		}
		
		mByteBuffer.clear();
	}


}
