# iv4xr-framework

The _iv4xr Framework_ is an agent-based framework for automated testing highly interactive systems such as computer games or computer simulators. Its cool features include intelligent agents, player experience testing, and integration with other tools such as Model based Testing (MBT) and TESTAR.

Although above we mention "games", iv4xr itself is generic enough to target other types of interactive systems, even services or Java classes as long as these entities can be viewed as interactable systems.

Iv4xr is a _framework_ because it is not a tool that can immediately do its work out of the box --this is not possible due to the unstandardized domains of its targets. E.g. there is no uniform way to interface with computer games, and there is no uniform representation of their states either. So before it can be used, iv4xr Framework requires an interface to be built that would allow it to control and observe the target system under test (SUT).



**Note:** _iv4xr_ itself stands for "intelligent verification/validation for extended reality (XR) based systems".
During its development we have focused on piloting the Framework for testing 3D games. Solving testing problems in 3D worlds provides the foundation for testing XR systems in general. E.g. a virtual reality (VR) application typically operates on an interactive 3D virtual world. While the used devices are different (VR glass, hand tracker), and will have separate concerns to be tested, much of the correctness of its 3D world would be independent of the used devices, and therefore shares much of the aspects of that of 3D games.
Testing an Augmented Reality (AR) application is very costly as it requires a physical agent (e.g. a robot) that interacts with our physical world. A mitigation of this is by first testing (and also re-testing) the application in a simulated 3D environment (hence reducing e.g. the frequency of costly physical re-test, or even its extent), for which we can fall back to a 3D world setup which iv4xr can address.

![iv4xr-architecture](./docs/iv4xr_architecture1.png)
_Iv4xr architecture_.

#### Features

  * **Tactical and goal-based agent programming** to implement test automation, e.g. to perform automated navigation to a certain destination, to program logic that handles adversaries, and to program complex scenarios. There is support for automated path finding and automated area exploration. And yes, iv4xr supports **multi agent** as well.

  * **Tracing and verification:** test agents can collect execution traces during lengthy test scenarios. Assertions and Linear Temporal Logic (LTL) can be used for checking correctness, either live during test runs, or post-mortem on collected traces. Beyond verification, traces can be processed in various ways. E.g. they typically contain the agents' positions, whcih can be aggregated to visualise (or calculate) the physical area coverage of the tests.

  * **Affective testing**: simulating users' emotion, e.g. to facilitate automated user experience assessment. **[Pedro/Saba one paragraph here]**

  *  **Automated explorative testing** with TESTAR. **[Fernando/Tanja one paragraph here]**

  * **Model based testing**: this allows good quality test suites to be rapidly generated using state of the art search-based testing algorithms **[FBK modify/add]**.

  * **Reinforcement learning** **[what should we say here? :)]**

### How to build

Just run `mvn compile` from the project root. This should download and build all the needed components.  

### How to use

This depends on your use case. E.g. you can use just iv4xr test agents to do your testing, but if we also want to do model based testing (MBT) then the MBT tool needs to be used (which in turn will drive iv4xr test agents). Regardless the use case, an interface to connect iv4xr to the SUT needs to be built first, along with a suitable goal-library that provides basic automation. This will require some effort, but it is a one-off investment, after which they can be used over and over again to provide intelligent automated testing on the SUT. How to build these two components will be described in the manuals below.

### Manuals

1. _Use case_: you want to do **automated testing of a computer game using goal-based agents**. In this setup, we use a goal structure to formulate a single testing task, e.g. to verify that a certain scenario ends in a correct state. Such a task can be given to a test agent to be executed. You can  course have, or even generate, multiple tasks, and have then executed by the test agent/s. [Documentation].

  _Require_: SUT-specific interface, SUT-specific goal-library.

1. Building your interface and goal-library. [Documentation].

1. _Use case_: you want to have an agent that **randomly explores and interacts with a computer game**, e.g. to test it against unexpecteds. [Documentation]

  _Require_: SUT-specific interface, SUT-specific goal-library, TESTAR (included in the Framework).

1. _Use case_: you want to do **model-based testing (MBT) on a computer game**. This needs a model, but on the other hand the benefit is that you can easily (and rapidly) generate test suites. [Documentation]

  _Require_: SUT-specific interface, SUT-specific goal-library, MBT component (included in the Framework).

1. _Use case_: you want to do **player experience (PX) testing** on a computer game. [Documentation] [Need help here, more use-cases??]

1. _Use case_: you want to use reinforcement testing.


#### Case studies

(TODO: GoodAI,Thales,GW,all add links to your case studies, papers here)

### License



### Papers

(TODO: all -> add your papers here)

  * Extended abstract: [_Aplib: An Agent Programming Library for Testing Games_](http://ifaamas.org/Proceedings/aamas2020/pdfs/p1972.pdf), I. S. W. B. Prasetya,  Mehdi Dastani, in the International Conference on Autonomous Agents and Multiagent Systems (AAMAS), 2020.

  * Concepts behind agent-based automated testing:
    [_Tactical Agents for Testing Computer Games_](https://emas2020.in.tu-clausthal.de/files/emas/papers-h/EMAS2020_paper_6.pdf)
  I. S. W. B. Prasetya, Mehdi Dastani, Rui Prada, Tanja E. J. Vos, Frank Dignum, Fitsum Kifetew,
  in Engineering Multi-Agent Systems workshop (EMAS), 2020.

  * Paper-2
  * Paper-3 etc

  ### Credits

  (TODO: add our names here?)

  (TODO: credit EU)
