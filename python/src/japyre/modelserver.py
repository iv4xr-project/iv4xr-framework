from commandserver import CommandServer
from typing import Any, Callable, Dict

class ModelServer:
    '''
    This class implements a server that can load a trained reinfocement-learning model.
    Given a state, a client of this server can query such a model what the best action to 
    do next. 

    The client can send two commands: 
        LOAD model-id : the server is then supposed to load this model
        GETNEXTACTION arg: the server passes arg to the model to ask what the next action is

    To interaction with the model, two functions must be attached to this server:
        loader(mId) : a function that is responsible for laoading a model, when requested to do it
        nextActionGetter(arg) : a function that actually passes arg to the model and returns back the model answer.

    These two functions are attached to the server through the method attachNeededFunctions(). 
    '''

    def __init__(self, host:str, port:int):
        self.commandserver = server = CommandServer(host,port)
    
    def attachNeededFunctions(self, 
                loader : Callable[[str],Any],
                nextActionGetter : Callable[[Dict],Any]) :
        '''
        Attach these two functions to the server:

            loader(mId) : a function that is responsible for laoading a model, when requested to do it
            nextActionGetter(arg) : a function that actually passes arg to the model and returns back the model answer.
        '''

        def act(cmd:str,arg):
            if cmd=="LOAD" :
                loader(arg)
                return True
            if cmd=="GETNEXTACTION" :
                nextAction = nextActionGetter(arg) 
                return nextAction

        self.commandserver.attachInterpreter(lambda cmd,arg: act(cmd,arg))

    def deploy(self):
        '''
        Deploy the model-sever.
        '''
        self.commandserver.deploy() 

# just for testing:
if __name__ == '__main__':
    def dummyModelLoader(modelId):
        print(f"> pretend loading model {modelId}")
    
    def dummyNextActionGetter(obs):
        print(f"> state {obs}")
        nextaction = ["kill", "bill"]
        print(f"> next action: {nextaction}")
        return nextaction
        

    server = ModelServer("127.0.0.1",9999)
    server.attachNeededFunctions(dummyModelLoader,dummyNextActionGetter)
    server.deploy() 

