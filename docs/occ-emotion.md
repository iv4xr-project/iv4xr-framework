## Emotive Testing

Emotive testing allows an _emotion appraisal system_ to be attached to a  test-agent so that the agent also feels emotions as it interacts with a System Under Test (SUT) during a test case run. It 'feels' in the sense that it can simulate the emotions of an actual user

A test agent with such 'emotions' is called an _emotive test agent_. An emotion appraisal system essentially consists of a general part that models how emotions emerges and their intensity, and a domain and user-specific model that specifies e.g. which observations that a test agent makes while interacting with the SUT would be relevant for determining emotions, along with emotion-related parameters for specific user-types (e.g. the model of new users might be configured to have low emotion-related thresholds, whereas a model of more experienced users can be given higher thresholds).

Note that such an appraisal system only simulates emotions. It does not actively influence the agent to respond to these emotions; this part is up to the programming of the test agent itself.

Current we provide two (??) emotion appraisal systems

### Example

```java
public class MyUserCharacterization extends UserCharacterization {
  ...
}
```

```java
var emotionAppraisalSystem = new EmotionAppraisalSystem("agentSmith") ;
eas.addGoal("goal1",10)
   .addGoal("goal2",5)
   .withUserModel(new MyUserCharacterization())
```

```java
var emotiveAgent = new EmotiveTestAgent("agentSmith","role") ;

var occState = new OCCState(emotiveAgent,emotionAppraisalSystem) ;

emotiveAgent.attachState(state)
  .attachEnvironment(env)
  .setTestDataCollector(testdata)
  .attachSyntheticEventsProducer(evensGenerator)
  .attachEmotionState(occState)

emotiveAgent.setGoal(G)
```

```java
for(int cycle i=0; i<maxBudget; cycle++) {
  emotiveAgent.update() ;
  var emotions = emotiveAgent.getEmotionState().getCurrentEmotion() ;
  // do something with the emotions :)
}
```

### API References

### Relevant papers
