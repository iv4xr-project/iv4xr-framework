# Model-based testing

## Use Case
Tool [iv4xr-mbt](https://github.com/iv4xr-project/iv4xr-mbt) combines model-based approach and search-based testing for automated test generation for XR systems. The approach uses extended finite state machines (EFSMs) with which a tester can model the desired aspect of the game behavior. 

The user of the iv4xr-framework that would like to adopt model-based approach to test generation should provide:

- an [EFSM model](link) describing the testing scenario
- a map from an abstract test (generated on the model) to a concrete test (a GoalStructure)


Iv4xr-mbt generates an abstract test suite on the model. Users of the iv4xr-framework may tune different generation parameters to match  specific testing requirements. Then, the user can invoke method nextGoal() to get concrete test cases that can be executed on the system under test (SUT). 


### Modeling games with EFSM
An EFSM is made by 3 different components:

- a set of states that represents the entity in the game
- a set of transition that model the action an agent can perform 
- a set of variables that stores the status of the entities in the game

An abstract test case is a list of transitions of the EFMS model that represents the sequence of actions an agent has to complete to accomplish the testing task.

[API for EFSM creation](link)

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

### Lab Recruits

#### EFSM model

#### From abstract to concrete test case





## Papers

* Ferdous, R., Kifetew, F., Prandi, D., Prasetya, I. S. W. B., Shirzadehhajimahmood, S., & Susi, A. (2021, October). *Search-Based Automated Play Testing of Computer Games: A Model-Based Approach.* In International Symposium on Search Based Software Engineering (pp. 56-71). Springer, Cham.
