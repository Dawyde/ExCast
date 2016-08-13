package fr.exensoft.audioserver.codec.manager;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.exensoft.audioserver.codec.decoder.StreamDecoder;
import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class CodecManager {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CodecManager.class);
	
	private List<AbstractStreamEncoderFactory<?>> mEncoders;
	
	private List<AbstractStreamDecoderFactory> mDecoders;

	public CodecManager(String pluginDirectory){
		this();
		init(pluginDirectory);
	}
	
	public void init(String pluginDirectory){
		LOGGER.info("Chargements des plugins dans le dossier \"{}\"", pluginDirectory);
		CodecLoader codecLoader = new CodecLoader(this, pluginDirectory);
		codecLoader.load();
	}
	
	public CodecManager(){
		LOGGER.info("Initialisation du CodecManager");
		mEncoders = new LinkedList<>();
		mDecoders = new LinkedList<>();
	}
	//Fonctions d'ajout d'encodeur
	public void addEncoder(AbstractStreamEncoderFactory<?> encoder){
		LOGGER.debug("Ajout de l'encodeur {}", encoder.getIdentifier());
		mEncoders.add(encoder);
	}
	
	public void addDecoder(AbstractStreamDecoderFactory decoder){
		LOGGER.debug("Ajout du décodeur {}", decoder.getIdentifier());
		mDecoders.add(decoder);
	}
	
	private String getCodecFromMimeType(String mimetype){
		if(mimetype.equals("audio/aac")
				|| mimetype.equals("audio/aacp")
			) return "aac";
		if(mimetype.equals("audio/mpeg")
			) return "mp3";
		return null;
	}
	
	//Fonction pour créer des encodeurs
	public StreamEncoder createEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, String identifier){
		AbstractStreamEncoderFactory<?> factory = mEncoders.stream()
				.filter(f->f.getIdentifier().equalsIgnoreCase(identifier))
				.findFirst()
				.orElse(null);
		if(factory == null){
			return null;
		}
		
		return factory.createStreamEncoder(bufferManager, streamFormats);
	}
	
	public StreamEncoder createEncoderFromCodec(BufferManager bufferManager, List<StreamFormat> streamFormats, String codec){
		AbstractStreamEncoderFactory<?> factory = mEncoders.stream()
				.filter(f->f.getCodec().equalsIgnoreCase(codec))
				.findFirst()
				.orElse(null);
		if(factory == null){
			return null;
		}
		
		return factory.createStreamEncoder(bufferManager, streamFormats);
	}
	
	public StreamEncoder createEncoderFromMimeType(BufferManager bufferManager, List<StreamFormat> streamFormats, String mimetype){
		String codec = getCodecFromMimeType(mimetype);
		if(codec == null){
			return null;
		}
		
		return createEncoderFromCodec(bufferManager, streamFormats, codec);
	}
	
	//Fonction pour créer des décodeurs
	public StreamDecoder createDecoder(BufferManager bufferManager, String identifier){
		AbstractStreamDecoderFactory factory = mDecoders.stream()
				.filter(f->f.getIdentifier().equalsIgnoreCase(identifier))
				.findFirst()
				.orElse(null);
		if(factory == null){
			return null;
		}
		
		return factory.createDecoder(bufferManager);
	}
	
	public StreamDecoder createDecoderFromCodec(BufferManager bufferManager, String codec){
		AbstractStreamDecoderFactory factory = mDecoders.stream()
				.filter(f->f.getCodec().equalsIgnoreCase(codec))
				.findFirst()
				.orElse(null);
		if(factory == null){
			return null;
		}
		
		return factory.createDecoder(bufferManager);
	}
	
	public StreamDecoder createDecoderFromMimeType(BufferManager bufferManager, String mimetype){
		String codec = getCodecFromMimeType(mimetype);
		if(codec == null){
			return null;
		}
		
		return createDecoderFromCodec(bufferManager, codec);
	}
}
