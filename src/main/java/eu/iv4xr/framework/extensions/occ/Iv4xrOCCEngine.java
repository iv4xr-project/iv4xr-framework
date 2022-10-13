package eu.iv4xr.framework.extensions.occ;

import java.util.function.Function;

import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import nl.uu.cs.aplib.multiAgentSupport.Message;

/**
 * An subclass of JOCC's EmotionAppraisalSystem that is meant to be
 * connected to iv4xr's {@link eu.iv4xr.framework.mainConcepts.EmotiveTestAgent}.
 * 
 * @author Wish
 */
public class Iv4xrOCCEngine extends EmotionAppraisalSystem{
	
	public Iv4xrOCCEngine(String agentName) {
		super(agentName);
	}
	
	/**
	 * Attach this OCC-engine to an iv4xr Emotive-test-agent.
	 */
	public Iv4xrOCCEngine attachToEmotiveTestAgent(EmotiveTestAgent agent) {
		this.agentName = agent.getId() ;
		OCCBeliefBase bbs = new OCCBeliefBase() ;
		bbs.attachFunctionalState(agent.state()) ;
		this.attachEmotionBeliefBase(bbs) ;
		var emotionState = new OCCState(this) ;
		agent.attachEmotionState(emotionState) ;
		return this ;
	}
	
	/**
	 * Attach a user-model/user-characterization to this OCC-engine.
	 */
	@Override
	public  Iv4xrOCCEngine withUserModel(UserCharacterization userModel) {
		super.withUserModel(userModel) ;
		return this ;
	}
	
	/**
	 * Register a "goal" to the OCC-engine. A goal is identified mainly
	 * by its name (wrapped as an instance of JOCC's Goal). We also need
	 * to specify what is the assumed initial likelihood that this goal
	 * could be achieved in the future.
	 */
	@Override
	public Iv4xrOCCEngine addGoal(Goal g, int likelihood) {
		super.addGoal(g,likelihood) ;
		return this ;
	}
	
	/**
	 * Just another name of {@link #addGoal(Goal, int)}.
	 */
	public Iv4xrOCCEngine addMentalGoal(Goal g, int likelihood) {
		return addGoal(g,likelihood) ;
	}
	
}
