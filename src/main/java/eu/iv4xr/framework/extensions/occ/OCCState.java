package eu.iv4xr.framework.extensions.occ;

import java.util.*;
import java.util.function.Function;

import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.framework.mainConcepts.IEmotion;
import eu.iv4xr.framework.mainConcepts.IEmotionState;
import nl.uu.cs.aplib.multiAgentSupport.Message;

public class OCCState implements IEmotionState {
	
	public EmotiveTestAgent testAgent ;
	public EmotionAppraisalSystem eas ;
	public Set<String> goalNames = new HashSet<>() ;
	
	public Function<Message,Event> eventTranslator = null ;
	
	/**
	 * Representing time. For now it just counts how many times the {@link updateEmotion}
	 * method has been called.
	 */
	public int time = 0 ;
	
	public OCCState(EmotiveTestAgent agent, EmotionAppraisalSystem eas) {
		testAgent = agent ;
		this.eas = eas ;
		goalNames.addAll(eas.beliefbase.getGoalsStatus().statuses.keySet()) ;
	}
	
	public OCCState setEventTranslator(Function<Message,Event> translator) {
		eventTranslator = translator ;
		return this ;
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
		for(String gname : goalNames) {
			for (var ety : emotionTypes) {
				Emotion e = eas.getEmotion(gname,ety) ;
				if(e != null) {
					e = e.shallowClone() ;
					emotions.add(new OCCEmotion(gname,e)) ;
				}
			}
			
		}
		return emotions ;
	}

	@Override
	public void updateEmotion(EmotiveTestAgent agent) {
		var events = testAgent.getSyntheticEventsProducer().getCurrentEvents() ;
		for(Message m : events) {
			Event e = eventTranslator.apply(m) ;
			eas.update(e,time);
		}
		time++ ;
	}

}
