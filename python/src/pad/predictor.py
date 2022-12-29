import time
import numpy as np
import math
import random
import glob
from datetime import datetime
import tensorflow as tf
import os
from io import StringIO 
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import SimpleRNN
from tensorflow.keras.layers import LSTM
from tensorflow.keras.layers import BatchNormalization
from tensorflow.keras.layers import InputLayer
from tensorflow.keras.callbacks import EarlyStopping
from numpy.random import seed
from tensorflow.random import set_seed
import matplotlib
matplotlib.use('Qt5Agg')  #'Qt5Agg'
import matplotlib.pyplot as plt
from sklearn.preprocessing import MultiLabelBinarizer
from imblearn.over_sampling import SMOTE 
from imblearn.over_sampling import SVMSMOTE
from imblearn.over_sampling import BorderlineSMOTE
from imblearn.over_sampling import KMeansSMOTE
from imblearn.over_sampling import RandomOverSampler
from imblearn.over_sampling import ADASYN
from imblearn.over_sampling import SMOTENC
from imblearn.under_sampling import RandomUnderSampler
from collections import Counter
from sklearn.ensemble import RandomForestClassifier
from sklearn.datasets import make_classification
from sklearn.metrics import accuracy_score
from sklearn.metrics import confusion_matrix
from sklearn.utils import shuffle
from collections import Counter
import trace_processor
from joblib import dump, load
from numpy import inf
import sys




B_CLASSES = ['n', ' ', 'w', 's', 'a', 'd', 'wa', 'wd', 'sa', 'sd', 'w ', 's ', 'a ', 'd ', 'wa ', 'wd ', 'sa ', 'sd ']



def new_get_processed_data(input_path, output_file, slice_number):

	my_data = np.genfromtxt(input_path, delimiter='_')

	my_output = np.genfromtxt(output_file)
	if len(my_data) > len(my_output):
		my_data = my_data[:-1]
	seconds_since = my_data[:, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]]   #[12, 13, 14]
	my_data = my_data[:, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]]

	my_output = my_output[..., None]



	new_data = np.zeros((int(len(my_data) - slice_number), len(my_data[0])))

	for i in range(0, len(my_data) - slice_number):

		for j in range(len(my_data[0])):

			if (my_data[i + slice_number][j] > 99999 or my_data[i + slice_number][j] < -99999 or my_data[i][j] > 99999 or my_data[i][j] < -99999):
				new_data[int(i/slice_number)][j] = 0

			else:
				new_data[i][j] = my_data[i + slice_number][j] - my_data[i][j]

	seconds_since[seconds_since == inf] = 425 #max distance from the player in a 600x600 map

	new_seconds =np.zeros((int(len(seconds_since) - slice_number), len(seconds_since[0])))

	for i in range(0, len(seconds_since) - slice_number):

		for j in range(len(seconds_since[0])):

			new_seconds[i][j] = seconds_since[i][j]


	new_data = np.concatenate((new_data, new_seconds), axis=1)


	
	forest_output = np.zeros((int(len(my_output) - slice_number), 1))
	neural_output = np.zeros((int(len(my_output) - slice_number), 3))

	for i in range(0, len(my_output) - slice_number):

		if (my_output[i + slice_number][0] - my_output[i][0]) > 0: #slice_number/3:  

			forest_output[i] = 2
			neural_output[i][2] = 1


		elif (my_output[i + slice_number][0] - my_output[i][0]) < 0:    

			forest_output[i] = 0
			neural_output[i][0] = 1

		else:

			forest_output[i] = 0 #1
			neural_output[i][1] = 1




	return new_data, forest_output, neural_output


def new_get_processed_data_no_output(input_path, slice_number):

	my_data = np.genfromtxt(input_path, delimiter='_')

	#print("My Data:", my_data)



	seconds_since = my_data[:, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]]   #[12, 13, 14]
	my_data = my_data[:, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]]


	new_data = np.zeros((int(len(my_data) - slice_number), len(my_data[0])))

	for i in range(0, len(my_data) - slice_number):

		for j in range(len(my_data[0])):

			if (my_data[i + slice_number][j] > 99999 or my_data[i + slice_number][j] < -99999 or my_data[i][j] > 99999 or my_data[i][j] < -99999):
				new_data[int(i/slice_number)][j] = 0

			else:
				new_data[i][j] = my_data[i + slice_number][j] - my_data[i][j]

	seconds_since[seconds_since == inf] = 425 #max distance from the player in a 600x600 map

	new_seconds =np.zeros((int(len(seconds_since) - slice_number), len(seconds_since[0])))

	for i in range(0, len(seconds_since) - slice_number):

		for j in range(len(seconds_since[0])):

			new_seconds[i][j] = seconds_since[i][j]


	new_data = np.concatenate((new_data, new_seconds), axis=1)


	return new_data



def get_behavioural_data(input_path, output_file):

	data_file = open(input_path)


	data = StringIO(data_file.read().replace(', ', '_').replace('[', '').replace(']', ''))


	my_data = np.loadtxt(data, delimiter='_')


	output = str(trace_processor.file_to_actions_translator(output_file)).replace('[[', '').replace(']]', '').replace('\'', '')

	my_output = output.split('], [')

	for action_num in range(len(my_output)):

		my_output[action_num] = my_output[action_num].split(', ')


		if len(my_output[action_num]) == 1:
			my_output[action_num] = 'n'

		if len(my_output[action_num]) == 2:
			my_output[action_num] = my_output[action_num][1] #+ '|'

		if len(my_output[action_num]) == 3:

			my_output[action_num] = my_output[action_num][1] + my_output[action_num][2] #+ '|'

		if len(my_output[action_num]) == 4:
			my_output[action_num] = my_output[action_num][1] + my_output[action_num][2] + my_output[action_num][3] #+ '|'



	my_output = np.array(my_output)


	if len(my_data) > len(my_output):
		my_data = my_data[:-1]


	my_output = my_output[..., None]

	

	n_data = np.delete(my_data[0][12:], 3)

	new_data = np.zeros((len(my_data), len(n_data)))

	for i in range(0, len(my_data)):

		n_data = np.delete(my_data[i][12:], 3)

		for j in range(len(n_data)):

			new_data[i][j] = n_data[j]

	
	forest_output = my_output



	nn_output = np.zeros((len(my_output), len(B_CLASSES)))


	for i in range(0, len(my_output)):

		nn_output[i][B_CLASSES.index(my_output[i])] = 1


	return new_data, forest_output, nn_output




def get_behavioural_input(inputs):

	data = StringIO(inputs.replace(', ', '_').replace('[', '').replace(']', ''))


	my_data = np.loadtxt(data, delimiter='_')

	initial_inputs_to_ignore = 12

	n_data = np.delete(my_data[12:], 3)

	new_data = np.zeros((1, (len(n_data))))

	for i in range(len(n_data)):

		new_data[0][i] = n_data[i]


	return new_data





def predict_traces_on_folder():


	my_data_list = sorted(glob.glob("./Traces/Trainers/Perceptor*.txt"))

	my_output_list = sorted(glob.glob("./Traces/Trainers/Fader*.txt"))



	model = Sequential([
		InputLayer(input_shape = (20,)),
		Dense(30, activation='tanh'),
		Dense(30, activation='tanh'),
		Dense(1)
		])

	model.compile(optimizer = "adam", loss = "mse", metrics=[tf.keras.metrics.MeanSquaredError()])

	es = EarlyStopping(patience=7)


	my_data, my_output = get_processed_data(my_data_list[0], my_output_list[0])

	for i in range(1, len(my_data_list)):

		prov_data, prov_out = get_processed_data(my_data_list[i], my_output_list[i])
		my_data = np.concatenate((my_data, prov_data), axis=0)
		my_output = np.concatenate((my_output, prov_out), axis=0)



	model.fit(my_data, my_output, epochs = 1000, batch_size = 128, validation_split = 0.1, callbacks=[es], shuffle=True)




	#Predicting


	my_data_list = sorted(glob.glob("./Traces/To_Predict/Perceptor*.txt"))

	print(my_data_list)


	my_output_list = sorted(glob.glob("./Traces/To_Predict/Fader*.txt"))



	for i in range(len(my_data_list)):
		# my_data = np.genfromtxt(my_data_list[i], delimiter='_')
		# #my_data = my_data[:-1]
		# my_output = np.genfromtxt(my_output_list[i])
		# my_output = my_output[..., None]

		my_data, my_output = get_processed_data(my_data_list[i], my_output_list[i])


		prediction = model.predict(my_data)


		total_number = 0
		right_polarity_number = 0
		positive_number = 0
		negative_number = 0

		for j in range(len(prediction)):
			print(str(j) + ": " + str(prediction[j]) + "->" + str(my_output[j]))
			total_number += 1
			if ((prediction[j] <= 0) and (my_output[j] <= 0)) or ((prediction[j] > 0) and (my_output[j] > 0)):
				right_polarity_number += 1
			if my_output[j] <= 0:
				negative_number += 1
			if my_output[j] > 0:
				positive_number += 1
		
		
		print("MEAN SQUARED ERROR OF PREDICTION:")
		mse = tf.keras.losses.MeanSquaredError()
		print(mse(my_output, prediction).numpy())
		print("PERCENTAGE OF ALL NEGATIVE:")
		print(negative_number/total_number * 100)
		print("PERCENTAGE OF ALL POSITIVE:")
		print(positive_number/total_number * 100)
		print("PERCENTAGE OF CORRECT GRADIENT RESULTS:")
		print(right_polarity_number/total_number * 100)


def remove_outliers(my_data_list, my_output_list):

	to_remove = []

	for i in range(len(my_output_list)):

		my_output = np.genfromtxt(my_output_list[i])
		remove = True
		for j in my_output:
			if j != 0.0:
				remove = False
		if remove:
			to_remove.append(i)


	inversy = to_remove[::-1]

	for remo in inversy:
		del my_data_list[remo]
		del my_output_list[remo]

	return my_data_list, my_output_list




def binary_neural_trainer(slice_number, balance_data):

	dimensions = ["Arousal", "Pleasure", "Dominance"]


	print_list = [[], [], []]

	print_counter = 0


	#sm = SMOTE(random_state=42)
	#sm = SVMSMOTE(random_state=42)
	#sm = BorderlineSMOTE(random_state=42)
	#sm = KMeansSMOTE(random_state=42)
	sm = RandomOverSampler(random_state=42)
	#sm = ADASYN(random_state=42)
	#sm = RandomUnderSampler(random_state=42)


	print("Slice Number: ", slice_number)

	print("Balancing: ", balance_data)



	print_list = [[], [], []]

	print_counter = 0



	for dim in dimensions:

		print("\n\n\n\n\n\n\n\n\n\nDimension: " + dim)


		dirty_data_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_Perceptor*.txt"))

		dirty_output_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_" + dim + "*.txt"))

		my_data_list, my_output_list = remove_outliers(dirty_data_list, dirty_output_list)

		my_data_list, my_output_list = shuffle(my_data_list, my_output_list)

		dim_accuracy = 0

		total_accuracy = 0

		sum_confusion_matrix = np.zeros((2,2))



		model = Sequential([
			InputLayer(input_shape = (40,)),
			Dense(30, activation='relu'),
			Dense(20, activation='relu'),
			Dense(1, activation='sigmoid')
			])

		model.compile(optimizer = "adam", loss = "binary_crossentropy", metrics=['accuracy'])

		es = EarlyStopping(patience=20, monitor ="val_accuracy")



		prediction_data_list = my_data_list[int(len(my_data_list)*0.8):]
		prediction_output_list = my_output_list[int(len(my_output_list)*0.8):]

		training_data_list = my_data_list[:int(len(my_data_list)*0.8)]
		training_output_list = my_output_list[:int(len(my_output_list)*0.8)]


		print("Total Traces: ", len(my_data_list))
		print("Prediction Traces: ", len(prediction_data_list))
		print("Training Traces: ", len(training_data_list))



		my_data, binary_output, my_output = new_get_processed_data(training_data_list[0], training_output_list[0], slice_number)

		for i in range(1, len(training_data_list)):



			prov_data, prov_bin_out, prov_out = new_get_processed_data(training_data_list[i], training_output_list[i], slice_number)
			my_data = np.concatenate((my_data, prov_data), axis=0)
			binary_output = np.concatenate((binary_output, prov_bin_out), axis=0)


		# for bina in binary_output:
		# 	print(bina)
		# exit()


		balanced_my_data, balanced_my_output = sm.fit_resample(my_data, binary_output)

		model.fit(balanced_my_data, balanced_my_output, epochs = 1000, batch_size = 30, validation_split = 0.2, callbacks=[es], shuffle=True)



		

		#Predicting

		my_data, binary_output, my_output = new_get_processed_data(prediction_data_list[0], prediction_output_list[0], slice_number)

		for i in range(1, len(prediction_data_list)):

			prov_data, prov_bin_out, prov_out = new_get_processed_data(prediction_data_list[i], prediction_output_list[i], slice_number)
			my_data = np.concatenate((my_data, prov_data), axis=0)
			binary_output = np.concatenate((binary_output, prov_bin_out), axis=0)

		prediction = model.predict(my_data)



		p = np.rint(prediction)
		#p[np.arange(len(prediction)), prediction.argmax(1)] = 1





		acc = accuracy_score(binary_output, p)

		conf_mat = confusion_matrix(binary_output, p, labels = [0., 1.])



		print("Accuracy: ", acc)

		print("Confusion Matrix: \n", conf_mat)



		total_accuracy += acc

		sum_confusion_matrix = sum_confusion_matrix + conf_mat

		# dirty_data_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_Perceptor*.txt"))

		# dirty_output_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_" + dim + "*.txt"))

		# my_data_list, my_output_list = remove_outliers(dirty_data_list, dirty_output_list)


		print_list[print_counter].append("\n\n\n\n-----> " + dim)

		print_list[print_counter].append("\n\n Accuracy:")

		print_list[print_counter].append(total_accuracy)

		print_list[print_counter].append("\n\n Confusion Matrix:")

		print_list[print_counter].append(sum_confusion_matrix)
		


		print_counter += 1


	#The final printing

	for i in range(3):
		for text in print_list[i]:
			print(text)






def new_leave_one_out(slice_number, balance_data):

	dimensions = ["Arousal", "Pleasure", "Dominance"]


	print_list = [[], [], []]

	print_counter = 0


	#sm = SMOTE(random_state=42)
	#sm = SVMSMOTE(random_state=42)
	#sm = BorderlineSMOTE(random_state=42)
	#sm = KMeansSMOTE(random_state=42)
	#sm = RandomOverSampler(random_state=42)
	#sm = ADASYN(random_state=42)
	sm = RandomUnderSampler(random_state=42)




	print("Slice Number: ", slice_number)

	print("Balancing: ", balance_data)





	print_list = [[], [], []]

	print_counter = 0



	for dim in dimensions:

		print("\n\n\n\n\n\n\n\n\n\nDimension: " + dim)


		dirty_data_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_Perceptor*.txt"))

		dirty_output_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_" + dim + "*.txt"))

		my_data_list, my_output_list = remove_outliers(dirty_data_list, dirty_output_list)

		dim_accuracy = 0

		forest_accuracy = 0

		rangy = len(my_data_list)

		mse_sum = 0 
		negative_per_sum = 0
		positive_per_sum = 0
		correct_pol_sum = 0
		forgiving_pol_sum = 0
		sum_confusion_matrix = np.zeros((3,3))


		

		for k in range(rangy):



			clf = RandomForestClassifier(n_estimators = 300, random_state = 42, criterion = "gini")  #class_weight = "balanced"

			print(my_output_list[k])

			prediction_data_list = [my_data_list[k]]
			prediction_output_list = [my_output_list[k]]

			del my_data_list[k]
			del my_output_list[k]



			my_data, forest_output, neural_output = new_get_processed_data(my_data_list[0], my_output_list[0], slice_number)



			for i in range(1, len(my_data_list)):

				prov_data, prov_forest, neural_output  = new_get_processed_data(my_data_list[i], my_output_list[i],slice_number)
				my_data = np.concatenate((my_data, prov_data), axis=0)
				forest_output = np.concatenate((forest_output, prov_forest), axis=0)


			if balance_data:

				balanced_my_data_forest, balanced_my_output_forest = sm.fit_resample(my_data, forest_output)
			
			else:

				balanced_my_data_forest = my_data
				balanced_my_output_forest = forest_output


			clf.fit(balanced_my_data_forest, balanced_my_output_forest)



			my_data, forest_output, neural_output  = new_get_processed_data(prediction_data_list[0], prediction_output_list[0], slice_number)




			forest_predict = clf.predict(my_data)


			for_acc = accuracy_score(forest_output, forest_predict)

			conf_mat = confusion_matrix(forest_output, forest_predict, labels = [0., 1., 2.])



			print(clf.feature_importances_)

			print("Accuracy: ", for_acc)

			print(conf_mat)




			forest_accuracy += for_acc

			sum_confusion_matrix = sum_confusion_matrix + conf_mat

			dirty_data_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_Perceptor*.txt"))

			dirty_output_list = sorted(glob.glob("./First_Study/" + dim + "/Traces_" + dim + "*.txt"))

			my_data_list, my_output_list = remove_outliers(dirty_data_list, dirty_output_list)

			

		print_list[print_counter].append("\n\n\n\n-----> " + dim)

		print_list[print_counter].append("\n\n Random Forest Accuracy:")

		print_list[print_counter].append(forest_accuracy/rangy)

		print_list[print_counter].append("\n\n Random Forest Confusion Matrix:")

		print_list[print_counter].append(sum_confusion_matrix/rangy)
		

		print_counter += 1

	#The final printing

	for i in range(3):
		for text in print_list[i]:
			print(text)



def predict_with_model(slice_number, trained_model_file, dim, file_location):

 
	trained_model = load(trained_model_file)

	data_list = sorted(glob.glob(file_location +"/Perceptor*.txt"))

	#print(data_list)

	prediction_list = []


	for input_data in data_list:

		my_data = new_get_processed_data_no_output(input_data, slice_number)

		#print(my_data)

		prediction = trained_model.predict(my_data)

		prediction_list.append(prediction)


	return prediction_list





def test_forests(slice_number, balance_data, save = False, folder = "./First_Study/"):

	dimensions = ["Arousal", "Pleasure", "Dominance"]



	#sm = SMOTE(random_state=42)
	#sm = SVMSMOTE(random_state=42)
	#sm = BorderlineSMOTE(random_state=42)
	#sm = KMeansSMOTE(random_state=42)
	#sm = RandomOverSampler(random_state=42)
	#sm = ADASYN(random_state=42)
	sm = RandomUnderSampler(random_state=42)




	print("Slice Number: ", slice_number)

	print("Balancing: ", balance_data)


	max_acc = 0

	min_samples_leaf_num = 1

	#for min_samples_leaf_num in range(1,60):


	print("########################## ---->", min_samples_leaf_num)



	print_list = [[], [], []]

	print_counter = 0



	for dim in dimensions:

		print("\n\n\n\n\n\n\n\n\n\nDimension: " + dim)


		dirty_data_list = sorted(glob.glob(folder + dim + "/Traces_Perceptor*.txt"))

		dirty_output_list = sorted(glob.glob(folder + dim + "/Traces_" + dim + "*.txt"))

		my_data_list, my_output_list = remove_outliers(dirty_data_list, dirty_output_list)

		dim_accuracy = 0

		forest_accuracy = 0

		rangy = len(my_data_list)

		mse_sum = 0 
		negative_per_sum = 0
		positive_per_sum = 0
		correct_pol_sum = 0
		forgiving_pol_sum = 0
		sum_confusion_matrix = np.zeros((3,3))

		my_data_list, my_output_list = shuffle(my_data_list, my_output_list)


		test_division = 0.2


		clf = RandomForestClassifier(n_estimators = 1000, random_state = 42, criterion = "gini", n_jobs = -1, min_samples_leaf = min_samples_leaf_num, oob_score = True)  #class_weight = "balanced"



		my_data, forest_output, neural_output = new_get_processed_data(my_data_list[0], my_output_list[0], slice_number)



		for i in range(1, len(my_data_list)):

			prov_data, prov_forest, neural_output  = new_get_processed_data(my_data_list[i], my_output_list[i],slice_number)
			my_data = np.concatenate((my_data, prov_data), axis=0)
			forest_output = np.concatenate((forest_output, prov_forest), axis=0)



		prediction_my_data = my_data[:int(len(my_data)*test_division)]

		prediction_forest_output = forest_output[:int(len(forest_output)*test_division)]

		my_data = my_data[int(len(my_data)*test_division):]

		forest_output = forest_output[int(len(forest_output)*test_division):]


		if balance_data:

			balanced_my_data_forest, balanced_my_output_forest = sm.fit_resample(my_data, forest_output)
		
		#print(np.bincount(np.ravel(balanced_my_output_forest).astype(int)))
		else:

			balanced_my_data_forest = my_data
			balanced_my_output_forest = forest_output


		clf.fit(balanced_my_data_forest, balanced_my_output_forest)



		#my_data, forest_output, neural_output  = new_get_processed_data(prediction_data_list[0], prediction_output_list[0], slice_number)


		forest_predict = clf.predict(prediction_my_data)


		for_acc = accuracy_score(prediction_forest_output, forest_predict)

		conf_mat = confusion_matrix(prediction_forest_output, forest_predict, labels = [0., 1., 2.])



		print(clf.feature_importances_)

		print("Accuracy: ", for_acc)

		print(conf_mat)




		forest_accuracy += for_acc

		sum_confusion_matrix = sum_confusion_matrix + conf_mat

		dirty_data_list = sorted(glob.glob(folder + dim + "/Traces_Perceptor*.txt"))

		dirty_output_list = sorted(glob.glob(folder + dim + "/Traces_" + dim + "*.txt"))

		my_data_list, my_output_list = remove_outliers(dirty_data_list, dirty_output_list)

		#Save the predictor
		dump(clf, "./python/src/pad/trained_forest_" + dim + ".pkl")

	



def save_dimension_over_time(file_name):

	#data, output = get_processed_data(input_path, output_file)

	output = np.genfromtxt(file_name)

	fig4 = plt.figure()
	ax4 = fig4.add_subplot(111)

	numbering = range(len(output))

	ax4.plot(numbering, output)


	plt.xlabel("Ticks")
	plt.ylabel("Level")
	fig_name = "Figures/" + file_name.replace('.txt','')
	fig4.savefig(fig_name)




def save_location_over_time(file_name):

	#data, output = get_processed_data(input_path, output_file)

	f = open(file_name)

	output = f.readlines()


	x_list = []
	y_list = []

	for inpy in output:
		x_y = inpy.split("_")
		x_list.append(int(x_y[0]))
		y_list.append(-int(x_y[1].replace('\n','')))




	fig4 = plt.figure()
	ax4 = fig4.add_subplot(111)

	numbering = range(len(output))

	ax4.plot(x_list, y_list)


	plt.xlabel("Level")
	plt.ylabel("Ticks")
	plt.axis('equal')
	fig_name = "Figures/" + file_name.replace('.txt','')
	fig4.savefig(fig_name)



def save_dimension_over_location(location_file, dimension_file, slice_number, show = False):


	f = open(location_file)

	output = f.readlines()
# #######
# 	print(location_file)
# 	print("Len of initial output: ", len(output))
# #######
	x_list = []
	y_list = []

	for inpy in output:
		x_y = inpy.split("_")
		x_list.append(int(x_y[0]))
		y_list.append(-int(x_y[1].replace('\n','')))


	dimension = np.genfromtxt(dimension_file)

	# print("Len of previous dimension: ", len(dimension))

	dimension = dimension[..., None]


# #######
# 	print(dimension)
# 	print("Len of dimension: ", len(dimension))
# #######	


	new_output = np.zeros((int(len(dimension)),1))

	for i in range(0, len(dimension) - slice_number):

		new_output[i][0] = dimension[i + slice_number][0] - dimension[i][0]


	#data, output = get_processed_data(input_path, output_file)

	colour_list = []


# ######	
# 	print(new_output)
# 	print("Len of new output: ", len(new_output))

# 	print("Len of x_list: ", len(x_list))
# ######

	size_dif = len(x_list)/len(new_output)
	# print("Size Dif: ", size_dif)


	counter = 0
	remainder = 0

	for i in range(len(new_output)):

		float_times = size_dif + remainder

		times = math.trunc(float_times)

		remainder = float_times - times
		for _ in range(times):

			colour_code = 'k'

			# if new_output[i][0] <= -1:
			# 	colour_code = 'r'

			if new_output[i][0] >= 1:
				colour_code = 'g'


			colour_list.append(colour_code)

	# print("Len of Colour List: ", len(colour_list))



	while(len(x_list) > len(colour_list)):
		colour_list.append(colour_list[len(colour_list)-1])




	fig4 = plt.figure()
	ax4 = fig4.add_subplot(111)
	img = plt.imread("./Maps/back_map.png")

	ax4.imshow(img, extent=[-190, 2230, -960, 260])



	for i in range(len(x_list)):
		ax4.scatter(x_list[i], y_list[i], c = colour_list[i], alpha=0.1)

	plt.axis('equal')


	plt.xlim([-200, 2200])
	plt.ylim([-1000, 200])
	fig_name = "Figures/" + dimension_file.replace('.txt','') + "_DIMENSION_LOC"
	fig4.savefig(fig_name)

	if show:
		plt.show()


	#idea: use this but with for loops to create a lot of tiny sections (1 or 3 seconds?).
	#Colour each either black (neutral), red (decreasing) or green (rising)

	# x = np.linspace(-10, 10, 1000)
	# y = np.sin(x)

	# # 4 segments defined according to some x properties
	# segment1 = (x<-5)
	# segment2 = (x>=-5) & (x<0)
	# segment3 = (x>=0) & (x<5)
	# segment4 = (x>=5)

	# plt.plot(x[segment1], y[segment1], '-k', lw=2)
	# plt.plot(x[segment2], y[segment2], '-g', lw=2)
	# plt.plot(x[segment3], y[segment3], '-r', lw=2)
	# plt.plot(x[segment4], y[segment4], '-b', lw=2)

	# plt.show()






def save_dimension_over_location_predicted(location_file, prediction_output, slice_number, show = False):


	f = open(location_file)

	output = f.readlines()


	x_list = []
	y_list = []

	for inpy in output:
		x_y = inpy.split("_")
		x_list.append(int(x_y[0]))
		y_list.append(-int(x_y[1].replace('\n','')))


	# dimension = np.genfromtxt(dimension_file)

	# dimension = dimension[..., None]

	# new_output = np.zeros((int(len(dimension)/slice_number),1))

	# for i in range(0, len(dimension) - slice_number, slice_number):

	# 	new_output[int(i/slice_number)][0] = dimension[i + slice_number][0] - dimension[i][0]

	new_output = prediction_output
	#data, output = get_processed_data(input_path, output_file)

	colour_list = []

	for i in range(len(new_output)):

		colour_code = 'k'

		# if new_output[i][0] <= -1:
		# 	colour_code = 'r'

		if new_output[i] >= 1:
			colour_code = 'g'


		colour_list.append(colour_code)


	print("C_list_sizw: ", len(colour_list))

	print("X_list_sizw: ", len(x_list))

	while(len(x_list) > len(colour_list)):
		colour_list.append(colour_list[len(colour_list)-1])




	fig4 = plt.figure()
	ax4 = fig4.add_subplot(111)
	img = plt.imread("./Maps/back_map.png")

	ax4.imshow(img, extent=[-190, 2230, -960, 260])



	for i in range(len(x_list)):
		ax4.scatter(x_list[i], y_list[i], c = colour_list[i], alpha=0.1)

	plt.axis('equal')


	plt.xlim([-200, 2200])
	plt.ylim([-1000, 200])
	fig_name = "Figures/full_pipeline_testing"
	fig4.savefig(fig_name)

	if show:
		print("Showing!")
		plt.show()











def save_location_green(location_file, slice_number):


	f = open(location_file)

	output = f.readlines()


	x_list = []
	y_list = []

	for inpy in output:
		x_y = inpy.split("_")
		x_list.append(int(x_y[0]))
		y_list.append(-int(x_y[1].replace('\n','')))


	fig4 = plt.figure()
	ax4 = fig4.add_subplot(111)
	img = plt.imread("./Maps/back_map.png")

	ax4.imshow(img, extent=[-190, 2230, -960, 260])



	for i in range(len(x_list)):
		ax4.scatter(x_list[i], y_list[i], c = "green", alpha=0.1)

	plt.axis('equal')


	plt.xlim([-200, 2200])
	plt.ylim([-1000, 200])
	fig_name = "Figures/" + location_file.replace('.txt','') + "_GREEN"
	fig4.savefig(fig_name)



def save_location(location_file, slice_number, save_name):


	f = open(location_file)

	output = f.readlines()


	x_list = []
	y_list = []

	for inpy in output:
		x_y = inpy.split("_")
		x_list.append(int(x_y[0]))
		y_list.append(-int(x_y[1].replace('\n','')))


	fig4 = plt.figure()
	ax4 = fig4.add_subplot(111)
	img = plt.imread("./Maps/back_map.png")

	ax4.imshow(img, extent=[-190, 2230, -960, 260])



	for i in range(len(x_list)):
		ax4.scatter(x_list[i], y_list[i], c = "blue", alpha=0.1)

	plt.axis('equal')


	plt.xlim([-200, 2200])
	plt.ylim([-1000, 200])
	fig_name = save_name
	fig4.savefig(fig_name)



def print_fitness_evolution(folder_name):

	files = glob.glob(folder_name + "/*.txt")

	fig4 = plt.figure()
	ax4 = fig4.add_subplot(111)

	for file in files:
		f = open(file)
		output = f.readlines()[3]
		print(output)
		f.close()

		output_list = output.replace("Best Fitness Evolution: [", "").replace("]\n", "").split(", ")
		int_output_list = []
		for element in output_list:
			int_output_list.append(int(element))
		print(int_output_list)
		numbering = range(len(int_output_list))

		ax4.plot(numbering, int_output_list)




	plt.xlabel("Generations")
	plt.ylabel("Fitness")
	plt.ylim([-1500, 0])
	fig_name = "Figures/Personas/fitness_over_time"
	fig4.savefig(fig_name)





def print_images_folder(folder_name, slice_number):


	position_arousal_traces_list = glob.glob(folder_name + "/Arousal/Traces_Position*.txt")
	position_pleasure_traces_list = glob.glob(folder_name + "/Pleasure/Traces_Position*.txt")
	position_dominance_traces_list = glob.glob(folder_name + "/Dominance/Traces_Position*.txt")

	position_traces_list = sorted(position_arousal_traces_list + position_pleasure_traces_list + position_dominance_traces_list)



	arousal_traces_list = glob.glob(folder_name + "/Arousal/Traces_Arousal*.txt")
	pleasure_traces_list = glob.glob(folder_name + "/Pleasure/Traces_Pleasure*.txt")
	dominance_traces_list = glob.glob(folder_name + "/Dominance/Traces_Dominance*.txt")

	dimension_traces_list = sorted(arousal_traces_list + pleasure_traces_list + dominance_traces_list)

	for i in range(len(position_traces_list)):

		print(i)
		save_dimension_over_time(dimension_traces_list[i])
		save_dimension_over_location(position_traces_list[i], dimension_traces_list[i], slice_number)
		save_location_green(position_traces_list[i], slice_number)








def number_crawlwer():
	number_list = glob.glob("First_Study/Student_Number/*.txt")

	writy = open("number_file.txt", "w")

	for numb in number_list:
		f = open(numb)
		number = f.readlines()

		writy.write(number[0])

		# for n in number:
		# 	print(n)



def behavioural_trainer():



	print_list = [[], [], []]

	print_counter = 0



	my_data_list = sorted(glob.glob("./B_Predictors/Perceptor*.txt"))


	#This must now be the actions, not the dimensions
	my_output_list = sorted(glob.glob("./First_Study/*/Traces_Actions*.txt"), key=lambda x: x.split('/')[3])



	dim_accuracy = 0

	forest_accuracy = 0

	rangy = len(my_data_list)


	sum_confusion_matrix = np.zeros((3,3))


	

	clf = RandomForestClassifier(n_estimators = 100, random_state=42, criterion = "gini")  #class_weight = "balanced"



	my_data, forest_output, nn_output = get_behavioural_data(my_data_list[0], my_output_list[0])



	for i in range(1, len(my_data_list)):

		prov_data, prov_forest, prov_nn = get_behavioural_data(my_data_list[i], my_output_list[i])
		my_data = np.concatenate((my_data, prov_data), axis=0)
		forest_output = np.concatenate((forest_output, prov_forest), axis=0)
		nn_output = np.concatenate((nn_output, prov_nn), axis=0)


	print(my_data.shape)
	print(forest_output.shape)
	print(nn_output.shape)



	clf.fit(my_data, forest_output)

	dump(clf, 'trained_forest.pkl')

	#print(clf.feature_importances)







def behaviour_predict(inputs, trained_model):

	processed_input = get_behavioural_input(inputs)

	prediction = trained_model.predict(processed_input)


	return prediction

















def main():
	
	seed(7)
	set_seed(7)

	slice_number = 4
	balance_data = True



	if len(sys.argv) > 1:

		if sys.argv[1] == "just_train":
			if len(sys.argv) < 3:
				print("Arguments missing!")
				exit()
			else:
				test_forests(slice_number, balance_data, save = False, folder = sys.argv[2])

		if sys.argv[1] == "train_and_save":
			if len(sys.argv) < 3:
				print("Arguments missing!")
				exit()
			else:
				test_forests(slice_number, balance_data, save = True, folder = sys.argv[2])

		elif sys.argv[1] == "predict":
			if len(sys.argv) < 5:
				print("Arguments missing!")
				exit()
			else:
				if sys.argv[2] == "Arousal":
					print(predict_with_model(slice_number, sys.argv[4], sys.argv[2], sys.argv[3]))
				elif sys.argv[2] == "Pleasure":
					print(predict_with_model(slice_number, sys.argv[4], sys.argv[2], sys.argv[3]))
				elif sys.argv[2] == "Dominance":
					print(predict_with_model(slice_number, sys.argv[4], sys.argv[2], sys.argv[3]))
	else:

		print("Please use either the 'train_model' or 'predicting_using_trained_model' arguments")

if __name__=="__main__":
	main()







