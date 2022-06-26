/***************************************************************************************************
 *
 * Copyright (c) 2021 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 - 2022 Open Universiteit - www.ou.nl
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.testar.protocols.iv4xr.SEProtocol;
import org.testar.protocols.iv4xr.iv4xrNavigableState;
import org.testar.protocols.iv4xr.iv4xrNavigableStateMap;
import org.testar.protocols.iv4xr.se.NavigableAreaSE;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.se.commands.seActionCommandMove;
import eu.testar.iv4xr.actions.se.goals.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import eu.testar.iv4xr.se.SeAgentTESTAR;
import nl.ou.testar.HtmlReporting.HtmlSequenceReport;
import spaceEngineers.model.Vec3F;
import uuspaceagent.DPos3;
import uuspaceagent.UUSeAgentState;

/**
 * iv4xr EU H2020 project - SpaceEngineers Use Case
 * 
 * In this protocol SpaceEngineers game will act as SUT.
 * 
 * se_commands_testar_teleport / test.setting file contains the:
 * - COMMAND_LINE definition to launch the SUT and the level
 * - SUT_PROCESS_NAME to connect with running SUT (and optionally launch a level)
 * - State model inference settings to connect and create the State Model inside OrientDB
 * 
 * TESTAR is the Agent itself, derives is own knowledge about the observed entities,
 * and takes decisions about the command actions to execute (move, rotate, interact)
 * 
 * Widget              -> Virtual Entity (Blocks)
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> SpaceEngineers low level command
 */
public class Protocol_se_commands_testar_navigate extends SEProtocol {

	// Memory map to calculate explored and unexplored 3D positions
	private NavigableAreaSE navigableAreaSE;

	// Used as helper classes for State Model Navigable State
	private iv4xrNavigableState navigableState = new iv4xrNavigableState("");

	private static Set<String> interestingEntities;
	static {
		interestingEntities = new HashSet<String>();
		interestingEntities.add("LargeBlockSmallGenerator");
		interestingEntities.add("LargeBlockBatteryBlock");
		interestingEntities.add("ButtonPanelLarge");
	}

	@Override
	protected SUT startSystem() throws SystemStartException {
		navigableAreaSE = new NavigableAreaSE();
		return super.startSystem();
	}

	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);
		if(this.mode != Modes.Generate) return;

		// We assume that the initial agent position is a navigable position
		Vec3 initialPosition = SVec3.seToLab(state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));
		System.out.println("Adding initial position: " + initialPosition);
		navigableAreaSE.addInitialPosition(initialPosition);
	}

	@Override
	protected State getState(SUT system) throws StateBuildException {
		State state = super.getState(system);
		System.out.println("AGENT pos: " + state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));

		return state;
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific Space Engineers state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();
		if(this.mode != Modes.Generate) return super.deriveActions(system, state);

		// Update Navigable State entities and navMesh positions information
		for(Widget w : state) {
			// For the interesting block entities calculate if they are reachable
			if(interestingEntities.contains(w.get(IV4XRtags.entityType))) {
				if(calculateReachablePosToEntity(system, w)) {
					// If an entity is reachable, think is better to just use one entity to interact with it
					// Maintain a list of multiple reachable entities may be problematic
					// because the reachability to interact with them may change, 
					// no available path from other part of the level
					// or a maintained previous entity just disappeared
					System.out.println("----> NEXT Reachable Entity: " + navigableAreaSE.getNextEntityDescription());

					// Add the visible entity information to the State Model helper class
					navigableState.addReachableEntity(navigableAreaSE.getNextEntityDescription(), true);

					Widget widgetReachable = navigableAreaSE.getNextEntity();
					labActions.add(new seActionNavigateGrinderBlock(widgetReachable, widgetReachable.get(IV4XRtags.reachablePosition), system, agentId, 4, 1.0));
					return labActions;
				}
			}
		}

		// Create free exploration movements if a path is available
		calculateExploratoryPositions(system, state);
		// And if exploratory reachable position is available derive smart explore action
		//Vec3 nextExploratoryPosition = navigableAreaSE.getNextRandomPosition();
		Vec3 nextExploratoryPosition = navigableAreaSE.getNextFarUnexploredPosition(state);
		if(nextExploratoryPosition != null) {
			labActions.add(new seActionExplorePosition(state, nextExploratoryPosition, system, agentId));
			return labActions;
		}

		// Finally if is not possible to navigate to an entity or smart exploring a position
		// prepare a dummy exploration
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, 1f), 30)); // Move to back
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, -1f), 30)); // Move to front
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(1f, 0, 0), 30)); // Move to Right
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(-1f, 0, 0), 30)); // Move to Left

		return labActions;
	}

	@Override
	protected boolean executeAction(SUT system, State state, Action action) {
		boolean executed = super.executeAction(system, state, action);
		// Reset internal list for next iteration
		navigableAreaSE.resetNextEntity();
		navigableAreaSE.resetNextPosition();

		// Print information
		navigableAreaSE.printExploredPositionsInfo();
		navigableAreaSE.printUnexploredPositionsInfo();
		System.out.println("Reachable Entities: " + navigableAreaSE.getInteractedEntitiesDescription());

		//Add the discovered navigable positions (explored and unexplored) to the State Model helper class 
		navigableState.addNavigableNode(navigableAreaSE.discoveredReachablePositions());

		return executed;
	}

	@Override
	protected void finishSequence() {
		navigableAreaSE.printExploredPositionsInfo();
		navigableAreaSE.printUnexploredPositionsInfo();
		System.out.println("Reachable Entities: " + navigableAreaSE.getInteractedEntitiesDescription());

		super.finishSequence();

		// Create last Navigable State in the State Model
		stateModelManager.notifyNewNavigableState(navigableState.getNavigableNodes(),
				navigableAreaSE.discoveredUnexploredPositions(),
				navigableState.getReachableEntities(), 
				"NotExecutedAction",
				"Initial");
	}

	private boolean calculateReachablePosToEntity(SUT system, Widget w) {
		// If the current observed state contains a block entity that is not saved as reachable yet
		if(!navigableAreaSE.alreadyInteractedEntity(w)) {
			// Calculate if we can reach them by using the pathfinding algorithm
			SeAgentTESTAR testAgent = (SeAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
			UUSeAgentState stateGrid = testAgent.getStateGrid();
			stateGrid.navgrid.enableFlying = false;
			stateGrid.updateState(agentId);

			Vec3 destination = w.get(IV4XRtags.entityPosition);

			var sqAgent = stateGrid.navgrid.gridProjectedLocation(stateGrid.wom.position);
			var sqDestination = stateGrid.navgrid.gridProjectedLocation(destination);
			List<DPos3> path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

			if (path == null) {
				// the pathfinder cannot find a path
				System.out.println("### NO path to " + destination);

				// 3D adjacent positions
				//[{-3,-3,-3}{-3,-3,-2}..{0,-1,0}{0,0,-1}{0,0,0},{0,0,1},{0,1,0}..{3,3,2}{3,3,3}]
				List<Vec3> recalculate_adjacent = new ArrayList();
				for(int i = 0; i < 4; i++) {
					for(int j = 0; j < 4; j++) {
						for(int k = 0; k < 4; k++) {
							recalculate_adjacent.add(new Vec3(i, j, k));
							recalculate_adjacent.add(new Vec3(i*-1, j*-1, k*-1));
						}
					}
				}
				System.out.println("Try to recalculate destination with adjacent list: " + Arrays.toString(recalculate_adjacent.toArray()));

				for(Vec3 adjacentDestination : recalculate_adjacent) {
					Vec3 recalculatedDestination = Vec3.add(destination, adjacentDestination);

					sqAgent = stateGrid.navgrid.gridProjectedLocation(stateGrid.wom.position);
					sqDestination = stateGrid.navgrid.gridProjectedLocation(recalculatedDestination);
					path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

					if(path != null) {
						System.out.println("!!! Path recalculated to " + recalculatedDestination);
						// If we finally found a path to the entity, using an adjacent position, ass as reachable entity + position
						w.set(IV4XRtags.reachablePosition, recalculatedDestination);
						navigableAreaSE.addNewReachableEntity(w);
						return true;
					} else {
						System.out.println("### NO modified path to " + recalculatedDestination);
					}
				}
			} else {
				// Path was not null in the beginning, so we have to add the default destination as reachable entity + position
				w.set(IV4XRtags.reachablePosition, destination);
				navigableAreaSE.addNewReachableEntity(w);
				return true;
			}
		}

		return false;
	}

	/**
	 * SE - Platform
	 * X and Z axes are the 2D to calculate the navigation movements. 
	 * Calculate a navigable area of nodes by adding coordinates to the current agent position. 
	 * Avoid the use of concrete decimals because this will potentially create always new nodes. 
	 * 
	 * @param system
	 * @param state
	 */
	private void calculateExploratoryPositions(SUT system, State state) {
		// Calculate if we can reach a near node position by using the pathfinding algorithm
		SeAgentTESTAR testAgent = (SeAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		UUSeAgentState stateGrid = testAgent.getStateGrid();
		stateGrid.navgrid.enableFlying = false;
		stateGrid.updateState(agentId);

		var sqAgent = stateGrid.navgrid.gridProjectedLocation(stateGrid.wom.position);

		// Circular positions relative to the agent center
		// https://stackoverflow.com/a/5301049
		Vec3 agentCenter = SVec3.seToLab(state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));

		// 1 block distance positions (near)
		// For near positions calculate 8 positions in circle
		int points = 8;
		double slice = 2 * Math.PI / points;
		double radius = 2.5;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a valid navigable node
			Vec3 destination = new Vec3(newX, agentCenter.y, newZ);

			var sqDestination = stateGrid.navgrid.gridProjectedLocation(destination);
			List<DPos3> path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

			// If the path is not null the free exploration position is added as a valid navigable node
			if (path != null){
				System.out.println("NEAR exploratory pos: " + destination);
				navigableAreaSE.updateExploredSpace(destination);
			}
		}

		// 2 block distance positions (medium)
		// For medium positions calculate 16 positions in circle
		points = 16;
		slice = 2 * Math.PI / points;
		radius = 5.0;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a valid navigable node
			Vec3 destination = new Vec3(newX, agentCenter.y, newZ);

			var sqDestination = stateGrid.navgrid.gridProjectedLocation(destination);
			List<DPos3> path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

			// If the path is not null the free exploration position is added as a valid navigable node
			if (path != null){
				System.out.println("MEDIUM exploratory pos: " + destination);
				navigableAreaSE.updateExploredSpace(destination);
			}
		}

		// 3 block distance positions (far)
		// For far positions calculate 16 positions in circle
		points = 16;
		slice = 2 * Math.PI / points;
		radius = 7.5;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a valid navigable node
			Vec3 destination = new Vec3(newX, agentCenter.y, newZ);

			var sqDestination = stateGrid.navgrid.gridProjectedLocation(destination);
			List<DPos3> path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

			// If the path is not null the free exploration position is added as a valid navigable node
			if (path != null){
				System.out.println("FAR exploratory pos: " + destination);
				navigableAreaSE.updateExploredSpace(destination);
			}
		}
	}
}
