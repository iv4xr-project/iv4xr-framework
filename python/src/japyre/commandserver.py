import socket
import json
from typing import Any, Callable, Dict

HOST = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT = 9999         # Port to listen on (non-privileged ports are > 1023)
  
debug = True 

class CommandServer:
    '''
    This class implements a generic command-server. To use it, first create an instance
    of this class, then run the method deploy().
    '''

    def __init__(self, host:str, port:int):
        '''
        The constructor. When created, the server does not run yet.
        The method deploy() will deploy/run the server; it will then be bound
        to the given ip (host) and port.

        Parameters:
        host (string): the ip where this server will be hosted
        port (int):    the port number
        '''
        self.host = host
        self.port = port
        self.commandInterpreter = None

    def attachInterpreter(self, commandInterpreter : Callable[[Dict,Dict],Any]) :
        ''' 
        Attach a function f that will act as an interpreter "commands" received by this server.
        This f that takes two string cmd and arg as arguments. 
        The arg is assumed to be a Json-string.
        
        The function f is assumed to return a Json-string.
        '''
        self.commandInterpreter = commandInterpreter


    def deploy(self, receiveBufferSize=4096) -> None :
        '''
        Deploy this command-server at the ip-address and port specified by
        self.host and self.port. This method will just run in a forever-cycle (until
        the client asks it to close) accepting "commands" sent by a client,
        interpreting it, and then sends to response back to the client.
        
        A command is a pair (cmd,arg) where
        cmd is a string specifying the command name, and arg is a Json-string
        representing the argument of the command.

        The function f attached in self.command-interpreter is used to interpret
        incoming commands. Upon receiving
        a pair (cmd,arg) from the client, f(cmd,arg) is invoked. The function is
        assumed to produce a Json-string, which is then sent back to the client.

        The server closes when the client asks it to close. 
        '''
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((self.host,self.port))
            s.listen()
            print(f"> Starting a sever at {self.host}:{self.port}")
            clientSocket, addr = s.accept()
            with clientSocket:
                print(f"> CONNECTED by {addr}")
                while True:
                    received = clientSocket.recv(receiveBufferSize) 
                    if not received:
                        print("> CLOSING the server.")
                        break
                    text = received.decode("utf-8")
                    myjson = json.loads(text)
                    cmd = myjson["cmd"] # the command-string
                    arg = myjson["arg"] # the arg-object, represented as a nested Dictionary
                    if debug :
                        #print(f"> receiving {text}")
                        print(f"> receiving cmd: {cmd}")
                        print(f">           arg: {arg}")
                        
                    # interpret the command:
                    result =  self.commandInterpreter(cmd,arg)   
                    resultJson = json.dumps(result) 
                    # send result back to the client:
                    # need to add that newline-char at the end, else the client does not
                    # know that the string that was sent has ended:
                    clientSocket.sendall(bytes(resultJson  + "\n",encoding="utf-8"))
                    if debug :
                        print(f"> sending {resultJson}")
            s.close()


# just for testing:
if __name__ == '__main__':
    def testCmdInterpreter(cmd,arg):
        xxx = { "id":0, "name":"Batman" }
        return xxx

    server = CommandServer(HOST,PORT)
    server.attachInterpreter(testCmdInterpreter)
    server.deploy()        
