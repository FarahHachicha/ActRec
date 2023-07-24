package org.example.validation;

import java.nio.file.Paths;
import java.util.Comparator;

import java.util.Map;


import org.apache.log4j.Logger;
import org.example.DataReader;


class ValueComparator implements Comparator<String> {

    Map<String, Double> base;

    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

public class Validator {
    final static Logger logger = Logger.getLogger(Validator.class);

    private String srcDir;
    private String subFolder;
    private int numOfProjects;
    private int numOfLibraries; //N nombre d'actions recommandées

    private static final String inputFile = "projects.txt";
    public Validator(String srcDir, int K) {
        this.srcDir = srcDir;
        this.numOfLibraries = K;

    }

    public void run() {
        System.out.println("Ten-fold cross validation");
        DataReader reader = new DataReader(this.srcDir);
        numOfProjects = reader.getNumberOfProjects(Paths.get(this.srcDir, inputFile).toString());
        computeEvaluationMetrics(inputFile);
    }

    public void computeEvaluationMetrics(String inputFile) {

        int step = (int) numOfProjects / 10;
        double recallRate = 0;
        for (int i = 0; i < 10; i++) {

            int trainingStartPos1 = 1;
            int trainingEndPos1 = i * step;
            int trainingStartPos2 = (i + 1) * step + 1;
            int trainingEndPos2 = numOfProjects;
            int testingStartPos = 1 + i * step;
            int testingEndPos = (i + 1) * step;
            int k = i + 1;
            subFolder = "Round" + Integer.toString(k);

            Metrics metrics = new Metrics(k, this.numOfLibraries, this.srcDir, this.subFolder, trainingStartPos1,
                    trainingEndPos1, trainingStartPos2, trainingEndPos2, testingStartPos, testingEndPos);




            metrics.successRate();
            metrics.successRateN();
            metrics.precisionRecall();
            recallRate += metrics.recallRate();
            metrics.computeAveragePrecisionRecall();
            metrics.computeAverageSuccessRateN();
            metrics.computeAverageSuccessRate();
            System.out.println("Average success rate: " + recallRate / 10);
        }

       
        return;
    }
}
