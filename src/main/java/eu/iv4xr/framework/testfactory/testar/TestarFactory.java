package eu.iv4xr.framework.testfactory.testar;

import static org.fruit.Util.compileProtocol;
import static org.fruit.monkey.ConfigTags.MyClassPath;
import static org.fruit.monkey.ConfigTags.ProtocolClass;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.fruit.UnProc;
import org.fruit.Util;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.windows.WinApiException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import es.upv.staq.testar.serialisation.LogSerialiser;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.testfactory.ITestFactory;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

public class TestarFactory implements ITestFactory {

	private UnProc<Settings> protocol;
	private Settings settings;
	private String settingsDir;
	private TestAgent testAgent;

	public TestarFactory(Settings settings, String settingsDir) {
		this.settings = settings;
		this.settingsDir = settingsDir;
		this.protocol = prepareTestarProtocol();
	}

	@Override
	public void attachAgent(TestAgent testAgent) {
		this.testAgent = testAgent;
	}

	@Override
	public TestAgent getAgent() {
		return testAgent;
	}

	@Override
	public void reset() {
		// Reset SUT + TESTAR Agent
	}

	@Override
	public GoalStructure execute(GoalStructure G, boolean reset) {
		// Execute TESTAR based on the desired settings + protocol
		protocol.run(settings);

		// TODO: If GoalStructure used = listening mode
		return null;
	}

	/**
	 * Start TESTAR protocol with the selected settings
	 * 
	 * This method get the specific protocol class of the selected settings to run TESTAR
	 * 
	 * @param settings
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public UnProc<Settings> prepareTestarProtocol() {

		compileProtocol(settingsDir, settings.get(ConfigTags.ProtocolClass), settings.get(ConfigTags.ProtocolCompileDirectory));			

		URLClassLoader loader = null;

		try {
			List<String> cp = new ArrayList<>(settings.get(MyClassPath));
			cp.add(settings.get(ConfigTags.ProtocolCompileDirectory));
			URL[] classPath = new URL[cp.size()];
			for (int i = 0; i < cp.size(); i++) {

				classPath[i] = new File(cp.get(i)).toURI().toURL();
			}

			loader = new URLClassLoader(classPath);

			String pc = settings.get(ProtocolClass);
			String protocolClass = pc.substring(pc.lastIndexOf('/')+1, pc.length());

			LogSerialiser.log("Trying to load TESTAR protocol in class '" + protocolClass + "' with class path '" + Util.toString(cp) + "'\n", LogSerialiser.LogLevel.Debug);

			LogSerialiser.log("TESTAR protocol loaded!\n", LogSerialiser.LogLevel.Debug);
			LogSerialiser.log("Starting TESTAR protocol ...\n", LogSerialiser.LogLevel.Debug);

			return (UnProc<Settings>) loader.loadClass(protocolClass).getConstructor().newInstance();
		} catch (Throwable t) {
			LogSerialiser.log("An unexpected error occurred: " + t + "\n", LogSerialiser.LogLevel.Critical);
			System.out.println("Main: Exception caught");
			t.printStackTrace();
			t.printStackTrace(LogSerialiser.getLogStream());
		}
		return null;
	}

}
