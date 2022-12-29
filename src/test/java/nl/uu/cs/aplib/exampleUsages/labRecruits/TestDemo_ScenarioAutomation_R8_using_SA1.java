package nl.uu.cs.aplib.exampleUsages.labRecruits;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
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

/**
 * In this demo, the testing task to do is to verify that the level's end-goal
 * is reachable. The end-goal is represented by a flag that will give the player
 * lots of point when touched. Internally, the flag is identified with ID
 * "Finish".
 * 
 * <p>We want to do the test differently than in {@link TestDemo_ScriptedScenario_BD}:
 * 
 * <ul>
 *     <li> We will run a different scenario/path to get to Finish.
 *     <li> We will use a simple search algorithm called SA1 to automate opening doors 
 *     (at least, some doors), so that we do not have to explicitly specify which 
 *     buttons to toggle to open a particular door. SA1 works well if the setup is not
 *     too complicated, e.g. if we have multi-connections where the correct button is
 *     further from the target door.
 * </ul>
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
public class TestDemo_ScenarioAutomation_R8_using_SA1 {

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
   					+ " -o tmp/lr_r8b_tgraph.png"
   					+ " health score") ;
   			py.runPythonFile("./python/src/aplib/heatmap.py -i " 
   					+ tracefile 
   					+ " -o tmp/lr_r8b_hmap.png"
   					+ " --width=100 --height=70 --maxval=100 --scale=0.5 "
   					+ "health") ;
   		}
   		catch(Exception e) {
   			System.out.println("### " + e.getMessage()) ;
   		}
   	}
    
    
    /**
     * A goal that will just explore the world for some time-budget. The goal itself
     * will never be achieved; we just use it to provide a bogus goal to trigger 
     * exploration work.
]    */
    GoalStructure exploreG(int budget) {
    	GoalStructure G = goal("exploring")
    			.toSolve((BeliefState S) -> false)
    			.withTactic(
    				FIRSTof(
    				  TacticLib.explore(),
    				  ABORT()))
    			.lift()
    			.maxbudget(budget)
    			 ;
    	return G  ;
    }
    
    /**
     * In this testing task/scenario we run the scenario:
     * 
     * <p>  door3 ; door10 ; door9 ; [door6] ; Finish 
     * 
     * <p> We want to check that Finish is reachable in this scenario. After passing door4
     * the agent health should be between 20..50 and it should have at least 34 points. 
     * After touching Finish the health should be 100 and the point should be at least 524.
     * 
     * <p>Unlike the previous way of 'scripting' a test scenario, this time we
     * use a simple search algorithm called SA1 to look for a button that can open
     * a given target door. The algorithm works if the setup is not too complicated (in
     * particular multi-connections make things complicated for SA1). Using this we
     * can avoid explicitly scripting how to open some doors.
     */
    @Test
    public void scenario2() throws InterruptedException, IOException {

        // Create an environment
    	var config = new LabRecruitsConfig("R8_fire3",levelsDir) ;
    	config.light_intensity = 0.3f ;
    	//config.agent_speed = 0.2f ;
    	//config.view_distance = 6 ;
    	var environment = new LabRecruitsEnvironment(config);
    	//TestSettings.youCanRepositionWindow();
    	
    	var testAgent = new LabRecruitsTestAgent("Elono")  ;
    	
    	// configuring SA1-solver:		
    	var sa1Solver = new Sa1Solver<Integer>(
    			(S, e) -> {
    				var B = (BeliefState) S ;
    				var f = (LabEntity) e ;
    				var path = B.pathfinder().findPath(B.worldmodel().getFloorPosition(), f.getFloorPosition(), BeliefState.DIST_TO_FACE_THRESHOLD) ;
    				return path != null ;
    			},
    			(S, e) -> Vec3.distSq(S.worldmodel().position, e.position),
    			S -> (e1, e2) -> Vec3.distSq(e1.position,e2.position),
    			eId -> GoalLib.entityInteracted(eId),
    			eId -> GoalLib.entityStateRefreshed(eId),
    			S -> {
    				var B = (BeliefState) S ;
    				var path = B.pathfinder().explore(B.worldmodel().getFloorPosition(), BeliefState.DIST_TO_FACE_THRESHOLD) ;
    			   return path == null;
    			},
    			budget -> exploreG(budget)
    			);

    	
    	// Configure the SA1-solver for tasks to open a door:
    	int explorationBudget = 20;
    	Function <String,GoalStructure> openWithSA1 = 
    		doorToOpen -> 
    			sa1Solver.solver(
    					testAgent, 
    					doorToOpen, 
    					e -> e.type.equals(LabEntity.SWITCH), 
    					S -> ((BeliefState) S) . isOpen(doorToOpen) , 
    					Policy.NEAREST_TO_TARGET, 
    					explorationBudget);
    	
    	// define the testing-task; notice we use the SA1-solver
        var G = SEQ(
        		openWithSA1.apply("door3"),
        		assertTrue_(testAgent,"","door3 is open",(BeliefState S) -> S.isOpen("door3")),
        		GoalLib.entityInteracted("b9"),
        		openWithSA1.apply("door10"),
        		assertTrue_(testAgent,"","door10 is open",(BeliefState S) -> S.isOpen("door10")),
        		openWithSA1.apply("door9"),
        		assertTrue_(testAgent,"","door9 is open",(BeliefState S) -> S.isOpen("door9")),
        		GoalLib.atBGF("gf1",0.2f,true),
        		GoalLib.atBGF("Finish",0.2f,true),
        		assertTrue_(testAgent,"","health and score max",(BeliefState S) -> 
					S.worldmodel().health == 100 && S.worldmodel().score >= 633)
        		
        );
        
        testAgent 
        	. attachState(new BeliefState())
        	. attachEnvironment(environment)
        	. setTestDataCollector(new TestDataCollector()) 
        	. setGoal(G) ;
        
        // optionally attach an instrumenter to save instrumented values to a trace-file;
     	// we can later visualize the trace file:
        testAgent.withScalarInstrumenter(S -> instrumenter((BeliefState) S)) ;
        
        try {
        	
	        //environment.startSimulation(); 
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>1500) {
	        		break ;
	        	}
	        }
	        
	        // check that we have passed both tests above:
	        assertTrue(testAgent.success());
	        assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
	        // saving trace-file and produce graphs from it; you can find them under ./tmp dir:
	        testAgent.getTestDataCollector().saveTestAgentScalarsTraceAsCSV(testAgent.getId(),"tmp/lr_r8b_demo_trace.csv");
			mkGraph("/usr/local/bin/python3","tmp/lr_r8b_demo_trace.csv") ;
	        //testAgent.printStatus();
	        //new Scanner(System.in) . nextLine() ;
        }
        finally { environment.close(); }
    }
    
 
}
