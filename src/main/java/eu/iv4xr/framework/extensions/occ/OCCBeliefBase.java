package eu.iv4xr.framework.extensions.occ;

import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import nl.uu.cs.aplib.mainConcepts.SimpleState;

/**
 * A class to hold OCC goals and their statuses, and also a reference
 * to an agent's functional state.
 */
public class OCCBeliefBase implements BeliefBase  {

	Goals_Status goals_status = new Goals_Status() ;
	public SimpleState functionalstate ;
	
	public OCCBeliefBase() { }
	
	public OCCBeliefBase attachFunctionalState(SimpleState functionalstate) {
		this.functionalstate = functionalstate ;
		return this ;
	}

	@Override
	public Goals_Status getGoalsStatus() { return goals_status ;  }

}
