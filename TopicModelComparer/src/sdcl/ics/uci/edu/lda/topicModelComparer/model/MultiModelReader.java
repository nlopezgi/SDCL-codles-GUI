package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.LightweightTopicModel;
import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.SerializableModelWriterReader;

/**
 * Reads a LighweightTopicModel object from a csv file. See @MultiModelWriter in
 * project LDAMultiModelAggregtor
 * 
 * @author nlopezgi
 * 
 */
public class MultiModelReader {
	private static final int NUM_TEST_AGGREGATE_MODELS = 18;
	private static final String aggregateModelOutPath = "D:/nlopezgi/devProjects/topicLocation/NewExperimentData/LDAModels/topicsOverTimeModels/aggregateModels/calico/testAggregateModels";
	private static final String MODEL_FILE_PREFIX = "testAggregateModel";
	private static final String MODEL_FILE_TYPE = ".csv";
	private static final String SERIAL_MODEL_FILE_TYPE = ".ser";
	private static final String DOC_MATRIX_SUFFIX = "-DocMatrix";

	public static LightweightTopicModel readTestModel() throws Exception {
		File testTopicModelFileOut = new File(aggregateModelOutPath,
				"testAggregateModel.csv");
		return readFromFile(testTopicModelFileOut);
	}

	public static List<LightweightTopicModel> readSeveralTestModels()
			throws Exception {
		List<LightweightTopicModel> ret = new ArrayList<LightweightTopicModel>();
		for (int i = 0; i < NUM_TEST_AGGREGATE_MODELS; i++) {
			File modelFile = new File(aggregateModelOutPath, MODEL_FILE_PREFIX
					+ i + MODEL_FILE_TYPE);
			System.out.println("reading:" + MODEL_FILE_PREFIX + i
					+ MODEL_FILE_TYPE);
			LightweightTopicModel topicModel = readFromFile(modelFile);

			File modelDocumentMatrixFile = new File(aggregateModelOutPath,
					MODEL_FILE_PREFIX + DOC_MATRIX_SUFFIX + i + MODEL_FILE_TYPE);
			readDocumentMatrixFromFile(modelDocumentMatrixFile, topicModel);
			ret.add(topicModel);
		}
		return ret;
	}

	public static List<LightweightTopicModel> readSeveralTestModelsFromSerializable()
			throws Exception {
		List<LightweightTopicModel> ret = new ArrayList<LightweightTopicModel>();
		for (int i = 0; i < NUM_TEST_AGGREGATE_MODELS; i++) {
			// File modelFile = new File(aggregateModelOutPath,
			// MODEL_FILE_PREFIX
			// + i + MODEL_FILE_TYPE);
			String filename = aggregateModelOutPath + "/" + "SERIAL-"
					+ MODEL_FILE_PREFIX + i + SERIAL_MODEL_FILE_TYPE;
			LightweightTopicModel model = SerializableModelWriterReader
					.readFromFile(filename);
			ret.add(model);
		}
		return ret;

	}

	public static LightweightTopicModel readFromFile(File modelFile)
			throws Exception {
		if (modelFile.exists()) {
			FileReader reader = new FileReader(modelFile);
			BufferedReader modelIn = new BufferedReader(reader);
			return readModel(modelIn);
		}
		return null;
	}

	private static LightweightTopicModel readModel(BufferedReader modelIn)
			throws Exception {
		LightweightTopicModel ret = new LightweightTopicModel();
		String[] terms;
		double[] aggregatedTopicDivergence;
		int[] originClusterSize;
		int[][] topicToTerm;

		String termsLine = modelIn.readLine();
		terms = termsLine.split(",");
		ret.terms = terms;

		String numTopicsLine = modelIn.readLine();
		int numTopics = Integer.parseInt(numTopicsLine);

		String divergenceLine = modelIn.readLine();
		String[] divergenceLineSplit = divergenceLine.split(",");
		aggregatedTopicDivergence = new double[divergenceLineSplit.length];
		for (int i = 0; i < divergenceLineSplit.length; i++) {
			aggregatedTopicDivergence[i] = Double
					.parseDouble(divergenceLineSplit[i]);
		}
		ret.aggregatedTopicDivergence = aggregatedTopicDivergence;

		String originClusterSizeLine = modelIn.readLine();
		String[] originClusterSizeLineSplit = originClusterSizeLine.split(",");
		originClusterSize = new int[originClusterSizeLineSplit.length];
		for (int i = 0; i < originClusterSizeLineSplit.length; i++) {
			originClusterSize[i] = Integer
					.parseInt(originClusterSizeLineSplit[i]);
		}
		ret.originClusterSize = originClusterSize;

		int numTerms = ret.terms.length;
		topicToTerm = new int[numTopics][numTerms];
		for (int i = 0; i < numTopics; i++) {
			String oneTopicLine = "";
			oneTopicLine = modelIn.readLine();
			String[] oneTopicLineSplit = oneTopicLine.split(",");
			for (int j = 0; j < oneTopicLineSplit.length; j++) {
				topicToTerm[i][j] = Integer.parseInt(oneTopicLineSplit[j]);
			}
		}
		ret.topicToTerm = topicToTerm;

		return ret;
	}

	public static void readDocumentMatrixFromFile(File modelFile,
			LightweightTopicModel topicModel) throws Exception {
		if (modelFile.exists()) {
			FileReader reader = new FileReader(modelFile);
			BufferedReader modelIn = new BufferedReader(reader);
			readDocumentmatrixModel(modelIn, topicModel);
		}
	}

	private static void readDocumentmatrixModel(BufferedReader modelIn,
			LightweightTopicModel topicModel) throws Exception {
		String[] classNames;

		double[][] topicToClasses;

		String termsLine = modelIn.readLine();
		classNames = termsLine.split(",");

		topicModel.classNames = classNames;

		String numTopicsLine = modelIn.readLine();
		int numTopics = Integer.parseInt(numTopicsLine);

		int numClasses = topicModel.classNames.length;
		topicToClasses = new double[numTopics][numClasses];
		for (int i = 0; i < numTopics; i++) {
			String oneTopicLine = "";
			oneTopicLine = modelIn.readLine();
			String[] oneTopicLineSplit = oneTopicLine.split(",");
			for (int j = 0; j < oneTopicLineSplit.length; j++) {
				topicToClasses[i][j] = Double.parseDouble(oneTopicLineSplit[j]);
			}
		}
		topicModel.topicToClasses = topicToClasses;
	}

}
