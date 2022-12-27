import random
from xmlrpc.client import Boolean
import gym
import numpy as np
from typing import Any, Callable, Dict, List

class Qlearning:
    '''
    An implementation of Q-learning, as a simple example of an RL-algorithm
    to try on Java GymEnv. The algorithm maintains a Q-table, such that
    for a state s and action a, Q(s,a) represents the value of executing the
    action a on the state s.

    We do not literally represent states and actions by e.g. names or some
    complex data-type. Rather they are just indices.

    The algorithm assumes finite number of states (NS) and actions (NA). 
    A state is identified by an id in [0..NS), and similarly an action
    is an id in [0..NA). The Q-table is represented by self.qtable, which
    is a 2D array, such that to know the value of Q[s][a] we first need
    to translate s and a to the corresponding index, say si and ai, and
    the we inspect self.qtable[si][ai].
    '''
    def __init__(self,numberOfStates,numberOfActions):
        '''
        Constructor. Specify the number of possible states and the number
        of possible actions to consider.
        '''
        self.numberOfStates  = numberOfStates
        self.numberOfActions = numberOfActions
        self.seed  = 127
        self.alpha = 0.5
        self.gamma = 0.9
        self.exploreProbability = 0.16
        random.seed(self.seed)
        a = [ [ random.random()/100.0 for y in range(numberOfActions) ] for x in range(numberOfStates) ]
        self.qtable = np.array(a)

        self.stateIndexer  = lambda i : i
        self.actionIndexer = lambda i : i

    def maxActionValue(self,indexOfcurrentState:int) -> Dict :
        '''
        Given an index si of some state s, this returns a pair (a,v),
        where a is the action (more precisely, it is an index of
        an action), such that qtable[si][a] = max{qtable[si][b] | b is action},
        and v is the value of qtable[si][a].
        '''
        bestAction = 0
        bestValue = self.qtable[indexOfcurrentState][0]
        for a in range(1,self.numberOfActions) :
            v = self.qtable[indexOfcurrentState][a]
            if v > bestValue :
               bestAction = a
               bestValue = v
        return { "bestAction" : bestAction, "bestValue" : bestValue }


    def getNextTrainedAction(self, currentState) -> int :
        ''' 
        Return an action (more precisely, the index of the action), that is 
        the best according to the qtable. 
        '''
        si = self.stateIndexer(currentState)
        best = self.maxActionValue(si)
        return best["bestAction"]

    def applyReward(self, oldstate:int, action:int, newstate:int, reward:float) -> None :
        '''
        Apply the Q-learning update: Q(s,a) = Q(s,a) + alpha(reward * max_b(Q(s',b)) - Q(s,a))
        '''
        oldSi = self.stateIndexer(oldstate)
        newSi = self.stateIndexer(newstate)
        ai = self.actionIndexer(action)
        valOldState = self.qtable[oldSi][ai]
        valNewState = self.maxActionValue(newSi)["bestValue"]
        self.qtable[oldSi][ai] = valOldState + self.alpha * (reward + self.gamma * valNewState - valOldState)
        
    def getNextAction(self, currentState) -> int :
        '''
        Select the next action (its index) to do (for learning). This can be the best action,
        according to the current qtable, or just a random action, by some probability
        epsilon.
        '''
        if random.random() < self.exploreProbability :
            action = random.randint(0,self.numberOfActions-1)
            return action  
        else:
            return self.getNextTrainedAction(currentState)
        

    def save(self,fname):
        print(f">>> implement this --> Saving a model to {fname}")
        raise 

    def load(self,fname):
        print(f">>> implement this --> Loading a model from {fname}")    
        raise

    def __str__(self) -> str:
        s = f"{self.qtable}"
        return s

    def learn(self, env : gym.Env, maxNumberOfSteps:int, verbose:Boolean=False) -> None :
        '''
        Run the Q-learning algorithm on the given Gym-env, for some maximum number
        of steps. Whenever a terminal state is encountered, the env will be reset
        to initial state, and a new episode of learning is started. This goed on
        until we reach the max-number of steps (totalled over all episodes).
        '''
        print("====== Learning ...")
        k = 0
        stepCountInEpisode = 0
        totalRewardInEpisode = 0
        episode = 0
        o = env.reset()
        while k < maxNumberOfSteps:
            action = self.getNextAction(o)
            #print(f"### action = {action}")
            nextObs,reward,done,i = env.step(action) 
            #print(f"### next={nextObs}, next_={nextO_}")
            totalRewardInEpisode = totalRewardInEpisode + reward
            self.applyReward(o,action,nextObs,reward)
            debugo = o
            o = nextObs
            if done :
                if verbose:
                    print(f">> Episode {episode}, #steps={stepCountInEpisode}, last-reward={reward}, tot-reward={totalRewardInEpisode}")
                episode = episode + 1
                o = env.reset()
                stepCountInEpisode = 0
                totalRewardInEpisode = 0
                #if (reward > 0) : 
                #    print(f"### o={debugo}, o_={o_}, next={nextObs}, next_={nextO_}, rw={reward}")
                #    print(f"### updated enrty={qalg.qtable[o_][action]}")
                #    break   
            else:
                stepCountInEpisode = stepCountInEpisode + 1
            k = k + 1

        print("====== Training has finished.")

    def getRun(self, env : gym.Env, maxNumberOfSteps:int, verbose:Boolean=False) -> Dict :
        '''
        Return a sequence of actions (starting from the env's initial state) that
        greedily choose each next action such that it is the action with the greatest
        value on the current state according to the qtable.
        '''
        seq = []
        k = 0
        totalRewardInEpisode = 0
        done = False
        o = env.reset()
        if verbose:
            print(f">> initial state: {o}")    
        while (not done) and k < maxNumberOfSteps :
            #print(f"### o={o}, o_={o_}")
            action = self.getNextTrainedAction(o)
            #print(f"### action = {action}")
            nextObs,reward,done,i = env.step(action) 
            totalRewardInEpisode = totalRewardInEpisode + reward
            a = env.javaGym.actionSpace[action]
            seq.append(a)
            print(f">> Action {a}, new state: {nextObs}, terminal={done}, last-reward={reward}, tot-reward={totalRewardInEpisode}")    
            o = nextObs
            k = k + 1

        return { "run":seq, "lastreward":reward, "terminal":done}

    
    
# just for testing:
if __name__ == '__main__':
    print("Baa!")
