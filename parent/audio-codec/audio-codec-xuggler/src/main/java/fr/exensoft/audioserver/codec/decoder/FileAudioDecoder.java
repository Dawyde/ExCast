package fr.exensoft.audioserver.codec.decoder;

import java.io.File;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IAudioSamples.Format;

public class FileAudioDecoder {
	
	private IContainer mContainer;
	
	private File mFile;
	
	private int mAudioStreamId = -1;
	
	private IStreamCoder mAudioCoder = null;
	
	private IPacket mPacket;
	
	private IAudioSamples mSamples;
	
	private int mPacketOffset = 0;
	
	private int mSampleOffset = 0;
	
	private int mSampleLength = 0;
	
	private boolean mIsEnded = false;
	
	public FileAudioDecoder(File file){
		mFile = file;
	}
	
	public void init(){
		mContainer = IContainer.make();

		// Open up the container
		if (mContainer.open(mFile.getAbsolutePath(), IContainer.Type.READ, null) < 0)
			throw new IllegalArgumentException("could not open file: " + mFile.getAbsolutePath());

		// query how many streams the call to open found
		int numStreams = mContainer.getNumStreams();
		for (int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = mContainer.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				mAudioStreamId = i;
				mAudioCoder = coder;
				break;
			}
		}
		if (mAudioStreamId == -1)
			throw new RuntimeException("could not find audio stream in container: " + mFile.getAbsolutePath());

		if (mAudioCoder.open(null, null) < 0)
			throw new RuntimeException("could not open audio decoder for container: " + mFile.getAbsolutePath());
		
		mPacket = IPacket.make();
		mAudioCoder.setSampleFormat(Format.FMT_S16);
		mSamples = IAudioSamples.make(1024, mAudioCoder.getChannels());
	}
	
	public void close(){
		mAudioCoder.close();
		mContainer.close();
	}
	
	public IStreamCoder getCoder(){
		return mAudioCoder;
	}
	
	public int readAudioData(byte[] buffer){
		int length = buffer.length;
		int offset = 0;
		int l = 0;
		
		while(length > 0 && mSamples != null){

			if(mSampleLength <= 0){
				nextSamples();
				if(mIsEnded){
					
					return offset;
				}
			}
			l = Math.min(length, mSampleLength);
			mSamples.get(mSampleOffset, buffer, offset, l);
			
			mSampleOffset += l;
			mSampleLength -= l;
			length -= l;
			offset += l;
			
		}
		return offset;
	}
	
	public IPacket nextPacket(){
		while (mContainer.readNextPacket(mPacket) >= 0) {
			if (mPacket.getStreamIndex() == mAudioStreamId){
				mPacketOffset = 0;
				return mPacket;
			}
		}
		mIsEnded = true;
		return null;
	}
	
	public boolean isEnded(){
		return mIsEnded;
	}
	
	public IAudioSamples nextSamples(){
		mSampleOffset = 0;
		do{
			while (mPacketOffset < mPacket.getSize()) {
				int bytesDecoded = mAudioCoder.decodeAudio(mSamples, mPacket, mPacketOffset);
				mPacketOffset += bytesDecoded;
				if(mSamples.isComplete()){
					mSampleLength = mSamples.getSize();
					return mSamples;
				}
			}
			nextPacket();
		} while(!mSamples.isComplete() && mPacket != null);
		return null;
	}
}
