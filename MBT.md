# Model-based testing

## Use Case
Tool [EvoMBT](https://github.com/iv4xr-project/iv4xr-mbt) combines model-based approach and search-based testing for automated test generation for XR systems. The approach uses extended finite state machines (EFSMs) with which a tester can model the desired aspect of the game behavior. 

The user of the iv4xr-framework that would like to adopt model-based approach to test generation should provide:

- an [EFSM model](https://github.com/iv4xr-project/iv4xr-mbt/blob/master/src/main/java/eu/fbk/iv4xr/mbt/efsm/EFSM.java) describing the testing scenario
- a map from an abstract test (generated on the model) to a concrete test (a GoalStructure)


Iv4xr-mbt generates an abstract test suite on the model. Users of the iv4xr-framework may tune different generation parameters to match  specific testing requirements. Then, the user can invoke method nextGoal() to get concrete test cases that can be executed on the system under test (SUT). 


### Modeling games with EFSM
An EFSM is made by 3 different components:

- a set of states that represents the entity in the game
- a set of transition that model the action an agent can perform 
- a set of variables that stores the status of the entities in the game

An abstract test case is a list of transitions of the EFMS model that represents the sequence of actions an agent has to complete to accomplish the testing task.

[API for EFSM creation](https://github.com/iv4xr-project/iv4xr-mbt/wiki/DeveloperGuide)

### From abstract to concrete test cases
An EFSM transition is the equivalent of an action performed by an agent in the SUT. For each transition, the user of the framework should proved a map to a GoalStructure.

### Usage scenario
The [iv4xr-framework](https://github.com/iv4xr-project/iv4xr-framework) is implemented in a dedicated branch [mbt-iv4xr-interface](https://github.com/iv4xr-project/iv4xr-mbt/tree/mbt-iv4xr-interface).

A possible use case of iv4xr-mbt follows:

```java

		// setup the test agent ..
		TestAgent agent = new TestAgent();
		
		// create the MBT test factory, with parameters ...
		MBTTestFactory factory = new MBTTestFactory();
		
		// attach the agent to the factory
		factory.attachAgent(agent);
		
		// iterate over the tests generated
		List<TestDataCollector> results = new ArrayList<>();
		GoalStructure goal;
		do {
			goal = factory.nextGoal();
			if (goal != null) {
				factory.execute(goal, false);
				TestDataCollector result = factory.getTestResults();
				results.add(result);
			}
		}while (goal != null);

```

### EvoMBT wiki

[EvoMBT wiki](https://github.com/iv4xr-project/iv4xr-mbt/wiki) provides a complete guide for model-based testing, including support to [LabRecruits](https://github.com/iv4xr-project/iv4xr-mbt/wiki/LabRecruits) and a [developer guide](https://github.com/iv4xr-project/iv4xr-mbt/wiki/DeveloperGuide) to implement custom EFSM models.



## Papers

<a id="1">[1]</a> 
R. Ferdous, F. M. Kifetew, D. Prandi, I. S. W. B. Prasetya, S. Shirzadehhajimahmood, A. Susi.
*Search-based automated play testing of computer games: A model-based approach.*
13th International Symposium, SSBSE 2021. 
[doi:10.1007/978-3-030-88106-1_5](https://link.springer.com/chapter/10.1007/978-3-030-88106-1_5). 

<a id="2">[2]</a> 
R. Ferdous, C. Hung, F. M. Kifetew, D. Prandi, A. Susi.
*EvoMBT*.
15th IEEE/ACM International Workshop on Search-Based Software Testing (tool competition), SBST@ICSE 2022.
[doi:10.1145/3526072.3527534](https://ieeexplore.ieee.org/document/9810734).

