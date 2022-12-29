/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package nl.uu.cs.aplib.exampleUsages.labRecruits;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.pad.PythonCaller;

import static eu.iv4xr.framework.extensions.ltl.LTL.* ;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import static org.junit.jupiter.api.Assertions.* ;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Scanner;

import org.junit.jupiter.api.*;

import game.LabRecruitsTestServer;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.DemoUtils;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.utils.Pair;
import world.BeliefState;

import static eu.iv4xr.framework.Iv4xrEDSL.assertTrue_;
import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * In this demo we use an iv4xr agent to test a Lab Recruits level. The testing
 * task to do is to verify that the level's end-goal is reachable. The end-goal
 * is represented by a flag that will give the player lots of point when touched.
 * Internally, the flag is identified with ID "Finish". 
 * 
 * <p>Moreover we want to verify that
 * when the agent reach this Finish, it should have at least 533 score and its
 * health is restored to 100.
 * 
 * <p>The access to Finish is guarded by a door. So showing that Finish is reachable
 * amounts to getting its guarding door open, which is not so trivial as it involves
 * toggling a button in another room which in turn is also guarded by a door. In short,
 * there is a small puzzle that has to be solved first. The raw search space over
 * a Lab Recruit game is way to big for a test agent to try to brute-force solving this
 * puzzle, so in the solution below we provide a guidance in the form of a scripted
 * steps how to open the needed doors. Fortunately, this can be scripted at a high level.
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
public class TestDemo_ScriptedScenario_BD {

    static private LabRecruitsTestServer labRecruitsTestServer;
    
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
    	
    	// TestSettings.USE_SERVER_FOR_TEST = false ;
    	TestSettings.USE_GRAPHICS = withGraphics ;

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
 					+ " -o tmp/lr_bd_tgraph.png"
 					+ " health score") ;
 			py.runPythonFile("./python/src/aplib/heatmap.py -i " 
 					+ tracefile 
 					+ " -o tmp/lr_bd_hmap.png"
 					+ " --width=17 --height=8 --maxval=100 --scale=2 "
 					+ "health") ;
 		}
 		catch(Exception e) {
 			System.out.println("### " + e.getMessage()) ;
 		}
 	}

    /**
     * The test below formulates the following high level scenario:
     * 
     * <p>    button1; door1; button3; door2; button4; door1; door3; Finish
     *     
     * <p>Each button in the scenario is to be toggled/interacted. If the scenario
     * succeeds then we know that Finish is reachable. We additionally add assertions
     * to check, including some Linear Temporal Logic (LTL) assertions.
     */
    @SuppressWarnings("unchecked")
	@Test
    public void test_FinishIsReachable() throws InterruptedException, IOException {


        // Create an environment
    	var config = new LabRecruitsConfig("buttons_doors_2",levelsDir) ;
    	config.light_intensity = 0.3f ;
    	var environment = new LabRecruitsEnvironment(config);
  
    	//TestSettings.youCanRepositionWindow() ;

        try {
        	

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") ; // should match the ID in the level-def file

	        // define the testing-task:
	        var testingTask = SEQ(
	            GoalLib.entityInteracted("button1"),
                GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityInteracted("button3"),
	        	GoalLib.entityStateRefreshed("door2"),
	        	GoalLib.entityInteracted("button4"),
	        	GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityStateRefreshed("door3"),
	        	GoalLib.atBGF("Finish",0.3f,true),
	        	assertTrue_(testAgent,"","point ok",
	        		(BeliefState S) -> S.worldmodel().score >= 533 && S.worldmodel().health == 100) 
	        );
	        
	        // Let's also define some LTL properties to check:
	        // The first one says that in this scenario, the agent's health won't drop below 80:
	        LTL<SimpleState> ltl1 = always((SimpleState S) -> ((BeliefState) S).worldmodel().health >= 80) ;
	        // The second one says that door-3 won't open until button3 is hit
	        LTL<SimpleState> ltl2 = now((SimpleState S)    -> !((BeliefState) S).isOpen("door3"))
	        		                .until((SimpleState S) -> ((BeliefState) S).isOn("button3")) ;
	        
	        
	        testAgent 
	        	. attachState(new BeliefState())
	        	. attachEnvironment(environment)
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(testingTask) 
	        	. addLTL(ltl1,ltl2) ;

	        // optionally attach an instrumenter to save instrumented values to a trace-file;
			// we can later visualize the trace file:
	        testAgent.withScalarInstrumenter(S -> instrumenter((BeliefState) S)) ;
			
	        int i = 0 ;
	        // keep updating the agent
	        while (testingTask.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>200) {
	        		break ;
	        	}
	        }	        
	        // check that the testing task is successfully completed and 
	        // that we have no assert-violation:
	        assertTrue(testingTask.getStatus().success());
	        assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
	        assertTrue(testAgent.evaluateLTLs()) ;
	        
	        // saving trace-file and produce graphs from it; you can find them under ./tmp dir:
	        testAgent.getTestDataCollector().saveTestAgentScalarsTraceAsCSV(testAgent.getId(),"tmp/lr_bd_demo_trace.csv");
			mkGraph("/usr/local/bin/python3","tmp/lr_bd_demo_trace.csv") ;
	        //new Scanner(System.in) . nextLine() ;
        }
        finally { environment.close(); }
    }
}
