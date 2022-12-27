package nl.uu.cs.aplib.exampleUsages.miniDungeon;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.extensions.ltl.LTL;
import static eu.iv4xr.framework.extensions.ltl.LTL.* ;

import eu.iv4xr.framework.extensions.occ.EmotionAppraisalSystem;
import eu.iv4xr.framework.extensions.occ.Iv4xrOCCEngine;
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
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;

import static nl.uu.cs.aplib.AplibEDSL.* ;

import static nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MiniDungeonPlayerCharacterization.* ;

public class TestDemoPX {
	
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
	
	GoalStructure testScenario1(TestAgent agent) {
		var goalLib = new GoalLib();
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
		return G ;
	}
	
	void runTheAgent(EmotiveTestAgent agent, GoalStructure G, int budget) throws InterruptedException {
		var state = (MyAgentState) agent.state() ;
		var emotionState = (OCCState) agent.getEmotionState() ;
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
			//Thread.sleep(50);
			if (k >= budget)
				break;
			k++;
		}

	}
	
	OCCState getEmotionState(SimpleState S) {
		return (OCCState) ((EmotiveTestAgent) S.owner()).getEmotionState() ;
	}
	
	@Test
	public void test1() throws Exception {
		// Create an instance of the game:
		DungeonApp app = deployApp() ;
		
		var agent = new EmotiveTestAgent("Frodo","Frodo") ;
		
		agent. attachState(new MyAgentState())
	         . attachEnvironment(new MyAgentEnv(app)) 
	         . attachSyntheticEventsProducer(new MiniDungeonEventsProducer()) ;

		var occEngine = new Iv4xrOCCEngine(agent.getId()) 
			 . attachToEmotiveTestAgent(agent) 
			 . withUserModel(new MiniDungeonPlayerCharacterization())
			 . addGoal(shrineCleansed, 70)
			 ;
		
		occEngine.addInitialEmotions();
		
		
		//
		// Specify a goal for the agent: search and grab scroll S0 then use it on the
		// Shrine.
		//
		var G = testScenario1(agent) ;
				
		LTL<SimpleState> fear = 
			eventually(S -> 
				getEmotionState(S).difFear(shrineCleansed.name ) != null
				&& getEmotionState(S).difFear(shrineCleansed.name ) > 0) ;
		
		LTL<SimpleState> distress = 
				eventually(S -> 
					getEmotionState(S).difDistress(shrineCleansed.name ) != null
					&& getEmotionState(S).difDistress(shrineCleansed.name ) > 0) ;

		// Now, create an agent, attach the game to it, and give it the above goal:
		agent.setGoal(G);
		agent.addLTL(fear) ;
		// if we add distress, it will fail:
		//agent.addLTL(fear,distress) ;

		Thread.sleep(1000);

		// Now we run the agent:
		runTheAgent(agent,G,600) ;
		assertTrue(agent.evaluateLTLs()) ;
		// System.exit(0);
	}

}
