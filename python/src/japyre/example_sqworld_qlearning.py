from qlearning   import Qlearning
from sqworld_env import SqWorldEnv
import gymenv_client

# This demonstrates an example of applying a Python-side RL algorithm
# on a Gym-env in Java.
#
# The Gym-env is the SquareWorld gym in Java. You first need to run it
# as a server, see the class SquareWorldGymServer in Java. Run the main
# of that class.
#
# Then you can run this class. It will demonstrate the use of Qlearning
# to learn how to do actions on the SquareWorld that would guide a 
# hypothetical robot there to reach a goal location (top-right corner of
# the world). It is not a difficult problem to solve for RL, but it demostrates
# the Python-Java connection.
#

gymenv_client.debug = False

# Run a Python-side client that would connect to the Java-side server of
# SquareWorld Gym. This client then also provides a Gym-env like interface
# for us, so we can treat it as if it is an all-Python Gym-env without being
# aware that under the hood it connects to a Java-side.
env = SqWorldEnv(6) # 6 is the size of the sq-world as set at the Java-side

# Create an instance of Qlearning algorithm. You need to configure it with
# the number of states and actions it has to deal with:
N = env.size
N_plus_2 = N+2
qalg = Qlearning(N_plus_2*N_plus_2, len(env.javaGym.actionSpace))
#qalg.alpha = 1
def convertToIndex(obs) :
    x = obs[0]
    y = obs[1]
    return ((x+1) * N_plus_2) + y +1
qalg.stateIndexer = convertToIndex    

# Ok.. now we can run the Q-learning algorithm to train it:
print("====== Training...")
qalg.learn(env,maxNumberOfSteps=2000,verbose=True)

# Training is done, now we obtain a sequence of action that
# use the trained model:
print("====== Demonstrating trained skill ...")
# now showing how to use the learned information
seq = qalg.getRun(env,10,verbose=True)
print("======")
print(f"obtained run: {seq}")
env.close()



# ignore the stuffs below:)
#
#for y in range(N_plus_2):
#    y_ = N_plus_2 - y - 1
#    y__ = y_ - 1
#    for x in range(N_plus_2) :
#        x__ = x - 1
#        index = x * N_plus_2 + y_ 
#        vals = qalg.qtable[index]
#        print(f"q({x__},{y__})={vals}")


