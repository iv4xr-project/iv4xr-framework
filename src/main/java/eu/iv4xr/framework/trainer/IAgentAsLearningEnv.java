package eu.iv4xr.framework.trainer;

import java.util.Map;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

/**
 * An interface to wrap over a test-agent with the intention to use it
 * for exploring the System under Test for the purpose of training some
 * machine-learning model. From the perspective of the learner, this
 * interface can be thought to provide a learning environment. The learner
 * can inspect the environment's current state, and ask available actions,
 * and then command the environment to do an action.
 * 
 * <p>The interface is similar to that of OpenAI.
 * 
 * @author Wish
 *
 */
public interface IAgentAsLearningEnv {
	
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
	
	public GoalStructure execute(GoalStructure G) ;
	

}
