package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicOverTimeLinks {
	public final int numModels;
	public final int numTopics;
	public final int[][] topicMatrix;

	public TopicOverTimeLinks(int[][] topicMatrix, int numModels, int numTopics) {
		this.numModels = numModels;
		this.numTopics = numTopics;
		this.topicMatrix = topicMatrix;
	}

	public int getTopicForCell(int modelRow, int topicColumn) {
		if (modelRow < numModels && topicColumn < numTopics) {
			return topicMatrix[modelRow][topicColumn];
		}
		return -1;
	}

	public void printMatrix() {

		for (int i = 0; i < topicMatrix.length; i++) {
			String line = "";
			for (int j = 0; j < topicMatrix[i].length; j++) {
				line += topicMatrix[i][j] + ",";
			}
			System.out.println(line);

		}
	}
}
