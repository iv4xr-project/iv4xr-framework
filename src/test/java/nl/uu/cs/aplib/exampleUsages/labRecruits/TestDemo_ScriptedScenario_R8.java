package nl.uu.cs.aplib.exampleUsages.labRecruits;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.pad.PythonCaller;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver.Policy;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.Entity.EntityType;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.utils.Pair;

import static org.junit.jupiter.api.Assertions.* ;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Scanner;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;
import world.LabEntity;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;
import static eu.iv4xr.framework.Iv4xrEDSL.* ;
import static eu.iv4xr.framework.extensions.ltl.LTL.always;
import static eu.iv4xr.framework.extensions.ltl.LTL.now;

/**
 * Another demo of testing with iv4xr. The testing task to do is to verify that
 * the level's end-goal is reachable. The end-goal is represented by a flag that
 * will give the player lots of point when touched. Internally, the flag is
 * identified with ID "Finish".
 * 
 * <p>
 * Moreover we want to verify that when the agent reach this Finish, it should
 * have at least 533 score and its health is restored to 100.
 * 
 * <p>
 * The access to Finish is guarded by doors. So showing that Finish is reachable
 * amounts to getting at least one of its guarding doors open, which is not so
 * trivial as it involves toggling a button in another room which in turn is
 * also guarded by a door. In short, there is a small puzzle that has to be
 * solved first. The raw search space over a Lab Recruit game is way to big for
 * a test agent to try to brute-force solving this puzzle, so in the solution
 * below we provide a guidance in the form of a scripted steps how to open the
 * needed doors. Fortunately, this can be scripted at a high level.
 * 
 * <p>Additionally we also show how to trace selected values from the agent state,
 * and save them to a trace-file, and then produce some graphs from the trace.
 * They can be found in ./tmp directory.
 * 
 * <p>
 * Set {@link #withGraphics} to true to see the graphics.
 * 
 * @author Wish
 */
public class TestDemo_ScriptedScenario_R8 {

    private static LabRecruitsTestServer labRecruitsTestServer;
    
    // switch to true if you want to see graphic
 	static boolean withGraphics = true ;
    static String projectRoot ;
    static String labRecruitesExeRootDir ;
    static String levelsDir ;

    @BeforeAll
    static void start() {
    	// Configuring the locations of the Lab-Recruits executable and
    	// the level-definition files:
    	String fileSeparator = FileSystems.getDefault().getSeparator();
    	projectRoot = System.getProperty("user.dir") ;
    	labRecruitesExeRootDir = projectRoot + fileSeparator + "suts" ;
    	levelsDir = labRecruitesExeRootDir 
    			+ fileSeparator + "gym"
    			+ fileSeparator + "levels" ;
    	
    	TestSettings.USE_GRAPHICS = withGraphics ;
    	// TestSettings.USE_SERVER_FOR_TEST = false ;

    	// Launch the game (the game acts as a server, accepting commands from our side):
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { 
    	// closing the game, if it was launches by us:
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); 
    }
    
    // an instrumenter for get some values from a state to be later saved in a trace file
  	Pair<String, Number>[] instrumenter(BeliefState S) {
  		Pair<String, Number>[] out = new Pair[5];
  		out[0] = new Pair<String, Number>("time", S.worldmodel().timestamp);
  		out[1] = new Pair<String, Number>("x", S.worldmodel().position.x);
  		out[2] = new Pair<String, Number>("y", S.worldmodel().position.z);
  		out[3] = new Pair<String, Number>("score", S.worldmodel().score);
  		out[4] = new Pair<String, Number>("health", S.worldmodel().health);
  		return out;
  	}
  	
     // for producing graphs from a trace-file
  	void mkGraph(String python, String tracefile) {
  		try {
  			var py = new PythonCaller(python) ;
  			py.runPythonFile("./python/src/aplib/timegraph.py -i " + tracefile 
  					+ " -o tmp/lr_r8a_tgraph.png"
  					+ " health score") ;
  			py.runPythonFile("./python/src/aplib/heatmap.py -i " 
  					+ tracefile 
  					+ " -o tmp/lr_r8a_hmap.png"
  					+ " --width=100 --height=70 --maxval=100 --scale=0.5 "
  					+ "health") ;
  		}
  		catch(Exception e) {
  			System.out.println("### " + e.getMessage()) ;
  		}
  	}

    
	/**
	 * In this testing task/scenario we run the scenario:
	 * 
	 * <p>
	 * door3 ; door1 ; door0 ; door4 ; Finish
	 * 
	 * <p>
	 * We want to check that Finish is reachable in this scenario. After passing
	 * door4 the agent health should be between 20..50 and it should have at least
	 * 33 points. After touching Finish the health should be 100 and the point
	 * should be at least 533.
	 * 
	 * <p>
	 * We additionally add assertions to check, including some Linear Temporal Logic
	 * (LTL) assertions.
	 * 
	 * <p>
	 * You will notice that in order to 'script' the above testing task/scenario,
	 * the approach below requires that we also specify how to open each of the
	 * doors above, e.g. by specifying that a certain button needs to be interacted
	 * first to get a certain door open.
	 */
    @SuppressWarnings("unchecked")
	@Test
    public void scripted_testscenario1() throws InterruptedException, IOException {

        // Create an environment
    	var config = new LabRecruitsConfig("R8_fire3",levelsDir) ;
    	config.light_intensity = 0.3f ;
    	var environment = new LabRecruitsEnvironment(config);
    	//TestSettings.youCanRepositionWindow();

    	// create a test agent
        var testAgent = new LabRecruitsTestAgent("Elono") ;
        
        // define the testing-task:
        var G = SEQ(
        		GoalLib.entityInteracted("b0"),
        		GoalLib.entityStateRefreshed("door3"),
        		assertTrue_(testAgent,"","door3 is open",(BeliefState S) -> S.isOpen("door3")),
        		GoalLib.entityStateRefreshed("door1"),
        		assertTrue_(testAgent,"","door1 is open",(BeliefState S) -> S.isOpen("door1")),
        		GoalLib.entityInteracted("b9"),
        		GoalLib.entityStateRefreshed("door0"),
        		assertTrue_(testAgent,"","door0 is open",(BeliefState S) -> S.isOpen("door0")),
        		GoalLib.entityInteracted("b3"),
        		GoalLib.entityStateRefreshed("door4"),
        		assertTrue_(testAgent,"","door4 is open",(BeliefState S) -> S.isOpen("door4")),
        		assertTrue_(testAgent,"","health and score ok",(BeliefState S) -> 
        			S.worldmodel().health >= 20 && S.worldmodel().health <= 50
        			&& S.worldmodel().score >= 33),
        		GoalLib.atBGF ("Finish",0.3f,true),
        		assertTrue_(testAgent,"","health and score max",(BeliefState S) -> 
    				S.worldmodel().health == 100 && S.worldmodel().score >= 533)
        );
        
        // Let's also define some LTL properties to check:
        // The first one says that in this scenario, the agent's health won't drop below 1 :
        LTL<SimpleState> ltl1 = always((SimpleState S) -> ((BeliefState) S).worldmodel().health > 0) ;
        // The second one says the agent health should not drop low, at least until it sees door0:
        LTL<SimpleState> ltl2 = now((SimpleState S)    -> ((BeliefState) S).worldmodel().health >= 90)
        		                .until((SimpleState S) -> ((BeliefState) S).worldmodel().getElement("door0") != null) ;

        
        testAgent 
        	. attachState(new BeliefState())
        	. attachEnvironment(environment)
        	. setTestDataCollector(new TestDataCollector()) 
        	. setGoal(G) 
        	. addLTL(ltl1,ltl2) ;
        
        // optionally attach an instrumenter to save instrumented values to a trace-file;
		// we can later visualize the trace file:
        testAgent.withScalarInstrumenter(S -> instrumenter((BeliefState) S)) ;
        
        try {
	        //environment.startSimulation(); 
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(40);
	            i++ ;
	        	testAgent.update();
	        	if (i>1500) {
	        		break ;
	        	}
	        }
	        
	        // check that we have passed both tests above:
	        assertTrue(testAgent.success());
	        assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
	        assertTrue(testAgent.evaluateLTLs()) ;
	        // saving trace-file and produce graphs from it; you can find them under ./tmp dir:
	        testAgent.getTestDataCollector().saveTestAgentScalarsTraceAsCSV(testAgent.getId(),"tmp/lr_r8a_demo_trace.csv");
			mkGraph("/usr/local/bin/python3","tmp/lr_r8a_demo_trace.csv") ;
	        //new Scanner(System.in) . nextLine() ;
        }
        finally { environment.close(); }
    }
 
}
