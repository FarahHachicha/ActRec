**DataSet.csv**
`DataSet.csv` contains the initial dataset collected, comprising 50,315 GitHub projects that utilize GitHub Actions, and 12,741 actions used by these projects.

**DataSet_Preprocessed.csv**
`DataSet_Preprocessed.csv` contains the dataset after preprocessing:

__For projects:__

Projects that do not contain any action (rows with only zeros) have been removed.
__For actions:__

Only the names of the actions are retained, removing any additional details(versions, Owner name ..)

The preprocessed dataset now consists of 49,673 projects and 4,436 distinct actions. The projects are represented in rows, and the actions are represented in columns. The value "1" in the cell indicates that the corresponding action exists in the respective project, while "0" indicates its absence.

**Projects.csv**
The `Projects.csv` dataset contains comprehensive information for each GitHub project, providing valuable insights into their characteristics and attributes. It includes details such as the number of forks, the number of stars, and the list of programming languages, etc used in each project.

**Actions.csv**
The `Actions.csv` dataset is a comprehensive collection of information for each action available in the GitHub Marketplace. It offers crucial details such as the name of the action, its primary and secondary categories, the list of available versions, and the number of stars it has received from the GitHub community. Out of a total of 4,436 actions, a subset of 3,300 actions exists in the GitHub Marketplace.

**D2.csv**
`D2.csv` is a CSV file containing the filtered DataSet that has undergone preprocessing. The dataset includes GitHub projects that exhibit a more complex and diverse usage of GitHub Actions, as it excludes projects with only a single action. After applying this filtering criteria, the dataset consists of 45 587 GitHub projects and 4,355 distinct actions.

The filtering process was employed to exclude projects with only one action as they have been observed to have a negative influence on the performance and reliability of our approach. Such projects might lack the necessary complexity and variability required for a comprehensive analysis of GitHub Actions usage patterns.

By focusing on projects with at least two actions, we ensure that the dataset contains a broader range of scenarios and interactions between actions, enabling us to draw more meaningful and accurate conclusions from the analysis.