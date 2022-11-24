# iv4xr-framework

The _iv4xr Framework_ is an agent-based framework for automated testing highly interactive systems such as computer games or computer simulators. Its cool features include intelligent agents, player experience testing, and integration with other tools such as Model based Testing (MBT) and TESTAR.

Although above we mention "games", iv4xr itself is generic enough to target other types of interactive systems, even services or Java classes as long as these entities can be viewed as interactable systems.

Iv4xr is a _framework_ because it is not a tool that can immediately do its work out of the box --this is not possible due to the unstandardized domains of its targets. E.g. there is no uniform way to interface with computer games, and there is no uniform representation of their states either. So before it can be used, iv4xr Framework requires an interface to be built that would allow it to control and observe the target system under test (SUT).



**Note:** _iv4xr_ itself stands for "intelligent verification/validation for extended reality (XR) based systems".
During its development we have focused on piloting the Framework for testing 3D games. Solving testing problems in 3D worlds provides the foundation for testing XR systems in general. E.g. a virtual reality (VR) application typically operates on an interactive 3D virtual world. While the used devices are different (VR glass, hand tracker), and will have separate concerns to be tested, much of the correctness of its 3D world would be independent of the used devices, and therefore shares much of the aspects of that of 3D games.
Testing an Augmented Reality (AR) application is very costly as it requires a physical agent (e.g. a robot) that interacts with our physical world. A mitigation of this is by first testing (and also re-testing) the application in a simulated 3D environment (hence reducing e.g. the frequency of costly physical re-test, or even its extent), for which we can fall back to a 3D world setup which iv4xr can address.



#### Features

(TODO: below are just tmp-txt)

  * Create a testing-task by expressing is as a goal and giving it to a test agent to be accomplished. Complex testing tasks (e.g. hierarchical and involving conditions) can be expressed using **Goal-combinators**.

  * **Multi agent**: programming multiple autonomous test-agents that collaboratively test a system.

  * **Emotive testing**: simulating users' emotion, e.g. to facilitate automated user experience assesment.

  * **Goal-solving** for automatically solving testing tasks. There are different types of solver provided by iv4xr Framework:

    * Develop domain-specific goal-solvers using **tactical programming**; this is suitable for developing a set of composable goals which do not require a complex algorithm to solve.
    * **A* pathfinder** to do automated spatial navigation (e.g. over a 3D surface). This is suitable for solving goals related to physical navigation.
    * **Prolog binding**: allowing agents to build a model of its environment as Prolog facts and uses prolog-based reasoning to for solving goals. This is suitable for solving goals that require some level of reasoning.
    * **Search-based algorithm**: when behavioral models are available (e.g. as EFSMs), search-based algorithms can be used to generate a test-suite for test agents. This is suitable for solving goals that require complex planning, if a model of the problem is given.
    * **Model checker**: or use our model-checker to do it.

  * Integration with TESTAR to do **Autotmated explorative testing**.
  * Integration with **Reinforcement Learning** libraries for training agents.    
  * In addition to usual assertions/invariants, **LTL and Bounded LTL** can be used for expressing temporal properties that can be checked during testing.

### How to build

(Batman Pacman Maman)

### Manuals


#### Agent-based testing

(TODO: UU, add link to docs on this topic)

(TODO: add link to APIs reference)



#### Emotive testing

(TODO: UU, INESC add link to docs on this topic)

(TODO: add link to APIs reference)



#### Automated explorative testing

(TODO: UPV add link to docs on this topic)

(TODO: add link to APIs reference)



#### Model-based testing

(TODO: FBK add link to docs on this topic)

(TODO: add link to APIs reference)



#### Reinforcement learning

(TODO: Thales,FBK,UU add link to docs on this topic)

(TODO: add link to APIs reference)



#### Case studies

(TODO: GoodAI,Thales,GW,all add links to your case studies, papers here)

### License

### Credits

(TODO: add our names here?)

(TODO: credit EU)

### Papers

(TODO: all -> add your papers here)

  * Extended abstract: [_Aplib: An Agent Programming Library for Testing Games_](http://ifaamas.org/Proceedings/aamas2020/pdfs/p1972.pdf), I. S. W. B. Prasetya,  Mehdi Dastani, in the International Conference on Autonomous Agents and Multiagent Systems (AAMAS), 2020.

  * Concepts behind agent-based automated testing:
    [_Tactical Agents for Testing Computer Games_](https://emas2020.in.tu-clausthal.de/files/emas/papers-h/EMAS2020_paper_6.pdf)
  I. S. W. B. Prasetya, Mehdi Dastani, Rui Prada, Tanja E. J. Vos, Frank Dignum, Fitsum Kifetew,
  in Engineering Multi-Agent Systems workshop (EMAS), 2020.

  * Paper-2
  * Paper-3 etc
