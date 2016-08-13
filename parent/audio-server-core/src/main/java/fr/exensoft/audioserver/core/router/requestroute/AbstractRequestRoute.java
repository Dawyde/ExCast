package fr.exensoft.audioserver.core.router.requestroute;

import java.util.List;

import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.protocol.RequestContext;
import fr.exensoft.audioserver.core.router.RequestHandler;
import fr.exensoft.audioserver.core.router.RequestRoute;

public abstract class AbstractRequestRoute implements RequestRoute{
	
	/**
	 * Protocol de la requete
	 */
	protected MainProtocol mMainProtocol;
	
	/**
	 * On peut restreindre la requête à certains ports uniquement
	 */
	protected List<Integer> mPorts = null;
	
	/**
	 * L'URI que l'on écoute
	 */
	protected String mUri;
	
	public AbstractRequestRoute(String uri, MainProtocol mainProtocol, List<Integer> ports) {
		mUri = uri;
		mMainProtocol = mainProtocol;
		mPorts = ports;
	}

	@Override
	public RequestHandler tryCreateRequestHandler(MainClient client, RequestContext context) {
		
		//Vérification du protocole
		if(context.getProtocol() != mMainProtocol) {
			return null;
		}
		
		//Vérification du port
		if(mPorts != null && !mPorts.contains(context.getPort())) {
			return null;
		}
		
		//Vérification de l'uri
		String uri = context.getUri();
		if(mMainProtocol == MainProtocol.RTSP){
			//Si le protocol est RTSP, il faut transformer l'url en uri
			String[] tmp = uri.split("/", 4);
			if(tmp.length == 4) {
				uri = "/"+tmp[3];
			}
		}
		if(!uri.equals(mUri)) {
			return null;
		}
		return createRequestHandler(client, context);
	}
	
	protected abstract RequestHandler createRequestHandler(MainClient client, RequestContext context);

}
