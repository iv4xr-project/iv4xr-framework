# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #
#                                                                              #
# Copyright © 2021-2021 Thales SIX GTS FRANCE                                  #
#                                                                              #
# **************************************************************************** #
#                                   Copyright                                  #
# **************************************************************************** #


import threading
import logging
import signal
import zmq
import gym
import numpy as np
from gym.spaces.discrete import Discrete
from gym.spaces.dict import Dict
from gym.spaces.box import Box
from gym.envs.registration import EnvSpec


logger = logging.getLogger(__name__)


def parse_gym_space(json_space):
    """Translate a JSON space received from iv4XR as a Gym space.
    Box, Discrete and Dict spaces are handled.

    Args:
        json_space (dict): space representation parsed from JSON.

    Returns:
        the associated gym space.
    """
    if json_space['name'] == 'Box':
        low = np.array(json_space['minBoundaries'])
        high = np.array(json_space['maxBoundaries'])
        return Box(low=low, high=high)
    elif json_space['name'] == 'Discrete':
        return Discrete(n=json_space['numberOfElements'])
    elif json_space['name'] == 'Dict':
        spaces = {}
        for key, sub_space in json_space['spaces'].items():
            spaces[key] = parse_gym_space(sub_space)
        return Dict(spaces)
    else:
        raise ValueError("Unhandled action/observation space: " + json_space['name'])


class EnvProperties:
    def __init__(self, action_space, observation_space, spec):
        """Container for an environment properties, gives the necessary information
        to initiate a reinforcement learning agent

        Args:
            action_space (`gym.Space`): action space.
            observation_space (`gym.Space`): observation space.
            spec (`gym.EnvSpec`): environment specification, contains its name.
        """
        self.action_space = action_space
        self.observation_space = observation_space
        self.spec = spec

    def __str__(self):
        return "{}\n{}\n{}".format(
            f"Name: {self.spec.id}",
            f"ActionSpace: {str(self.action_space)}",
            f"Observation Space: {str(self.observation_space)}"
        )


class DisconnectEnvException(Exception):
    """Custom exception: disconnection to a iv4XR RL environment."""
    pass


class PolicyClient:
    def __init__(self, port, timeout=None):
        """Low-level class to interact as a client with a iv4XR RL environment,
        through a Gym interface.

        Manages the TCP socket and the exchange of JSON messages with the
        ZeroMQ library.

        Args:
            port (int): port of the iv4XR RL iv4xrl in server mode.
            timeout (int|None): if None, the socket will be blocking. Otherwise,
                it defines the timeout in seconds for a non-blocking socket.
        """
        self.context = zmq.Context()
        if timeout:
            self.context.setsockopt(zmq.RCVTIMEO, timeout)
            self.context.setsockopt(zmq.SNDTIMEO, timeout)
        self.socket = self.context.socket(zmq.REQ)
        self.socket.connect(f"tcp://localhost:{port}")
        self._env_properties = None

    def get_spec(self):
        """Get the environment specifications.

        Returns:
            parsed JSON response.
        """
        return self._request({
            'command': 'GET_SPEC',
            'arg': {},
        })

    def step(self, action):
        """Play a step of the iv4XR RL environment.

        Args:
            action: RL agent action.

        Returns:
            (tuple): next_state, reward, done, info
        """
        return self._request({
            'command': 'STEP',
            'arg': {"rawAction": action}
        })

    def reset(self):
        """Reset the iv4XR RL environment.

        Returns:
            initial state.
        """
        return self._request({
            'command': 'RESET',
            'arg': {},
        })

    def _request(self, req):
        """Manages the request-response exchanges with the iv4XR RL environment.

        Args:
            req (dict): request to send.

        Returns:
            received response.
        """
        self.socket.send_json(req)
        #  Get the reply.
        content = self.socket.recv_json()
        if req["command"] == "GET_SPEC":
            try:
                env_spec = EnvSpec(content['envName'], 'iv4xr')
            except gym.error.Error:
                env_spec = EnvSpec(content['envName'] + "-v0", 'iv4xr')
            action_space = parse_gym_space(content['actionSpace'])
            observation_space = parse_gym_space(content['observationSpace'])
            self._env_properties = EnvProperties(
                action_space, observation_space, env_spec
            )
            logger.info(f"Connected to environment: {self.env_spec.id}")
            return self._env_properties
        elif req["command"] == "STEP":
            return (
                    content["nextObservation"]["rawObservation"],
                    content["reward"],
                    content["done"],
                    content["info"]
            )
        elif req["command"] == "RESET":
            return content["rawObservation"]
        else:
            raise AttributeError("Invalid command : " + req["command"])

    def stop(self):
        """Disconnect and destroy the socket."""
        print("[Client] Closing...")
        self.socket.close()
        self.context.destroy()

    def has_env(self):
        """Check whether the environment properties have been received.

        Returns:
            whether the environment properties have been received.
        """
        return self._env_properties is not None

    @property
    def action_space(self):
        assert(self.has_env())
        return self._env_properties.action_space

    @property
    def observation_space(self):
        assert(self.has_env())
        return self._env_properties.observation_space

    @property
    def env_spec(self):
        assert(self.has_env())
        return self._env_properties.spec
