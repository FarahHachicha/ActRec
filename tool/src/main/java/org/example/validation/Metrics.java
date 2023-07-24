package org.example.validation;




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.example.DataReader;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;



public class Metrics {
	final static Logger logger = Logger.getLogger(Metrics.class);

	private String srcDir;
	private String groundTruth;
	private String recDir;
	private String resDir;
	private String prDir;
	private String succesRateDir;

	private int fold;
	private int numLibs;
	private int trainingStartPos1;
	private int trainingEndPos1;
	private int trainingStartPos2;
	private int trainingEndPos2;
	private int testingStartPos;
	private int testingEndPos;
    private String succesRateDirN;
	private DataReader reader;
	private Map<Integer, String> testingProjects;

	public Metrics(int k, int numLibs, String srcDir, String subFolder, int trStartPos1, int trEndPos1, int trStartPos2,
			int trEndPos2, int teStartPos, int teEndPos) {

		this.fold = k;
		this.numLibs = numLibs;
		this.srcDir = srcDir;
		this.groundTruth = Paths.get(this.srcDir, subFolder, "GroundTruth").toString();
		this.recDir = Paths.get(this.srcDir, subFolder, "Recommendations").toString();
		this.prDir = Paths.get(this.srcDir, subFolder, "PrecisionRecall").toString();
		this.succesRateDir = Paths.get(this.srcDir, subFolder, "SuccesRate").toString();
        this.succesRateDirN = Paths.get(this.srcDir, subFolder, "SuccesRateN").toString();
		this.resDir = Paths.get(this.srcDir, "Results").toString();
		this.reader = new DataReader(this.srcDir);
		this.trainingStartPos1 = trStartPos1;
		this.trainingEndPos1 = trEndPos1;
		this.trainingStartPos2 = trStartPos2;
		this.trainingEndPos2 = trEndPos2;
		this.testingStartPos = teStartPos;
		this.testingEndPos = teEndPos;
		testingProjects = reader.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(),
				this.testingStartPos, this.testingEndPos);
	}

	public double recallRate() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */

		Set<String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		int count = 0;

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String tmp = Paths.get(this.recDir, filename).toString();
			recommendationFile = reader.readRecommendationFile(tmp, numLibs);
			tmp = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(tmp);
			Set<String> common = Sets.intersection(recommendationFile, groundTruthFile);
			int size = common.size();
			if (size == 0)
				count += 1;
		}

		String tmp2 = Paths.get(this.resDir, "Recall_Round" + Integer.toString(fold)).toString();

		int total = keyTestingProjects.size();
		double recallRate = (double) (total - count) / total;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp2));
			logger.info(recallRate);
			writer.append(Double.toString(recallRate));
			writer.newLine();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp2))) {
			bw.write(Double.toHexString(recallRate));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return recallRate;
	}


	public void successRate() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */

		Map<Integer, String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		Set<String> temp = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {

			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String groundTruthData = Paths.get(this.recDir, filename).toString();

			recommendationFile = reader.readRecommendationFile(groundTruthData);
			groundTruthData = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(groundTruthData);

			keySet = recommendationFile.keySet();
			int size = 0;
			int count = 1;
			double f_score = 0;

			temp = new HashSet<String>();

			try {
				File successRateFolder = new File(this.succesRateDir);
				if (!successRateFolder.exists())
					successRateFolder.mkdir();
				String successRatePath = Paths.get(this.succesRateDir, filename).toString();
				BufferedWriter writer = new BufferedWriter(new FileWriter(successRatePath));

				for (Integer key : keySet) {
					temp.add(recommendationFile.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthFile);
					size = common.size();
					String content = key + "\t";
					if (size == 0)
						content = content + "0";
					else
						content = content + "1";
					writer.append(content);
					writer.newLine();
					writer.flush();
					content = key + "\t" + f_score;
					count++;
					if (count > numLibs)
						break;
				}

				
				String lastLine = null;
				try (BufferedReader br = new BufferedReader(new FileReader(successRatePath))) {
					String line;
					while ((line = br.readLine()) != null) {
						lastLine = line;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (lastLine != null) {
					String[] vals = lastLine.split("\t");
					if (vals.length >= 2) {
						int id = Integer.parseInt(vals[0].trim());
						String SuccessRate2 =  vals[1].trim();
						while (id < numLibs) {
							String content2 = (id + 1) + "\t" + SuccessRate2 ;
							writer.append(content2);
							writer.newLine();
							writer.flush();
							id++;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return;
	}

	
	/*
	 * 
	 */
private Map<Integer, Double> initSRMap() {
		Map<Integer, Double> result = Maps.newHashMap();
		for (int i = 1; i < 21; i++) {
			result.put(i, 0.0);
		}
		return result;
	}

	public void computeAverageSuccessRateN() {
		Map<Integer, String> testingProjects = new HashMap<Integer, String>();
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		String tmp = "";

		Map<Integer, Double> successRateMap1 = initSRMap();
		Map<Integer, Double> successRateMap2 = initSRMap();
		Map<Integer, Double> successRateMap3 = initSRMap();
		Map<Integer, Double> successRateMap4 = initSRMap();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			tmp = Paths.get(this.succesRateDirN, filename).toString();
			try (BufferedReader bufread = new BufferedReader(new FileReader(tmp))) {
				String line = null;
				String[] vals = null;
				int id = 1;

				while ((line = bufread.readLine()) != null) {
					double sr1 = successRateMap1.get(id), sr2 = successRateMap2.get(id), sr3 = successRateMap3.get(id),
							sr4 = successRateMap4.get(id);
					double successRate1 = 0;
					vals = line.split("\t");
					successRate1 = Double.parseDouble(vals[1].trim());
					if (successRateMap1.containsKey(id)) {
						if (successRate1 == 1)
							sr1 = successRateMap1.get(id) + 1;

						if (successRate1 > 1 & successRate1 < 3) {
							sr1 = successRateMap1.get(id) + 1;
							sr2 = successRateMap2.get(id) + 1;

						}
						if (successRate1 > 2 & successRate1 < 4) {
							sr1 = successRateMap1.get(id) + 1;
							sr2 = successRateMap2.get(id) + 1;
							sr3 = successRateMap3.get(id) + 1;
						}
						if (successRate1 > 3) {
							sr1 = successRateMap1.get(id) + 1;
							sr2 = successRateMap2.get(id) + 1;
							sr3 = successRateMap3.get(id) + 1;
							sr4 = successRateMap4.get(id) + 1;
						}

					}
					successRateMap1.put(id, sr1);
					successRateMap2.put(id, sr2);
					successRateMap3.put(id, sr3);
					successRateMap4.put(id, sr4);
					id++;
					if (id > numLibs)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = successRateMap1.keySet();
		int size = testingProjects.size();
		tmp = Paths.get(this.resDir, "SR_STAR" + "_Round" + Integer.toString(fold)).toString();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {

			for (Integer key : keySet) {
				double successRate1 = 0;
				double successRate2 = 0;
				double successRate3 = 0;
				double successRate4 = 0;
				if (size != 0) {
					successRate1 = successRateMap1.get(key) / size;
					successRate2 = successRateMap2.get(key) / size;
					successRate3 = successRateMap3.get(key) / size;
					successRate4 = successRateMap4.get(key) / size;
				}
				String content = String.format("%s\t%.03f\t%.03f\t%.03f\t%.03f	", key, successRate1, successRate2,
						successRate3, successRate4);
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}
    public void successRateN() {

		Set<Integer> keyTestingProjects = testingProjects.keySet();

		/* Select top libraries */

		Map<Integer, String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		Set<String> temp = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {

			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String groundTruthData = Paths.get(this.recDir, filename).toString();

			recommendationFile = reader.readRecommendationFile(groundTruthData);
			groundTruthData = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(groundTruthData);

			keySet = recommendationFile.keySet();
			int size = 0;
			int count = 1;
			double f_score = 0;

			temp = new HashSet<String>();

			try {
				File successRateFolder = new File(this.succesRateDirN);
				if (!successRateFolder.exists())
					successRateFolder.mkdir();
				String successRatePath = Paths.get(this.succesRateDirN, filename).toString();
				BufferedWriter writer = new BufferedWriter(new FileWriter(successRatePath));
				for (Integer key : keySet) {
					temp.add(recommendationFile.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthFile);
					size = common.size();
					String content = key + "\t";
					content = content + size;
					writer.append(content);
					writer.newLine();
					writer.flush();
					content = key + "\t" + f_score;
					count++;
					if (count > numLibs)
						break;
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return;
	}
	public void precisionRecall() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();
		/* Select top libraries */
		Map<Integer, String> recommendationFile = null;
		Set<String> groundTruthFile = null;
		Set<String> temp = null;
		Set<Integer> keySet = null;
		
		double precision = 0, recall = 0;
		int totalOfRelevant = 0;
		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String tmp = Paths.get(this.recDir, filename).toString();
			recommendationFile = reader.readRecommendationFile(tmp);
			tmp = Paths.get(this.groundTruth, filename).toString();
			groundTruthFile = reader.readGroundTruthFile(tmp);
			totalOfRelevant = groundTruthFile.size();
			keySet = recommendationFile.keySet();
			int size = 0;
			int count = 1;
			double f_score = 0;
			temp = new HashSet<String>();
			try {
				tmp = Paths.get(this.prDir, filename).toString();
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
				for (Integer key : keySet) {
					temp.add(recommendationFile.get(key));

					Set<String> common = Sets.intersection(temp, groundTruthFile);
					size = common.size();
					precision = 0;
					recall = 0;
					if (key != 0) {
						precision = (double) size / key;
					}
					if (totalOfRelevant != 0) {
						recall = (double) size / totalOfRelevant;
					}
					double val1 = 2 * recall * precision;
					double val2 = recall + precision;
					if (val1 != 0 && val2 != 0)
						f_score = (2 * recall * precision) / (recall + precision);
					else
						f_score = 0;
					String content = key + "\t" + recall + "\t" + precision;
					writer.append(content);
					writer.newLine();
					writer.flush();
			
					content = key + "\t" + f_score;
					count++;
					if (count > numLibs)
						break;
				}
			
				
				String lastLine = null;
				try (BufferedReader br = new BufferedReader(new FileReader(tmp))) {
					String line;
					while ((line = br.readLine()) != null) {
						lastLine = line;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (lastLine != null) {
					String[] vals = lastLine.split("\t");
					if (vals.length >= 3) {
						int id = Integer.parseInt(vals[0].trim());
						double recall2 = Double.parseDouble(vals[1].trim());
						double precision2 = Double.parseDouble(vals[2].trim());

						while (id < numLibs) {
							precision2 = (precision2 * id) / (id + 1);
							
							String content2 = (id + 1) + "\t" + recall2 + "\t" + precision2;
							writer.append(content2);
							writer.newLine();
							writer.flush();
							id++;
						}
					}
				}


			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		


		return;
	}

	


	public void computeAveragePrecisionRecall() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double precision = 0, recall = 0;
		double val1 = 0, val2 = 0;
		String tmp = "";
		Integer countexist = 0 ;
		Map<Integer, Double> Precision = new HashMap<Integer, Double>();
		Map<Integer, Double> Recall = new HashMap<Integer, Double>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");

			try {

				tmp = Paths.get(this.prDir, filename).toString();
				String line = null;
				String[] vals = null;
				int id = 1;
				BufferedReader bufread = new BufferedReader(new FileReader(tmp));
				while ((line = bufread.readLine()) != null) {
					vals = line.split("\t");
					recall = Double.parseDouble(vals[1].trim());
					precision = Double.parseDouble(vals[2].trim());
					if (Precision.containsKey(id) && Recall.containsKey(id)) {
						val1 = Recall.get(id) + recall;
						val2 = Precision.get(id) + precision;
						
					} else {
						val1 = recall;
						val2 = precision;
						countexist +=1;
						
					}
					Recall.put(id, val1);
					Precision.put(id, val2);
					id++;
					if (id > numLibs)
						break;
				}

				bufread.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = Precision.keySet();
		int size = testingProjects.size();
		tmp = Paths.get(this.resDir, "PRC" + "_Round" + Integer.toString(fold)).toString();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {
			for (Integer key : keySet) {
				precision = 0;
				recall = 0;
				if (size != 0) {
					recall = Recall.get(key) / size ;
					precision = Precision.get(key) /size;
				}

				String content = key + "\t" + recall + "\t" + precision;
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}
	public void computeAverageSuccessRate() {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double successRate = 0;
		double val2 = 0;
		String tmp = "";

		Map<Integer, Double> successRateMap = new HashMap<Integer, Double>();

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			tmp = Paths.get(this.succesRateDir, filename).toString();
			try (BufferedReader bufread = new BufferedReader(new FileReader(tmp))) {
				String line = null;
				String[] vals = null;
				int id = 1;

				while ((line = bufread.readLine()) != null) {
					vals = line.split("\t");
					successRate = Double.parseDouble(vals[1].trim());
					if (successRateMap.containsKey(id))
						val2 = successRateMap.get(id) + successRate;
					successRateMap.put(id, val2);
					id++;
					if (id > numLibs)
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		double f_score = 0;
		Set<Integer> keySet = successRateMap.keySet();
		int size = testingProjects.size();
		tmp = Paths.get(this.resDir, "SR" + "_Round" + Integer.toString(fold)).toString();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
			for (Integer key : keySet) {
				successRate = 0;
				if (size != 0) {
					successRate = successRateMap.get(key) / size;
				}

				String content = key + "\t" + successRate + "\t";
				writer.append(content);
				writer.newLine();
				writer.flush();
				content = key + "\t" + f_score;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	public void getPrecisionRecallScores(int cutOffValue, Map<String, Double> Recall, Map<String, Double> Precision) {
		Set<Integer> keyTestingProjects = testingProjects.keySet();

		double precision = 0, recall = 0;
		String tmp = "";

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");

			/*
			 * read the FScore folder to get all the corresponding f-scores of the testing
			 * projects
			 */

			try {
				tmp = this.prDir + filename;
				String line = null;
				String[] vals = null;
				int id = 1;

				BufferedReader bufread = new BufferedReader(new FileReader(tmp));
				while ((line = bufread.readLine()) != null) {
					if (id == cutOffValue) {
						vals = line.split("\t");
						recall = Double.parseDouble(vals[1].trim());
						precision = Double.parseDouble(vals[2].trim());
						Recall.put(filename, recall);
						Precision.put(filename, precision);
						break;
					}
					id++;
				}
				bufread.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return;
	}

	public Map<String, Double> getSomeScores(int cutOffValue, String name) {

		double val = 0;
		String tmp = "";
		Map<String, Double> Vals = new HashMap<String, Double>();

		try {
			tmp = Paths.get(this.resDir, name + "_Round" + Integer.toString(fold)).toString();
			String line = null;
			int id = 1;

			BufferedReader bufread = new BufferedReader(new FileReader(tmp));
			while ((line = bufread.readLine()) != null) {
				if (id == cutOffValue) {
					val = Double.parseDouble(line.trim());
					logger.info(val);
					Vals.put(tmp, val);
					break;
				}
				id++;
			}
			bufread.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Vals;
	}

	public Set<String> getAllItems() {
		Set<String> allItems = new HashSet<String>();
		String trainingDictFilename = "";
		String trainingPro = "", filename = "";

		Map<Integer, String> trainingProjects = reader
				.readProjectList(Paths.get(this.srcDir, "projects.txt").toString(), trainingStartPos1, trainingEndPos1);
		if (trainingStartPos2 != 0 && trainingEndPos2 != 0) {
			Map<Integer, String> tempoProjects = reader.readProjectList(
					Paths.get(this.srcDir, "projects.txt").toString(), trainingStartPos2, trainingEndPos2);
			trainingProjects.putAll(tempoProjects);
		}
		Set<Integer> keyTrainingProjects = trainingProjects.keySet();
		for (Integer keyTraining : keyTrainingProjects) {
			trainingPro = trainingProjects.get(keyTraining);
			filename = trainingPro.replace("git://github.com/", "").replace("/", "__");
			trainingDictFilename = Paths.get(this.srcDir, "dicth_" + filename).toString();
			allItems.addAll(reader.getLibraries(trainingDictFilename));
		}
		return allItems;
	}
	public Map<String, Double> frequency() {

		Map<String, Double> pop = new HashMap<String, Double>();

		Set<Integer> keyTestingProjects = testingProjects.keySet();
		Map<Integer, String> recommendations = null;
		Set<Integer> keySet = null;

		for (Integer keyTesting : keyTestingProjects) {
			String testingPro = testingProjects.get(keyTesting);
			String filename = testingPro.replace("git://github.com/", "").replace("/", "__");
			String str = this.recDir + filename;
			recommendations = reader.readRecommendationFile(str);
			keySet = recommendations.keySet();

			for (Integer key : keySet) {
				double val = 0;
				String lib = recommendations.get(key);
				/* count the number of occurrence of a library */
				if (pop.containsKey(lib))
					val = pop.get(lib) + 1;
				else
					val = 1;
				pop.put(lib, val);
			}
		}	
		return pop;
	}


}

