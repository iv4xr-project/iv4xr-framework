import random
import string
import Levenshtein as lv 
import glob
import numpy as np
from sklearn.cluster import AffinityPropagation, AgglomerativeClustering
import os
import shutil
from scipy.cluster.hierarchy import dendrogram
import pickle
import matplotlib
matplotlib.use('Qt5Agg')  #'Qt5Agg'
import matplotlib.pyplot as plt
from tensorflow import keras 
from keras import layers
import sklearn
from predictor import *
from collections import Counter
from nltk import ngrams, edit_distance
import edlib

from keras.callbacks import TensorBoard

#testing
from keras.datasets import mnist



ACTION_LIST = ['w', 's', 'a', 'd', ' ']

B_CLASSES = ['n', ' ', 'w', 's', 'a', 'd', 'wa', 'wd', 'sa', 'sd', 'w ', 's ', 'a ', 'd ', 'wa ', 'wd ', 'sa ', 'sd ']


def file_to_actions_translator(file_location):

	w_pressed = 0
	s_pressed = 0
	a_pressed = 0
	d_pressed = 0

	attack = 0

	new_actions = []

	f = open(file_location)
	lines = f.readlines()



	for line in lines:
		line_array = eval(line)
		new_line = []
		for i in range(0, len(line_array)):
			if i == 0:
				new_line.append(line_array[i])
			else:
				if line_array[i] == 'dd':
					if a_pressed == 2:
						a_pressed = 1
					d_pressed = 2
					
				if line_array[i] == 'du':
					if a_pressed == 1:
						a_pressed = 2
					d_pressed = 0


				if line_array[i] == 'ad':
					if d_pressed == 2:
						d_pressed = 1
					a_pressed = 2
					
				if line_array[i] == 'au':
					if d_pressed == 1:
						d_pressed = 2
					a_pressed = 0


				if line_array[i] == 'wd':
					if s_pressed == 2:
						s_pressed = 1
					w_pressed = 2
					
				if line_array[i] == 'wu':
					if s_pressed == 1:
						s_pressed = 2
					w_pressed = 0


				if line_array[i] == 'sd':
					if w_pressed == 2:
						w_pressed = 1
					s_pressed = 2
					
				if line_array[i] == 'su':
					if w_pressed == 1:
						w_pressed = 2
					s_pressed = 0

				if line_array[i] == ' ':
					attack = 1

		if w_pressed == 2:
			new_line.append('w')
		if s_pressed == 2:
			new_line.append('s')
		if a_pressed == 2:
			new_line.append('a')
		if d_pressed == 2:
			new_line.append('d')
		if attack:
			new_line.append(' ')
			attack = 0

		new_actions.append(new_line)


	return new_actions



def bot_file_to_actions_translator(file_location):

	new_actions = []

	f = open(file_location)
	lines = f.readlines()

	for line in lines:
		splity = line.replace('\'', '').replace('[', '').replace(']', '').replace('\n', '').split(", ")
		new_actions.append(splity[1:])
	return new_actions

def actions_to_file_translator(action_array, new_file_location = None):


	to_write = []

	w_pressed = 0
	s_pressed = 0
	a_pressed = 0
	d_pressed = 0

	attack = 0


	for actions in action_array:

		write_line = []

		if len(actions):
			write_line.append(actions[0])

		if w_pressed:

			w_found = 0
			for i in range(len(actions)):
				if actions[i] == 'w':
					w_found = 1
					break

			if w_found:
				pass
			else:
				write_line.append('wu')
				w_pressed = 0


		if s_pressed:

			s_found = 0
			for i in range(len(actions)):
				if actions[i] == 's':
					s_found = 1
					break

			if s_found:
				pass
			else:
				write_line.append('su')
				s_pressed = 0

		if a_pressed:

			a_found = 0
			for i in range(len(actions)):
				if actions[i] == 'a':
					a_found = 1
					break

			if a_found:
				pass
			else:
				write_line.append('au')
				a_pressed = 0


		if d_pressed:

			d_found = 0
			for i in range(len(actions)):
				if actions[i] == 'd':
					d_found = 1
					break

			if d_found:
				pass
			else:
				write_line.append('du')
				d_pressed = 0






		for i in range(len(actions)):


			if actions[i] == 'w':
				if w_pressed == 0:
					w_pressed = 1
					write_line.append('wd')

			if actions[i] == 's':
				if s_pressed == 0:
					s_pressed = 1
					write_line.append('sd')

			if actions[i] == 'a':
				if a_pressed == 0:
					a_pressed = 1
					write_line.append('ad')

			if actions[i] == 'd':
				if d_pressed == 0:
					d_pressed = 1
					write_line.append('dd')

			if actions[i] == ' ':
				write_line.append(' ')

		to_write.append(write_line)


	if new_file_location != None:

		writy = open(new_file_location, "w+")

		for line in to_write:
			writy.write(str(line) + '\n')

	return to_write





def random_trace_generator(trace_lenght, file_name):

	action_array = []

	action_array.append([])



	for i in range(trace_lenght):
		actions = []
		actions.append(i)
		actions.append(random.choice(ACTION_LIST))
		action_array.append(actions)



	actions_to_file_translator(action_array, "Generated_Traces/" + file_name)



def probability_trace_generator(trace_lenght, file_name, probability_list):

	action_array = []

	action_array.append([])



	for i in range(trace_lenght):
		actions = []
		actions.append(i)
		actions.append(random.choices(ACTION_LIST, weights=probability_list, k=1)[0])
		action_array.append(actions)



	actions_to_file_translator(action_array, "Generated_Traces/" + file_name)


def actions_to_string_translator(action_list):
	
	possible_actions = ['n', ' ', 'w', 's', 'a', 'd', 'wa', 'wd', 'sa', 'sd', 'w ', 's ', 'a ', 'd ', 'wa ', 'wd ', 'sa ', 'sd ']

	action_encoding = list(string.ascii_lowercase)[:len(possible_actions)]

	action_string = ""

	for action in action_list:
		if len(action) == 0:
			pass
		if len(action) == 1:
			action_string += action_encoding[possible_actions.index('n')]
		if len(action) == 2:
			action_string += action_encoding[possible_actions.index(action[1])]
		if len(action) == 3:
			lil_string = str(action[1]) + str(action[2])
			action_string += action_encoding[possible_actions.index(lil_string)]
		if len(action) == 4:
			lil_string = str(action[1]) + str(action[2]) + str(action[3])
			action_string += action_encoding[possible_actions.index(lil_string)]

	return action_string


def lev_distance(string1, string2):
	return lv.distance(string1, string2)


def len_discounted_lev_distance(string1, string2):
	return lv.distance(string1, string2) + (abs(len(string1) - len(string2)))


def levenshtein_distance_between_traces(trace_file_1, trace_file_2):

	actions1 = file_to_actions_translator(trace_file_1)
	actions2 = file_to_actions_translator(trace_file_2)

	string1 = actions_to_string_translator(actions1)
	string2 = actions_to_string_translator(actions2)

	distance = lev_distance(string1, string2)

	return distance


def save_lev_similarity_matrix(glob_file_path):

	words = glob.glob(glob_file_path)

	words = np.asarray(words) #So that indexing with a list will work
	lev_similarity = -1*np.array([[levenshtein_distance_between_traces(w1,w2) for w1 in words] for w2 in words])

	with open('lev_similarity_matrix_Level_1.pkl', 'wb') as file:
		# A new file will be created
		pickle.dump(lev_similarity, file)


def get_lev_similarity_matrix(level):

	if level == "all":

		with open('lev_similarity_matrix.pkl', 'rb') as file:
		  
			# Call load method to deserialze
			lev_similarity = pickle.load(file)
	elif level == "1":
		with open('lev_similarity_matrix_Level_1.pkl', 'rb') as file:
		  
			# Call load method to deserialze
			lev_similarity = pickle.load(file)
	elif level == "2":
		with open('lev_similarity_matrix_Level_2.pkl', 'rb') as file:
		  
			# Call load method to deserialze
			lev_similarity = pickle.load(file)
	elif level == "3":
		with open('lev_similarity_matrix_Level_3.pkl', 'rb') as file:
		  
			# Call load method to deserialze
			lev_similarity = pickle.load(file)
	else:
		raise Exception("Level not found")
		  
	return lev_similarity




def levenshtein_afinity_clustering(glob_file_path):

	words = glob.glob(glob_file_path)

	words = np.asarray(words) #So that indexing with a list will work
	lev_similarity = -1*np.array([[levenshtein_distance_between_traces(w1,w2) for w1 in words] for w2 in words])

	affprop = AffinityPropagation(affinity="precomputed", damping=0.6, max_iter = 500, convergence_iter = 30)
	affprop.fit(lev_similarity)
	counter = 1

	#Delete previous clustering
	folder = "./Clusters"
	for filename in os.listdir(folder):
		file_path = os.path.join(folder, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))


	for cluster_id in np.unique(affprop.labels_):
		print("\n\nCluster Number: ", counter)
		exemplar = words[affprop.cluster_centers_indices_[cluster_id]]
		cluster = np.unique(words[np.nonzero(affprop.labels_==cluster_id)])
		folder_name = "./Clusters/" + str(counter) + "_____" + str(len(cluster))
		os.mkdir(folder_name)
		print("\n\nNumber of Traces in Cluster: ", len(cluster))
		fily = open("./" + str(folder_name) + "/cluster_exemplar.txt", "w")
		fily.write(exemplar)
		for clusty in cluster:
			shutil.copy(clusty, folder_name)
			#Copying the images related to the traversal of the game. We need this to know what is going on
			clusty_id = clusty.split('/')[3][15:-4]
			print(clusty_id)
			images = glob.glob("./Figures/*/*/*"+clusty_id+"_DIMENSION_LOC.png")
			print("GLOB: ", images)
			for img in images:
				shutil.copy(img, folder_name)



		cluster_str = ", ".join(cluster)
		print("Example:   %s:\n Members:  %s" % (exemplar, cluster_str))
		counter += 1





def get_action_lenght_distribution():

	file_name_list = glob.glob("./First_Study/*/Traces_Actions_Level*.txt")

	len_list = []

	for file in file_name_list:
		actions = file_to_actions_translator(file)

		len_list.append(len(actions))


	plt.hist(len_list, bins=400)

	plt.show()









def auto_encoder_3d(compressed_length):
    """
    Function to create the structure of a 3D auto-encoder, using a series of convolution and sampling layers.
    This model is specifically designed to take a lattice with resolution (20x20x20).

    :param compressed_length: desired latent vector size.
    :return: auto-encoder model, as well as the encoder and decoder separately.
    """

    # Constructing the model for the encoder
    encoder_model = Sequential(name="Encoder_"+str(compressed_length))
    encoder_model.add(Conv3D(compressed_length / 4, kernel_size=(3, 3, 3), activation='relu', input_shape=(20, 20, 20, 5)))
    encoder_model.add(MaxPooling3D((2, 2, 2), strides=(2, 2, 2)))
    encoder_model.add(Conv3D(compressed_length / 2, kernel_size=(2, 2, 2), activation='relu'))
    encoder_model.add(MaxPooling3D((2, 2, 2), strides=(2, 2, 2)))
    encoder_model.add(Conv3D(compressed_length, kernel_size=(3, 3, 3), activation='relu'))
    encoder_model.add(MaxPooling3D((2, 2, 2), strides=(2, 2, 2)))
    encoder_model.add(Flatten())

    # Constructing the model for the decoder
    decoder_model = Sequential(name="Decoder_"+str(compressed_length))
    decoder_model.add(Reshape((1, 1, 1, compressed_length)))
    decoder_model.add(UpSampling3D(size=(2, 2, 2)))
    decoder_model.add(Conv3DTranspose(compressed_length / 2, kernel_size=(3, 3, 3), activation='relu'))
    decoder_model.add(UpSampling3D(size=(2, 2, 2)))
    decoder_model.add(Conv3DTranspose(compressed_length / 4, kernel_size=(2, 2, 2), activation='relu'))
    decoder_model.add(UpSampling3D(size=(2, 2, 2)))
    decoder_model.add(Conv3DTranspose(5, kernel_size=(3, 3, 3), activation='sigmoid'))

    # Combining the two models into the auto-encoder model
    ae_input = Input((20, 20, 20, 5))
    ae_encoder_output = encoder_model(ae_input)
    ae_decoder_output = decoder_model(ae_encoder_output)
    ae = Model(ae_input, ae_decoder_output)

    return ae, encoder_model, decoder_model









def actions_to_one_hot_tensor(action_string, tensor_lenght):
	"""
	This function transforns an action string into a one hot representation of it, usable by Keras as input
	"""
	action_encoding = list(string.ascii_lowercase)[:len(B_CLASSES)]


	# The 20 is there to ensure that the down-sizing goes as expected. When it was 18, problems occurred as it wasn't divisible by 2 more that once
	one_hot_tensor = np.zeros((tensor_lenght, 24))


	for i in range(len(action_string)):
		one_hot_tensor[i][action_encoding.index(action_string[i])] = 1

	return one_hot_tensor






def get_hot_tensors_from_file(glob_file_path, level):

	raw_file_names = glob.glob(glob_file_path)

	lev_distance = -1*get_lev_similarity_matrix(level)

	_, file_names = pre_process(lev_distance, raw_file_names, 4000)

	traces = []
	traces_len = []

	for file in file_names:

		action_string = actions_to_string_translator(file_to_actions_translator(file))

		traces.append(action_string)

		traces_len.append(len(action_string))

	max_trace_len = max(traces_len)

	tensors = []
	for i in range(len(traces)):
		tensors.append(actions_to_one_hot_tensor(traces[i], max_trace_len))

	tensors = np.reshape(np.asarray(tensors), (len(tensors), max_trace_len, 24, 1))

	return tensors, max_trace_len, file_names






def get_autoencoder(glob_file_path, encoding_dim, level):

	tensors, max_trace_len, _ = get_hot_tensors_from_file(glob_file_path, level)


	input_s = keras.Input(shape=(max_trace_len, 24, 1))

	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(input_s)
	x = layers.MaxPooling2D((2, 2), padding='same')(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	x = layers.MaxPooling2D((2, 2), padding='same')(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	x = layers.MaxPooling2D((3, 2), padding='same')(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	x = layers.MaxPooling2D((3, 3), padding='same')(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	encoded = layers.MaxPooling2D((7, 1), padding='same')(x)


	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(encoded)
	x = layers.UpSampling2D((7, 1))(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	x = layers.UpSampling2D((3, 3))(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	x = layers.UpSampling2D((3, 2))(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	x = layers.UpSampling2D((2, 2))(x)
	x = layers.Conv2D(1, (3, 3), activation='relu', padding='same')(x)
	x = layers.UpSampling2D((2, 2))(x)
	decoded = layers.Conv2D(1, (3, 3), activation='softmax', padding='same')(x)

	encoder = keras.Model(input_s, encoded)
	autoencoder = keras.Model(input_s, decoded)
	autoencoder.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy']) #categorical?

	autoencoder.summary()

	callback = keras.callbacks.EarlyStopping(monitor='loss', patience=5)

	autoencoder.fit(tensors, tensors,
                epochs=50,
                batch_size=32,
                shuffle=True,
                validation_split = 0.1,
                callbacks=[TensorBoard(log_dir='/tmp/autoencoder'), callback])


	encoder.save("Autoencoder/encoder")
	autoencoder.save("Autoencoder/autoencoder")


	return autoencoder, encoder





def plot_dendrogram(model, **kwargs):
    # Create linkage matrix and then plot the dendrogram

    # create the counts of samples under each node
    counts = np.zeros(model.children_.shape[0])
    n_samples = len(model.labels_)
    for i, merge in enumerate(model.children_):
        current_count = 0
        for child_idx in merge:
            if child_idx < n_samples:
                current_count += 1  # leaf node
            else:
                current_count += counts[child_idx - n_samples]
        counts[i] = current_count

    linkage_matrix = np.column_stack(
        [model.children_, model.distances_, counts]
    ).astype(float)

    # Plot the corresponding dendrogram
    dendrogram(linkage_matrix, **kwargs)



def pre_process(distance_list, words, distance_treshold):

	del_list = []

	for i in range(len(distance_list)):
		if sum(distance_list[i]) / len(distance_list[i]) > distance_treshold:

			del_list.append(i)

	new_list = distance_list
	new_words = words

	for j in reversed(del_list):

		new_list = np.delete(new_list, j, 1)
		new_list = np.delete(new_list, j, 0)

		new_words = np.delete(new_words, j, 0)

	print(len(words)-len(new_words), "traces were removed during pre-processing")

	return new_list, new_words



def levenshtein_hierarchical_clustering(glob_file_path, level):

	words = glob.glob(glob_file_path)

	words = np.asarray(words) #So that indexing with a list will work
	# lev_similarity = -1*np.array([[levenshtein_distance_between_traces(w1,w2) for w1 in words] for w2 in words])


	lev_distance = -1*get_lev_similarity_matrix(level)

	clean_lev_distance, clean_words = pre_process(lev_distance, words, 1000)

	print("Len Before Pre-Process: ", len(lev_distance))
	print("Len After Pre-Process: ", len(clean_lev_distance))
	print("Outliers: ", len(lev_distance) - len(clean_lev_distance))



	hierprop = AgglomerativeClustering(n_clusters = 4, affinity="precomputed", compute_full_tree = True, linkage = "average", compute_distances = True)
	hierprop.fit(clean_lev_distance)
	counter = 1

	print(hierprop.labels_)



	plot_dendrogram(hierprop, truncate_mode="level", p=200)
	plt.xlabel("Number of points in node (or index of point if no parenthesis).")
	plt.show()	

	exit()

	#Delete previous clustering
	folder = "./Clusters"
	for filename in os.listdir(folder):
		file_path = os.path.join(folder, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))


	for cluster_id in np.unique(hierprop.labels_):
		print("\n\nCluster Number: ", counter)
		cluster = np.unique(clean_words[np.nonzero(hierprop.labels_==cluster_id)])
		folder_name = "./Clusters/" + str(counter) + "_____" + str(len(cluster))
		os.mkdir(folder_name)
		print("\n\nNumber of Traces in Cluster: ", len(cluster))
		for clusty in cluster:
			shutil.copy(clusty, folder_name)
			#Copying the images related to the traversal of the game. We need this to know what is going on
			clusty_id = clusty.split('/')[3][15:-4]
			print(clusty_id)
			images = glob.glob("./Figures/*/*/*"+clusty_id+"_DIMENSION_LOC.png")
			print("GLOB: ", images)
			for img in images:
				shutil.copy(img, folder_name)



		cluster_str = ", ".join(cluster)
		#print("Example:   %s:\n Members:  %s" % (exemplar, cluster_str))
		counter += 1

	return hierprop



def encode(glob_file_path, level, encoder_path):
	encoder = keras.models.load_model(encoder_path)

	tensor_list, _, clean_words = get_hot_tensors_from_file(glob_file_path, level)

	print(tensor_list.shape)


	encoded_tensor_list = encoder.predict(tensor_list)


	# print(encoded_tensor_list[100])
	# print(encoded_tensor_list[100].shape)

	return encoded_tensor_list, clean_words
	# for tensor in tensor_list:
	# 	print(tensor.shape)
	# 	exit()
	# 	encoded_tensor = encoder.predict(tensor)
	# 	print(encoded_tensor)
	# 	encoded_tensor_list.append(encoded_tensor)





def encoded_hierarchical_clustering(glob_file_path, level):


	encoded_tensors, clean_words = encode(glob_file_path, level, "Autoencoder/encoder")

	encoded_tensors = np.reshape(encoded_tensors, (len(encoded_tensors), 17))

	print(encoded_tensors[4])

	### Euclidian might be a bad idea!!!!
	hierprop = AgglomerativeClustering(n_clusters = None, distance_threshold = 0.012, affinity="euclidean", compute_full_tree = True, linkage = "ward", compute_distances = True)
	hierprop.fit(encoded_tensors)
	counter = 1

	print(hierprop.labels_)



	plot_dendrogram(hierprop, truncate_mode="level", p=15)
	plt.xlabel("Number of points in node (or index of point if no parenthesis).")
	plt.show()	


	#Delete previous clustering
	folder = "./Clusters"
	for filename in os.listdir(folder):
		file_path = os.path.join(folder, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))


	for cluster_id in np.unique(hierprop.labels_):
		print("\n\nCluster Number: ", counter)
		cluster = np.unique(clean_words[np.nonzero(hierprop.labels_==cluster_id)])
		folder_name = "./Clusters/" + str(counter) + "_____" + str(len(cluster))
		os.mkdir(folder_name)
		print("\n\nNumber of Traces in Cluster: ", len(cluster))
		for clusty in cluster:
			shutil.copy(clusty, folder_name)
			#Copying the images related to the traversal of the game. We need this to know what is going on
			clusty_id = clusty.split('/')[3][15:-4]
			print(clusty_id)
			images = glob.glob("./Figures/*/*/*"+clusty_id+"_DIMENSION_LOC.png")
			print("GLOB: ", images)
			for img in images:
				shutil.copy(img, folder_name)



		cluster_str = ", ".join(cluster)
		#print("Example:   %s:\n Members:  %s" % (exemplar, cluster_str))
		counter += 1

	return hierprop






def encoded_behavioural_hierarchical_clustering():



	my_data_list = sorted(glob.glob("./B_Predictors/Perceptor*.txt"))


	#This must now be the actions, not the dimensions
	my_output_list = sorted(glob.glob("./First_Study/*/Traces_Actions*.txt"), key=lambda x: x.split('/')[3])


	my_data, _, _ = get_behavioural_data(my_data_list[0], my_output_list[0])



	print(my_data[0])








def getParameterList(glob_file_path):

	files = glob.glob(glob_file_path + "*")

	name_list = []
	parameter_list = []

	for file in files:
		with open(file, 'r') as content:
			everything = content.read()
		parameters = everything.split("], ")[-1].replace("]]", "]")

		char_par_in_list = parameters.replace("]", "").replace("[", "").split(", ")
		int_par_in_list = []
		for char in char_par_in_list:
			int_par_in_list.append(int(char))

		final_par_list = int_par_in_list[1:5] + [int_par_in_list[6]] + [int_par_in_list[9]] 

		parameter_list.append(final_par_list)

		#name = file.replace("./All_Trace_Evo/Persona_Evolution/Traces_Actions_", "").replace(".txt", "")
		name_list.append(file)


	return parameter_list, name_list
		





def parameter_based_afinity_clustering(glob_file_path):


	parameter_list, name_list = getParameterList(glob_file_path)

	#words = glob.glob(glob_file_path)

	np_name_list = np.asarray(name_list)

	words = np.asarray(parameter_list) #So that indexing with a list will work
	#lev_similarity = -1*np.array([[levenshtein_distance_between_traces(w1,w2) for w1 in words] for w2 in words])

	affprop = AffinityPropagation(damping=0.9, max_iter = 500, convergence_iter = 30)
	affprop.fit(words)
	counter = 1


	######IM HERE!!!!!


	#Delete previous clustering
	folder = "./Clusters"
	for filename in os.listdir(folder):
		file_path = os.path.join(folder, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))



	for cluster_id in np.unique(affprop.labels_):



		print("\n\nCluster Number: ", counter)
		exemplar = np_name_list[affprop.cluster_centers_indices_[cluster_id]]
		print(np.nonzero(affprop.labels_==cluster_id))

		cluster = np.unique(np_name_list[np.nonzero(affprop.labels_==cluster_id)])
		folder_name = "./Clusters/" + str(counter) + "_____" + str(len(cluster))
		os.mkdir(folder_name)
		print("\n\nNumber of Traces in Cluster: ", len(cluster))
		fily = open("./" + str(folder_name) + "/cluster_exemplar.txt", "w+")
		fily.write(str(exemplar))
		par_fily = open("./" + str(folder_name) + "/cluster_parameters.txt", "a+")
		
		for clusty in cluster:
			print("CLUSTYYYYY ", clusty)
			shutil.copy(clusty, folder_name)
			par_fily.write(str(parameter_list[name_list.index(clusty)])+"\n")
			#Copying the images related to the traversal of the game. We need this to know what is going on
			clusty_id = clusty.split('/')[3][15:-4]
			images = glob.glob("./Figures/*/*/*"+clusty_id+"_DIMENSION_LOC.png")
			print("GLOB: ", images)
			for img in images:
				shutil.copy(img, folder_name)



		cluster_str = ", ".join(cluster)
		print("Example:   %s:\n Members:  %s" % (exemplar, cluster_str))
		counter += 1


def get_pad_classes(dimension_file, slice_number = 1):

	dimension = np.genfromtxt(dimension_file)

	dimension = dimension[..., None]

	new_output = [0]*int(len(dimension))

	for i in range(0, len(dimension) - slice_number):

		if dimension[i + slice_number][0] - dimension[i][0] > 0:
			new_output[i] = 1


	return new_output


def get_pad_raw(dimension_file):

	dimension = np.genfromtxt(dimension_file)

	dimension = dimension[..., None]

	return dimension



def get_pad_expanded_to_action_lenght(glob_file_path):

	dimensions = ["Pleasure", "Arousal", "Dominance"]

	for dim in dimensions:

		files_actions = sorted(glob.glob(glob_file_path + "/" + dim + "*/Traces_Actions*.txt"))
		files_pad =sorted(glob.glob(glob_file_path + "/" + dim + "/Traces_" + dim + "*.txt"))

		for i in range(len(files_actions)):
			actions = list(actions_to_string_translator(file_to_actions_translator(files_actions[i])))
			pad_raw = get_pad_raw(files_pad[i])

			size_dif = len(actions)/len(pad_raw)
			counter = 0
			remainder = 0
			pad_new_list = []

			for j in range(len(pad_raw)):

				float_times = size_dif + remainder
				times = math.trunc(float_times)
				remainder = float_times - times
				for _ in range(times):
					pad_new_list.append(pad_raw[j][0])

			while(len(actions)>len(pad_new_list)):
				pad_new_list.append(pad_new_list[-1])


			f = open("./Expanded_PAD_First_Study/" + dim + "/" + files_pad[i].split('/')[3], 'w+')

			f_act = open("./Expanded_PAD_First_Study/" + dim + "/" + files_actions[i].split('/')[3], 'w+')

			for paddy_boy in pad_new_list:
				f.write(str(paddy_boy))
				f.write("\n")

			for acty_boy in actions:
				f_act.write(str(acty_boy))
				f_act.write("\n")


			f.close()
			f_act.close()


def get_csv_dataset(glob_file_path):

	dimensions = ["Pleasure", "Arousal", "Dominance"]

	for dim in dimensions:

		header = "player_unique_id; map_name; action; " + str(dim) + "; distance_closest_enemy; distance_closest_food_item; distance_closest_coin; number_enemies_view; number_food_item_view; number_coins_view; sum_enemy_values; sum_food_item_values; sum_coins_values; sum_value_slash_distance_enemies; sum_value_slash_distance_food_item; sum_value_slash_distance_coins; seconds_since_enemy; seconds_since_food_item; seconds_since_coin; distance_to_objective; hp; coins_collected; kills; damage_done; wall_perception_vec; coin_perception_vec; cakes_perception_vec; enemy_perception_vec; objective_perception_vec\n"

		files_actions = sorted(glob.glob(glob_file_path + "/" + dim + "*/Traces_Actions*.txt"))
		files_pad = sorted(glob.glob(glob_file_path + "/" + dim + "/Traces_" + dim + "*.txt"))
		files_perceptor = sorted(glob.glob(glob_file_path + "/" + dim + "/Perceptor*.txt"))

		csv_file = open("./Expanded_PAD_First_Study/" + dim + ".csv", "w+")


		csv_file.write(header)


		for i in range(len(files_actions)):

			action_file = open(files_actions[i], "r")
			pad_file = open(files_pad[i], "r")
			perceptor_file = open(files_perceptor[i], "r")

			actions = action_file.read().split('\n')
			pads = pad_file.read().split('\n')
			perceptions = perceptor_file.read().replace('_','').split('\n')

			del perceptions[0]
			del pads[-1]
			del actions[-1]


			player_id = files_actions[i].split('Level')[1][2:].replace('.txt', '')

			level = files_actions[i].split('Actions_')[1][:6]

	
			for j in range(len(actions)):
				# print(str(actions[j]))
				# print(str(pads[j]))
				line = str(player_id) + ';' + str(level) + ';' + actions[j] + ';' + str(pads[j]) + ';' + str(perceptions[j].replace(' \'','').replace('[\'','').replace('\']','').replace('\',', ';'))


				csv_file.write(line)
				csv_file.write('\n')

		csv_file.close()










def fuse_actions_and_affect(glob_file_path):


	########################################
	###			Under Construction
	########################################

	arousal_files_actions = sorted(glob.glob(glob_file_path + "/Arousal/Traces_Actions*.txt"))
	arousal_files_pad =sorted(glob.glob(glob_file_path + "/Arousal/Traces_Arousal*.txt"))

	print(len(arousal_files_actions))
	print(len(arousal_files_pad))

	fused_action_affect_list = []


	for i in range(len(arousal_files_actions)):
		actions = list(actions_to_string_translator(file_to_actions_translator(arousal_files_actions[i])))
		pad_classes = get_pad_classes(arousal_files_pad[i])


		size_dif = len(actions)/len(pad_classes)
		counter = 0
		remainder = 0
		pad_new_list = []

		for j in range(len(pad_classes)):

			float_times = size_dif + remainder
			times = math.trunc(float_times)
			remainder = float_times - times
			for _ in range(times):
				pad_new_list.append(pad_classes[j])

		while(len(actions)>len(pad_new_list)):
			pad_new_list.append(pad_new_list[-1])

		fused_action_affect = []

		for k in range(len(actions)):
			fused_action_affect.append(actions[k])
			fused_action_affect.append(pad_new_list[k])

		print("------------------")
		print(fused_action_affect)
		print("------------------")
	
		fused_action_affect_list.append(fused_action_affect)


	return fused_action_affect_list, arousal_files_actions, arousal_files_pad






def bot_n_gram_hierarical_clustering(glob_file_path):


	file_name_list = glob.glob(glob_file_path)

	string_list = []

	for file in file_name_list:
		actions = list(actions_to_string_translator(bot_file_to_actions_translator(file)))
		string_list.append(actions)


	n_gram_compressed_list, n_gram_meaning = ascending_n_gram_compression(string_list, 2, 1, 25)


	# Cut the K longest elements, which are outliers
	k = 0

	len_list = []

	for string in n_gram_compressed_list:

		len_list.append(len(string))

	for _ in range(k):
		max_index = len_list.index(max(len_list))

		del n_gram_compressed_list[max_index]
		del file_name_list[max_index]
		del len_list[max_index]


	# len_list = []

	# for string in n_gram_compressed_list:

	# 	len_list.append(len(string))

	# plt.hist(len_list, bins=100)

	# plt.show()

	np_name_list = np.asarray(file_name_list)

	lev_dist = np.array([[(edlib.align(w1,w2)['editDistance'] - abs(len(w1)-len(w2)))for w1 in n_gram_compressed_list] for w2 in n_gram_compressed_list])


	hierprop = AgglomerativeClustering(n_clusters = 3, affinity="precomputed", compute_full_tree = True, linkage = "complete", compute_distances = True)
	hierprop.fit(lev_dist)



	plot_dendrogram(hierprop, truncate_mode="level", p=100)
	plt.xlabel("Number of points in node (or index of point if no parenthesis).")
	plt.show()	

	print(lev_dist)

	counter = 0

	#Delete previous clustering
	folder = "./Clusters"
	for filename in os.listdir(folder):
		file_path = os.path.join(folder, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))



	for cluster_id in np.unique(hierprop.labels_):



		print("\n\nCluster Number: ", counter)
		print(np.nonzero(hierprop.labels_==cluster_id))

		cluster = np.unique(np_name_list[np.nonzero(hierprop.labels_==cluster_id)])
		folder_name = "./Clusters/" + str(counter) + "_____" + str(len(cluster))
		os.mkdir(folder_name)
		print("\n\nNumber of Traces in Cluster: ", len(cluster))


		for clusty in cluster:
			print("CLUSTYYYYY ", clusty)
			shutil.copy(clusty, folder_name)
			#Copying the images related to the traversal of the game. We need this to know what is going on
			# clusty_id = clusty.split('/')[1][12:-4]
			# images = glob.glob("./Figures/*/*/*"+clusty_id+"_DIMENSION_LOC.png")
			# print("GLOB: ", images)
			# for img in images:
			# 	shutil.copy(img, folder_name)


		counter += 1
	





def n_gram_hierarical_clustering(glob_file_path):


	file_name_list = glob.glob(glob_file_path)

	string_list = []

	for file in file_name_list:
		actions = list(actions_to_string_translator(file_to_actions_translator(file)))  #file_to_actions_translator
		string_list.append(actions)


	n_gram_compressed_list, n_gram_meaning = ascending_n_gram_compression(string_list, 2, 1, 1)


	# Cut the K longest elements, which are outliers
	k = 11

	len_list = []

	for string in n_gram_compressed_list:

		len_list.append(len(string))

	for _ in range(k):
		max_index = len_list.index(max(len_list))

		del n_gram_compressed_list[max_index]
		del file_name_list[max_index]
		del len_list[max_index]


	# len_list = []

	# for string in n_gram_compressed_list:

	# 	len_list.append(len(string))

	# plt.hist(len_list, bins=100)

	# plt.show()

	np_name_list = np.asarray(file_name_list)

	lev_dist = np.array([[(edlib.align(w1,w2)['editDistance'] - abs(len(w1)-len(w2)))for w1 in n_gram_compressed_list] for w2 in n_gram_compressed_list])


	hierprop = AgglomerativeClustering(n_clusters = 10, affinity="precomputed", compute_full_tree = True, linkage = "complete", compute_distances = True)
	hierprop.fit(lev_dist)



	plot_dendrogram(hierprop, truncate_mode="level", p=200)
	plt.xlabel("Number of points in node (or index of point if no parenthesis).")
	plt.show()	

	print(lev_dist)

	counter = 0

	#Delete previous clustering
	folder = "./Clusters"
	for filename in os.listdir(folder):
		file_path = os.path.join(folder, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))



	for cluster_id in np.unique(hierprop.labels_):



		print("\n\nCluster Number: ", counter)
		print(np.nonzero(hierprop.labels_==cluster_id))

		cluster = np.unique(np_name_list[np.nonzero(hierprop.labels_==cluster_id)])
		folder_name = "./Clusters/" + str(counter) + "_____" + str(len(cluster))
		os.mkdir(folder_name)
		print("\n\nNumber of Traces in Cluster: ", len(cluster))


		for clusty in cluster:
			print("CLUSTYYYYY ", clusty)
			shutil.copy(clusty, folder_name)
			#Copying the images related to the traversal of the game. We need this to know what is going on
			clusty_id = clusty.split('/')[3][15:-4]
			images = glob.glob("./Figures/*/*/*"+clusty_id+"_DIMENSION_LOC.png")
			print("GLOB: ", images)
			for img in images:
				shutil.copy(img, folder_name)


		counter += 1
	



def ascending_n_gram_compression(string_list, gram_number, commonality_treshline, runs):


	# len_list = []

	# for string in string_list:

	# 	len_list.append(len(string))

	# plt.hist(len_list, bins=100)

	# plt.show()

	test_string = 5

	initial_len = len(string_list[test_string])
	initial_string = string_list[test_string].copy()

	n_gram_list = []

	for run in range(runs):

		print("\n\n------> Run Number: ", run)


		found_n_grams = []

		for string in string_list:
			n_grams = ngrams(string, gram_number)
			found_n_grams += n_grams

		ngram_counts = Counter(found_n_grams)
		common_n_grams = ngram_counts.most_common(commonality_treshline)

		n_gram_list.append(common_n_grams)

		print("Most Common 2-Grams:", common_n_grams)

		for i in range(commonality_treshline):
			for string in string_list:
				for j in reversed(range(len(string) - (gram_number - 1))):
					equal = True
					for k in range(gram_number):
						if string[j + k] == common_n_grams[i][0][k]:
							continue
						else:
							equal = False
							break
					if 	equal:	
						string[j] = str(run) + "_" + str(i)
						for k in reversed(range(gram_number - 1)):
							del string[j + k +1]



	# print("Initial String: \n", initial_string)

	# print("Final String: \n", string_list[test_string])

	# print("Initial Lenght: ", initial_len)
	# print("Final Lenght: ", len(string_list[test_string]))

	# print("Possible Symbols in Language: ", 18 + commonality_treshline * runs)

	# len_list = []

	# for string in string_list:

	# 	len_list.append(len(string))

	# plt.hist(len_list, bins=100)

	# plt.show()

	return string_list, n_gram_list




def transform_arrays_into_same_size(array_1, array_2):


	if len(array_1) > len(array_2):
		one_bigger = True
		big_array = array_1
		small_array = array_2
	elif len(array_2) > len(array_1):
		one_bigger = False
		big_array = array_2
		small_array = array_1
	else: #They're the same size already
		return array_1, array_2


	size_dif = len(big_array)/len(small_array)
	counter = 0
	remainder = 0
	expanded_small_array = []

	for j in range(len(small_array)):

		float_times = size_dif + remainder
		times = math.trunc(float_times)
		remainder = float_times - times
		for _ in range(times):
			expanded_small_array.append(small_array[j])

	while(len(big_array)>len(expanded_small_array)):
		expanded_small_array.append(expanded_small_array[-1])


	if one_bigger:
		return big_array, expanded_small_array
	else:
		return expanded_small_array, big_array








if __name__ == '__main__':
	#levenshtein_afinity_clustering("./First_Study/*/Traces_Actions_Level1*.txt")

	#parameter_based_afinity_clustering("./All_Trace_Evo/Persona_Evolution/Traces_Actions_Level3")


	#save_lev_similarity_matrix("./First_Study/*/Traces_Actions_Level1*.txt")


	#levenshtein_hierarchical_clustering("./First_Study/*/Traces_Actions_Level1*.txt", "1")

	#get_action_lenght_distribution()


	#bot_n_gram_hierarical_clustering("./Traces/Bot_Actions_*.txt")

	#fuse_actions_and_affect("./First_Study")


	#get_pad_expanded_to_action_lenght("./First_Study")

	#get_csv_dataset("./Expanded_PAD_First_Study")


	#print(file_to_actions_translator("./First_Study/Pleasure/Traces_Actions_Level1_01-05-2021_19-23-58_257.txt"))


	#array_1 = [1,2,3,4,5,6,7,8,9,10]
	#array_2 = [0,1,1,0,1]


	#print(transform_arrays_into_same_size(array_1, array_2))

	n_gram_hierarical_clustering("./First_Study/*/Traces_Actions_Level1*.txt")


	#get_autoencoder("./First_Study/*/Traces_Actions_Level*.txt", 42, "all")


	#encoded_hierarchical_clustering("./First_Study/*/Traces_Actions_Level*.txt", "all")

	#encoded_behavioural_hierarchical_clustering()










