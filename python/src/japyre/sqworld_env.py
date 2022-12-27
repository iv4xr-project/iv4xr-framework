import gym
import numpy as np
from gym import spaces
from gymenv_client import GymEnvClient
from stable_baselines3 import PPO

class SqWorldEnv(gym.Env) :
    '''
    Provides an example of how to write a custom OpenAI Gym-env for a Gym written in Java.
    In this example, the real Gym-env is the SquareWorld gym, provided in Java. This Java-gym
    can be interacted to from python through a server, which can be launched from the Java-class
    SquareWorldGymServer. From the Python-side, the class GymEnvClient can be used to provide
    a Gym-like APIs over this client-server connection (so, the connection will become transparent
    for you). This GymEnvClient already have methods similar to OpenAI Gym-Env, but it is not
    a subclass of the latter. So, this class SqWorldEnv is just a wrapper and a subclass of OpenAI
    Gym-Env.

    The SquareWorld gym itself represents a tiled NxN grid, where a robot is dropped in its 
    center. Its goal is to reach the top-right corner of the grid (location (N-1,N-1)). The 
    robot can move left/right/up/down. Moving off the grid causes the robot to be broken.
    Getting to the goal location gives a reward of 100; getting broken -100; and else the reward
    is 0. The task of RL is to find a sequence of interactions that would maximize the total
    reward.
    '''

    def __init__(self, worldSize):
        super(SqWorldEnv, self).__init__()
        self.size = worldSize
        self.javaGym = GymEnvClient("127.0.0.1",9999)
        self.action_space = spaces.Discrete(len(self.javaGym.actionSpace))
        self.observation_space = spaces.Box(low=-1, high=worldSize, shape=(1,), dtype=np.int8) 

    def reset(self):
        o = self.javaGym.reset()
        return np.array([o["x"], o["y"]])

    def close(self):
        self.javaGym.close()

    def step(self,action):
        o,rw,done,info = self.javaGym.step(self.javaGym.actionSpace[action])
        return (np.array([o["x"], o["y"]]), rw, done, info)

#model = PPO("MlpPolicy", env, verbose=1)
#model.learn(total_timesteps=10_000)

# just for testing:
if __name__ == '__main__':
    env = SqWorldEnv(6)
    env.reset()
    for k in range(10):
        print(f'[{k}] ---------------------')
        o,rew,done,_ = env.step(env.action_space.sample())
        print(f'Observation : {o}')
        print(f'Reward      : {rew}')
        print(f'Done        : {done}')
        if done: 
            break   
    env.close()    