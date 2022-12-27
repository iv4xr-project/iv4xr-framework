package nl.uu.cs.aplib.exampleUsages.miniDungeon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Scanner;
import java.util.logging.Level;

import static nl.uu.cs.aplib.AplibEDSL.*;
import static eu.iv4xr.framework.Iv4xrEDSL.* ;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.pad.PythonCaller;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver.Policy;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.Entity.EntityType;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.MiniDungeon.GameStatus;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.MiniDungeon.MiniDungeonConfig;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.GoalLib;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentEnv;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.TacticLib;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.utils.Pair;

/**
 * A demo showing a more abstract way to construct a test scenario with iv4xr
 * and to run the scenario. The test scenario is to cleanse the altar in
 * level-0, use the altar, then cleanse the altar in level-1.
 * 
 * <p>
 * The scenario will be "more abstractly" specified as we will not fully specify
 * how exactly we can cleanse a shrine. We will employ a simple search algorithm
 * (called SA1) to repeatedly find a scroll and try it on the shrine, until one
 * is found that cleanse the shrine.
 * 
 * 
 * <p>
 * Several assertions will be check during the scenario. But additionally, we
 * will also use <b>Linear Temporal Logic (LTL) properties</b> to formulate
 * correctness properties.
 * 
 * <p>Additionally we also show how to trace selected values from the agent state,
 * and save them to a trace-file, and then produce some graphs from the trace.
 * 
 * <p>
 * Set {@link #withGraphics} to true to see the graphics. Set {@link #delay} to
 * some higher value to slow the play-test for visualisation.
 */
public class TestDemo_ScenarioAutomation_using_SA1 {
	
	// switch to true if you want to see graphic
	boolean withGraphics = true ;
	boolean supressLogging = true ;
	
	// to control the speed of the play-test. Increase this to slow the play so
	// you can see it better
	long delay = 10 ;
	
	
	DungeonApp create_MiniDungeon() throws Exception {
		// Create an instance of the game, attach an environment to it:
		MiniDungeonConfig config = new MiniDungeonConfig();
		config.numberOfHealPots = 4;
		config.viewDistance = 4;
		config.randomSeed = 79371;
		System.out.println(">>> Configuration:\n" + config);

		// setting sound on/off, graphics on/off etc:
		DungeonApp app = new DungeonApp(config);
		app.soundOn = false;
		app.headless = !withGraphics;
		if (withGraphics)
			DungeonApp.deploy(app);
		return app;
	}
	
	// an instrumenter for get some values from a state to be later saved in a trace file
	Pair<String, Number>[] instrumenter(MyAgentState S) {
		Pair<String, Number>[] out = new Pair[5];
		out[0] = new Pair<String, Number>("time", S.worldmodel.timestamp);
		out[1] = new Pair<String, Number>("x", S.worldmodel.position.x);
		out[2] = new Pair<String, Number>("y", S.worldmodel.position.z);
		out[3] = new Pair<String, Number>("maze", (Integer) DemoUtils.avatarState(S).properties.get("maze"));
		out[4] = new Pair<String, Number>("hp", (Integer) DemoUtils.avatarState(S).properties.get("hp"));
		return out;
	}
	
	// for producing graphs from a trace-file
	void mkGraph(String python, String tracefile) {
		try {
			var py = new PythonCaller(python) ;
			py.runPythonFile("./python/src/aplib/timegraph.py -i " + tracefile 
					+ " -o tmp/mdtgraph.png"
					+ " hp maze") ;
			py.runPythonFile("./python/src/aplib/heatmap.py -i " 
					+ tracefile 
					+ " -o tmp/mdhmap.png"
					+ " --width=20 --height=20 --maxval=20 --scale=1 "
					+ " hp") ;
		}
		catch(Exception e) {
			System.out.println("### " + e.getMessage()) ;
		}
	}
	
	@Test
	public void scenario2() throws Exception {	
		DungeonApp app = create_MiniDungeon() ;
		MyAgentEnv env = new MyAgentEnv(app);
		MyAgentState state = new MyAgentState();
		var goalLib = new GoalLib();
		var tacticLib = new TacticLib();

		// should be after create the agent, else the constructor sets the visibility again
		if (supressLogging) {
			Logging.getAPLIBlogger().setLevel(Level.OFF);
		}
		
		// create an agent, play as Frodo:
		var agent = new TestAgent("Frodo","Frodo");
				
		// configuring SA1-solver:		
		int explorationBudget = 20;
		var sa1Solver = new Sa1Solver<Void>((S, e) -> Utils.isReachable((MyAgentState) S, e),
				(S, e) -> Utils.distanceToAgent((MyAgentState) S, e),
				S -> (e1, e2) -> Utils.distanceBetweenEntities((MyAgentState) S, e1, e2),
				eId -> SEQ(goalLib.smartEntityInCloseRange(agent, eId), goalLib.entityInteracted(eId)),
				eId -> SEQ(goalLib.smartEntityInCloseRange(agent, eId), goalLib.entityInteracted(eId)),
				S -> tacticLib.explorationExhausted(S), budget -> goalLib.smartExploring(agent, null, budget));

		// We will now specify our test scenario.
		// Goal-1: find the first shrine and cleanse it:
		var G1 = sa1Solver.solver(agent, 
				"SM0", 
				e -> e.type.equals("" + EntityType.SCROLL), 
				S -> { var S_ = (MyAgentState) S;
					   var e = S.worldmodel.elements.get("SM0");
					   if (e == null)
						   return false;
					   var clean = (boolean) e.properties.get("cleansed");
					   return clean; }, 
				Policy.NEAREST_TO_AGENT, explorationBudget);

		// Goal-2: find the final shrine and clease it; check if then Frodo wins:
		var G2 = sa1Solver.solver(agent, 
				"SI1", 
				e -> e.type.equals("" + EntityType.SCROLL),
				S -> ((MyAgentState) S).gameStatus() == GameStatus.FRODOWIN, 	
				Policy.NEAREST_TO_AGENT, explorationBudget);

		// Now, the whole scenario: cleanse shrine in level1, use the shrine, then cleanse
		// the shrine in level2.
		// We also add few assertions to check:
		var G = SEQ(G1, // cleansing shrine-0
				assertTrue_(agent,"","shrine-0 is cleansed, the agent is in level-0", 
						(MyAgentState S) -> 
				        (Boolean) state.worldmodel.elements.get("SM0").properties.get("cleansed")
				        && (Integer) DemoUtils.avatarState(S).properties.get("maze") == 0),		
				goalLib.entityInteracted("SM0"), // use shrine-0 to teleport
				assertTrue_(agent,"","the agent is in level-1", 
						(MyAgentState S) -> 
				        (Integer) DemoUtils.avatarState(S).properties.get("maze") == 1),
				G2,  // cleansing shrine-1
				assertTrue_(agent,"","shrine-1 is cleansed", 
						(MyAgentState S) -> 
				        (Boolean) state.worldmodel.elements.get("SI1").properties.get("cleansed"))
				);
		
		// We will additionally specify some LTL properties to check:
		// (1) Frodo's never carry more than 2 items (its bag capacity)
		LTL<SimpleState> phi1 = LTL.always(
			(SimpleState S) -> (Integer) DemoUtils.avatarState((MyAgentState) S).properties.get("bagUsed") <= 2
		) ;
		// (2) first time shrine-0 is found, it is NOT clean:
		LTL<SimpleState> phi2 = LTL.now(
				(SimpleState S) -> ((MyAgentState) S) .worldmodel.elements.get("SM0") == null)
				.until(
				(SimpleState S) -> ((MyAgentState) S) .worldmodel.elements.get("SM0") != null
				&& ! (Boolean) ((MyAgentState) S).worldmodel.elements.get("SM0").properties.get("cleansed")
		);
		
		// Now, create an agent, attach the game to it, and give it the above goal:
		agent. attachState(state)
			 . attachEnvironment(env)
			 . setTestDataCollector(new TestDataCollector()) 
			 . setGoal(G) ;
		// attach LTL properties to check:
		agent.addLTL(phi1,phi2) ;
		
		// optionally attach an instrumenter to save instrumented values to a trace-file;
		// we can later visualize the trace file:
		agent.withScalarInstrumenter(S -> instrumenter((MyAgentState) S)) ;
		
		

		Thread.sleep(1000);
				
		// Now we run the agent:
		System.out.println(">> Start agent loop...") ;
		int k = 0 ;
		agent.resetLTLs() ;
		while(G.getStatus().inProgress()) {
			agent.update();
			System.out.println("** [" + k + "] agent @" + Utils.toTile(state.worldmodel.position)) ;
			// delay to slow it a bit for displaying:
			Thread.sleep(delay); 
			if (k>=700) break ;
			k++ ;
		}	
		assertTrue(G.getStatus().success()) ;
		assertTrue(agent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
		assertTrue(agent.evaluateLTLs()) ;	
		
		// saving trace-file and produce graphs from it; you can find them under ./tmp dir:
		agent.getTestDataCollector().saveTestAgentScalarsTraceAsCSV(agent.getId(),"tmp/mddemo_trace.csv");
		mkGraph("/usr/local/bin/python3","tmp/mddemo_trace.csv") ;
	}
	
}
