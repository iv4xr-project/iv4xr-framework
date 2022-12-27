import socket
import json
from typing import Any, Callable, Dict

HOST = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT = 9999         # Port to listen on (non-privileged ports are > 1023)
debug = False 

class GymEnvClient:
    '''
    This class provides a Python-side Gym-environment that 'wraps' over an actual
    Java-side Gym. The class provides the usual Gym-env method such as reset() and
    step(), which in turn will call the corresponding method at the Java-side Gym-env.
    The communication between Python and Java goes via socket. 

    To use this GymEnvClient, you need to run an instance of Java-side GymEnvServer.
    See an example provided in SquareWorldGymServer. When the server is deployed,
    we can create an instance of GymEnvClient, which will then connect to that server.
    From this point, you can call methods like reset() and step() as if it is an ordinary
    RL-gym.

    Methods like reset() and step() return observations. Do note that these observations
    will be represented either primitive value, or as dictionaries (possibly nested) of 
    name-value pairs. You will need to convert them to suitable datatypes expected by 
    your RL algorithm.
    '''

    def __init__(self,host,port,receiveBufferSize=4096):
        self.host = host 
        self.port = port
        self.receiveBufferSize = receiveBufferSize
        self.socket = socket.socket()
        # connect to the Java-side GymEnvServer:
        self.socket.connect((host,port))
        self.actionSpace = self.sendCommand("GET_ACTIONSPACE") 


    def sendCommand(self,cmd,arg=None):
        '''
        Send a command to the GymEnvServer.

        Parameters:
           cmd (str) : the name of the command
           arg       : an argument to send along with the command; it can be
                       a primitive value e.g. a string, or a dictionary.
        '''
        pckg     = {"cmd":cmd, "arg":arg}
        jsonPckg = json.dumps(pckg) 
        self.socket.sendall(bytes(jsonPckg  + "\n",encoding="utf-8"))
        if debug :
            print(f"> sending {jsonPckg}")
        if cmd == "KILL" :
            return None
        received = self.socket.recv(self.receiveBufferSize)     
        text = received.decode("utf-8")
        receivedJson = json.loads(text)
        if debug :
            print(f"> receiving {receivedJson}")
        if cmd == "RESET" :
            return receivedJson
        if cmd == "STEP" :
            observation = receivedJson["obs"]
            reward = receivedJson["rw"]
            episodeDone = receivedJson["end"]
            return (observation,reward,episodeDone,None)
        if cmd == "GET_ACTIONSPACE" :
            return receivedJson
        return None

    def reset(self) :
        '''
        Reset the Java-side GymEnv to its intial state. The method returns
        the GymEnv initial state, as an observation.
        Observation is returned either as a privitive value, or as a Dictionary.
        '''
        o = self.sendCommand("RESET")
        return o 

    def softClose(self):
        '''
        Close the Java-side GymEnv; this will not close the GymEnvServer that runs
        the GymEnv.
        '''
        self.sendCommand("CLOSE")    

    def close(self):
        '''
        Close the Java-side GymEnv and the GymEnvServer that runs it.
        '''
        self.sendCommand("CLOSE")    
        self.sendCommand("KILL")    
        self.socket.close()

    def step(self,action):
        '''
        Command the Java-side GymEnv to execute the given action. The method
        returns a tuple (obs,r,done,info) where obs is the resulting GymEnv
        state, expressed as an observation; r is the reward the GymEnv gave
        for the action; done is true if the new state is terminal; and
        info is additional information, if there is any.
        Observation is returned either as a privitive value, or as a Dictionary.
        '''
        o = self.sendCommand("STEP",action)
        return o  


# just for testing:
if __name__ == '__main__':
    client = GymEnvClient(HOST,PORT)
    print(f"### action space: {client.actionSpace}" )
    client.reset()
    o = client.step("up")
    print(f"### {o}" )
    client.softClose()
    client.close()
