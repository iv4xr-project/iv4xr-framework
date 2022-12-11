/**
 * 
 */
package eu.iv4xr.framework.testfactory.rlbt;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.testfactory.ITestFactory;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

/**
 * @author kifetew
 *
 */
public class RlbtTestFactory implements ITestFactory  {

	TestAgent testAgent;
	RlbtMain rlbt;
	/**
	 * 
	 */
	public RlbtTestFactory() {
		rlbt = new RlbtMain();
	}

	@Override
	public void attachAgent(TestAgent agent) {
		this.testAgent = agent;
		rlbt.generateGoals (agent);
	}

	@Override
	public TestAgent getAgent() {
		return testAgent;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GoalStructure nextGoal() {
		return rlbt.nextGoal();
	}

	@Override
	public GoalStructure execute(GoalStructure G, boolean reset) {
		return G;
	}

	/**
	 * only for testing purposes
	 * @param args
	 */
	public static void main(String[] args) {
		RlbtTestFactory rlbtTestFactory = new RlbtTestFactory();
		TestAgent agent = new TestAgent("agent1", "RL agent");
		rlbtTestFactory.attachAgent(agent );
		GoalStructure goal;
		while ((goal = rlbtTestFactory.nextGoal()) != null) {
			System.out.println(goal);
		}
	}
}
