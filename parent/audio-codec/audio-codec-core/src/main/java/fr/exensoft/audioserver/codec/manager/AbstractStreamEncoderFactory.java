package fr.exensoft.audioserver.codec.manager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public abstract class AbstractStreamEncoderFactory<T extends AbstractCodecProfile> extends AbstractCodecFactory{
	
	public AbstractStreamEncoderFactory(String identifier, String codec, String mimeType) {
		super(identifier, codec, mimeType);
	}

	protected List<T> mCodecProfiles = new LinkedList<>(); 
	
	protected T mDefaultProfile = null;
	
	public void addProfile(T profile){
		mCodecProfiles.add(profile);
		if(mDefaultProfile == null){
			mDefaultProfile = profile;
		}
	}
	
	protected T getProfileByIdentifier(String id) {
		if(id == null){
			return null;
		}
		return mCodecProfiles.stream()
				.filter(p->p.getName().equalsIgnoreCase(id))
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * Création du StreamEncoder par défaut
	 * @return StreamEncoder par défaut
	 */
	public StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats){
		return createStreamEncoder(bufferManager, streamFormats, mDefaultProfile);
	}
	
	/**
	 * Création du StreamEncoder associé au profil défini en paramètre
	 * @param profile Profile de StreamEncoder à créer
	 * @return StreamEncoder correspondant au profile
	 */
	public abstract StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, T profile);
	
	/**
	 * Crée un encoder selon la configuration données en paramètre
	 * @param configuration
	 * @return
	 */
	public abstract StreamEncoder createStreamEncoder(BufferManager bufferManager, List<StreamFormat> streamFormats, Map<String, String> configuration);
}
