package eu.iv4xr.framework.testfactory.testar;

import static org.fruit.monkey.ConfigTags.AbstractStateAttributes;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.fruit.Environment;
import org.fruit.UnknownEnvironment;
import org.fruit.alayer.Tag;
import org.fruit.alayer.windows.Windows10;
import org.fruit.monkey.ConfigException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.NativeLinker;
import es.upv.staq.testar.OperatingSystems;
import es.upv.staq.testar.StateManagementTags;

public class TestarExecutor {

	public static void main(String[] args) throws IOException, ConfigException {
		// TESTAR uses the windows.dll to use Windows native methods
		FileUtils.copyFileToDirectory(new File("resources//windows10//windows.dll"), new File("."));

		// Prepare the path that contains the TESTAR protocol to be executed
		String settingsDir = "." + File.separator + "resources\\settings" + File.separator;
		String protocolName = "labrecruits_commands_testar_agent_navmesh_explorer";
		String testSettingsFileName = settingsDir + protocolName + File.separator + "test.settings";
		System.out.println("Test settings is <" + testSettingsFileName + ">");

		Settings settings = org.fruit.monkey.Main.loadSettings(args, testSettingsFileName);

		//setTestarDirectory(settings);

		initCodingManager(settings);

		initOperatingSystem();

		TestarFactory testar = new TestarFactory(settings, settingsDir);
		testar.execute(null, false);

		System.exit(0);
	}

	/**
	 * This method initializes the coding manager with custom tags to use for constructing
	 * concrete and abstract state ids, if provided of course.
	 * @param settings
	 */
	private static void initCodingManager(Settings settings) {
		// we look if there are user-provided custom state tags in the settings
		// if so, we provide these to the coding manager

		Set<Tag<?>> stateManagementTags = StateManagementTags.getAllTags();
		// for the concrete state tags we use all the state management tags that are available
		if (!stateManagementTags.isEmpty()) {
			CodingManager.setCustomTagsForConcreteId(stateManagementTags.toArray(new Tag<?>[0]));
		}

		// then the attributes for the abstract state id
		if (!settings.get(ConfigTags.AbstractStateAttributes).isEmpty()) {
			Tag<?>[] abstractTags = settings.get(AbstractStateAttributes).stream().map(StateManagementTags::getTagFromSettingsString).filter(Objects::nonNull).toArray(Tag<?>[]::new);
			CodingManager.setCustomTagsForAbstractId(abstractTags);
		}
	}

	/**
	 * Set the concrete implementation of IEnvironment based on the Operating system on which the application is running.
	 */
	private static void initOperatingSystem() {
		if (NativeLinker.getPLATFORM_OS().contains(OperatingSystems.WINDOWS_10)) {
			Environment.setInstance(new Windows10());
		} else {
			System.out.printf("WARNING: Current OS %s has no concrete environment implementation, using default environment\n", NativeLinker.getPLATFORM_OS());
			Environment.setInstance(new UnknownEnvironment());
		}
	}

}
