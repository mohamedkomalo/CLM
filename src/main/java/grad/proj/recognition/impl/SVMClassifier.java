package grad.proj.recognition.impl;

import grad.proj.recognition.FeatureVectorClassifier;
import grad.proj.recognition.Normalizer;
import grad.proj.utils.opencv.MatConverters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.SVM;

@SuppressWarnings("unused")
public class SVMClassifier implements FeatureVectorClassifier {
	private static final long serialVersionUID = 1L;
	public Map<String, SVM> svmArray = null;
	private Normalizer normalizer = null;
	
//	extracted from opencv c++ code
	private static final int ROW_SAMPLE = 0;
	private static final int RAW_OUTPUT = 1;
	
	private static final double cMinVal = 0.1;
	private static final double cMaxVal = 500;
	private static final double cLogStep = 5; // total iterations = 5
	
	private static final double gammaMinVal = 1e-5;
	private static final double gammaMaxVal = 0.6;
	private static final double gammaLogStep = 15;
	
	private static final double pMinVal = 0.01;
	private static final double pMaxVal = 100;
	private static final double pMogStep = 7; // total iterations = 4
	
	private static final double nuMinVal = 0.01;
	private static final double nuMaxVal = 0.2;
	private static final double nuLogStep = 3; // total iterations = 3
	
	private static final double coeffMinVal = 0.1;
	private static final double coeffMaxVal = 300;
	private static final double coeffLogStep = 14; // total iterations = 3

	private static final double degreeMinVal = 0.01;
	private static final double degreeMaxVal = 4;
	private static final double degreeLogStep = 7; // total iterations = 3
    
	public SVMClassifier(Normalizer normalizer) {
		this.normalizer = normalizer;
	}

	public String classify(List<Double> featureVector) {
		String classLabel = null;
		double bestDistance = Double.MIN_VALUE;
		
		featureVector = normalizer.normalize(featureVector);
		
		Mat featureVectorMat = MatConverters.ListDoubleToMat(featureVector);
		
		for(Entry<String, SVM> svmEntry : svmArray.entrySet()){
			Mat predictRes = new Mat();
			
			svmEntry.getValue().predict(featureVectorMat, predictRes, RAW_OUTPUT);
			
			double distanceFromMargin = predictRes.get(0, 0)[0];
			boolean belongToClass = distanceFromMargin < 0;
			
			if(belongToClass){
				distanceFromMargin = Math.abs(distanceFromMargin); 
				if(distanceFromMargin > bestDistance){
					classLabel = svmEntry.getKey();
					bestDistance = distanceFromMargin;
				}
			}
		}
		
		return classLabel;
	}
	
	public double classify(List<Double> featureVector, String classLabel){
		SVM svm = svmArray.getOrDefault(classLabel, null);

		if(svm == null)
			throw new RuntimeException("invalid class label " + classLabel);
		
		featureVector = normalizer.normalize(featureVector);
		
		Mat featureVectorMat = MatConverters.ListDoubleToMat(featureVector);
		
		Mat predictRes = new Mat();
		svm.predict(featureVectorMat, predictRes, RAW_OUTPUT);
		return predictRes.get(0, 0)[0];
	}
	
	public void train(Map<String, List<List<Double>>> trainingData) {
		if(trainingData.size() < 2)
			throw new RuntimeException("number of classes below minimum " +
					trainingData.size());
		
		int trainingDataRows = 0;
		int trainingDataCols = trainingData.entrySet().iterator().next().getValue().get(0).size();
		
		for(Entry<String, List<List<Double>>> classData : trainingData.entrySet())
			trainingDataRows += classData.getValue().size();
		
		Mat trainingDataMat = new Mat(trainingDataRows,
				trainingDataCols,CvType.CV_32FC1);
		Mat labels = new Mat(trainingDataRows, 1, CvType.CV_32FC1);
		int curRow = 0;

		trainingData = normalizer.reset(trainingData, -1, 1);
		
		int classIndex=0;
		for(Entry<String, List<List<Double>>> classEntry : trainingData.entrySet()){
			List<List<Double>> classData = classEntry.getValue();
			for(int r=0; r<classData.size(); ++r){
				for(int c=0; c<trainingDataCols; ++c)
					trainingDataMat.put(curRow, c, classData.get(r).get(c));
				labels.put(curRow++, 0, classIndex);
			}
			
			classIndex++;
		}
		svmArray = new HashMap<>();
		
		classIndex=0;
		for(Entry<String, List<List<Double>>> classEntry : trainingData.entrySet()){
			svmArray.put(classEntry.getKey(), constructSVM(trainingDataMat, labels, classIndex));
			classIndex++;
		}
	}
	
	private static SVM constructSVM(Mat trainingData, Mat Labels, int classLabel){
		Mat binaryLabels = new Mat(trainingData.rows(), 1, CvType.CV_32SC1);
		for(int i=0; i<trainingData.rows(); ++i)
			binaryLabels.put(i, 0, ((Labels.get(i, 0)[0] == classLabel) ? 1.0 : -1.0));
		
		SVM svm = SVM.create();
		svm.setType(SVM.C_SVC);
		svm.setKernel(SVM.LINEAR);
		svm.setC(1.0);
		
		svm.setTermCriteria(new TermCriteria(TermCriteria.COUNT, 
				2000, TermCriteria.EPS));
		
		svm.trainAutoFlat(trainingData, ROW_SAMPLE, binaryLabels, 10, 
				cMinVal, cMaxVal, cLogStep,
				gammaMinVal, gammaMaxVal, gammaLogStep,
				pMinVal, pMaxVal, pMogStep,
				nuMinVal, nuMaxVal, nuLogStep,
				coeffMinVal, coeffMaxVal, coeffLogStep,
				degreeMinVal, degreeMaxVal, degreeLogStep,
				true);
		
		return svm;
	}
	
	public double getC(String classLabel){
		return svmArray.get(classLabel).getC();
	}
	
	public Mat getSupportVector(String classLabel){
		return svmArray.get(classLabel).getSupportVectors();
	}

	@Override
	public Set<String> getClasses() {
		return svmArray.keySet();
	}
}
