package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.cloudio.TagCloud;
import org.eclipse.zest.cloudio.Word;

public class WordListCreator {

	static int currg = 102;

	static int currTopic = -1;
	static int currR = 0;
	static int currG = 0;
	static int currB = 0;

	static final int MAX_NEW_COLORS = 10;

	public static List<Word> createWordsForTestTopic(TagCloud cloud)
			throws Exception {
		LightweightTopicModel model = MultiModelReader.readTestModel();
		int testTopic = 0;
		return createWordsForTopic(testTopic, model, cloud, testTopic, false);
	}

	/**
	 * Returns a list of words for a topic ready to be set on a new cloud using
	 * cloud.setWords
	 * 
	 * @param topic
	 * @param model
	 * @param cloud
	 * @param columnInMatrix
	 * @return
	 */
	public static List<Word> createWordsForTopic(int topic,
			LightweightTopicModel model, TagCloud cloud, int columnInMatrix,
			boolean classClouds) {
		List<Word> words = new ArrayList<Word>();
		int numTopics = model.topicToTerm.length;
		if (classClouds) {
			double[] classWeights = model.topicToClasses[topic];
			for (int i = 0; i < model.classNames.length; i++) {
				if (model.topicToClasses[topic][i] > 0) {
					// TODO: THIS IS A HACK TO REMOVE SOME USELESS CLASSES: MUST
					// REMOVE THEM FROM MODEL GEN STAGE:
					if (!model.classNames[i].contains("package-info")) {
						Word w = new Word(trimClassName(model.classNames[i]));
						w.setColor(getNextColor(columnInMatrix));
						w.weight = classWeights[i];
						w.setFontData(cloud.getFont().getFontData().clone());
						words.add(w);
					}
				}
			}
		} else {
			int numTerms = model.terms.length;
			double[][] termProbabilities = convertTermWeightsToProbabilities(
					numTopics, numTerms, model);
			double[] termWeights = termProbabilities[topic];
			for (int i = 0; i < model.terms.length; i++) {
				if (model.topicToTerm[topic][i] > 0) {
					Word w = new Word(model.terms[i]);
					w.setColor(getNextColor(columnInMatrix));
					w.weight = termWeights[i];
					w.setFontData(cloud.getFont().getFontData().clone());
					words.add(w);
				}
			}
		}
		return words;
	}

	public static String trimClassName(String className) {
		return className.substring(className.lastIndexOf('.') + 1);
	}

	/**
	 * Returns a list of words for a topic, this method does not set a font or a
	 * color for the words. DO NOT USE THIS TO THEN SET WORDS ON A NEW CLOUD
	 * 
	 * @param topic
	 * @param model
	 * @return
	 */
	public static List<Word> createLightweightWordsForTopic(int topic,
			LightweightTopicModel model) {
		List<Word> words = new ArrayList<Word>();

		int numTopics = model.topicToTerm.length;
		int numTerms = model.terms.length;
		double[][] termProbabilities = convertTermWeightsToProbabilities(
				numTopics, numTerms, model);
		double[] termWeights = termProbabilities[topic];
		for (int i = 0; i < model.terms.length; i++) {
			if (model.topicToTerm[topic][i] > 0) {
				Word w = new Word(model.terms[i]);
				w.weight = termWeights[i];
				words.add(w);
			}
		}
		return words;
	}

	private static Color getNextColor(int topic) {
		if (currTopic == topic) {
			shiftColor(topic);
			return new Color(null, currR, currG, currB);
		} else {
			currTopic = topic;
			setBaseColorForTopic(topic);
			return new Color(null, currR, currG, currB);
		}
		// currg++;
		// if (currg == 255) {
		// currg = 50;
		// }
		// return new Color(null, 255, currg, 0);
	}

	public static List<Color> getColorsForNewWords(int topic) {
		List<Color> colors = new ArrayList<Color>();
		setBaseColorForTopic(topic);
		for (int i = 0; i < MAX_NEW_COLORS; i++) {
			colors.add(getNextColor(topic));
		}
		return colors;
	}

	private static void shiftColor(int topic) {
		currR++;
		currG++;
		currB++;
		if (currR == 255 || currG == 255 || currB == 255) {
			setBaseColorForTopic(topic);
		}
	}

	private static double[][] convertTermWeightsToProbabilities(int numTopics,
			int numTerms, LightweightTopicModel model) {
		int[][] topicToTerm = model.topicToTerm;
		double[][] ret = new double[numTopics][numTerms];

		for (int topic = 0; topic < numTopics; topic++) {
			int maxWeight = 0;
			for (int term = 0; term < numTerms; term++) {
				if (topicToTerm[topic][term] > maxWeight) {
					maxWeight = topicToTerm[topic][term];
				}
			}
			for (int term = 0; term < numTerms; term++) {
				if (topicToTerm[topic][term] != 0) {
					ret[topic][term] = ((double) topicToTerm[topic][term])
							/ (double) maxWeight;
				}
			}
		}

		return ret;
	}

	public static void printTopicTerms(int[] termVector,
			LightweightTopicModel model) {
		Map<Integer, String> termToWeight = new HashMap<Integer, String>();

		String terms = ":";
		for (int i = 0; i < termVector.length; i++) {
			if (termVector[i] != 0) {
				int weight = termVector[i];
				while (termToWeight.containsKey(weight)) {
					weight++;
				}
				termToWeight.put(weight, model.terms[i]);
			}
		}
		System.out.println("SIZE OF TOPIC:" + termToWeight.size());
		List<Integer> sortedTermWeights = new ArrayList<Integer>(
				termToWeight.keySet());

		Collections.sort(sortedTermWeights);
		Collections.reverse(sortedTermWeights);
		for (Integer oneWeigth : sortedTermWeights) {
			terms = terms + termToWeight.get(oneWeigth) + "|";
		}
		System.out.println(terms);
	}

	private static void setBaseColorForTopic(int topic) {
		if (topic % 7 == 0) {
			// blue
			currR = 0;
			currG = 50;
			currB = 150;
		}
		if (topic % 7 == 1) {
			// Red
			currR = 210;
			currG = 10;
			currB = 10;
		}
		if (topic % 7 == 2) {
			// Mustard
			currR = 220;
			currG = 200;
			currB = 0;
		}
		if (topic % 7 == 3) {
			// violet
			currR = 220;
			currG = 130;
			currB = 220;
		}
		if (topic % 7 == 4) {
			// Purplish
			currR = 72;
			currG = 61;
			currB = 139;
		}
		if (topic % 7 == 5) {
			// Orange
			currR = 220;
			currG = 60;
			currB = 10;
		}
		if (topic % 7 == 6) {
			// Forest Green
			currR = 34;
			currG = 139;
			currB = 34;
		}

	}
}
