package fr.exensoft.audioserver.codec.manager;

public interface CodecPlugin {
	
	String getName();
	
	abstract void registerCodecs(CodecManager manager);
	
}
