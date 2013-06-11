package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.zest.cloudio.TagCloud;

public class NodeModel {

	private List<NodeConnectionModel> sourceConnections = new ArrayList<NodeConnectionModel>();
	private List<NodeConnectionModel> targetConnections = new ArrayList<NodeConnectionModel>();

	// private final TagCloud cloud;
	private final String[] terms;

	public String[] getTerms() {
		return terms;
	}

	public double[] getWeights() {
		return weights;
	}

	private final double[] weights;

	public NodeModel(String[] terms, double[] weights) {
		this.terms = terms;
		this.weights = weights;
	}

	public String getLabel() {
		return "test";
	}

	public List<NodeConnectionModel> getSourceConnections() {
		return sourceConnections;
	}

	public List<NodeConnectionModel> getTargetConnections() {
		return targetConnections;
	}

	public void addSourceConnection(NodeConnectionModel iConnection) {
		sourceConnections.add(iConnection);
	}

	public void addTargetConnection(NodeConnectionModel iConnection) {
		targetConnections.add(iConnection);
	}
}
