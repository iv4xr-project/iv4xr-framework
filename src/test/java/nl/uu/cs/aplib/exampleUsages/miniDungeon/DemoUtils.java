package nl.uu.cs.aplib.exampleUsages.miniDungeon;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;

public class DemoUtils {

	/**
	 * Just a utility function to get the WorldEntity representing a player-character
	 * (avatar) state. This is to be distinguished with the "MyAgentState" that despite
	 * the name actually represents the world-state.
	 */
	public static WorldEntity avatarState(MyAgentState S) {
		return S.worldmodel.elements.get(S.worldmodel.agentId) ;
	}

}
