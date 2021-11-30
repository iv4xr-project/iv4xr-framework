package eu.iv4xr.framework.testingtoolPlugins;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

/**
 * Use this interface to wrap around a testing-tool to produce either a single
 * test-case or a whole test-suite, which is then given to a test-agent to be
 * executed.
 * 
 * If the testing-tool produces a test-suite, the tool should formulate each
 * test-case in the suite as a goal-structure
 * ({@link nl.uu.cs.aplib.mainConcepts.GoalStructure}) so that it is executable
 * by the test-agent. If the testing-tool produces just a single test-case,
 * we can either treat it as a test-suite with just one test-case, or we apply
 * a more refined control by breakting the test-case into a series of goal-structure.
 * 
 * @author Wish
 *
 */

public interface ITestExecutor {
	
	/**
	 * Attach a test-agent to this executor. The agent is assumed to already have an
	 * Environment ({@link nl.uu.cs.aplib.mainConcepts.Environment}) that attached
	 * to it. The latter represent the interface to the System under Test (SUT).
	 */
	public void attachAgent(TestAgent agent) ;
	
	/**
	 * Return the test-agent that is attached to this executor.
	 */
	public TestAgent getAgent() ;
	
	/**
	 * Reset the test-agent back to its initial state, and also reset
	 * the Environment ({@link nl.uu.cs.aplib.mainConcepts.Environment}) that
	 * is attached to the agent. The latter represent the interface to the
	 * System under Test (SUT). Reseting the Environment might entail resetting
	 * the SUT as well.
	 */
	public void reset() ;
	
	/**
	 * Return the next test-case to execute. A test-case is represented as a
	 * goal-structure. The method returns null if there is no next text-case
	 * to return.
	 */
	public GoalStructure nextGoal() ;
	
	/**
	 * Gives the goal/test-case G for the agent attached to this executor, to
	 * be executed. After the execution, the method returns the goal. It can
	 * be inspected for its status (e.g. if it was achieved/success or failed).
	 */
	public GoalStructure execute(GoalStructure G) ;
	
	// TODO
	public Object getTestResults() ;
	

}
