package eu.iv4xr.framework.extensions.occ;

public abstract class XUserCharacterization extends UserCharacterization {
	
	public void eventEffect(Event e, BeliefBase beliefbase) {
		eventEffect((XEvent) e, beliefbase) ;
	}
	
    /**
     * This method should model the semantic of an Event in terms of an update to
     * the given belief-base.
     */
	public abstract void eventEffect(XEvent e, BeliefBase beliefbase);

}
