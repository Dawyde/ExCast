package fr.exensoft.audioserver.codec.decoder.aac;

import com.xuggle.xuggler.ICodec;

import fr.exensoft.audioserver.codec.decoder.XugglerStreamDecoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;

public class AACStreamDecoder extends XugglerStreamDecoder {

	private byte[] mFrameBuffer = new byte[4096];
	
	private byte[] mBuffer = new byte[8128];
	
	private int mOffset = 0;
	
	private int mFrameOffset = 0;
	
	private boolean mInResynchro = true;
	
	private int mRemaining = 0;

	public AACStreamDecoder(BufferManager bufferManager) {
		super(bufferManager, ICodec.ID.CODEC_ID_AAC);
	}
	
	@Override
	public void decodeAudioData(byte[] buffer, int offset, int length) {
		System.arraycopy(buffer, offset, mBuffer, mOffset, length);
		length += mOffset;
		int i = 0;
		while(i<length){
			if(mInResynchro){
				if((mBuffer[i]&255) == 255 && ((mBuffer[i+1]&255)>>4) == 15 && (mBuffer[i+2]&0xC3) == 0x40){
					mRemaining = ((mBuffer[i+3]&3)<<11) | ((mBuffer[i+4]&255)<<3) | ((mBuffer[i+5]&255)>>5);
					if(mRemaining <= 1500 && mRemaining > 5){
						mRemaining--;
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
			mBuffer[0] = mBuffer[length-5];
			mBuffer[1] = mBuffer[length-4];
			mBuffer[2] = mBuffer[length-3];
			mBuffer[3] = mBuffer[length-2];
			mBuffer[4] = mBuffer[length-1];
			mOffset = 5;
		}
		else{
			mOffset = 0;
		}
	}
	
}
