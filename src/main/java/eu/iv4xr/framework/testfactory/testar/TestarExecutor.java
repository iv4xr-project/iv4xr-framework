package eu.iv4xr.framework.testfactory.testar;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.fruit.monkey.ConfigException;
import org.fruit.monkey.Settings;

public class TestarExecutor {

	public static void main(String[] args) throws IOException, ConfigException {
		// TESTAR uses the windows.dll to use Windows native methods
		FileUtils.copyFileToDirectory(new File("resources//windows10//windows.dll"), new File("."));

		// TODO: The existence of the steam_appid.txt file allows the execution of the OpenCover tool. 
		// But it is not invoking steam to launch SE with the iv4xr-plugin automatically

		// Prepare the path that contains the TESTAR protocol to be executed
		String settingsDir = "." + File.separator + "resources\\settings" + File.separator;

		// Lab Recruits protocol example
		//String protocolName = "labrecruits_commands_testar_agent_navmesh_explorer";

		// Space Engineers protocol example
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
