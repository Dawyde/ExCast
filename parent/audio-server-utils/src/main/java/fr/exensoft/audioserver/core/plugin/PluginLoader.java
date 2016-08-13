package fr.exensoft.audioserver.core.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.reflections.Reflections;

public class PluginLoader<T> {

	private Class<T> mClassPlugin;

	private File mDirectory = null;
	
	private boolean mUseLocalClasses = false;
	
	private Set<Class<? extends T>> mClasses;

	public PluginLoader(Class<T> classPlugin) {
		mClassPlugin = classPlugin;
	}

	public void setDirectory(File directory) {
		mDirectory = directory;
	}
	
	public void useLocalClasses(boolean useLocalClasses){
		mUseLocalClasses = useLocalClasses;
	}

	public Set<Class<? extends T>> findAll() {
		mClasses = new HashSet<>();
		if(mUseLocalClasses){
			findLocalClasses();
		}
		if(mDirectory != null && mDirectory.isDirectory()){
			findJars();
		}
		
		return mClasses;
	}

	public void findLocalClasses() {
		Reflections reflections = new Reflections("fr.exensoft");
		Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(mClassPlugin);
		mClasses.addAll(subTypes);
	}

	public void findJars() {
		File[] files = mDirectory.listFiles(f -> f.getName().endsWith(".jar"));
		for (int i = 0; i < files.length; i++) {
			loadJarFile(files[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadJarFile(File file) {
		// On va regarder le contenu
		JarInputStream jarfile;
		try {
			// On ouvre le fichier JAR
			jarfile = new JarInputStream(file.toURI().toURL().openStream());
		} catch (Exception e) {
			return;
		}
		JarEntry entry;
		URLClassLoader url_loader = null;
		try {
			// On liste son contenu, on cherche des .class
			while ((entry = jarfile.getNextJarEntry()) != null) {
				if (entry.getName().endsWith(".class")) {
					// Si on a bien un .class on va regarder si sa superclass
					// est bien celle qu'on veut
					if (url_loader == null) {
						url_loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, PluginLoader.class.getClassLoader());
					}

					// On forme le nom de classe comme il faut
					String name = entry.getName().substring(0, entry.getName().length() - 6).replace("/", ".");
					try {
						// On vérifie que c'est bien une classe héritant de
						// AbstractProgram
						Class<?> c = Class.forName(name, true, url_loader);
						if (isValidClass(c)) mClasses.add((Class<? extends T>) c);
					} catch (NoClassDefFoundError e) {
						e.printStackTrace();
					}
				}
			}
			jarfile.close();
		} catch (Exception e) {
		}
	}

	public boolean isValidClass(Class<?> cl) {
		// Ne devrait pas arriver
		if (cl == null)
			return false;
		
		
		for(Class<?> i : cl.getInterfaces()){
			if(i == mClassPlugin) return true;
		}
		
		return false;

	}
}
