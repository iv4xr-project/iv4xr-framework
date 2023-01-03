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

import java.util.HashSet;
import java.util.Set;
import org.fruit.alayer.*;
import org.fruit.monkey.Settings;
import org.testar.protocols.iv4xr.SEProtocol;

import eu.testar.iv4xr.actions.se.commands.*;
import eu.testar.iv4xr.actions.se.goals.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import nl.ou.testar.RandomActionSelector;
import spaceEngineers.model.Vec3F;

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
public class Protocol_se_testar_navigate_survival extends SEProtocol {

	/*
	 * Modify agent ObservationRadius in the file: 
	 * C:\Users\<user>\AppData\Roaming\SpaceEngineers\ivxr-plugin.config
	 */

	private static Set<String> toolEntities;
	static {
		toolEntities = new HashSet<String>();
		toolEntities.add("LargeBlockBatteryBlock");
		toolEntities.add("SurvivalKitLarge");
		toolEntities.add("LargeHydrogenEngine");
	}

	private static Set<String> fragileEntities;
	static {
		fragileEntities = new HashSet<String>();
		fragileEntities.add("LargeBlockSmallGenerator");
		fragileEntities.add("ConveyorTubeCurved");
		fragileEntities.add("LargeBlockSmallContainer");
	}

	private static Set<String> interactiveEnergyEntities;
	static {
		interactiveEnergyEntities = new HashSet<String>();
		interactiveEnergyEntities.add("LargeBlockCockpit");
		interactiveEnergyEntities.add("LargeBlockCockpitSeat");
		interactiveEnergyEntities.add("CockpitOpen");
		interactiveEnergyEntities.add("OpenCockpitLarge");
		interactiveEnergyEntities.add("LargeBlockCryoChamber");
	}

	/**
	 * Called once during the life time of TESTAR
	 * This method can be used to perform initial setup work
	 * @param settings the current TESTAR settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		super.initialize(settings);
		// The SE level that TESTAR is going to explore
		SE_LEVEL_PATH = "suts/se_levels/manual-world-survival";
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// Apply an Oracle to check if Grinder action worked properly
		if(lastExecutedAction != null && lastExecutedAction instanceof seActionNavigateGrinderBlock) {
			// Check the block attached to the previous executed grinder action
			Widget previousBlock = ((seActionNavigateGrinderBlock)lastExecutedAction).get(Tags.OriginWidget);
			Float previousIntegrity = previousBlock.get(IV4XRtags.seIntegrity);
			System.out.println("Previous Block Integrity: " + previousIntegrity);
			// Try to find the same block in the current state using the block id
			for(Widget w : state) {
				if(w.get(IV4XRtags.entityId).equals(previousBlock.get(IV4XRtags.entityId))) {
					Float currentIntegrity = w.get(IV4XRtags.seIntegrity);
					System.out.println("Current Block Integrity: " + currentIntegrity);
					// If previous integrity is the same or increased, something went wrong
					if(currentIntegrity >= previousIntegrity) {
						String blockType = w.get(IV4XRtags.entityType);
						functional_verdict = new Verdict(Verdict.BLOCK_INTEGRITY_ERROR, "The integrity of interacted block " + blockType + " didn't decrease after a Grinder action");
					}
				}
			}
			// If the previous block does not exist in the current state, it has been destroyed after the grinder action
			// We consider this OK by default, but more sophisticated oracles can be applied here
		}

		// Apply an Oracle to check if shooting action worked properly
		if(lastExecutedAction != null && lastExecutedAction instanceof seActionNavigateShootBlock) {
			// Check the block attached to the previous executed shooting action
			Widget previousBlock = ((seActionNavigateShootBlock)lastExecutedAction).get(Tags.OriginWidget);
			Float previousIntegrity = previousBlock.get(IV4XRtags.seIntegrity);
			System.out.println("Previous Block Integrity: " + previousIntegrity);
			// Try to find the same block in the current state using the block id
			for(Widget w : state) {
				if(w.get(IV4XRtags.entityId).equals(previousBlock.get(IV4XRtags.entityId))) {
					Float currentIntegrity = w.get(IV4XRtags.seIntegrity);
					System.out.println("Current Block Integrity: " + currentIntegrity);
					// If previous integrity is the same or increased, something went wrong
					if(currentIntegrity >= previousIntegrity) {
						String blockType = w.get(IV4XRtags.entityType);
						functional_verdict = new Verdict(Verdict.BLOCK_INTEGRITY_ERROR, "The integrity of interacted block " + blockType + " didn't decrease after a shooting action");
					}
				}
			}
			// If the previous block does not exist in the current state, it has been destroyed after the shooting action
			// We consider this OK by default, but more sophisticated oracles can be applied here
		}

		// Goal Actions have an oracle associated
		// Here we check the agent properties (energy, health, oxygen, hydrogen, jetpack) and triggeredBlockConstruction oracles
		if(lastExecutedAction != null && lastExecutedAction instanceof seActionGoal) {
			functional_verdict = ((seActionGoal)lastExecutedAction).getActionVerdict();
		}

		return super.getVerdict(state).join(functional_verdict);
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific Space Engineers state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// For each block widget (see movementEntities types), rotate and move until the agent is close to the position of the block
		for(Widget w : state) {
			if(toolEntities.contains(w.get(IV4XRtags.entityType)) && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				// Always Grinder and shoot by default
				labActions.add(new seActionNavigateGrinderBlock(w, system, agentId, 1, 1.0));
				labActions.add(new seActionNavigateShootBlock(w, system, agentId));
				// But only welder if the integrity is not the maximum
				if(w.get(IV4XRtags.seIntegrity) < w.get(IV4XRtags.seMaxIntegrity)) {
					labActions.add(new seActionNavigateWelderBlock(w, system, agentId, 1, 1.0));
				}
			}

			if(fragileEntities.contains(w.get(IV4XRtags.entityType)) && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				// Always Grinder and shoot by default
				labActions.add(new seActionNavigateShootBlock(w, system, agentId));
				// But only welder if the integrity is not the maximum
				if(w.get(IV4XRtags.seIntegrity) < w.get(IV4XRtags.seMaxIntegrity)) {
					labActions.add(new seActionNavigateWelderBlock(w, system, agentId, 1, 1.0));
				}
			}

			// FIXME: Fix Ladder2 is not observed as entityType
			if(w.get(IV4XRtags.seDefinitionId, "").contains("Ladder2") && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				labActions.add(new seActionNavigateInteract(w, system, agentId));
			}

			// Some interactive entities allow the agent to rest inside and charge the energy
			if(interactiveEnergyEntities.contains(w.get(IV4XRtags.entityType)) && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				labActions.add(new seActionNavigateRechargeEnergy(w, system, agentId));
			}

			// If a Medical Room exists in the level, the agent can use the panel to charge the health and energy
			// FIXME: Navigate near to medical room is not completely functional yet
			if(w.get(IV4XRtags.entityType, "").contains("MedicalRoom") && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				labActions.add(new seActionNavigateRechargeHealth(w, system, agentId));
			}
		}

		// If the agent has a reachable position in front of him, trigger a place block action
		Vec3F agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition);
		Vec3F frontPosition = new Vec3F((agentPosition.getX() - 2.5f), agentPosition.getY(), agentPosition.getZ());
		if(sePositionRotationHelper.calculateIfPositionIsReachable(system, frontPosition)) {
			labActions.add(new seActionTriggerBlockConstruction(state, system, agentId, "LargeHeavyBlockArmorBlock"));
		}

		// Now add the set of actions to explore level positions
		labActions = sePositionRotationHelper.calculateExploratoryNodeMap(system, state, agentId, labActions, 7f);

		// If it was not possible to navigate to an entity or realize a smart exploration
		// prepare a dummy exploration
		if(labActions.isEmpty()) {
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, 1f), 30)); // Move to back
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, -1f), 30)); // Move to front
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(1f, 0, 0), 30)); // Move to Right
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(-1f, 0, 0), 30)); // Move to Left
		}

		return labActions;
	}

	/**
	 * Select one of the available actions using an action selection algorithm (for example random action selection)
	 *
	 * @param state the SUT's current state
	 * @param actions the set of derived actions
	 * @return  the selected action (non-null!)
	 */
	@Override
	protected Action selectAction(State state, Set<Action> actions){

		//Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, actions);
		if (retAction == null) {
			//if no preSelected actions are needed, then implement your own action selection strategy
			//using the action selector of the state model:
			retAction = stateModelManager.getAbstractActionToExecute(actions);
		}
		if(retAction == null) {
			// Invoke the SE action selector to prioritize interactive actions
			retAction = actionSelectorSE.prioritizedAction(state, actions);
		}
		if(retAction == null) {
			System.out.println("State model and prioritized based action selection did not find an action. Using default action selection.");
			// if state model and prioritize interaction fails, use default:
			retAction = RandomActionSelector.selectAction(actions);
		}
		return retAction;
	}

	/**
	 * Execute the selected action.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @param action the action to execute
	 * @return whether or not the execution succeeded
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		boolean actionExecuted = super.executeAction(system, state, action);
		if(actionExecuted) actionSelectorSE.addExecutedAction(action);
		return actionExecuted;
	}

}