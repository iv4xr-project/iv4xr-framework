package eu.iv4xr.framework.trainer;

import java.util.Map;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

/**
 * An interface to wrap over a test-agent with the intention to use it
 * as a trainer to train some machine-learning model to do certain
 * tasks in some System Under Test (SUT). The learner does not directly
 * control the SUT, but instead control it through the test-agent, with
 * the benefit that the test-agent can provide actions that are higher
 * level, and hence making the learning easier.
 * 
 * <p>This training interface provides methods similar to OpenAI. E.g.
 * we have {@link #observe()} to allow the learner to observe the state
 * of the SUT, and the method {@link #availableActions()} that provides
 * a list of available actions that are executable on the SUT's current
 * state, which subsequently the learner can try.
 * 
 * <p>The main difference is that an 'action' is offered in terms of
 * a goal-structure ({@link nl.uu.cs.aplib.mainConcepts.GoalStructure}),
 * which means that it can be a high-level action, which internally
 * may consists of a complex sequence of primitive actions. We have
 * a test agent that is capable of executing goal-structures.
 * Providing actions at higher level makes learning easier for the
 * learner.
 * 
 * @author Wish
 *
 */
public interface ITrainer {
	
	/**
	 * Attach a test-agent to this executor. The agent is assumed to already have an
	 * Environment ({@link nl.uu.cs.aplib.mainConcepts.Environment}) that attached
	 * to it. The latter represent the interface to the System under Test (SUT).
	 */
	public void attachAgent(TestAgent agent) ;
	
	/**
	 * Return the test-agent that is attached to this learning-env.
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
	
	public WorldModel observe() ;
	
	public float reward() ;
	
	public boolean done() ;
	
	public Map<String,GoalStructure> availableActions() ;
	
	/**
	 * Gives the goal/test-case G for the agent attached to this executor, to be
	 * executed. After the execution, the method returns the goal. It can be
	 * inspected for its status (e.g. if it was achieved/success or failed).
	 * 
	 * <p>When the 'reset' parameter is true, it indicates that the test agent should
	 * be reset first (using {@link #reset()} before executing the goal.
	 */
	public GoalStructure execute(GoalStructure G, boolean reset) ;
	

}
