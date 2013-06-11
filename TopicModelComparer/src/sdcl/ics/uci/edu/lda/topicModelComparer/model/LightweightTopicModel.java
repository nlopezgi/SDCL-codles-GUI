package sdcl.ics.uci.edu.lda.topicModelComparer.model;

public class LightweightTopicModel {
	/**
	 * List of terms
	 */
	public String[] terms;
	/**
	 * The list of divergence for each cluster (the average divergence from each
	 * source topic to the centroid term vector)
	 */
	public double[] aggregatedTopicDivergence;
	/**
	 * The size of the cluster (in number of topics aggregated)
	 */
	public int[] originClusterSize;
	/**
	 * The topic to terms matrix, for each topic (a cluster which is now
	 * interpreted as a topic) the terms and their weights (each vector ordered
	 * in the same way as the terms vector)
	 */
	public int[][] topicToTerm;

	/**
	 * list of classes
	 */
	public String[] classNames;
	/**
	 * The topic to classNames matrix, for each topic (a cluster which is now
	 * interpreted as a topic) the probability of being related to this topic
	 * (each vector ordered in the same way as the classNames vector)
	 */
	public double[][] topicToClasses;
}
