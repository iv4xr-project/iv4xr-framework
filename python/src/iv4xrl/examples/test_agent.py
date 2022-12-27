# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #
#                                                                              #
# Copyright © 2021-2021 Thales SIX GTS FRANCE                                  #
#                                                                              #
# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #


from iv4xrl.env import Iv4xrClientEnv
from iv4xrl.policy_server import DisconnectEnvException
import coloredlogs
import logging


logger = logging.getLogger('iv4xr_gym')
coloredlogs.install(level='INFO')

PORT = 5555

def gym_client_env():
    """Implement a Iv4XR Gym environment in client mode. Must be used with the
    JAVA RLAgentSocketConnector in server mode.

    Associated class in the java side: EnvironmentServer
    """
    env = Iv4xrClientEnv(PORT)
    print(f"Environment initialized: {env.spec.id}")
    print(f"Action space: {env.action_space}")
    print(f"State space: {env.observation_space}")

    try:
        num_episodes = 10
        for _ in range(num_episodes):
            state = env.reset()
            print(f"Initial state: {state}")
            done = False
            step = 0
            while not done:
                # Sucessful policy
                action = 0 if step % 2 == 0 else 3
                # Failing policy
                # action = 0
                state, reward, done, info = env.step(action)
                print(f"Step #{step} (state, reward, done, info): {(state, reward, done, info)}")
                assert env.observation_space.contains(state)
                step += 1
        # This lets us notify the JAVA test that the last episode is done
        env.reset()
    except DisconnectEnvException:
        env.close()


if __name__ == "__main__":
    gym_client_env()
