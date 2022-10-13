package eu.iv4xr.framework.extensions.occ;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.framework.mainConcepts.IEmotion;
import eu.iv4xr.framework.mainConcepts.IEmotionState;
import nl.uu.cs.aplib.multiAgentSupport.Message;

/**
 * An implementation of IEmotionState, representing the current emotional
 * state of an agent. This emotional state is a list of emotions, each
 * represents a type of emotion, e.g. fear, and specifies the intensity
 * of the emotion, and the target that the emotion is directed at. 
 * For now, this target usually representing some goal of the agent, e.g.
 * to win a game. The class provides getters to obtain the current
 * intensity of various emptions as held in this emotion-state.
 * 
 * <p>Currently there are six types of emotion implemented: hope, joy,
 * satisfaction, fear, distress, and disappointment. All are goal-oriented
 * type of emotions. E.g. "hope" here is meant to be the hope towards
 * achieving some goal g. If the agent has multiple goals, g1 and g2, then
 * we can have hope or joy towards each of them separately.
 * 
 * <p>Goals are assumed to be identifiable by their names.
 * 
 * @author Wish
 *
 */
public class OCCState implements IEmotionState {
	
	public Iv4xrOCCEngine occEngine ;	
	public List<IEmotion> previousEmotions ;
	
	/**
	 * Representing time. For now it just counts how many times the {@link updateEmotion}
	 * method has been called.
	 */
	public int time = 0 ;
	
	public OCCState(Iv4xrOCCEngine occEngine) {
		this.occEngine = occEngine ;
	}
	
	private EmotionType[] emotionTypes = {
		EmotionType.Hope,
		EmotionType.Joy,
		EmotionType.Satisfaction,
		EmotionType.Fear,
		EmotionType.Distress,
		EmotionType.Disappointment
	} ;

	@Override
	public List<IEmotion> getCurrentEmotion() {	
		List<IEmotion> emotions = new LinkedList<>() ;	
		Set<String> goalNames = occEngine.beliefbase.getGoalsStatus().statuses.keySet()  ;
		for(String gname : goalNames) {
			for (var ety : emotionTypes) {
				Emotion e = occEngine.getEmotion(gname,ety) ;
				if(e != null) {
					e = e.shallowClone() ;
					emotions.add(new OCCEmotion(gname,e)) ;
				}
			}
			
		}
		return emotions ;
	}

	/**
	 * Search the current list of emotions for the emotion for the given emotion-type,
	 * with respect to a given goal. If the emotion exists, its intensity is returned,
	 * and else null.
	 */
	private Float getGoalBasedEmotionIntensity(List<IEmotion> emotions, String goal, EmotionType ety) {
		var ez = emotions.stream().filter(e -> 
		         ((OCCEmotion) e).goalName.equals(goal) &&
		         ((OCCEmotion) e).em.etype == ety)
				 .collect(Collectors.toList());
		if (ez.isEmpty()) return null ;
		return ez.get(0).getIntensity() ;
	}
	
	private Float getGoalBasedEmotionIntensity(String goal, EmotionType ety) {
		return getGoalBasedEmotionIntensity(getCurrentEmotion(),goal,ety) ;
	}
	
	private Float getGoalBasedEmotionDeltaIntensity(String goal, EmotionType ety) {
		if (previousEmotions == null || previousEmotions.isEmpty())
			return null ;
		Float intensity = getGoalBasedEmotionIntensity(getCurrentEmotion(),goal,ety) ;
		if (intensity == null)
			return null ;
		Float prevIntensity = getGoalBasedEmotionIntensity(previousEmotions,goal,ety) ;
		if (prevIntensity == null)
			return null ;
		return intensity - prevIntensity ;
	}
	
	/**
	 * Get the current intensity of the emotion hope towards the specified goal.
	 * The goal is identified by its name.
	 */
	public Float hope(String goal) {
		return getGoalBasedEmotionIntensity(goal,EmotionType.Hope) ;
	}

	/**
	 * Get the current intensity of the emotion joy towards the specified goal.
	 * The goal is identified by its name.
	 */
	public Float joy(String goal) {
		return getGoalBasedEmotionIntensity(goal,EmotionType.Joy) ;
	}
	
	/**
	 * Get the current intensity of the emotion satisfaction towards the specified goal.
	 * The goal is identified by its name.
	 */
	public Float satisfaction(String goal) {
		return getGoalBasedEmotionIntensity(goal,EmotionType.Satisfaction) ;
	}
	
	/**
	 * Get the current intensity of the emotion fear towards the specified goal.
	 * The goal is identified by its name.
	 */	
	public Float fear(String goal) {
		return getGoalBasedEmotionIntensity(goal,EmotionType.Fear) ;
	}
	
	/**
	 * Get the current intensity of the emotion distress towards the specified goal.
	 * The goal is identified by its name.
	 */		
	public Float distress(String goal) {
		return getGoalBasedEmotionIntensity(goal,EmotionType.Distress) ;
	}
	
	/**
	 * Get the current intensity of the emotion disappointment towards the specified goal.
	 * The goal is identified by its name.
	 */		
	public Float disappointment(String goal) {
		return getGoalBasedEmotionIntensity(goal,EmotionType.Disappointment) ;
	}
	
	/**
	 * Get the current increase in intensity of the emotion hope towards the
	 * specified goal. The goal is identified by its name.
	 * Negative value means decrease.
	 */			
	public Float difHope(String goal) {
		return getGoalBasedEmotionDeltaIntensity(goal,EmotionType.Hope) ;
	}
	
	/**
	 * Get the current increase in intensity of the emotion joy towards the
	 * specified goal. The goal is identified by its name.
	 * Negative value means decrease.
	 */	
	public Float difJoy(String goal) {
		return getGoalBasedEmotionDeltaIntensity(goal,EmotionType.Joy) ;
	}
	
	/**
	 * Get the current increase in intensity of the emotion satisfaction towards the
	 * specified goal. The goal is identified by its name.
	 * Negative value means decrease.
	 */	
	public Float difSatisfaction(String goal) {
		return getGoalBasedEmotionDeltaIntensity(goal,EmotionType.Satisfaction) ;
	}
	
	/**
	 * Get the current increase in intensity of the emotion fear towards the
	 * specified goal. The goal is identified by its name.
	 * Negative value means decrease.
	 */	
	public Float difFear(String goal) {
		return getGoalBasedEmotionDeltaIntensity(goal,EmotionType.Fear) ;
	}
	
	/**
	 * Get the current increase in intensity of the emotion distress towards the
	 * specified goal. The goal is identified by its name.
	 * Negative value means decrease.
	 */	
	public Float difDistress(String goal) {
		return getGoalBasedEmotionDeltaIntensity(goal,EmotionType.Distress) ;
	}
	
	/**
	 * Get the current increase in intensity of the emotion disappointment towards the
	 * specified goal. The goal is identified by its name.
	 * Negative value means decrease.
	 */	
	public Float difDisappointment(String goal) {
		return getGoalBasedEmotionDeltaIntensity(goal,EmotionType.Disappointment) ;
	}

	@Override
	public void updateEmotion(EmotiveTestAgent agent) {
		previousEmotions = getCurrentEmotion() ;
		var events = agent.getSyntheticEventsProducer().getCurrentEvents() ;
		for(Message m : events) {
			XEvent e = new XEvent(m) ;
			occEngine.update(e,time);
		}
		agent.getSyntheticEventsProducer().currentEvents.clear();
		time++ ;
	}

}
