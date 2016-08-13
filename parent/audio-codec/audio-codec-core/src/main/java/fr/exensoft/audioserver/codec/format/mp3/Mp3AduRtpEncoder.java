package fr.exensoft.audioserver.codec.format.mp3;

import java.util.LinkedList;

import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.buffers.BaseBuffer;
import fr.exensoft.audioserver.core.buffers.BaseBufferFactory;
import fr.exensoft.audioserver.format.encoder.RTPFormatEncoder;

public class Mp3AduRtpEncoder extends RTPFormatEncoder{
	
	private static int[] sBitrateTable = new int[]{
	  -1, 32, 40, 48, 56, 64, 80, 96,112,128,160,192,224,256,320, -1
	};
	private static int[] sSamplerateTable = new int[]{
	  44100,  48000, 32000,  -1
	};
	
	public static int MAX_DATA_SIZE = 1400;

	private class MP3FrameFactory implements BaseBufferFactory<MP3Frame>{
		@Override
		public MP3Frame create() {
			return new MP3Frame(2048);
		}
		
	}
	
	private class MP3Frame implements BaseBuffer{
		private byte[] mData;
		private int mBackPointer;
		private int mFrameDataLength;
		private int mHeaderLength = 0;
		private boolean mHasCrc = false;
		private int mAduSize;
		private int mMode;

		
		private MP3Frame(int length){
			mData = new byte[length];
		}
		
		private void init(byte[] data, int length){
			System.arraycopy(data, 0, mData, 0, length);
			readHeader();
			readSideInfo();
		}
		
		private void readHeader(){
			int header = ((mData[0]&255)<<24)|((mData[1]&255)<<16)|((mData[2]&255)<<8)|(mData[3]&255);
			
			if(((header>>20)&0xFFF)!=0xFFF) {
				System.err.println("Erreur");
			}
			
			if((header & (1<<20)) == 0) {
				System.err.println("Erreur c'est pas du MP3");
			}
			
			int layer = ((header >> 17) & 3);
			if(layer != 1){
				System.err.println("Erreur c'est pas du layer 3");
			}
			
			int protection = ((header >> 16) & 1) ^ 1;
			int bitrate_index = (header >> 12) & 0xf;
			int samplerfindex = (header >> 10) & 3;
			mMode = (header >> 6) & 3;
			
			int bitrate    = sBitrateTable[bitrate_index];
			int samplerate = sSamplerateTable[samplerfindex];
			int si_size    = (mMode != 3) ? 32 : 17;
			int padding = (header >> 9) & 1;

			int frame_size = 144000 * bitrate;
			frame_size /= samplerate;
			frame_size += padding;
			mFrameDataLength = frame_size - 4 - si_size;
			
			int offset = 0;
			mHasCrc = (protection == 1);
			
			if(mHasCrc) {
				mFrameDataLength -= 2;
			}
			mHeaderLength = si_size+4+offset;
		}
		
		private void readSideInfo(){
			int offset = 0;
			if(mHasCrc) {
				offset = 2;
			}
			BitStreamReader reader = new BitStreamReader(mData, 4+offset);
			//mBackPointer = ((mData[4+offset]&255)<<1)|((mData[5+offset]&255)>>7);
			mBackPointer = reader.readBits(9);
			int nch = (mMode != 3)?2:1;
			
			//private_bits
			reader.skip(nch==2?3:5);
			
			//bands
			//Boucle sur le nommbre de canaux => boucle sur 4 valeurs, on skip nch*4 bits
			reader.skip(4*nch);
			int length = 0;
			for(int gri=0;gri<2;gri++){
				for(int i=0;i<nch;i++){
					int part2_3_length = reader.readBits(12);
					length += part2_3_length;
					//bigvalue
					reader.skip(9);
					
					//global gain
					reader.skip(8);
					
					//scale comp
					reader.skip(4);
					
					//blocksplit flag
					if(reader.readBit()!=0){
						//blocktype
						reader.skip(2);
						//switchpoint
						reader.skip(1);
						//tbl_sel
						reader.skip(10);
						
						//subgain
						reader.skip(9);
					}
					else{
						//tbl_set x3
						reader.skip(15);
						
						//reg0_cnt
						reader.skip(4);
						
						//reg1_cnt
						reader.skip(3);
					}
					//Preflag
					reader.skip(1);
					
					//scale_scale
					reader.skip(1);
					
					//cnt1tlb_sel
					reader.skip(1);
					
					
				}
			}
			mAduSize = (length+7)/8;
		}
	}
	
	private LinkedList<MP3Frame> mFrames = new LinkedList<MP3Frame>();
	
	private byte[] mBuffer = new byte[2048];
	
	private int mOutputLength = 0;

	private int mOutputIndex = 2;
	
	private int mOutputOffset = 0;
	
	private boolean mOutputIsContinuation = false;
	
	private long mStartOffset = 0;
	
	private long mEndOffset = 0;
	
	private int mTotalDataBefore = 0;
	private int mTotalDataAfter = 0;
	
	private BaseBufferManager<MP3Frame> mMP3FrameBuffer;

	public Mp3AduRtpEncoder(BufferManager bufferManager) {
		bufferManager.registerBufferManager(MP3Frame.class, new MP3FrameFactory());
		mMP3FrameBuffer = bufferManager.getBaseBufferManager(MP3Frame.class);
	}

	public void writeFrame(byte[] buffer, int offset, int length, int totalSize){
		int remaining = MAX_DATA_SIZE-mOutputLength;
		
		while(length > 0){
			int l = Math.min(remaining, length);
			System.arraycopy(buffer, offset, mBuffer, mOutputIndex, l);
			length -= l;
			mOutputIndex += l;
			mOutputLength += l;
			remaining -= l;
			if(remaining <= 0){
				frameReady(totalSize);
				remaining = MAX_DATA_SIZE;
			}
		}
		
		
		
	}
	
	public void endFrame(int totalSize){
		if(mOutputLength > 0){
			frameReady(totalSize);
		}
		mOutputIsContinuation = false;
	}
	
	private void frameReady(int totalSize){
		if(totalSize > 63){
			mBuffer[0] = (byte) ((mOutputIsContinuation?128:0) | 64 | ((totalSize>>8)&63));
			mBuffer[1] = (byte) (totalSize & 255);
			mOutputOffset = 0;
			mOutputLength += 2;
		}
		else{
			mBuffer[1] = (byte) ((mOutputIsContinuation?128:0) | ((totalSize>>8)&63));
			mOutputOffset = 1;
			mOutputLength += 1;
		}
		newPacketReady(mBuffer, mOutputLength, mStartOffset, mEndOffset);
		mStartOffset = mEndOffset;
		mOutputIsContinuation = true;
		mOutputIndex = 2;
		mOutputLength = 0;
		
	}
	
	public int read(byte[] buffer, int offset, int length){
		System.arraycopy(mBuffer, mOutputOffset, buffer, offset, mOutputLength);
		return mOutputLength;
	}
	

	@Override
	public void init() {
		
	}
	
	@Override
	public void encodeAudioData(byte[] buffer, int length, long startOffset, long endOffset) {
		mEndOffset = endOffset;
		addMP3Frame(buffer, length);
	}
	
	public void addMP3Frame(byte[] data, int length){
		MP3Frame frame = mMP3FrameBuffer.getPacket(); 
		frame.init(data, length);
		addFrame(frame);
		makePacket(frame);
	}
	
	public void addFrame(MP3Frame frame){
		mTotalDataBefore = mTotalDataAfter;
		mTotalDataAfter+=frame.mFrameDataLength;
		mFrames.add(frame);
	}
	
	public void dequeue(){
		MP3Frame frame = mFrames.removeFirst();
		mMP3FrameBuffer.releasePacket(frame);
		mTotalDataAfter -= frame.mFrameDataLength;
		mTotalDataBefore -= frame.mFrameDataLength;
	}
	
	private void makePacket(MP3Frame frame){
		if(mTotalDataAfter < frame.mAduSize || mTotalDataBefore < frame.mBackPointer) return;
		if(frame.mAduSize> (frame.mBackPointer+frame.mFrameDataLength)) return;
		int endIndex = mFrames.size()-2;
		int firstIndex = -1;
		int offset = 0;
		int remaining = frame.mBackPointer;
		for(int i=endIndex;i>=0;i--){
			MP3Frame f = mFrames.get(i);
			if(f.mFrameDataLength < remaining){
				remaining -= f.mFrameDataLength;
			}
			else{
				offset = (f.mFrameDataLength-remaining);
				firstIndex = i;
				break;
			}
		}
		
		if(remaining == 0 && firstIndex == -1){
			firstIndex = mFrames.size()-1;
		}
		if(firstIndex >= 0){
			int totalSize = frame.mHeaderLength+frame.mAduSize;
			writeFrame(frame.mData, 0, frame.mHeaderLength, totalSize);
			MP3Frame f;
			int length;
			remaining = frame.mAduSize;
			for(int i=firstIndex;i<mFrames.size();i++){
				f = mFrames.get(i);
				length = f.mFrameDataLength-offset;
				writeFrame(f.mData, f.mHeaderLength+offset, Math.min(remaining, length), totalSize);
				remaining -= length;
				if(remaining < 0) break;
				offset = 0;
			}
			for(int i=0;i<firstIndex;i++){
				dequeue();
			}
			endFrame(totalSize);
		}
	}

	@Override
	public int getPayloadType() {
		return 96;
	}

	@Override
	public void close() {
		for(MP3Frame frame : mFrames){
			mMP3FrameBuffer.releasePacket(frame);
		}
		mFrames.clear();
	}

	@Override
	public String getDescribe(String output, String streamname) {
		StringBuilder sb = new StringBuilder();
		sb.append("v=0\r\n");
		sb.append("o=").append(output).append("\r\n");
		sb.append("s=").append(streamname).append("\r\n");
		sb.append("m=audio 8000 RTP/AVP 96\r\n");
		sb.append("a=rtpmap:96 mpa-robust/90000\r\n");
		return sb.toString();
	}

}
