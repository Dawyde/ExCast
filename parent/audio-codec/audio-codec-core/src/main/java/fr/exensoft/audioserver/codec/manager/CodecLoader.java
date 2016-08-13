package fr.exensoft.audioserver.codec.manager;

import java.io.File;
import java.util.Set;

import fr.exensoft.audioserver.core.plugin.PluginLoader;

public class CodecLoader {
	
	private File mPluginPath;
	
	private CodecManager mCodecManager;
	
	public CodecLoader(CodecManager manager, String path){
		mCodecManager = manager;
		mPluginPath = new File(path);
	}
	
	public void load(){
		PluginLoader<CodecPlugin> loader = new PluginLoader<>(CodecPlugin.class);
		loader.setDirectory(mPluginPath);
		loader.useLocalClasses(true);

		Set<Class<? extends CodecPlugin>> pluginClasses = loader.findAll();
		for(Class<? extends CodecPlugin> pluginClass : pluginClasses){
			try {
				CodecPlugin plugin = pluginClass.newInstance();
				plugin.registerCodecs(mCodecManager);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
}
