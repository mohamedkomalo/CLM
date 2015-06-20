package grad.proj.recognition.train.impl;

import static org.junit.Assert.assertTrue;
import grad.proj.recognition.RequiresLoadingTestBaseClass;
import grad.proj.utils.DataFileLoader;
import grad.proj.utils.TestsDataSetsHelper;
import grad.proj.utils.TestsDataSetsHelper.FeaturesVectorsDataSets;
import grad.proj.utils.TestsDataSetsHelper.Type;
import grad.proj.utils.FilesImageList;
import grad.proj.utils.Image;
import grad.proj.utils.ImageLoader;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class SVMClassifierTest  extends RequiresLoadingTestBaseClass{
	
	@Test
	public void testSimpleData() {
		List<List<List<Double>>> trainingData = new ArrayList<>(2);

		List<List<Double>> class0 = new ArrayList<>();
		class0.add(Arrays.asList(0.0, 0.0));
		class0.add(Arrays.asList(0.5, 0.5));
		class0.add(Arrays.asList(-0.5, -0.5));

		List<List<Double>> class1 = new ArrayList<>();
		class0.add(Arrays.asList(-1.0, 0.5));
		class0.add(Arrays.asList(-0.5, 1.0));

		trainingData.add(class0);
		trainingData.add(class1);
		
		SVMClassifier classifier = new SVMClassifier(new LinearNormalizer());
		classifier.train(trainingData);
		
		double class0Value1 = classifier.classify(Arrays.asList(0.25, 0.25));
		
		double class0Value2 = classifier.classify(Arrays.asList(-0.25, -0.25));
		
		double class1Value1 = classifier.classify(Arrays.asList(-0.75, 0.75));
		
		System.out.println("MultiClassSVMClassifierTest::testSimpleData:");
		System.out.println("class0Value1 " + class0Value1);
		System.out.println("class0Value2 " + class0Value2);
		System.out.println("class1Value1 " + class1Value1);
		
		assertTrue("class 0 vector 1 not recognized",
				class0Value1 == 0);
		assertTrue("class 0 vector 2 not recognized",
				class0Value2 == 0);
		assertTrue("class 1 vector 1 not recognized",
				class1Value1 == 1);
	}

	@Test
	public void testRealData() {
		// training and testing data already scaled
		List<List<List<Double>>> trainingData = 
				TestsDataSetsHelper.loadFeaturesVectosDataSetSeperated(FeaturesVectorsDataSets.satimage,
						Type.Train);
		
		List<List<Double>> testingData = 
				TestsDataSetsHelper.loadFeaturesVectosDataSetCombined(FeaturesVectorsDataSets.satimage,
						Type.Test);

		SVMClassifier classifier = new SVMClassifier(new LinearNormalizer());
		classifier.train(trainingData);
		
		double correctLabels = 0;
		
		for(List<Double> testingPair : testingData){
			int classLabel = testingPair.get(0).intValue();
			
			List<Double> testVector = new ArrayList<>();
			
			for(int i = 1; i<testingPair.size(); ++i)
				testVector.add(testingPair.get(i));
				
			double predictedLabel = classifier.classify(testVector);
			
			correctLabels += ((classLabel == predictedLabel)?1:0);
		}
		
		System.out.println("MultiClassSVMClassifierTest::testRealData:");
		System.out.println("number of vectors: " + testingData.size());
		System.out.println("number of correctly classified vectors: " + correctLabels);
		System.out.println("percentage: " + (correctLabels*100)/testingData.size() + "%");
		
		assertTrue("correct predicted labels percentage below 75%",
				((correctLabels*100)/testingData.size()) >= 75.0);
	}
	
//	@Test
//	public void testRealImages() throws Exception{
//		File trainDataSetDirectory = new File(TestsDataSetsHelper.CLASSIFIER_FILES_PATH
//												+ "\\train");
//		ArrayList<File> inputImagesFiles = new ArrayList<File>();
//		ArrayList<Integer> labels = new ArrayList<Integer>();
//		SurfFeatureVectorGenerator generator = new SurfFeatureVectorGenerator();
//		SVMClassifier classifier = new SVMClassifier();
//		Integer currentLabel = 0;
//		Integer classesNum = 0;
//		Integer featuresNum = 0;
//		ArrayList<Integer> classVectorsNum = new ArrayList<Integer>();
//
//		File[] classesDirectories = trainDataSetDirectory.listFiles();
//		for(File classDirectory : classesDirectories){
//			if(!classDirectory.isDirectory())
//				continue;
//			File imageFiles[] = classDirectory.listFiles();
//			classVectorsNum.add(imageFiles.length);
//			for(File imageFile : imageFiles){
//				inputImagesFiles.add(imageFile);
//				labels.add(currentLabel);
//			}
//			++currentLabel;
//		}
//		
//		List<Image> inputImages = new FilesImageList(inputImagesFiles);
//		generator.prepareGenerator(inputImages);
//		
//		classesNum = classesDirectories.length;
//		featuresNum = generator.getFeatureVectorSize();
//		
//		List<Mat> trainingData = new ArrayList<Mat>(classesNum);
//		int index = 0;
//		for(File classDirectory : classesDirectories){
//			if(!classDirectory.isDirectory())
//				continue; // for safety
//			Mat classTrainingData = new Mat(classVectorsNum.get(index++),
//					featuresNum, CvType.CV_32FC1);
//			File imageFiles[] = classDirectory.listFiles();
//			int row = 0;
//			for(File imageFile : imageFiles){
//				List<Double> featureVector = generator.generateFeatureVector(ImageLoader.loadImage(imageFile));
//				for(int col = 0;col<featuresNum;++col)
//					classTrainingData.put(row, col, featureVector.get(0, col)[0]);
//				++row;
//			}
//			trainingData.add(classTrainingData);
//		}
//		
//		classifier.train(trainingData);
//		
//		File testDataSetDirectory = new File(TestsDataSetsHelper.CLASSIFIER_FILES_PATH
//												+ "\\test");
//		double correctLabels = 0;
//		double numberOfRows = 0;
//		int classLabel = 0;
//		
//		classesDirectories = testDataSetDirectory.listFiles();
//		
//		for(File classDirectory : classesDirectories){
//			if(!classDirectory.isDirectory())
//				continue; // for safety
//			File imageFiles[] = classDirectory.listFiles();
//			numberOfRows += imageFiles.length;
//			for(File imageFile : imageFiles){
//				List<Double> testVector = generator.generateFeatureVector(ImageLoader.loadImage(imageFile));
//				double predictedLabel = classifier.classify(testVector);
//				correctLabels += ((classLabel == predictedLabel)?1:0);
//			}
//			++classLabel;
//		}
//		
//		System.out.println("MultiClassSVMClassifierTest::testRealData:");
//		System.out.println("number of vectors: " + numberOfRows);
//		System.out.println("number of correctly classified vectors: " +
//				correctLabels);
//		System.out.println("percentage: " + (correctLabels*100)/numberOfRows +
//				"%");
//		assertTrue("correct predicted labels percentage below 75%",
//				((correctLabels*100)/numberOfRows) >= 75.0);
//	}
}
