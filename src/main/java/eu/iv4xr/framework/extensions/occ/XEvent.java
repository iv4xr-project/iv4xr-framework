package eu.iv4xr.framework.extensions.occ;

import eu.iv4xr.framework.extensions.occ.Event;
import nl.uu.cs.aplib.multiAgentSupport.Message;

/**
 * A wrapper over {@link nl.uu.cs.aplib.multiAgentSupport.Message} to make it
 * an instance of {@link eu.iv4xr.framework.extensions.occ.Event}.
 */
public class XEvent extends Event {

	public Message msg ;
	
	public XEvent(String name) { super(name)  ; }
	public XEvent(Message m) {
		super(m.getMsgName()) ;
		this.msg = m ; 
	}
	
}
