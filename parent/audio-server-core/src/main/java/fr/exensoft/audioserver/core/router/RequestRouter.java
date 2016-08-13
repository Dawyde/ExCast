package fr.exensoft.audioserver.core.router;

import java.util.LinkedList;

import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.RequestContext;

public class RequestRouter {

	private LinkedList<RequestRoute> mRoutes;
	
	public RequestRouter(){
		mRoutes = new LinkedList<>();
	}
	
	public void addRoute(RequestRoute route){
		mRoutes.add(route);
	}
	
	public RequestHandler createRequestHandler(MainClient client, RequestContext requestContext) {
		RequestHandler retour = null;
		for(RequestRoute route : mRoutes){
			retour = route.tryCreateRequestHandler(client, requestContext);
			if(retour != null) break;
		}
		System.out.println("ICI "+retour);
		return retour;
	}
	
	
}
