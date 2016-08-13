package fr.exensoft.audioserver.codec.manager;

public abstract class AbstractCodecFactory {
	
	protected String mIdentifier;
	
	protected String mCodec;
	
	protected String mMimeType;
	
	public AbstractCodecFactory(String identifier, String codec, String mimeType){
		mIdentifier = identifier;
		mCodec = codec;
		mMimeType = mimeType;
	}
	
	public String getIdentifier(){
		return mIdentifier;
	}
	
	public String getCodec(){
		return mCodec;
	}
	
	public String getMimeType(){
		return mMimeType;
	}
	
	
}
