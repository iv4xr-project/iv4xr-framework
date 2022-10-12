## UX Testing with OCC

**OCC** standards for "Ortony-Clore-Collins". It refers to a [theory from Psychology](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4243519/#:~:text=The%20model%20proposed%20by%20Ortony%2C%20Clore%2C%20and%20Collins%20(commonly,and%20those%20focused%20on%20objects.) about different types of emotions (e.g. hope, distress, etc) and what makes them emerge. The theory has been formalized into computational models by various computer scientists, providing a way to 'predict' when certain emotion emerges. By 'predicting' we mean that such a computational model, if additionally given a model of a person's mental process, will be able to calculate if certain emotions would emerge when e.g. we simulate the occurrence of some events on the person.

This part of iv4xr offers a way to use such an OCC-based computational model to test user experience (UX). Under the hood, we use a package called JOCC that provides an implementation of an OCC Computational Model. A typical example would be to test the user experience of a given computer game, e.g. whether certain areas or certain scenarios trigger the right emotion, or the right patterns of emotion.


The theory behind JOCC computational model can be found in this paper:

-----

[_An Appraisal Transition System for Event-Driven Emotions in Agent-Based Player Experience Testing_](https://arxiv.org/pdf/2105.05589), Ansari, Prasetya, Dastani, Dignum, Keller. In
International Workshop on Engineering Multi-Agent Systems (EMAS), 2021.

-----

JOCC is a 'model-based' approach (as opposed to a machine-learning based approach). This means that we first to construct a model of a user's 'mental process' (we will get back to what this means later). JOCC then uses this model to calculate which emotions would emerge when the user receives certain events, and how the intensity of these emotions would decay over time. This means that if we now run a test scenario on the SUT, and let JOCC monitors the run, it then can predicts which emotions would emerge during the scenario, and how they would decay. This can be matched with some requirements as a form of User Experience (UX) testing.

JOCC has six types of emotions: hope, joy, satisfaction, fear, distress, and disappointment. Furthermore, only event-driven and goal-oriented emotions are considered (this is not to say that these are the only aspects that can trigger emotion; but just limit ourself to the aforementioned setup). The goal-oriented part means that we only consider emotions directed towards some goals. For example, if we are testing a game, we can think goals like g1 = "collecting (enough) gold coins" and g2 = "getting to the next level". Emotions such as being "hopeful" are then defined towards achieving such goals, e.g. being hopeful towards (achieving) g1 and being hopeful towards g2. These two "hopes" can co-exist.

The event-driven part means that we only consider emotions that emerge due to the occurrence of events. For example, seeing a treasure chest that was not seen before (an event) may trigger the emergence of hope towards achieving g1. Being badly hurt by a monster (event) may trigger fear towards achieving g2, maybe also towards g1.

JOCC itself contains some generic parts e.g. for calculating how emotions decay. But it cannot do all the calculations. It needs need some domain-specific information as well. E.g. if we are testing a game X, JOCC will need some information about X. For example, we need to specify which goals are relevant for X's players (e.g. g1 and g2 above), and what are their relative importance.
We also need to specify what are the events that we consider relevant towards influencing players' emotion, e.g. seeing a treasure chest, or being hit by a monster as in the example above, and how desirable these events are towards each goal.

JOCC's most important source of information to calculate how emotions emerge is the current likelihood of achieving each goal. Well.. we don't actually know this likelihood. Moreover, what we want is not the actual likelihood, but rather, how players would perceive what the likelihood is (e.g. do players think that achieving g1 now becomes quite likely, or conversely very unlikely?). We also don't know how players would perceive this, we can still model how we think they would perceive. This is what is meant by "modelling players' mental process" that we mention before. In fact, specifying all parameters that JOCC needs before we can use it (e.g. specifying what are players' goals, the desirability of events, and how they affect goals' likelihood etc) is what we mean by modelling players. In JOCC, this is called **Player Characterization**; JOCC will need such a characterization before it can do its work.

The general steps to do UX testing with JOCC are as follows:

   1. Construct a Player Characterization. You only need to do this once for every SUT, but you may want to tune it over several iterations to get a model/characterzation that makes sense.

   2. Construct one or more test cases (test scenarios), e.g. several plays of a game e.g. all finish the game, but in different ways.

   3. Use an iv4xr test agent to run the scenarios. We also hook in JOCC to the test agent. This results in the test agent has its state extended with an emotion state.

   4. We can e.g. use an Linear Temporal Logic (LTL) formula to express what kind of patterns of emotions we expect to see on those test runs, and then have this LTL formula checked during the agent's run. It will result in either: the LTL formula is satisfied (by the test case), or violated.


### Using JOCC by Example

As an example we will use the game MiniDungeon as our (System Under Test/SUT). This game is included in the packaging of iv4xr. This example is not meant to be complete; we just want to show the steps needed to use JOCC. Some screenshots are shown below. Players' avatars are circled in blue (one or two players can play in the same game). Players actually have limited sight; the screenshot to the left shows how the game actually look like for players. The screenshot on the right shows an entire game-level by artificially set the players' visibility range to unlimited.

![MiniDungeon Screenshot-1](./minidungeonShot3.png)![MiniDungeon Screenshot-2](./minidungeonShot2.png)

In this game, the player can go from one level to the next one, until it gets to the final level. Access to the next level is guarded by a shrine, which can teleport the player to the next level. However, the shrine must be cleansed first before it can be used as a teleporter. To cleanse it, the player need to use a scroll (gray icon in the game). There are usually several scrolls dropped in a level, but only one of them (a holy scroll) can do the cleansing. The player does not know which scroll is holy until it tries to use it on a shrine. There are also monsters in the levels that can hurt the player, but also potions that can heal the player or enhance its combat.

Imagine we want to run some test scenarios to see that the first level of MiniDungeon can generate tension. In terms of OCC, we want to see if one of these scenarios would generate the distress emotion, or at least fear.

#### 1. Determine the goals and relevant events.

As a goal of interest, let us take g = "a shrine is cleansed". If we specify this to JOCC, it means that emotions it produce would emotions with respect to this g. We could have more goals, but here let us just have g as our only goal of interest. Since the player starts in level-1 of the game, and there is only one shrine there, then g simply means the shrine of level-1 is cleansed.

Next we decide what "events" would be relevant towards this goal g. For this example let's just restrict to the following events:

   1. "OUCH": triggered when a monster hits/hurts the player.
   2. "HEAL": tiggered when the player heals itself by drinking a health potion.
   3. "SEESHRINE": triggered when the player sees level-1 shrine for the first time.
   4. "CLEANSE": triggered when the player manages to cleanse level-1 shrine.

The game does not actually produces these events as (it was not implemented using an event-system), but we would argue that players experience them as meaningful changes, hence "events". Since the game does not produce these events (as "objects" that we can intercept), we somehow have to produce them so that they are visible to JOCC. We can do this by implementing the abstract class `SyntheticEventsProducer`. A fragment of this implementation for MiniDungeon is shown below:

```Java
public class MiniDungeonEventsProducer extends SyntheticEventsProducer {
  ...
  public void generateCurrentEvents() {
     ...
     var player = wom.elements.get(wom.agentId) ;
     var prevState = player.getPreviousState() ;
     if (..) // if player's hp < its hp in the prevState
        currentEvents.add(OUCH) ;
    ...
  }
}
```

The [full implementation is here](../src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/MiniDungeonEventsProducer.java).



#### 2. Constructing a Player Characterization

Next we need to provide a "model" of the game players ("model" is of course a very foggy word; we will clarify this in a minute).
Indeed, users may have different play styles, so we may want to construct different models representing different play styles.
For our example, let's just have one model :)

In JOCC, a player-model, also called a _player characterization_, is a class that implements the abstract class `UserCharacterization`. We show below the methods that need to be implemented:

```java
public class MiniDungeonPlayerCharacterization extends UserCharacterization {
   public void eventEffect(Event e, BeliefBase beliefbase) ...
   public int desirabilityAppraisalRule(Goals_Status goals_status, String eventName, String goalName) ...
   public int emotionIntensityDecayRule(EmotionType etype) ...
   public int intensityThresholdRule(EmotionType etyp) ...
}
```

  * `intensityThresholdRule(ety)` specifies what would the the threshold value for producing an emotion of type ety (e.g. fear). The underlying calculation first infers a raw intensity (also called potential intensity). The threshold is subtracted from this raw intensity value. This becomes the output intensity, but only if it is non-negative.

  For the exact formula used to calculate the raw intensity, see [Ansari et al. _An Appraisal Transition System for Event-Driven Emotions in Agent-Based Player Experience Testing_](https://arxiv.org/pdf/2105.05589).

  * `emotionIntensityDecayRule(ety)` specifies has fast intensity decays.

  * `desirabilityAppraisalRule(status,e,g)` specifies how desirable an event e would be, towards achieving the goal g.

  * `eventEffect(e,B)` descrives how the event e would affect the player's (that is, the player that we are modelling) perception on the likelihood on achieving different goals. B is a model of what the player's currently believe (e.g. it might believe that achieving a goal g is still possible).

As an example the [full code of MiniDungeonPlayerCharacterization can be seen here](../src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/MiniDungeonPlayerCharacterization.java).

#### 3. Putting things together

We will first create a test agent and also create an instance of the MiniDungeon game. Then we attach to it agent: a state, an 'environment' that interfaces it with the game, and the event producer we discussed before:

```Java
var agent          = new EmotiveTestAgent("Frodo","Frodo") ;
DungeonApp app     = deployApp() ;

var state = ...
agent. attachState(state)
     . attachEnvironment(new MyAgentEnv(app))
     . attachSyntheticEventsProducer(new MiniDungeonEventsProducer()) ;
```

Next, we need an instance of JOCC. More precisely, we need an instance of its `EmotionAppraisalSystem`. This is the entry class to JOCC. It will need a representation of 'belief'; we can use one provided by the class `OCCBeliefBase`. As 'belief' it stores goals (that you register to it), and for each goal a value representing the perceived/believed likelihood of achieving it in the future. The belief will also hold a reference to the agent's state that records things about the MiniDungeon's state that the agent observed/observed (e.g. information on the player health).

```Java
OCCBeliefBase bbs = new OCCBeliefBase()
			 . attachFunctionalState(state) ;

EmotionAppraisalSystem eas = new EmotionAppraisalSystem(agent.getId())
   . withUserModel(new MiniDungeonPlayerCharacterization())
   . attachEmotionBeliefBase(bbs) ;

... // few other things we need to setup
```

Then, below we attach JOCC/the emotion appraisal system created above to our testagent:

```Java
OCCState emotionState = new OCCState(agent,eas)
     .setEventTranslator(msg -> MiniDungeonEventsProducer.translateAplibMsgToOCCEvent(msg)) ;
agent.attachEmotionState(emotionState) ;
```

Now we are ready to do a test. JOCC itself only provides a system to calculates if, and which, emotions would emerge. JOCC cannot on its own trigger any execution on the MiniDungeon Game. You will need a test scenario, that will interact with the game, e.g. to try to get to the level-0 shrine and cleanse it. Using iv4xr we can program with with goals. The game MiniDungeon already comes with a set of basic goals (and tactics). Let's use this to program a test scenario. The scenario below guides the test-agent to automatically play the game. It is programmed to first get a scroll with id `S0_1`, then it goes to the shrine of level-0, and use the scroll there (which then would cleanse the shrine).

```Java
var goalLib = new GoalLib(); // MiniDungeon's goal-library
var G = SEQ(goalLib.smartEntityInCloseRange(agent, "S0_1"),
   goalLib.entityInteracted("S0_1"),
   goalLib.smartEntityInCloseRange(agent, "SM0"),
   goalLib.entityInteracted("SM0"),
   SUCCESS());

agent.setGoal(G)   
```

Now we are ready to run the test. We run the agent in an update-loop, until the goal G is either achieved or failed. After each update you can inspect the emotion-state, e.g. to check is a certain emotion is present:

```Java
while (G.getStatus().inProgress()) {
  agent.update();
  // the agent does a single update, after which we can inspect the emotion-state
  // e.g. to check if emotionState.fear() > 0
}
```

#### 4. Verifying UX property

We can also use Linear Temporal Logic (LTL) to express a UX requirement as an LTL formula and then check it on a run such as the one above, as shown below:

```Java
// eventually there is an increase in fear's intensity:
LTL<SimpleState> f1 = eventually(S ->
   getEmotionState(S).difFear(gCleansedName) != null
   && getEmotionState(S).difFear(gCleansedName) > 0) ;

// eventually there is an increase in distress' intensity:
LTL<SimpleState> f2 = eventually(S ->
    getEmotionState(S).difDistress(gCleansedName) != null
    && getEmotionState(S).difDistress(gCleansedName) > 0) ;
```

We can now add these formulas to the agent, and asks it to re-run the previous scenario:

```Java
agent.addLTL(f1,f2) ;
while (G.getStatus().inProgress()) agent.update();
assertTrue(agent.evaluateLTLs()) ;
```

### API References

### Relevant papers

[_An Appraisal Transition System for Event-Driven Emotions in Agent-Based Player Experience Testing_](https://arxiv.org/pdf/2105.05589), Ansari, Prasetya, Dastani, Dignum, Keller. In
International Workshop on Engineering Multi-Agent Systems (EMAS), 2021.
