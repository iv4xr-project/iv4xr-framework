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
	    
		
		String query = "./python/src/pad/predictor.py just_train " + folder_path;
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	/**
	 * Train and save a PAD model.
	 * @param folder_path ???
	 * @return ???
	 */
	public ArrayList<String> trainAndSaveModel(String folder_path) throws IOException, InterruptedException {
	    
		
		String query = "./python/src/pad/predictor.py train_and_save " + folder_path;
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	/**
	 * Predict emotion using a trained PAD model.
	 * @param folder_path   the folder where the model is located.
	 * @param trained_model_file the file name of the model.
	 * @return ???
	 */
	public ArrayList<String> predictWithTrainedModel(String folder_path, String pad_dimension, String trained_model_file) throws IOException, InterruptedException {
	    
		
		String query = "./python/src/pad/predictor.py predict " + pad_dimension + " " + folder_path + " " + trained_model_file;
		System.out.println(query);
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	// just for test
	public static void main(String args[]) throws IOException, InterruptedException{  
		PADModel testy = new PADModel();
		
		String folder_path = "./python/src/pad/First_Study/";
		
		testy.trainAndSaveModel(folder_path);
		
		//String folder_path = "./python/src/pad/Traces/";
		//String trained_model_file = "./python/src/pad/trained_forest_Arousal.pkl";
		//String pad_dimension = "Arousal";
		
		//System.out.println(testy.predictWithTrainedModel(folder_path, pad_dimension, trained_model_file));
		
		 
	}  

}
