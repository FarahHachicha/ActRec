**Extract_DataSet.ipynb**
`Extract_DataSet.ipynb` is a Python script I developed to extract the dataset containing GitHub projects that utilize GitHub Actions. The script verifies the existence of the ``.github/workflows`` directory in each project and extracts the actions used for each project by inspecting the ``YAML`` files under the ``.github/workflows`` directory. It specifically identifies actions using the ``uses`` keyword in the YAML files : An action is an individual task that can be shared for reuse on a public GitHub repository and on the GitHub Marketplace. It utilizes the GitHub API to collect this information within a specified date range. After execution, the script obtained data from 50,315 projects and 12,741 actions.

**Projects_DataSet.ipynb**
`Projects_DataSet.ipynb` is a Python script that focuses on extracting information from the collected projects. It gathers statistics such as the number of stars, the number of contributors, the number of forks, the list of programming languages, etc used in each project.

**Actions_DataSet.ipynb**
`Actions_DataSet.ipynb` is a Python script responsible for preprocessing the previously collected actions data. The script performs the following steps:

1. Removing custom-developed actions and actions specific to Docker.
2. Eliminating owners who created the actions.
3. Removing action versions to obtain only the action name.
4. Extracting information for actions present in the GitHub Marketplace, such as stars, primary and secondary categories (if available), and a list of versions for each action, etc.

