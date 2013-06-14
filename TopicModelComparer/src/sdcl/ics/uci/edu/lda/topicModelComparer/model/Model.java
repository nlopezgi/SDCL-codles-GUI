package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.util.ArrayList;
import java.util.List;

import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.LightweightTopicModel;

public class Model {
	private List<NodeModel> nodes;

	public Model(LightweightTopicModel topicModel) {

		nodes = new ArrayList<NodeModel>();
		int numTopics = topicModel.topicToTerm.length;
		int numTerms = topicModel.terms.length;

		double[][] topicToTerms = normalizeWeights(numTopics, numTerms,
				topicModel);
		for (int i = 0; i < topicToTerms.length; i++) {
			NodeModel node = new NodeModel(topicModel.terms, topicToTerms[i]);
			nodes.add(node);
		}
		// for (int i = 0; i < tagClouds.size(); i++) {
		// NodeModel node = new NodeModel(tagClouds.get(i));
		// nodes.add(node);
		// }

		// for (int i = 0; i < 10; i++) {
		// NodeModel node = new NodeModel(null);
		// nodes.add(node);
		// }

		// set the connection here
		// for (int i = 0; i < 10 - 1; i++) {
		// NodeConnectionModel connection = new NodeConnectionModel();
		//
		// connection.setSource((NodeModel) nodes.get(i));
		// connection.setTarget((NodeModel) nodes.get(i + 1));
		//
		// ((NodeModel) nodes.get(i)).addSourceConnection(connection);
		// ((NodeModel) nodes.get(i + 1)).addTargetConnection(connection);
		// }

	}

	public List<NodeModel> getNodes() {
		return nodes;
	}

	/**
	 * Converts weight integer vectors for each topic to vectors with values
	 * between 0 and 1 for each topic (values per topic are proportional to the
	 * most relevant term for that topic)
	 * 
	 * @param numTopics
	 * @param numTerms
	 * @param model
	 * @return
	 */
	private static double[][] normalizeWeights(int numTopics, int numTerms,
			LightweightTopicModel model) {
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
}
