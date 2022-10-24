# TESTAR scriptless exploratory testing agent

## TESTAR tool
TESTAR is a scriptless testing tool [TESTAR_iv4xr](https://github.com/iv4xr-project/TESTAR_iv4xr) that works as an exploratory agent in the iv4xr-framework.  
TESTAR connects with the XR System Under Test, recognizes all virtual entities (in an observation range), derives possible actions to reach or interact with said entities, and automatically selects actions to explore the virtual environment.  

To run the TESTAR agent, it is important to take into account some dependencies:

### pom.xml dependency
We use jitpack with java 11 in the main TESTAR repository [TESTAR_iv4xr](https://github.com/iv4xr-project/TESTAR_iv4xr) to build the libraries.  
We can indicate the desired TESTAR version in the pom.xml file :
```
    <dependency>
        <groupId>com.github.iv4xr-project</groupId>
        <artifactId>TESTAR_iv4xr</artifactId>
        <version>v3.4</version>
    </dependency>
```

### windows.dll dependency
TESTAR uses Windows Accessibility API to manage XR systems processes in Windows environments, take Windows systems screenshots, and create a Canvas with Spy mode to paint visual information.  
Currently, this implementation is available by using the dynamic link library `resources/windows10/windows.dll`  

### TESTAR settings and java protocol
TESTAR test.settings and java protocol define how the agent connects and interacts with the SUT.  
These protocols are a set of directories inside `resources/settings` that contain a java protocol and a test.settings file, on which it is possible to add new directories to create additional protocols.  

### LabRecruits SUT
TESTAR needs to know where the XR system is located (indicated in the test.settings file).  
We needed to extract LabRecruits in the directory `suts/gym/windows`, or change the `SUTConnectorValue` value in the test.settings file.  

## TESTAR usage
TESTAR uses its own action selection algorithm to explore and test XR systems. It does not follow specific goal structures or state machine models for decision-making.  
Based on a protocol, the TESTAR agent will explore the XR system until it reaches the maximum number of actions or the time limit or until an error is found in the system.  

1. Compile the project using maven instructions: `mvn clean install`  
2. Run `TestarExecutor` as a Java application.  

### TestarExecutor
- Prepare the `windows.dll` file  
- Load the desired TESTAR settings  
- Create and execute the TESTAR exploratory agent  

### TestarFactory
- Implement the iv4xr ITestFactory interface  
- Use the loaded settings to compile and run the TESTAR protocol as an exploratory agent   

## Others

### OrientDB State Model
TESTAR can infer a state model of observed states and executed actions on an OrientDB database.  

## Papers

* Vos, T. E., Aho, P., Pastor Ricos, F., Rodriguez Valdes, O., & Mulders, A. (2021). *testar–scriptless testing through graphical user interface.* Software Testing, Verification and Reliability, 31(3), e1771.  

* Pastor Ricós, F. (2022). *Scriptless Testing for Extended Reality Systems.* In International Conference on Research Challenges in Information Science (pp. 786-794). Springer, Cham.