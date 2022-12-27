package nl.uu.cs.aplib.exampleUsages.miniDungeon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static nl.uu.cs.aplib.AplibEDSL.*;
import static eu.iv4xr.framework.Iv4xrEDSL.* ;

import eu.iv4xr.framework.extensions.pad.PythonCaller;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.DungeonApp;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.MiniDungeon.MiniDungeonConfig;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.GoalLib;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentEnv;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;

/**
 * A demo showing how construct a 'scripted' test scenario with iv4xr and to run
 * the scenario. The test scenario is to pick up scroll with id S0_1, and then
 * use it on the shrine of level-0. We expect that the shrine will then be
 * cleansed.
 * 
 * <p>
 * The scenario is "scripted" as we specify explicitly which scroll should be
 * used to cleanse the shrine. However, note that the scenario is still
 * abstractly specified, as we do not program how the test agent should move
 * step by step to e.g. find and then use the scroll.
 * 
 * <p>
 * Set {@link #withGraphics} to true to see the graphics. Set {@link #delay} to
 * some higher value to slow the play-test for visualisation.
 */
public class Demo_ScriptedScenario {
	
	// switch to true if you want to see graphic
	boolean withGraphics = false ;
	boolean supressLogging = true ;
	
	// to control the speed of the play-test. Increase this to slow the play so
	// you can see it better
	long delay = 10 ;
	
	DungeonApp create_MiniDungeon() throws Exception {
		// Create an instance of the game:
		MiniDungeonConfig config = new MiniDungeonConfig();
		config.numberOfHealPots = 4;
		config.viewDistance = 4;
		System.out.println(">>> Configuration:\n" + config);

		// setting sound on/off, graphics on/off etc:
		DungeonApp app = new DungeonApp(config);
		app.soundOn = false;
		app.headless = !withGraphics;
		if (withGraphics)
			DungeonApp.deploy(app);
		return app;
	}
	
	/**
	 * A simple test scenario. In this test the agent will search and pick up scroll
	 * with id S0_1, and then use it on the shrine of level-0. We expect that the
	 * shrine will then be cleansed.
	 */
	@Test
	public void simple_scripted_testscenario() throws Exception {
		DungeonApp app = create_MiniDungeon() ;
		MyAgentEnv env = new MyAgentEnv(app);
		MyAgentState state = new MyAgentState() ;
		var goalLib = new GoalLib() ;
		
		//var agent = new TestAgent("Frodo","Frodo")  ;
		var agent = new TestAgent("Smeagol","Smeagol")  ;
		
		//
		// We script a test scenario here. In between, we add few assertions to check.
		//
		var G = SEQ(
				  goalLib.smartEntityInCloseRange(agent,"S0_1"),  // find scroll with that id
				  assertTrue_(agent,"","agent does not have a scroll", 
					(MyAgentState S) -> 
			       	(Integer) DemoUtils.avatarState(S).properties.get("scrollsInBag") == 0),
				  goalLib.entityInteracted("S0_1"), // get the scroll
				  assertTrue_(agent,"","bag has one scroll and the shrine is NOT clean", 
					(MyAgentState S) -> 
				    (Integer) DemoUtils.avatarState(S).properties.get("scrollsInBag") == 1),
				  goalLib.smartEntityInCloseRange(agent,"SM0"),   // find shrine of level-0
				  assertTrue_(agent,"","the shrine is NOT clean", 
							(MyAgentState S) -> 
					        ! (Boolean) S.worldmodel.elements.get("SM0").properties.get("cleansed")),
				  goalLib.entityInteracted("SM0"), // use scroll on shrine
				  assertTrue_(agent,"","the scroll should be consumed and the shrine is clean", 
					(MyAgentState S) -> 
			        (Integer) DemoUtils.avatarState(S).properties.get("bagUsed") == 0
			         && (Boolean) S.worldmodel.elements.get("SM0").properties.get("cleansed")),
				  SUCCESS()
				) ;

		// Now, create an agent, attach the game to it, and give it the above goal:
		agent. attachState(state)
			 . attachEnvironment(env)
			 . setTestDataCollector(new TestDataCollector()) 
			 . setGoal(G) ;

		Thread.sleep(1000);
		
		// Now we run the agent:
		System.out.println(">> Start agent loop...") ;
		int k = 0 ;
		while(G.getStatus().inProgress()) {
			agent.update();
			System.out.println("** [" + k + "] agent @" + Utils.toTile(state.worldmodel.position)) ;
			// delay to slow it a bit for displaying:
			Thread.sleep(delay); 
			if (k>=300) break ;
			k++ ;
		}	
		// check that the scenario is completed:
		assertTrue(G.getStatus().success())	 ;
		// WorldEntity avatar = state.worldmodel.elements.get(agent.getId()) ;
		// check that no assertions inserted in the goal-structure were violated:
		assertTrue(agent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
		WorldEntity shrine = state.worldmodel.elements.get("SM0") ;
		System.out.println("=== " + shrine) ;
		// just to show that we also directly check the correctness of the agent-state like
		// this:
		assertTrue((Boolean) shrine.properties.get("cleansed")) ;
	}
	
}
