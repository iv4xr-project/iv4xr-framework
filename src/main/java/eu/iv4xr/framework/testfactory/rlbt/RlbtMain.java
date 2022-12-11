/**
 * 
 */
package eu.iv4xr.framework.testfactory.rlbt;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import eu.fbk.iv4xr.rlbt.RlbtMultiAgentMain;
import eu.fbk.iv4xr.rlbt.labrecruits.RLActionToTestCaseEncoder;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

/**
 * @author kifetew
 *
 */
public class RlbtMain {

	List<GoalStructure> goals;
	Iterator<GoalStructure> goalIterator;
	/**
	 * 
	 */
	public RlbtMain() {
		boolean success = runRlbt();
	}

	public boolean runRlbt(){
		String[] args = {
//				"-multiagentTrainingMode",
//				"-sutConfig", "assets/rlbt/configurations/lrLevelMultiAgent.config",
				"-trainingMode", 
				"-sutConfig", "assets/rlbt/configurations/lrLevelSingleAgent.config", 
				"-burlapConfig", "assets/rlbt/configurations/burlap_train.config"};
		try {
			RlbtMultiAgentMain.main(args );
			return true;
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private File getLastActionsFile() {
		String rlbtFilesPath = "rlbt-files/results/";
		File startingDir = new File(rlbtFilesPath );
		
	    FileFilter fileFilter = new WildcardFileFilter("*");
	    File[] files = startingDir.listFiles(fileFilter);
		
	    if (files.length > 0) {
	        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
	        return new File (files[0].getAbsoluteFile() + File.separator + "actions.csv");
	    }else {
	    	return null;
	    }
	}

	public void generateGoals(TestAgent agent) {
		File actionsFile = getLastActionsFile ();
		if (actionsFile != null) {
			String actions;
			try {
				actions = FileUtils.readFileToString(actionsFile, Charset.defaultCharset());
				goals = RLActionToTestCaseEncoder.serializeActionsToGoals(agent, actions);
				goalIterator = goals.iterator();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public GoalStructure nextGoal() {
		if (goalIterator != null 
				&& goalIterator.hasNext()) {
			return goalIterator.next();
		}else {
			return null;
		}
	}
	
}
