package eu.iv4xr.framework.extensions.pad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Provide methods to train and use the a PAD model. Note that the library is
 * actually implemented in Python. This class just provides methods to call
 * the key scripts from that library.
 * 
 * This seems to assume the scripts are in the project root!! FIX THIS.
 */
public class PADModel {
	
	
	PythonCaller p_caller = new PythonCaller();
	
	
	/**
	 * Train a PAD model.
	 * @param folder_path  ???
	 * @return  ???
	 */
	public ArrayList<String> trainModel(String folder_path) throws IOException, InterruptedException {
	    
		
		String query = "predictor.py just_train " + folder_path;
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	/**
	 * Train and save a PAD model.
	 * @param folder_path ???
	 * @return ???
	 */
	public ArrayList<String> trainAndSaveModel(String folder_path) throws IOException, InterruptedException {
	    
		
		String query = "predictor.py train_and_save " + folder_path;
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	/**
	 * Predict emotion using a trained PAD model.
	 * @param folder_path   the folder where the model is located.
	 * @param trained_model_file the file name of the model.
	 * @return ???
	 */
	public ArrayList<String> predictWithTrainedModel(String folder_path, String trained_model_file) throws IOException, InterruptedException {
	    
		
		String query = "predictor.py predict " + folder_path + " " + trained_model_file;
		System.out.println(query);
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	// just for test
	public static void main(String args[]) throws IOException, InterruptedException{  
		PADModel testy = new PADModel();
		
		//String folder_path = "./data/First_Study/";
		
		//testy.trainAndSaveModel(folder_path);
		
		String folder_path = "./data/Traces/";
		String trained_model_file = "./trained_forest_Arousal.pkl";
		
		System.out.println(testy.predictWithTrainedModel(folder_path, trained_model_file));
		
		 
	}  

}
