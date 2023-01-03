package eu.iv4xr.framework.testfactory.testar;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.fruit.monkey.ConfigException;
import org.fruit.monkey.Settings;

public class TestarExecutor {

	public static void main(String[] args) throws IOException, ConfigException {
		// TESTAR uses the windows.dll to use Windows native methods
		FileUtils.copyFileToDirectory(new File("assets/testar/windows10/windows.dll"), new File("."));

		// Prepare the path that contains the TESTAR protocol to be executed
		String settingsDir = "." + File.separator + "assets/testar/settings" + File.separator;

		// Lab Recruits protocol example
		//String protocolName = "labrecruits_commands_testar_agent_navmesh_explorer";

		// Space Engineers protocol example
		// https://github.com/iv4xr-project/TESTAR_iv4xr/releases/download/v3.5/SE_0.9.0_pdbs.zip
		String protocolName = "se_testar_navigate_survival";

		String testSettingsFileName = settingsDir + protocolName + File.separator + "test.settings";
		System.out.println("Test settings is <" + testSettingsFileName + ">");

		// Load the settings that indicate how to connect with the iv4XR SUT
		Settings settings = org.testar.iv4xr.TestarAgentLoader.loadSettings(args, testSettingsFileName);

		TestarFactory testar = new TestarFactory(settings, settingsDir);
		testar.execute(null, false);

		System.exit(0);
	}

}
