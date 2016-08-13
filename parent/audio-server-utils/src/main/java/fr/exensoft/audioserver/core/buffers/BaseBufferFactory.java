package fr.exensoft.audioserver.core.buffers;

public interface BaseBufferFactory<T extends BaseBuffer>{
	T create();
}
