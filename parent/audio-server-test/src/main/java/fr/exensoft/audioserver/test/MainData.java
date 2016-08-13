package fr.exensoft.audioserver.test;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import fr.exensoft.audioserver.codec.encoder.StreamEncoder;
import fr.exensoft.audioserver.codec.manager.CodecManager;
import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.router.requestroute.AudioStreamRequestRoute;
import fr.exensoft.audioserver.core.server.MainSocketServer;
import fr.exensoft.audioserver.core.source.FileAudioSource;
import fr.exensoft.audioserver.core.stream.AudioStream;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class MainData {
	public static void main(String[] args){
		
		AudioServer audioServer = new AudioServer();
		AudioStreamManager audioStreamManager = new AudioStreamManager(audioServer, "test");
		audioServer.setAudioStreamManager(audioStreamManager);
		audioServer.setCodecManager(new CodecManager("../../plugins/"));

		//StreamEncoder encoder = audioServer.getCodecManager().createEncoder(audioServer.getBufferManager(), Arrays.asList(StreamFormat.ICECAST), "libfdk-aac");
		StreamEncoder encoder = audioServer.getCodecManager().createEncoderFromCodec(audioServer.getBufferManager(), Arrays.asList(StreamFormat.ICECAST), "aac");
		AudioStream stream = new AudioStream(audioStreamManager, "test-aac", encoder);
		audioStreamManager.addAudioStream(stream);
		
		FileAudioSource source = new FileAudioSource();
		File folder = new File("../../resources");
		File[] files = folder.listFiles();
		int index = new Random().nextInt(files.length);
		for(int i=0;i<files.length;i++){
			source.addAudioFile(files[(index+i)%files.length]);
		}
		audioStreamManager.addAudioSource(source);

		audioServer.getRequestRouter().addRoute(new AudioStreamRequestRoute.Builder()
				.withUri("/test")
				.withMainProtocol(MainProtocol.HTTP)
				.withStreamFormat(StreamFormat.ICECAST)
				.withAudioServer(audioServer)
				.withAudioStream(stream)
				.withAudioStreamManager(audioStreamManager)
				.build());
		
		MainSocketServer server = new MainSocketServer(audioServer, 80);
		
		audioStreamManager.start();
	}
}
