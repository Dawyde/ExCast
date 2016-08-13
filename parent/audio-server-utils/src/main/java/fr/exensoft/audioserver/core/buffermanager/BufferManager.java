package fr.exensoft.audioserver.core.buffermanager;

import java.util.HashMap;

import fr.exensoft.audioserver.core.buffers.BaseBuffer;
import fr.exensoft.audioserver.core.buffers.BaseBufferFactory;

public class BufferManager {
	private HashMap<Class<? extends BaseBuffer>, BaseBufferManager<? extends BaseBuffer>> mBufferManagers;
	
	public BufferManager(){
		mBufferManagers = new HashMap<>();
	}
	
	public <T extends BaseBuffer> BaseBufferManager<T> getBaseBufferManager(Class<T> classType){
		if(!mBufferManagers.containsKey(classType)){
			return null;
		}
		else{
			return (BaseBufferManager<T>) mBufferManagers.get(classType);
		}
	}
	
	public <T extends BaseBuffer> void registerBufferManager(Class<T> classType, BaseBufferFactory<T> factory){
		if(!mBufferManagers.containsKey(classType)){
			mBufferManagers.put(classType, new BaseBufferManager<T>(factory));
		}
	}
}
