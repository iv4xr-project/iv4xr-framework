package nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent;

import eu.iv4xr.framework.extensions.occ.Event;

/**
 * Just subclass of OCC-Event, that can also hold the id of the target of
 * the event.
 */
public class MiniDungeonOCCEvent extends Event {

	String targetEntityId = null ;
	
	public MiniDungeonOCCEvent(String name) { super(name)  ; }
	
	public MiniDungeonOCCEvent(String name, String targetEntityId) { 
		super(name)  ; 
		this.targetEntityId = targetEntityId ;
	}
	
	
}
