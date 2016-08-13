package fr.exensoft.audioserver.codec.decoder.mp3;

import com.xuggle.xuggler.ICodec;

import fr.exensoft.audioserver.codec.decoder.XugglerStreamDecoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.buffers.BaseBuffer;

public class MP3StreamDecoder extends XugglerStreamDecoder {

	
	private static int[] sBitrateTable = new int[]{
	  -1, 32, 40, 48, 56, 64, 80, 96,112,128,160,192,224,256,320, -1
	};
	private static int[] sSamplerateTable = new int[]{
	  44100,  48000, 32000,  -1
	};
	
	private class MP3Frame implements BaseBuffer{
		private int mFrameDataLength;
		//private int mMode;
		
		
		private MP3Frame(){
		}
		
		private boolean readHeader(byte[] data, int offset){
			if((data[offset]&255) != 255){
				return false;
			}
			
			int header = ((data[offset]&255)<<24)|((data[offset+1]&255)<<16)|((data[offset+2]&255)<<8)|(data[offset+3]&255);
			
			if(((header>>20)&0xFFF)!=0xFFF) {
				return false;
			}
			
			if((header & (1<<20)) == 0) {
				return false;
			}
			
			int layer = ((header >> 17) & 3);
			if(layer != 1){
				return false;
			}
			
			//int protection = ((header >> 16) & 1) ^ 1;
			int bitrate_index = (header >> 12) & 0xf;
			int samplerfindex = (header >> 10) & 3;
			//mMode = (header >> 6) & 3;
			
			int bitrate    = sBitrateTable[bitrate_index];
			int samplerate = sSamplerateTable[samplerfindex];
			//int si_size    = (mMode != 3) ? 32 : 17;
			int padding = (header >> 9) & 1;

			int frame_size = 144000 * bitrate;
			frame_size /= samplerate;
			frame_size += padding;
			//mFrameDataLength = frame_size - 4 - si_size;
			mFrameDataLength = frame_size;
			return true;
		}
	}
	private byte[] mFrameBuffer = new byte[4096];
	
	private byte[] mBuffer = new byte[8128];
	
	private MP3Frame mMP3Frame = new MP3Frame();
	
	private int mOffset = 0;
	
	private int mFrameOffset = 0;
	
	private boolean mInResynchro = true;
	
	private int mRemaining = 0;

	public MP3StreamDecoder(BufferManager bufferManager) {
		super(bufferManager, ICodec.ID.CODEC_ID_MP3);
	}
	
	@Override
	public void decodeAudioData(byte[] buffer, int offset, int length) {
		System.arraycopy(buffer, offset, mBuffer, mOffset, length);
		length += mOffset;
		int i = 0;
		while(i<length){
			if(mInResynchro){
				if(mMP3Frame.readHeader(mBuffer, i)){
					mRemaining = mMP3Frame.mFrameDataLength-1;
					if(mRemaining <= 1500 && mRemaining > 5){
						mFrameOffset = 1;
						mFrameBuffer[0] = mBuffer[i];
						mInResynchro = false;
					}
					else{
						System.out.println("Resynchro foireuse "+mRemaining);
					}
				}
			}
			else{
				//On continue à récupérer notre frame
				mFrameBuffer[mFrameOffset] = mBuffer[i];
				mFrameOffset++;
				mRemaining--;
				if(mRemaining <= 0){
					super.decodeAudioData(mFrameBuffer, 0, mFrameOffset);
					mFrameOffset = 0;
					mInResynchro = true;
				}
			}
			
			if(mInResynchro && i>=length-3){
				break;
			}
			i++;
		}
		
		if(mInResynchro){
			mBuffer[0] = mBuffer[length-3];
			mBuffer[1] = mBuffer[length-2];
			mBuffer[2] = mBuffer[length-1];
			mOffset = 3;
		}
		else{
			mOffset = 0;
		}
	}
	
}
