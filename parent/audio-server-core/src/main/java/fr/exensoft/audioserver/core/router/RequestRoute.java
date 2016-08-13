package fr.exensoft.audioserver.core.router;

import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.RequestContext;

public interface RequestRoute {
	
	RequestHandler tryCreateRequestHandler(MainClient client, RequestContext context); 
	
}
