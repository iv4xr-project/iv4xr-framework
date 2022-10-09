package nl.uu.cs.aplib.exampleUsages.miniDungeon;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.extensions.occ.EmotionAppraisalSystem;
import eu.iv4xr.framework.extensions.occ.OCCBeliefBase;
import eu.iv4xr.framework.extensions.occ.OCCState;
import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.MiniDungeon.MiniDungeonConfig;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.GoalLib;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MiniDungeonEventsProducer;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MiniDungeonPlayerCharacterization;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentEnv;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import static nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MiniDungeonPlayerCharacterization.* ;

public class PXTest_MiniDungeon {
	
	DungeonApp deployApp() throws Exception {
		MiniDungeonConfig config = new MiniDungeonConfig();
		config.numberOfHealPots = 4;
		config.viewDistance = 4;
		System.out.println(">>> Configuration:\n" + config);
		DungeonApp app = new DungeonApp(config);
		//app.soundOn = false;
		DungeonApp.deploy(app);
		return app ;
	}
	
	@Test
	public void test1() throws Exception {
		// Create an instance of the game:
		DungeonApp app = deployApp() ;
		
		var goalLib        = new GoalLib();
		MyAgentState state = new MyAgentState();
		var agent          = new EmotiveTestAgent("Frodo","Frodo") ;
		//var agent = new TestAgent("Smeagol", "Smeagol");
		
		agent. attachState(state)
	         . attachEnvironment(new MyAgentEnv(app)) 
	         . attachSyntheticEventsProducer(new MiniDungeonEventsProducer()) ;
		
		OCCBeliefBase bbs = new OCCBeliefBase() 
			 . attachFunctionalState(state) ;

		EmotionAppraisalSystem eas = new EmotionAppraisalSystem(agent.getId()) 
			 . withUserModel(new MiniDungeonPlayerCharacterization())
			 . attachEmotionBeliefBase(bbs) 
			 . addGoal(shrineCleansed, 70)
			 ;
		
		eas.addInitialEmotions();
		
		OCCState emotionState = new OCCState(agent,eas) 
			 . setEventTranslator(msg -> MiniDungeonEventsProducer.translateAplibMsgToOCCEvent(msg)) ;
		
		
		agent.attachEmotionState(emotionState) ;

		
		//
		// Specify a goal for the agent: search and grab scroll S0 then use it on the
		// Shrine.
		//
		var G = SEQ(//goalLib.smartEntityInCloseRange(agent, "S0_0"), 
				    //goalLib.entityInteracted("S0_0"),
				    //goalLib.smartEntityInCloseRange(agent, "SM0"), 
				    //goalLib.entityInteracted("SM0"),
				    goalLib.smartEntityInCloseRange(agent, "S0_1"), 
				    goalLib.entityInteracted("S0_1"),
				    goalLib.smartEntityInCloseRange(agent, "SM0"), 
				    goalLib.entityInteracted("SM0"),
				    SUCCESS());

		// Now, create an agent, attach the game to it, and give it the above goal:
	agent
		     .setGoal(G);

		Thread.sleep(1000);

		// Now we run the agent:
		System.out.println(">> Start agent loop...");
		int k = 0;
		while (G.getStatus().inProgress()) {
			agent.update();
			var a = state.worldmodel.elements.get(state.worldmodel.agentId) ;
			int hp = (Integer) a.properties.get("hp") ;
			var shrine = state.worldmodel.elements.get("SM0") ;
			String info = "** [" + k + "] agent @" 
				    + Utils.toTile(state.worldmodel.position) 
				    + ", hp=" + hp ;
			if (shrine != null && shrine.timestamp == state.worldmodel.timestamp) {
				info += ", shrine SM0 in sight" ;
				if ((Boolean) shrine.properties.get("cleansed")) {
					info += ", CLEANSDED." ;
				}
			}
			info += ", fear=" + emotionState.fear(shrineCleansed.name) ;
			info += ", dFear=" + emotionState.difFear(shrineCleansed.name) ;
			System.out.println(info);
			// delay to slow it a bit for displaying:
			Thread.sleep(50);
			if (k >= 600)
				break;
			k++;
		}
		// System.exit(0);
	}

}
