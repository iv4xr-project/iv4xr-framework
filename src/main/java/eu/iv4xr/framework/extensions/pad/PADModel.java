import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PADModel {
	
	
	PythonCaller p_caller = new PythonCaller();
	
	
	public ArrayList<String> trainModel(String folder_path) throws IOException, InterruptedException {
	    
		
		String query = "predictor.py just_train " + folder_path;
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	public ArrayList<String> trainAndSaveModel(String folder_path) throws IOException, InterruptedException {
	    
		
		String query = "predictor.py train_and_save " + folder_path;
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	public ArrayList<String> predictWithTrainedModel(String folder_path, String trained_model_file) throws IOException, InterruptedException {
	    
		
		String query = "predictor.py predict " + folder_path + " " + trained_model_file;
		System.out.println(query);
		
		return this.p_caller.runPythonFile(query);
    
	}
	
	public static void main(String args[]) throws IOException, InterruptedException{  
		PADModel testy = new PADModel();
		
		//String folder_path = "./data/First_Study/";
		
		//testy.trainAndSaveModel(folder_path);
		
		String folder_path = "./data/Traces/";
		String trained_model_file = "./trained_forest_Arousal.pkl";
		
		System.out.println(testy.predictWithTrainedModel(folder_path, trained_model_file));
		
		 
	}  

}
