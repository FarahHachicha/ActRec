import pandas as pd
from mlxtend.frequent_patterns import apriori, association_rules
import os
import sys

parameter1 = sys.argv[1]
parameter2 = sys.argv[2]
#parameter1 = int(parameter1)
Data = pd.read_csv(f'{parameter2}/training_data2.csv',header=None)
folder_path = f'{parameter2}/Round{parameter1}/GroundTruth' 

 # Spécifiez le chemin du dossier ici

GroundTruth =[]
if os.path.exists(folder_path) and os.path.isdir(folder_path):
    files = os.listdir(folder_path)
    for file in files:
        file_path = os.path.join(folder_path, file)
        if os.path.isfile(file_path):
            file_name = os.path.basename(file_path).replace("__","/")
            GroundTruth.append(file_name)
  
else:
    print("Le dossier spécifié n'existe pas ou n'est pas un dossier.")

Transactions = []
for _,row in Data.iterrows() :
    
    project = str(row[0]).split(',')[0]

    if project not in GroundTruth:
        valeurs = [v.strip() for v in str(row[0]).split(",")][1:]
            
        Transactions.append(valeurs)

# Conversion des données en encodage one-hot
encoded_transactions = []
for transaction in Transactions:
    encoded_transactions.append({item: True for item in transaction})

# Convertir en dataframe binaire

Data_train = pd.DataFrame(encoded_transactions).fillna(False)
frequent_itemsets = apriori(Data_train, min_support=0.01, use_colnames=True)

#Confidence plus haut est plus performant mais il faut plus de 50 %
rules = association_rules(frequent_itemsets, 
                          metric="support", 
                          min_threshold=0.01)


#final_data = final_data.reset_index(drop =True)
data_columns = ['Projet', 'Action']
GroundTruth_Data = pd.DataFrame(columns=data_columns)
Query_Data = pd.DataFrame(columns=data_columns)

for _,row in Data.iterrows():
    project = str(row[0]).split(',')[0]

    if project in GroundTruth:
        project = project.replace("/","__")
        # Lecture du fichier GroundTruth/project
        with open(f'{parameter2}/Round{parameter1}/GroundTruth/{project}', 'r') as file:
            ground_truth_actions = file.readlines()
      

        # Création d'une liste pour stocker les projets et les actions
        project_actions = []
        query_actions= []
        # Parcourir chaque ligne du fichier
        for line in ground_truth_actions:
            line = line.strip() 
            if line[0].isdigit:
                line = line[1:]
                line = line.strip() # Supprimer les espaces vides et les sauts de ligne
            if line.startswith('#DEP#'):
                # Extraire le nom de l'action en supprimant la partie '#DEP#'
                action = line.replace('#DEP#', '')
                # Ajouter le projet et l'action à la liste
                project_actions.append([project, action])
    
        # Création d'une dataframe temporaire pour les actions existantes
        temp_df = pd.DataFrame(project_actions, columns=data_columns)
        # Ajouter les actions existantes à la dataframe GroundTruth_Data
        GroundTruth_Data = pd.concat([GroundTruth_Data, temp_df], ignore_index=True)
     

        # Récupération des actions existantes dans Data
        existing_actions = [v.strip() for v in str(row[0]).split(',')][1:]
      

        # Ajouter les actions existantes à la dataframe QUERY
        for action in temp_df['Action']:
            existing_actions.remove(action)
        for action in existing_actions:
            query_actions.append([project, action])
        temp_df2 = pd.DataFrame(query_actions, columns=data_columns)
        Query_Data = pd.concat([Query_Data,temp_df2], ignore_index=True)
def get_existing_confidence(file_path, action):
    with open(file_path, 'r') as file:
        for line in file:
            if line.startswith(f"#DEP#{action}\t"):
                _, confidence = line.strip().split('\t')
                return float(confidence)
    # Return a default confidence value if the action is not found
    return 0.0
def update_confidence(file_path, action, confidence):
    updated_lines = []
    with open(file_path, 'r') as file:
        for line in file:
            if line.startswith(f"#DEP#{action}\t"):
                line = f"#DEP#{action}\t{confidence}\n"
            updated_lines.append(line)
    
    with open(file_path, 'w') as file:
        file.writelines(updated_lines)
# Parcourir chaque projet dans QUERY

for project in Query_Data['Projet'].unique():

    # Filtrer les lignes correspondantes au projet
    project_rows = Query_Data[Query_Data['Projet'] == project]

    # Extraire les actions déjà présentes
    existing_actions = project_rows['Action'].tolist()

    
    # Filtrer les règles d'association pour les actions déjà présentes
    filtered_rules = rules[rules['antecedents'].apply(lambda x: all(item in existing_actions for item in x))]
    
    
    #filtered_rules = rules[rules['antecedents'].apply(lambda x: set(antecedent) == x)]
    best_recommendations = []
    
    for _, row in filtered_rules.iterrows():
        consequents = row['consequents']
        confidence = row['confidence']      
        for action in consequents:
            # Vérifier si la conséquence existe déjà dans les actions existantes
            if action not in existing_actions:
                best_recommendations.append((action, confidence))
    
    # Créer une dataframe de recommandations pour le projet
    recommendations_df = pd.DataFrame(best_recommendations, columns=['Action', 'Confidence'])
    recommendations_df=recommendations_df.sort_values(by='Confidence', ascending=False)
    recommendations_df = recommendations_df.drop_duplicates(subset='Action',keep='first')      
    recommendations_file = f'{parameter2}/Round{parameter1}/Recommendations/{project}' 
    existing_actions_FC = set()  # Set to store existing actions

    # Read the existing file and extract existing actions
    with open(recommendations_file, 'r') as file:
        for line in file:
            if line.startswith("#DEP#"):
                action, confidence = line.strip().split('\t')
                existing_actions_FC.add(action.lstrip("#DEP#"))

    # Write new recommendations to the file, updating actions if necessary
    if(len(existing_actions_FC) < 10 ):
        
        with open(recommendations_file, 'a') as file:
            for _, row in recommendations_df.iterrows():
                action = row['Action']
                confidence = row['Confidence']

                if action not in existing_actions_FC:
                    existing_actions_FC.add(action)
                    file.write(f"#DEP#{action}\t{confidence}\n")

