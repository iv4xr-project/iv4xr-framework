# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #
#                                                                              #
# Copyright © 2021-2021 Thales SIX GTS FRANCE                                  #
#                                                                              #
# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #


from .policy_server import PolicyClient, DisconnectEnvException
import gym


class Iv4xrClientEnv(gym.Env):
    def __init__(self, port, timeout=None):
        """Gym environment wrapper over a PolicyClient. Ease integration with
        existing RL code.

        Args:
            port (int): port of the iv4XR RL iv4xrl in server mode.
            timeout (int|None): if None, the socket will be blocking. Otherwise,
                it defines the timeout in seconds for a non-blocking socket.
        """
        self.policy_client = PolicyClient(port, timeout)
        self.policy_client.get_spec()
        self.action_space = self.policy_client.action_space
        self.observation_space = self.policy_client.observation_space
        self.spec = self.policy_client.env_spec

    def step(self, action):
        """Play a step of the iv4XR RL environment.

        Args:
            action: RL agent action.

        Returns:
            (tuple): next_state, reward, done, info
        """
        return self.policy_client.step(action)

    def reset(self):
        """Reset the iv4XR RL environment.

        Returns:
            initial state.
        """
        return self.policy_client.reset()

    def render(self, mode="human"):
        raise NotImplementedError

    def close(self):
        self.policy_client.stop()
