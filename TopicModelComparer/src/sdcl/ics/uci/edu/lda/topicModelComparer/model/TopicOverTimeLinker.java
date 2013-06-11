package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeTree.TopicOverTimeNode;

public class TopicOverTimeLinker {
	// private static final double THRESHOLD_FOR_TOPIC_MATRIX = 200;

	/**
	 * Creates a matrix indicating the links between topics from all models
	 * passed as parameters. The first model in the list is assumed to be the
	 * 'starting point' for the links. The first row corresponds to the indexes
	 * for topics from this model. Starting on the second row the index
	 * indicates which topic from that model should be placed in that 'cell'.
	 * The topic indicated is the closest one to the one directly above it.
	 * cells with -1 represent locations in which no topic should be placed
	 * (empty spots)
	 * 
	 * 
	 * @param models
	 * @return
	 */
	public TopicOverTimeLinks createToTLinksGreedily(
			List<LightweightTopicModel> models) {
		int maxTopics = 0;
		int numModels = models.size();

		// find the max number of topics
		for (int i = 0; i < numModels; i++) {
			if (models.get(i).topicToTerm.length > maxTopics) {
				maxTopics = models.get(i).topicToTerm.length;
			}
		}

		final int[][] topicMatrix = new int[numModels][maxTopics];

		// compare each model with the next one
		for (int i = 0; i < numModels; i++) {

			if (i == 0) {

				// the first row of the matrix is just ordered as the topics
				// are in the first model
				int j = 0;
				for (j = 0; j < models.get(i).topicToTerm.length; j++) {
					topicMatrix[i][j] = j;
				}
				while (j < maxTopics) {
					topicMatrix[i][j] = -1;
					j++;
				}
			} else {
				// System.out.println("creating row " + i + " of links matrix");
				LightweightTopicModel prevModel = models.get(i - 1);
				LightweightTopicModel thisModel = models.get(i);
				int numTopicsFromCurrent = thisModel.topicToTerm.length;
				Set<Integer> linkedTopics = new HashSet<Integer>();
				int indexOnPrev = 0;
				// iterate over a row from the previous model
				for (indexOnPrev = 0; indexOnPrev < maxTopics; indexOnPrev++) {
					// for each element get the actual topic index (its index
					// from the LightweightTopicModel)
					int topicFromPrev = topicMatrix[i - 1][indexOnPrev];
					if (topicFromPrev != -1) {
						// find the closest one in the current model
						int closest = findClosestTopic(linkedTopics,
								topicFromPrev, prevModel, thisModel);
						linkedTopics.add(closest);
						// set the index on the current row of the matrix (for
						// the current model) as the closest one
						topicMatrix[i][indexOnPrev] = closest;
					}

				}
				// if the indexOnPrev is less than the amount of topics on the
				// current model we must add those "loose topics" at the end
				if (indexOnPrev < numTopicsFromCurrent) {
					List<Integer> looseTopics = findLooseTopics(linkedTopics,
							thisModel);
					int looseTopicIndex = 0;
					while (indexOnPrev < numTopicsFromCurrent
							&& looseTopicIndex < looseTopics.size()) {
						topicMatrix[i][indexOnPrev] = looseTopics
								.get(looseTopicIndex);
						looseTopicIndex++;
						indexOnPrev++;
					}
				}
				// we must finally complete the row with -1
				while (indexOnPrev < maxTopics) {
					topicMatrix[i][indexOnPrev] = -1;
					indexOnPrev++;
				}
			}

		}
		TopicOverTimeLinks ret = new TopicOverTimeLinks(topicMatrix, numModels,
				maxTopics);
		return ret;
	}

	/**
	 * Creates a topicOverTimeLinks object storing links starting from a given
	 * topic number in the first model of the list. Each row in the links object
	 * corresponds to topics from a given model. Topics are included in a row of
	 * the links object if they are similar enough to (any of) the topics from
	 * the previous row. The threshold for similarity is defined as a class
	 * constant (in this class). In the first row we also include topics that
	 * are similar to the one passed as parameter.
	 * 
	 * Modified to leave a nicer looking matrix: the method actually creates a
	 * tree starting from the given topic (and any other very similar ones) and
	 * linking to any other similar topics from each consecutive model. In this
	 * way the end result is that the models in the TotLinks matrix are ordered
	 * so that "chains" of related topics are in the same column
	 * 
	 * Modified again to return a TREE structure representing the topic over
	 * time (a chain of connected topics starting from a single one in model 0)
	 * 
	 * 
	 * @param topic
	 * @param models
	 * @return
	 */
	public TopicOverTimeTree createToTTreeForTopic(int topic,
			List<LightweightTopicModel> models, int threshold) {
		System.out.println("CREATING TOT TREE FOR TOPIC " + topic
				+ " with threshold " + threshold);
		int numModels = models.size();
		Map<TopicRef, TopicOverTimeNode> topicReferences = new HashMap<TopicRef, TopicOverTimeNode>();
		TopicOverTimeNode root = new TopicOverTimeNode(-1, -1, true);

		List<Integer> topicsOnRow = new ArrayList<Integer>();

		topicsOnRow.add(topic);
		LightweightTopicModel startingModel = models.get(0);

		topicsOnRow.addAll(findCloseTopicsFromSameModel(topic, startingModel,
				threshold));
		for (Integer oneTopicInRow : topicsOnRow) {

			TopicOverTimeNode trt = new TopicOverTimeNode(0, oneTopicInRow,
					false);
			topicReferences.put(new TopicRef(0, oneTopicInRow), trt);
			root.addChild(trt, 0);

		}

		List<Integer> previousRow = null;
		int maxTopicsInARow = 0;

		for (int i = 1; i < numModels; i++) {
			previousRow = topicsOnRow;
			topicsOnRow = new ArrayList<Integer>();
			LightweightTopicModel prevModel = models.get(i - 1);
			LightweightTopicModel thisModel = models.get(i);
//			System.out.println("calculating divergence between model " + i
//					+ " and the previous model");
			double[][] divergenceBetweenModels = getDivergenceMatrixBetweenModels(
					prevModel, thisModel);
			// Pick the ones that are closest to the ones in the previous row
//			System.out.println("picking topics close to " + previousRow.size()
//					+ " topics from previous model");
			for (Integer oneTopicFromPrevious : previousRow) {
				for (int j = 0; j < divergenceBetweenModels[oneTopicFromPrevious].length; j++) {
					if (divergenceBetweenModels[oneTopicFromPrevious][j] < threshold) {
						Integer newTopicInRow = j;
						if (!topicsOnRow.contains(newTopicInRow)) {
							topicsOnRow.add(newTopicInRow);
						}

						// the tree leave starting on the topic from the
						// previous row
						TopicOverTimeNode refsForTopic = topicReferences
								.get(new TopicRef(i - 1, oneTopicFromPrevious));
						TopicOverTimeNode referred = null;
						if (!topicReferences.containsKey(new TopicRef(i, j))) {
							referred = new TopicOverTimeNode(i, j, false);
							topicReferences.put(new TopicRef(i, j), referred);
						} else {
							referred = topicReferences.get(new TopicRef(i, j));
						}
						refsForTopic
								.addChild(
										referred,
										divergenceBetweenModels[oneTopicFromPrevious][j]);
					}
				}
			}
			if (maxTopicsInARow < topicsOnRow.size()) {
				maxTopicsInARow = topicsOnRow.size();
			}

		}
		System.out.println("CREATED TOT TREE FOR TOPIC " + topic);
		return new TopicOverTimeTree(root);
	}

	public List<Integer> findCloseTopicsFromSameModel(int topic,
			LightweightTopicModel model, int threshold) {
		List<Integer> similarTopics = new ArrayList<Integer>();
		int numTopics = model.topicToTerm.length;
		int numTerms = model.terms.length;
		for (int i = 0; i < numTopics; i++) {
			if (i != topic) {
				double divergence = KLDivergenceCalculator
						.getKLDivergenceVectorSpaceDistance(
								convertTermWeightsToProbabilities(model.topicToTerm[topic]),
								convertTermWeightsToProbabilities(model.topicToTerm[i]),
								numTerms);
				if (divergence < threshold) {
					similarTopics.add(new Integer(i));
				}
			}
		}
		return similarTopics;
	}

	/**
	 * Creates links between the topics in each model. For each pair of
	 * successive models it creates a divergence matrix measuring the distance
	 * from every pair of topics. It then iterates over this matrix, selecting
	 * consecutively the closest pairs of topics (the ones with the smallest
	 * divergence value) and removes them from the matrix (it actually just
	 * ignores topics that have been paired up already each iteration). Stops
	 * when no more pairs of matching topics can be found. Stores the results in
	 * a TopicOverTimeLinks object.
	 * 
	 * @param models
	 * @return
	 */
	public TopicOverTimeLinks smarterCreateToTLinks(
			List<LightweightTopicModel> models) {

		int maxTopics = 0;
		int numModels = models.size();

		// find the max number of topics
		for (int i = 0; i < numModels; i++) {
			if (models.get(i).topicToTerm.length > maxTopics) {
				maxTopics = models.get(i).topicToTerm.length;
			}
		}

		final int[][] topicMatrix = new int[numModels][maxTopics];

		// compare each model with the next one
		for (int i = 0; i < numModels; i++) {

			if (i == 0) {

				// the first row of the matrix is just ordered as the topics
				// are in the first model
				int j = 0;
				for (j = 0; j < models.get(i).topicToTerm.length; j++) {
					topicMatrix[i][j] = j;
				}
				while (j < maxTopics) {
					topicMatrix[i][j] = -1;
					j++;
				}
			} else {
				// fill the row out with -1
				for (int j = 0; j < maxTopics; j++) {
					topicMatrix[i][j] = -1;
				}
				LightweightTopicModel prevModel = models.get(i - 1);
				LightweightTopicModel thisModel = models.get(i);
				List<Match> matches = findMatchesBetweenModels(prevModel,
						thisModel);
				boolean[] matched = new boolean[thisModel.topicToTerm.length];
				for (Match match : matches) {
					// find the location in the previous row of match.t1
					// place match.t2 directly below
					int location = -1;
					for (int j = 0; j < maxTopics; j++) {
						if (topicMatrix[i - 1][j] == match.t1) {
							location = j;
							break;
						}
					}
					int indexInLastRow = location;

					topicMatrix[i][indexInLastRow] = match.t2;
					matched[match.t2] = true;
				}
				for (int j = 0; j < matched.length; j++) {
					if (!matched[j]) {
						// find an empty spot
						for (int z = 0; z < maxTopics; z++) {
							if (topicMatrix[i][z] == -1) {
								topicMatrix[i][z] = j;
								matched[j] = true;
								break;
							}
						}
					}
				}
			}
			// printRowIndexes(i, topicMatrix[i]);

		}
		TopicOverTimeLinks ret = new TopicOverTimeLinks(topicMatrix, numModels,
				maxTopics);
		return ret;
	}

	private void printRowIndexes(int row, int[] topicIndexes) {
		String out = "[";
		for (int i = 0; i < topicIndexes.length; i++) {
			out += topicIndexes[i] + ",";
		}
		out += "]";
		System.out.println("Indexes for row" + row + ":" + out);

	}

	private List<Match> findMatchesBetweenModels(
			LightweightTopicModel prevModel, LightweightTopicModel toModel) {
		// System.out.println("Starting to match two models");
		int numTopicsInPrevModel = prevModel.topicToTerm.length;
		int numTopicsInToModel = toModel.topicToTerm.length;
		double[][] divergenceMatrix = new double[numTopicsInPrevModel][numTopicsInToModel];
		for (int i = 0; i < numTopicsInPrevModel; i++) {
			for (int j = 0; j < numTopicsInToModel; j++) {
				TopicVectorPair pair = normalizeTopicVectorsToSingleAlphabet(
						prevModel, toModel, i, j);
				double[] topic1Probs = convertTermWeightsToProbabilities(pair.vector1);
				double[] topic2Probs = convertTermWeightsToProbabilities(pair.vector2);
				int vectorLength = topic1Probs.length;
				divergenceMatrix[i][j] = KLDivergenceCalculator
						.getKLDivergenceVectorSpaceDistance(topic1Probs,
								topic2Probs, vectorLength);
			}
		}

		boolean[] matchedFromPrev = new boolean[numTopicsInPrevModel];
		boolean[] matchedFromTo = new boolean[numTopicsInToModel];
		List<Match> ret = new ArrayList<Match>();
		Match m = findLowestMatch(matchedFromPrev, matchedFromTo,
				divergenceMatrix, numTopicsInPrevModel, numTopicsInToModel);
		while (m != null) {
			matchedFromPrev[m.t1] = true;
			matchedFromTo[m.t2] = true;
			ret.add(m);
			m = findLowestMatch(matchedFromPrev, matchedFromTo,
					divergenceMatrix, numTopicsInPrevModel, numTopicsInToModel);
		}
		return ret;
	}

	private double[][] getDivergenceMatrixBetweenModels(
			LightweightTopicModel prevModel, LightweightTopicModel toModel) {
		// System.out.println("Starting to match two models");
		int numTopicsInPrevModel = prevModel.topicToTerm.length;
		int numTopicsInToModel = toModel.topicToTerm.length;
		double[][] divergenceMatrix = new double[numTopicsInPrevModel][numTopicsInToModel];
		for (int i = 0; i < numTopicsInPrevModel; i++) {
			for (int j = 0; j < numTopicsInToModel; j++) {
				TopicVectorPair pair = normalizeTopicVectorsToSingleAlphabet(
						prevModel, toModel, i, j);
				double[] topic1Probs = convertTermWeightsToProbabilities(pair.vector1);
				double[] topic2Probs = convertTermWeightsToProbabilities(pair.vector2);
				int vectorLength = topic1Probs.length;
				divergenceMatrix[i][j] = KLDivergenceCalculator
						.getKLDivergenceVectorSpaceDistance(topic1Probs,
								topic2Probs, vectorLength);
			}
		}
		return divergenceMatrix;
	}

	private Match findLowestMatch(boolean[] matchedFromPrev,
			boolean[] matchedFromTo, double[][] divergenceMatrix,
			int numTopicsInPrevModel, int numTopicsInToModel) {
		double smallestDivergence = 1000;
		int pickedFromPrev = -1;
		int pickedFromTo = -1;
		for (int i = 0; i < numTopicsInPrevModel; i++) {
			for (int j = 0; j < numTopicsInToModel; j++) {
				if (!matchedFromPrev[i] && !matchedFromTo[j]
						&& divergenceMatrix[i][j] < smallestDivergence) {
					pickedFromPrev = i;
					pickedFromTo = j;
					smallestDivergence = divergenceMatrix[i][j];
				}
			}
		}
		// System.out.println(" Found a close match between " + pickedFromPrev
		// + " and " + pickedFromTo);
		Match m = new Match();
		m.t1 = pickedFromPrev;
		m.t2 = pickedFromTo;
		if (pickedFromPrev != -1 && pickedFromTo != -1) {

			return m;
		}
		return null;

	}

	private class Match {
		int t1;
		int t2;
	}

	/**
	 * Finds a close topic (a topic from the toModel that diverges as little as
	 * possible to the given topic from the fromModel) such that it is not
	 * already in the ignoreTopics list
	 * 
	 * @param ignoreTopics
	 * @param topic
	 * @param fromModel
	 * @param toModel
	 * @return
	 */
	public int findClosestTopic(Set<Integer> ignoreTopics, int topic,
			LightweightTopicModel fromModel, LightweightTopicModel toModel) {
		double closestDivergence = 1000;
		int closestTopicIndex = -1;
		int numTopicsInToModel = toModel.topicToTerm.length;

		for (int i = 0; i < numTopicsInToModel; i++) {
			if (!ignoreTopics.contains(i)) {
				double divergence;
				TopicVectorPair pair = normalizeTopicVectorsToSingleAlphabet(
						fromModel, toModel, topic, i);
				double[] topic1Probs = convertTermWeightsToProbabilities(pair.vector1);
				double[] topic2Probs = convertTermWeightsToProbabilities(pair.vector2);
				int vectorLength = topic1Probs.length;
				divergence = KLDivergenceCalculator
						.getKLDivergenceVectorSpaceDistance(topic1Probs,
								topic2Probs, vectorLength);
				if (divergence < closestDivergence) {
					closestTopicIndex = i;
					closestDivergence = divergence;
				}
			}
		}

		return closestTopicIndex;
	}

	/**
	 * returns a list of all topics from the given model that are not in the
	 * given set
	 * 
	 * @param ignoreTopics
	 * @param model
	 * @return
	 */
	public List<Integer> findLooseTopics(Set<Integer> ignoreTopics,
			LightweightTopicModel model) {
		int numTopics = model.topicToTerm.length;
		List<Integer> looseTopics = new ArrayList<Integer>();
		for (int i = 0; i < numTopics; i++) {
			if (!ignoreTopics.contains(i)) {
				looseTopics.add(i);
			}
		}
		return looseTopics;
	}

	public static double getKLDivergence(LightweightTopicModel model1,
			LightweightTopicModel model2, int topic1, int topic2) {
		TopicVectorPair pair = normalizeTopicVectorsToSingleAlphabet(model1,
				model2, topic1, topic2);
		double[] topic1Probs = convertTermWeightsToProbabilities(pair.vector1);
		double[] topic2Probs = convertTermWeightsToProbabilities(pair.vector2);
		int vectorLength = topic1Probs.length;
		return KLDivergenceCalculator.getKLDivergenceVectorSpaceDistance(
				topic1Probs, topic2Probs, vectorLength);
	}

	/**
	 * Topic vectors from each model reference different alphabets. This method
	 * normalizes two topic vectors so that they represent weights corresponding
	 * to the same alphabet.
	 * 
	 * @param model1
	 * @param model2
	 * @param topic1
	 * @param topic2
	 * @return
	 */
	public static TopicVectorPair normalizeTopicVectorsToSingleAlphabet(
			LightweightTopicModel model1, LightweightTopicModel model2,
			int topic1, int topic2) {
		// int MAX_TERMS = 20;
		// TODO: THIS METHOD IS NOT WORKING AS GOOD AS I'D LIKE TO

		Map<String, Integer> topic1TermsToWeights = new HashMap<String, Integer>();
		Map<String, Integer> topic2TermsToWeights = new HashMap<String, Integer>();
		for (int i = 0; i < model1.terms.length; i++) {
			if (model1.topicToTerm[topic1][i] != 0) {
				topic1TermsToWeights.put(model1.terms[i],
						model1.topicToTerm[topic1][i]);
			}
		}
		for (int i = 0; i < model2.terms.length; i++) {
			if (model2.topicToTerm[topic2][i] != 0) {
				topic2TermsToWeights.put(model2.terms[i],
						model2.topicToTerm[topic2][i]);
			}
		}

		// TODO: STILL NOT SURE THIS WOULD HAVE ANY IMPACT: It removes some low
		// ranking words (it leaves at most MAX_TERMS for each vector)
		// if (topic1TermsToWeights.size() > MAX_TERMS) {
		// // FILTER LOW RANKING TERMS
		// List<Integer> weigthsForT1 = new ArrayList<Integer>(
		// topic1TermsToWeights.values());
		// Collections.sort(weigthsForT1);
		// Collections.reverse(weigthsForT1);
		// int minWeight = weigthsForT1.get(MAX_TERMS - 1);
		// List<String> toRemove = new ArrayList<String>();
		// for (String s : topic1TermsToWeights.keySet()) {
		// if (topic1TermsToWeights.get(s) < minWeight) {
		// toRemove.add(s);
		// }
		// }
		// for (String remove : toRemove) {
		// topic1TermsToWeights.remove(remove);
		// }
		// }
		//
		// if (topic2TermsToWeights.size() > MAX_TERMS) {
		// // FILTER LOW RANKING TERMS
		// List<Integer> weigthsForT2 = new ArrayList<Integer>(
		// topic2TermsToWeights.values());
		// Collections.sort(weigthsForT2);
		// Collections.reverse(weigthsForT2);
		// int minWeight = weigthsForT2.get(MAX_TERMS - 1);
		// List<String> toRemove = new ArrayList<String>();
		// for (String s : topic2TermsToWeights.keySet()) {
		// if (topic2TermsToWeights.get(s) < minWeight) {
		// toRemove.add(s);
		// }
		// }
		// for (String remove : toRemove) {
		// topic2TermsToWeights.remove(remove);
		// }
		// }

		Set<String> terms = new HashSet<String>();
		terms.addAll(topic1TermsToWeights.keySet());
		terms.addAll(topic2TermsToWeights.keySet());

		int numTerms = terms.size();
		Iterator<String> termIterator = terms.iterator();
		int i = 0;
		int[] vectorTopic1 = new int[numTerms];
		int[] vectorTopic2 = new int[numTerms];
		while (termIterator.hasNext()) {
			String term = termIterator.next();
			if (topic1TermsToWeights.containsKey(term)) {
				vectorTopic1[i] = topic1TermsToWeights.get(term);
			}
			if (topic2TermsToWeights.containsKey(term)) {
				vectorTopic2[i] = topic2TermsToWeights.get(term);
			}
			i++;
		}
		TopicVectorPair pair = new TopicVectorPair();
		pair.vector1 = vectorTopic1;
		pair.vector2 = vectorTopic2;
		return pair;
	}

	public static double[] convertTermWeightsToProbabilities(int[] weights) {
		double[] ret = new double[weights.length];

		int totalWeight = 0;
		for (int term = 0; term < weights.length; term++) {
			totalWeight += weights[term];
		}
		for (int term = 0; term < weights.length; term++) {
			if (weights[term] != 0) {
				ret[term] = ((double) weights[term]) / (double) totalWeight;
			}
		}
		return ret;
	}

	private static class TopicVectorPair {
		int[] vector1;
		int[] vector2;
	}

	// / -- CLASSES TO BUILD REFERENCES FOR A set of related topics

	private class TopicRef {
		public TopicRef(int model, int topic) {
			this.topic = topic;
			this.model = model;
		}

		final int topic;
		final int model;

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TopicRef) {
				return ((TopicRef) arg0).topic == topic
						&& ((TopicRef) arg0).model == model;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 1;
			hash = hash * model * topic;
			return hash;
		}
	}

	private class TopicRefTree {
		public TopicRefTree(int model, int topic) {
			this.topic = topic;
			this.model = model;
			children = new HashSet<TopicRefTree>();
		}

		final int topic;
		final int model;
		Set<TopicRefTree> children;

		public void addChild(TopicRefTree child) {
			children.add(child);
		}

		public Set<TopicRefTree> getChildren() {
			return children;
		}

		public int getNumLeaves() {
			if (children.size() == 0) {
				return 1;
			} else {
				int total = 0;
				for (TopicRefTree child : children) {
					total += child.getNumLeaves();
				}
				return total;
			}
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TopicRefTree) {
				return ((TopicRefTree) arg0).topic == topic
						&& ((TopicRefTree) arg0).model == model;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 1;
			hash = hash * model * topic;
			return hash;
		}
	}
}
