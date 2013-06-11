package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Point;
import org.eclipse.zest.cloudio.Word;

public class TopicOverTimeTree {

	public final TopicOverTimeNode root;

	private Map<Integer, Set<TopicOverTimeNode>> nodesAtDepth;

	public TopicOverTimeTree(int model, int topic) {
		root = new TopicOverTimeNode(model, topic, true);
		nodesAtDepth = new HashMap<Integer, Set<TopicOverTimeNode>>();
	}

	public TopicOverTimeTree(TopicOverTimeNode root) {
		this.root = root;
		root.depth = -1;
		nodesAtDepth = new HashMap<Integer, Set<TopicOverTimeNode>>();
	}

	/**
	 * Returns all nodes in a given depth. If depth> maxDepth of tree, returns
	 * empty set
	 * 
	 * @param depth
	 * @return
	 */
	public Set<TopicOverTimeNode> getNodesInDepth(int depth) {

		Set<TopicOverTimeNode> ret = new HashSet<TopicOverTimeNode>();
		// if depth is 0 get them
		if (depth == 0) {
			root.getNodesInDepth(depth, ret);
			nodesAtDepth.put(depth, ret);
		}
		// if they have been asked for previously, return them
		if (nodesAtDepth.containsKey(depth)) {
			return nodesAtDepth.get(depth);
		} else {
			// get the nodes for the previous depth and then get their children,
			// store them in the map
			Set<TopicOverTimeNode> nodesInPreviousDepth = getNodesInDepth(depth - 1);
			for (TopicOverTimeNode node : nodesInPreviousDepth) {
				node.getNodesInDepth(depth, ret);
			}
			nodesAtDepth.put(depth, ret);
		}

		return ret;
	}

	public static class TopicOverTimeNode {

		public final int topic;
		public final int model;
		private int depth;
		List<TopicOverTimeNode> children;
		List<Double> divergence;
		Point location;
		List<Word> words;
		Point upperLeft;

		Point bottomRight;

		public TopicOverTimeNode(int model, int topic, boolean isRoot) {
			this.topic = topic;
			this.model = model;
			if (isRoot) {
				depth = -1;
			}
			children = new ArrayList<TopicOverTimeNode>();
			divergence = new ArrayList<Double>();
		}

		public List<Word> getWords() {
			return words;
		}

		public void setWords(List<Word> words) {
			this.words = words;
		}

		public Point getLocation() {
			return location;
		}

		public void setLocation(Point location) {
			this.location = location;
		}

		public void addChild(TopicOverTimeNode child, double divergence) {
			children.add(child);
			this.divergence.add(divergence);
			child.depth = this.depth + 1;
		}

		public List<TopicOverTimeNode> getChildren() {
			return children;
		}

		public double getDivergenceForChild(int numChild) {
			return divergence.get(numChild);
		}

		public TopicOverTimeNode getChild(int numChild) {
			return children.get(numChild);
		}

		public int getNumLeaves() {
			if (children.size() == 0) {
				return 1;
			} else {
				int total = 0;
				for (TopicOverTimeNode child : children) {
					total += child.getNumLeaves();
				}
				return total;
			}
		}

		public Point getUpperLeft() {
			return upperLeft;
		}

		public void setUpperLeft(Point upperLeft) {
			this.upperLeft = upperLeft;
		}

		public Point getBottomRight() {
			return bottomRight;
		}

		public void setBottomRight(Point bottomRight) {
			this.bottomRight = bottomRight;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof TopicOverTimeNode) {
				return ((TopicOverTimeNode) arg0).topic == topic
						&& ((TopicOverTimeNode) arg0).model == model;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 1;
			hash = hash * model * topic;
			return hash;
		}

		public void getNodesInDepth(int depth, Set<TopicOverTimeNode> ret) {

			if (this.depth == depth) {
				ret.add(this);
			} else if (depth > this.depth) {
				for (TopicOverTimeNode child : children) {
					child.getNodesInDepth(depth, ret);
				}
			}
		}
	}
}
