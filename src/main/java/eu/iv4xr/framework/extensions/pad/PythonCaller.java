package eu.iv4xr.framework.extensions.pad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PythonCaller {
	
	//String command = "python /c start python /Users/stuff/Desktop/PAD_emotion_game/map_generator.py";
    //Process p = Runtime.getRuntime().exec(command);
	public ArrayList<String> runPythonFile(String file_path) throws IOException, InterruptedException {
	    String command = "/anaconda3/bin/python3 " + file_path;
	    Process p = Runtime.getRuntime().exec(command);
	    ArrayList<String> arr = new ArrayList<>();
	    p.waitFor();
	    BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	          String line;
	        while ((line = bri.readLine()) != null) {
	            System.out.println(line);
	            arr.add(line);
	          }
	          bri.close();
	          while ((line = bre.readLine()) != null) {
	            System.out.println(line);
	          }
	          bre.close();
	          p.waitFor();
	
	    p.destroy();
	    
	    return arr;
    
	}

	
	
	public static void main(String args[]) throws IOException, InterruptedException{  
		PythonCaller test_runner = new PythonCaller();
		
		String file_path = "predictor.py";
		
		test_runner.runPythonFile(file_path);
		 
	}  
		
	

}








