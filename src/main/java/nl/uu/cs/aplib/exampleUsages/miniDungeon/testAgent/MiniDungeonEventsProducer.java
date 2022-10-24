package nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.extensions.occ.XEvent;
import eu.iv4xr.framework.mainConcepts.SyntheticEventsProducer;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.Entity.ShrineType;
import nl.uu.cs.aplib.multiAgentSupport.Message;
import nl.uu.cs.aplib.multiAgentSupport.Message.MsgCastType;
import nl.uu.cs.aplib.utils.Pair;

/**
 * Implementing a SyntheticEventsProducer for the game MiniDungeon. As this
 * class is only meant as a part of demonstrating OCC, we will keep it somewhat
 * simple. We will only produce few types of events:
 * 
 * <ol>
 * <li>Ouch-event: triggered when the agent's hp goes down.
 * <li>Heal-event: triggered when the agent's hp goes up.
 * <li>SeeShrine-event: triggered when the agent sees a (Moon or Immortal)
 * shrine for the first time. Seeing it again does not re-trigger the event.
 * Seeing another Moon/Immortal shrine triggers it again.
 * <li>Cleanse-event: triggered when the agent cleanses a shrine. a shrine).
 * </ol>
 * 
 * @author Wish
 */
public class MiniDungeonEventsProducer extends SyntheticEventsProducer {
	
	static final String OUCH = "Ouch" ;
	static final String HEAL = "Heal" ;
	static final String SEESHRINE = "SeeShrine" ;
	static final String CLEANSE   = "Cleanse" ;
	
	List<Pair<String,String>> history = new LinkedList<>() ;
	
	//public MiniDungeonEventsProducer() {}

	private MyAgentState getMiniDungeonTestState() {
		 return (MyAgentState) this.agent.state() ;
	}
	
	@Override
	public void generateCurrentEvents() {
		var state = getMiniDungeonTestState() ;
		var wom = state.worldmodel ;
		WorldEntity a = wom.elements.get(wom.agentId) ;
		Message event ;
		
		// first handle events that do not require previous state;
		// shrine-related events:
		
		var mazeId = (Integer) a.properties.get("maze") ;
		var shrines = wom.elements.values().stream() 
			   .filter(e -> e.type.equals("SHRINE") && 
					   mazeId.equals((Integer) e.properties.get("maze")))	
			   .collect(Collectors.toList()) ;
		if (! shrines.isEmpty()) {
			var s0 = shrines.get(0) ;
			var shrineType = (ShrineType) s0.properties.get("shrinetype") ;
			if (shrineType != ShrineType.SunShrine) {
				// see-shrine-event:
				Pair<String,String> event1id_ = new Pair<>(SEESHRINE,s0.id) ;
				if (!history.contains(event1id_)) {
					// one-time event:
					event = new Message("wom.agentId",0,MsgCastType.BROADCAST,"*",SEESHRINE,s0.id) ;
					currentEvents.add(event) ;
					history.add(event1id_) ;
				}
				// cleanse-event:
				var cleansed = (Boolean) s0.properties.get("cleansed") ;
				Pair<String,String> event2id_ = new Pair<>(CLEANSE,s0.id) ;
				if (!history.contains(event2id_) && cleansed) {
					// one-time event:
					event = new Message("wom.agentId",0,MsgCastType.BROADCAST,"*",CLEANSE,s0.id) ;
					currentEvents.add(event) ;
					history.add(event2id_) ;
				}	
			}	
		}
		
		// events that require comparison with previous state, ouch and heal:
		if (!a.hasPreviousState()) {
			return ;
		}
		WorldEntity prev = a.getPreviousState() ;
		// ouch event:
		int hp = (Integer) a.properties.get("hp")  ;
		int hpPrev = (Integer) prev.properties.get("hp")  ;
		if (hp < hpPrev && a.lastStutterTimestamp < 0) {
			event = new Message("wom.agentId",0,MsgCastType.BROADCAST,"*",OUCH) ;
			currentEvents.add(event) ;
		}
		// heal event:
		if (hp > hpPrev && a.lastStutterTimestamp < 0) {
			event = new Message("wom.agentId",0,MsgCastType.BROADCAST,"*",HEAL) ;
			currentEvents.add(event) ;
		}	
	}

}
