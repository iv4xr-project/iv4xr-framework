/**
 * 
 */
package eu.iv4xr.framework.testfactory.mbt;

import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.Main;
import eu.fbk.iv4xr.mbt.strategy.CoverageTracker;
import eu.fbk.iv4xr.mbt.strategy.GenerationStrategy;
import eu.fbk.iv4xr.mbt.strategy.SearchBasedStrategy;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;

/**
 * @author kifetew
 *
 */
public class MBTMain extends Main {

	public SuiteChromosome runTestGeneration () {

		// determine the test generation strategy
		GenerationStrategy generationStrategy = new SearchBasedStrategy<MBTChromosome>();
//		if (line.hasOption("random")) {
//			generationStrategy = new RandomTestStrategy<MBTChromosome>();
//		}
//		
//		if (line.hasOption("planning")) {
//			generationStrategy = new PlanningBasedStrategy<MBTChromosome>();
//		}
//		
//		// set parameters in MBTProperties and Properties
//		setGlobalProperties (line);


		MBTProperties.SessionId = "" + System.currentTimeMillis();

		SuiteChromosome solution = generationStrategy.generateTests();

		CoverageTracker coverageTracker = generationStrategy.getCoverageTracker();

//		if (!line.hasOption("silent_mode")) {
//			// write tests to disk
//			writeTests (solution, coverageTracker.getCoverageMap());
//		}
//		
//		// write model on disk
//		writeModel(line);
//		

//		// write statistics to disk
//		writeStatistics (coverageTracker.getStatistics(), coverageTracker.getStatisticsHeader(),MBTProperties.STATISTICS_FILE());
//		logger.info(coverageTracker.getStatistics());
//		
		// print final coverage
		System.out.println();
		System.out.println("Final coverage: " + coverageTracker.getCoverage()*100 + "%");

//		// if enabled, print uncovered goals
//		if (org.evosuite.Properties.PRINT_MISSED_GOALS) {
//			List<FitnessFunction<MBTChromosome>> uncoveredGoals = coverageTracker.getUncoveredGoals();
//			printUncoveredGoals(uncoveredGoals);
//		}

		return solution;
	}
}
