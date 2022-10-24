package eu.iv4xr.framework.testfactory.testar;

import org.fruit.UnProc;
import org.fruit.monkey.Settings;

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
		this.protocol = new org.testar.iv4xr.TestarAgentLoader().prepareProtocol(settings, settingsDir);
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
	public GoalStructure nextGoal() {
		// TESTAR agent does not implement this method functionality
		return null;
	}

	@Override
	public GoalStructure execute(GoalStructure G, boolean reset) {
		// Execute TESTAR based on the desired settings + protocol
		protocol.run(settings);

		// TODO: If GoalStructure used = listening mode
		return null;
	}

}
