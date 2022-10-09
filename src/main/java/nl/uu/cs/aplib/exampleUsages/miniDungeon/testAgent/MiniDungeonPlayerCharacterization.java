package nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent;

import eu.iv4xr.framework.extensions.occ.BeliefBase;
import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.extensions.occ.GoalStatus;
import eu.iv4xr.framework.extensions.occ.OCCBeliefBase;
import eu.iv4xr.framework.extensions.occ.UserCharacterization;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;

/**
 * A simple PlayerCharacterization for the MiniDungeon. It currently responds to
 * four events: Ouch, Heal, See a shrine (a moon or final shrine), and cleanse
 * a shrine; see also {@link MiniDungeonEventsProducer}.
 * 
 * <p>Generally a "PlayerCharacterization" is a model of a player's mind :) towards things
 * that are relevant for predicting its emotion. 
 * Technically, it should implement the abstract class 
 * <{@link eu.iv4xr.framework.extensions.occ.UserCharacterization}.
 * 
 * There several methods that have to be implement to form this model, see the doc
 * of each method. Among other things we need to model how different events
 * that happen in the game affects the player's perception on the likelihood
 * of achieving its different goals.
 * 
 * @author Wish
 *
 */
public class MiniDungeonPlayerCharacterization extends UserCharacterization{

	public static Goal shrineCleansed = new Goal("A shrine is cleansed.").withSignificance(8) ;
	
	/**
	 * Specify how each type of event influence the player's perception on the
	 * likelihood of different goals.
	 */
	@Override
	public void eventEffect(Event e, BeliefBase beliefbase) {
		
		GoalStatus status = beliefbase.getGoalsStatus().goalStatus(shrineCleansed.name) ;
		if(status == null) return ;
		MyAgentState functionalState = (MyAgentState) ((OCCBeliefBase) beliefbase).functionalstate ;
		WorldModel wom = functionalState.worldmodel ;
		WorldEntity a  = wom.elements.get(wom.agentId) ;
		int hp    = (Integer) a.properties.get("hp") ;
		int hpmax = (Integer) a.properties.get("hpmax") ;
		
		float hpThreshold = 0.65f * (float) hpmax ;
		
		if(e.name.equals(MiniDungeonEventsProducer.OUCH)) {
			// Ouch lowers the player's belief on the goal-likelihood, or fail it:
			if (hp <= 0) {
				status.setAsFailed();
				status.likelihood = 0 ;
			}
			else if (hp <= hpThreshold) {
				float likelihood = 100f * (float) hp / (float) hpmax ;
				status.likelihood = (int) Math.floor(likelihood) ;
			}
			return ;
		}
		if(e.name.equals(MiniDungeonEventsProducer.HEAL)) {
			// Heal increases the player's belief on the goal-likelihood:
			float likelihood = 100f * (float) hp / (float) hpmax ;
			status.likelihood = (int) Math.floor(likelihood) ;
			return ;
		}
		if(e.name.equals(MiniDungeonEventsProducer.SEESHRINE)) {
			status.likelihood = 100 ;
			return ;
		}
		if(e.name.equals(MiniDungeonEventsProducer.CLEANSE)) {
			status.setAsAchieved() ;
			status.likelihood = 100 ;
			return ;
		}	
	}

	/**
	 * Specify how desirable each event towards each goal. Positive means desirable, negative
	 * is undesirable.
	 */
	@Override
	public int desirabilityAppraisalRule(Goals_Status goals_status, String eventName, String goalName) {
		if(eventName.equals(MiniDungeonEventsProducer.OUCH) && goals_status.goalStatus(shrineCleansed.name).likelihood==0) 
			return -800 ;
		if(eventName.equals(MiniDungeonEventsProducer.OUCH)) 
			return -100 ;
		if(eventName.equals(MiniDungeonEventsProducer.HEAL) && goalName.equals(shrineCleansed.name)) {
			return 400 ;
		}
		if(eventName.equals(MiniDungeonEventsProducer.SEESHRINE) && goalName.equals(shrineCleansed.name)){
			return 800;
		}
		return 0 ;
	}

	/**
	 * Specify a factor that controls how fast each emotion type decays. The
	 * bigger the factor, the faster is the decay.
	 * TODO: paste here the decay formula.
	 */
	@Override
	public int emotionIntensityDecayRule(EmotionType etype) {
		return 2;
	}

	/**
	 * Specify for each emotion type, what the threshold is for the emotion
	 * to be triggered.
	 */
	@Override
	public int intensityThresholdRule(EmotionType etyp) {
		return 0;
	}

}
