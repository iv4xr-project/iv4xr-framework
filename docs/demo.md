# Demos

## Simple demos with a 2D game

These demos are simple to setup. The demos show a number of automated tests on a simple 2D game as the system under test (SUT). The game is called _MiniDungeon_ which has the flavour of the Nethack game from 80's. Run the class [`DemoPlayMiniDungeon`](../src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/DemoPlayMiniDungeon.java) if you first want to play the game (could be fun...). The simplest way to run the class is using Maven:

   * Clone the iv4xr-framework project. Then from the project root, run:

   ```mvn compile exec:java -Dexec.mainClass=nl.uu.cs.aplib.exampleUsages.miniDungeon.DemoPlayMiniDungeon```

   You can also import the project into an IDE like Eclipse, and then run the class from there.



For the demos, there are three JUnit classes to run. Inspect their source code to see the intent of every demo-class and how the test there is being prepared and run:

  * [TestDemo_ScriptedScenario](../src/test/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/TestDemo_ScriptedScenario.java) shows how to script a test scenario.
  * [TestDemo_ScenarioAutomation_using_SA1](../src/test/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/TestDemo_ScenarioAutomation_using_SA1.java) shows how to use a simple search algorithm to simplify the formulation of a test scenario. The demo also shows the use of LTL assertions (see the source code), tracing, producing graphs from the produced trace. Trace and graphs can be found in the ```tmp``` directory in the project root.

  To produce the graphs the demo invokes some Python scripts. You need Python3 installed, with matplotlib, numpy, and scipy. You may also need to adjust the full path to python in [TestDemo_ScenarioAutomation_using_SA1](../src/test/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/TestDemo_ScenarioAutomation_using_SA1.java).

  * [TestDemoPX](../src/test/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/TestDemoPX.java) showing a simple example of a User/Player Experience testing. More on PX testing [see here](./occ/occ-emotion.md).

The easiest way to run the demos is by using Maven. I assume you have close the project. From the project root you can run a single test, which is one of the above classes, as in:

```mvn test -Dtest=nl.uu.cs.aplib.exampleUsages.miniDungeon.TestDemo_ScriptedScenario```

You can do the same with the other demo-classes.

Alternatively, you can also import the project into an IDE like Eclipse, and then run those test-classes from there.

## Demo testing 3D game
