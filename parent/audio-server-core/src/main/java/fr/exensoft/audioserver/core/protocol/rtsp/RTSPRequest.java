package fr.exensoft.audioserver.core.protocol.rtsp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Objet contenant une requête RTSP
 */
public class RTSPRequest {
	/**
	 * Type de requête
	 */
	private String mType;
	/**
	 * Url de la requête
	 */
	private String mUrl;

	/**
	 * Entêtes contenus dans la requête
	 */
	private Map<String, String> mHeaders;
	
	/**
	 * Création de la requête RTSP
	 * 
	 * @param type
	 *            Type de requête
	 * @param url
	 *            Url de la requête
	 */
	public RTSPRequest(String type, String url) {
		mUrl = url;
		mType = type;
		mHeaders = new LinkedHashMap<String, String>();
	}

	
	/**
	 * Ajoute un couple clé/valeurs aux entêtes de la requête
	 * 
	 * @param key
	 *            Clé
	 * @param value
	 *            Valeur
	 */
	public void set(String key, String value) {
		mHeaders.put(key, value);
	}

	/**
	 * Récupère une valeur à partir de sa clé
	 * 
	 * @param key
	 *            Clé
	 * @return Valeur associée à la clé ou null si la clé n'existe pas
	 */
	public String get(String key) {
		return mHeaders.get(key);
	}

	/**
	 * Récupère une valeur numérique à partir de sa clé, retourne null si la
	 * valeur n'est pas une valeur numérique
	 * 
	 * @param key
	 *            Clé
	 * @return Valeur numérique associée à la clé ou null si la clé n'existe pas
	 *         ou si la valeur n'est pas un entier
	 */
	public Integer getInt(String key) {
		String value = get(key);
		if (value == null) return null;
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mType).append(" ").append(mUrl).append(" RTSP/1.0\r\n");
		for (Entry<String, String> e : mHeaders.entrySet()) {
			sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}

	/**
	 * Retourne le type de la requête
	 * 
	 * @return Type de la requête
	 */
	public String getType() {
		return mType;
	}

	/**
	 * Retourne l'URL de la requête
	 * 
	 * @return Url de la requête
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * Détermine l'URL de la requête
	 * 
	 * @param url
	 *            Url de la requête
	 */
	public void setUrl(String url) {
		mUrl = url;
	}

	/**
	 * Copie le contenu des entêtes de la requête passée en paramètre dans les
	 * entêtes de la requête actuelle
	 * 
	 * @param clientRequest
	 *            Requête contenant les entêtes à copier
	 */
	public void cloneData(RTSPRequest clientRequest) {
		for (Entry<String, String> e : clientRequest.mHeaders.entrySet()) {
			if (e.getKey().equalsIgnoreCase("cseq")) set("CSeq", e.getValue());
			else set(e.getKey(), e.getValue());
		}
	}
}