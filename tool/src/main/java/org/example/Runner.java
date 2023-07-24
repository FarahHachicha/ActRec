package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.example.validation.Validator;


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



public class Runner {

    private String srcDir; 
    private String subFolder; 
    private int numOfProjects; 
    private int numOfNeighbours;
    private static String _propFile = "evaluation.properties";
    final static Logger logger = Logger.getLogger(Runner.class);
    public Runner(){

    }

   public String loadConfigurations() throws IOException{
        Properties prop = new Properties();
        prop.load(getClass().getClassLoader().getResourceAsStream(_propFile));
        return prop.getProperty("sourceDirectory");
    }

    public void run(int K, int N) throws  IOException{
        System.out.println("TopActions : Recommender System!");
        this.numOfNeighbours = N ;
        String srcDir = loadConfigurations();
        this.srcDir = srcDir;
        DataReader dr = new DataReader(srcDir);
        numOfProjects = dr.getNumberOfProjects(Paths.get(this.srcDir, "projects.txt").toString());
        dr.ConstructDicth();
        dr.ConstructGraph();
        tenFoldCrossValidation();
        System.out.println(System.currentTimeMillis());
        Validator validator = new Validator(srcDir,K);
        validator.run();
    }
    public void tenFoldCrossValidation() {
        int step = (int)numOfProjects/10;

        for(int i=0;i<10;i++) {

            int trainingStartPos1 = 1;
            int trainingEndPos1 = i*step;
            int trainingStartPos2 = (i+1)*step+1;
            int trainingEndPos2 = numOfProjects;
            int testingStartPos = 1+i*step;
            int testingEndPos =   (i+1)*step;

            int k=i+1;
            subFolder = "Round" + Integer.toString(k);
            String pythonScript = "C:/Users/Asus/Desktop/ActRec/tool/ARM.py";
            String parameter1 = Integer.toString(k);        
            String parameter2 = this.srcDir;
            System.out.println("Computing similarities fold " + i);
              SimilarityCalculator calculator = new SimilarityCalculator(this.srcDir,this.subFolder,
                    trainingStartPos1,
                    trainingEndPos1,
                    trainingStartPos2,
                    trainingEndPos2,
                    testingStartPos,
                    testingEndPos);

            calculator.computeWeightCosineSimilarity();
           System.out.println("\tComputed similarities fold " + i);
            System.out.println("Computing recommendations fold " + i);
            RecommendationEngine engine = new RecommendationEngine(this.srcDir,this.subFolder,numOfNeighbours,testingStartPos,testingEndPos);
            engine.userBasedRecommendation();
            System.out.println("\tComputed recommendations fold " + i);
            logger.info("Computing AssociationRules fold " + i);
           
   
   
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScript, parameter1,parameter2);
                Process process = processBuilder.start();
               
                // Read output or perform other operations on the process if needed
                 // Récupérer la sortie du processus Python
                 InputStream inputStream = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 String line; while ((line = reader.readLine()) != null)
                 { System.out.println(line); }
                int exitCode = process.waitFor();
                System.out.println("Python script exited with code: " + exitCode);
   
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("\tComputed AssociationRules fold " + i);
        }


  

    }

    public static void main(String[] args) {
        Runner runner = new Runner();
        try {
            runner.run(10, 25);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return;
    }
}
