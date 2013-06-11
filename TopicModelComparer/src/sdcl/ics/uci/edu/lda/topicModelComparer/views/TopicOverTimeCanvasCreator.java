package sdcl.ics.uci.edu.lda.topicModelComparer.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.cloudio.TagCloud;
import org.eclipse.zest.cloudio.Word;

import sdcl.ics.uci.edu.lda.topicModelComparer.model.LightweightTopicModel;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeTree;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeTree.TopicOverTimeNode;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.WordListCreator;

/**
 * Creates a big canvas with the info stored in a TopicOverTimeTree object
 * 
 * @author nlopezgi
 * 
 */
public class TopicOverTimeCanvasCreator {
	private static final int COL_OFFSET = 1000;
	private static final int ROW_OFFSET = 800;

	/**
	 * This method receives a TopicOverTimeTree object and creates a new
	 * composite with every topic in the tree object. To do this, it creates a
	 * big canvas, and creates each cloud independently. Once each cloud is
	 * created and layed-out its words are painted in teh big canvas with an
	 * offset to define the clouds region. Source clouds are all disposed, so
	 * each time this is called a slightlty different cloud is created
	 * 
	 * @param parent
	 * @param topicId
	 * @param modelId
	 * @param totLinks
	 * @param models
	 * @param columnInTotLinksMatrix
	 * @return
	 */
	public static ToTCanvasData createTopicOverTimeCompositeWithBranching(
			final Composite parent, final int topicId, final int modelId,
			TopicOverTimeTree totTree,
			final List<LightweightTopicModel> models,
			final int columnInTotLinksMatrix) {
		System.out.println("CREATING CLOUD FOR TOT TREE");
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL);
		Composite cloudComp = new Composite(sc, SWT.NONE);
		sc.setContent(cloudComp);
		GridLayout gridLayout = new GridLayout(3, false);
		cloudComp.setLayout(gridLayout);

		// CREATE A MEGA CLOUD, that contains all of the other smaller clouds

		// Create clouds for each individual tree element and define their
		// locations

		int row = 0;
		int col = 0;
		int maxCol = 0;

		Set<TopicOverTimeNode> nodes = totTree.getNodesInDepth(row);
		final List<Integer> topicsInFirstRow = new ArrayList<Integer>();
		Iterator<TopicOverTimeNode> nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()) {
			TopicOverTimeNode oneNode = nodeIterator.next();
			topicsInFirstRow.add(oneNode.topic);
		}
		final List<TopicOverTimeNode> allNodes = new ArrayList<TopicOverTimeNode>();
		while (nodes.size() > 0) {
			nodeIterator = nodes.iterator();
			while (nodeIterator.hasNext()) {
				TopicOverTimeNode oneNode = nodeIterator.next();

				TagCloud cloud = createCloud(cloudComp, oneNode.topic,
						models.get(oneNode.model), columnInTotLinksMatrix);
				oneNode.setWords(cloud.getWords());
				oneNode.setLocation(new Point(COL_OFFSET * col, ROW_OFFSET
						* row));
				cloud.dispose();
				allNodes.add(oneNode);
				col++;
			}
			row++;
			if (col > maxCol) {
				maxCol = col;
			}
			col = 0;
			nodes = totTree.getNodesInDepth(row);
		}
		GridData gridData = new GridData();
		gridData.widthHint = COL_OFFSET * maxCol;
		gridData.heightHint = ROW_OFFSET * row;

		final Canvas canvas = new Canvas(cloudComp, SWT.NONE);
		canvas.setLayoutData(gridData);

		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {

				canvas.setBackground(new Color(canvas.getDisplay(), 245, 245,
						245));
				drawMegaCloud(canvas, e.gc, allNodes, models, topicId);
			}
		});

		cloudComp.setSize(COL_OFFSET / 2 * (maxCol + 1), ROW_OFFSET / 2 * row);
		sc.setSize(COL_OFFSET / 2 * (maxCol + 1), ROW_OFFSET / 2 * row);
		return new ToTCanvasData(canvas, allNodes, topicsInFirstRow, row,
				maxCol);
	}

	public static class ToTCanvasData {
		public ToTCanvasData(Canvas canvas, List<TopicOverTimeNode> allNodes,
				List<Integer> topicsInFirstRow, int numRows, int numCols) {
			this.canvas = canvas;
			this.topicsInFirstRow = topicsInFirstRow;
			this.allNodes = allNodes;
			this.numRows = numRows;
			this.numCols = numCols;
		}

		final Canvas canvas;
		final List<TopicOverTimeNode> allNodes;
		final List<Integer> topicsInFirstRow;
		final int numRows;
		final int numCols;
	}

	private static void drawMegaCloud(final Canvas canvas, final GC gc,
			final List<TopicOverTimeNode> allNodes,
			final List<LightweightTopicModel> models, final int topicId) {

		Transform t = new Transform(gc.getDevice());
		t.scale(0.45f, 0.45f);
		gc.setTransform(t);
		Font font = new Font(canvas.getDisplay(), new FontData("Helvetica", 20,
				SWT.NORMAL));

		for (TopicOverTimeNode oneNode : allNodes) {

			for (Word word : oneNode.getWords()) {
				drawWordInGC(gc, word, oneNode.getLocation());
			}
			setNodeBounds(oneNode, gc);
		}
		for (TopicOverTimeNode oneNode : allNodes) {
			gc.setForeground(new Color(canvas.getDisplay(), 10, 10, 10));
			createArrowsForNode(oneNode, gc, font);
			createStatsForNode(oneNode, gc, font, models);
		}
		gc.setTransform(null);
		t.dispose();
	}

	private static void createArrowsForNode(TopicOverTimeNode oneNode, GC gc,
			Font font) {
		if (oneNode.getChildren().size() > 0) {
			Point outbound = calculateOutboundPoint(oneNode);
			for (int i = 0; i < oneNode.getChildren().size(); i++) {
				TopicOverTimeNode child = oneNode.getChild(i);
				Point landing = calculateLanding(child);
				double divergence = oneNode.getDivergenceForChild(i);
				String divergenceStr = (divergence + "").substring(0, 5);
				gc.drawLine(outbound.x, outbound.y, landing.x, landing.y);
				int divergenceTextX = findMidpoint(outbound.x, landing.x);
				int divergenceTextY = findMidpoint(outbound.y, landing.y);
				gc.setFont(font);
				gc.drawText("div:" + divergenceStr, divergenceTextX,
						divergenceTextY);
			}
		}
	}

	private static void createStatsForNode(TopicOverTimeNode oneNode, GC gc,
			Font font, List<LightweightTopicModel> models) {

		LightweightTopicModel topicModel = models.get(oneNode.model);
		int divergence = (int) topicModel.aggregatedTopicDivergence[oneNode.topic];
		int clusterSize = topicModel.originClusterSize[oneNode.topic];
		Point corner = new Point(oneNode.getUpperLeft().x - 150,
				oneNode.getUpperLeft().y);
		gc.setFont(font);
		gc.drawText("m:" + oneNode.model + "-t:" + oneNode.topic, corner.x,
				corner.y);

		gc.drawText("Cl_Div:" + divergence, corner.x, corner.y + 40);
		gc.drawText("Cl_Size:" + clusterSize, corner.x, corner.y + 80);
	}

	/**
	 * Finds the mid point between two locations (both parameters assumed to
	 * refer to either x or y points)
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	private static int findMidpoint(int one, int two) {

		if (one > two) {
			return ((one - two) / 2) + two;
		} else {
			return ((two - one) / 2) + one;
		}
	}

	/**
	 * Calculates the point to which arrows land (center top)
	 * 
	 * @param oneNode
	 * @return
	 */
	private static Point calculateLanding(TopicOverTimeNode oneNode) {
		int middleX = (int) ((oneNode.getBottomRight().x - oneNode
				.getUpperLeft().x) / 2) + oneNode.getUpperLeft().x;
		return new Point(middleX, oneNode.getUpperLeft().y);
	}

	/**
	 * Calculates teh point from which arrows start (Center bottom)
	 * 
	 * @param oneNode
	 * @return
	 */
	private static Point calculateOutboundPoint(TopicOverTimeNode oneNode) {
		int middleX = (int) ((oneNode.getBottomRight().x - oneNode
				.getUpperLeft().x) / 2) + oneNode.getUpperLeft().x;
		return new Point(middleX, oneNode.getBottomRight().y);
	}

	/**
	 * Finds the bounds of a node's cloud according to the words contained and
	 * their location
	 * 
	 * @param oneNode
	 * @param gc
	 */
	private static void setNodeBounds(TopicOverTimeNode oneNode, GC gc) {
		int minX = -1;
		int minY = -1;
		int maxX = -1;
		int maxY = -1;

		for (Word word : oneNode.getWords()) {
			if (minX == -1 || word.x < minX) {
				minX = word.x;
			}
			if (minY == -1 || word.y < minY) {
				minY = word.y;
			}
			if (maxX == -1 || word.x + word.width > maxX) {
				maxX = word.x + word.width;
			}
			if (maxY == -1 || word.y + word.height > maxY) {
				maxY = word.y + word.height;
			}
		}
		minX += oneNode.getLocation().x;
		maxX += oneNode.getLocation().x;
		minY += oneNode.getLocation().y;
		maxY += oneNode.getLocation().y;
		oneNode.setUpperLeft(new Point(minX, minY));
		oneNode.setBottomRight(new Point(maxX, maxY));
		gc.drawRectangle(minX, minY, maxX - minX, maxY - minY);
	}

	/**
	 * Creates a cloud which will be disposed off once its words are layed out
	 * 
	 * @param cloudComp
	 * @param topicId
	 * @param topicModel
	 * @param columnInTotLinksMatrix
	 * @return
	 */
	private static TagCloud createCloud(Composite cloudComp, int topicId,
			LightweightTopicModel topicModel, int columnInTotLinksMatrix) {
		final TagCloud cloud = new TagCloud(cloudComp, SWT.NONE, 5, 1280);
		cloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cloud.setBounds(0, 0, TopicsOverTimeView.CLOUD_WIDTH,
				TopicsOverTimeView.CLOUD_HEIGHT);
		List<Word> words = WordListCreator.createWordsForTopic(topicId,
				topicModel, cloud, columnInTotLinksMatrix, false);
		cloud.setWords(words, null);

		return cloud;
	}

	/**
	 * Draws a word in the given GC with the given offset.
	 * 
	 * @param gc
	 * @param word
	 * @param color
	 */
	private static void drawWordInGC(final GC gc, final Word word,
			final Point regionOffset) {
		final Color color = word.getColor();
		gc.setForeground(color);
		Font font = new Font(gc.getDevice(), word.getFontData());
		gc.setFont(font);
		gc.setAntialias(SWT.ON);
		gc.setAlpha(225);
		gc.setForeground(color);
		int xOffset = word.x + regionOffset.x;
		int yOffset = word.y + regionOffset.y;
		gc.drawString(word.string, xOffset, yOffset, true);
		font.dispose();
	}

	// public static void saveToFileTest(final Canvas canvas,
	// final List<TopicOverTimeNode> allNodes,
	// final List<LightweightTopicModel> models, final int topicId) {
	// String testFile =
	// "D:/nlopezgi/devProjects/topicLocation/NewExperimentData/LDAModels/topicsOverTimeModels/aggregateModels/calico/testAggregateModels/generatedToTs/test-"
	// + topicId + ".png";
	// File file = new File(testFile);
	// if (!file.exists()) {
	//
	// Display display = canvas.getDisplay();
	// Image image = new Image(display, 2000, 6000);
	// GC gc = new GC(image);
	// drawMegaCloud(canvas, gc, allNodes, models, topicId);
	// ImageLoader loader = new ImageLoader();
	// loader.data = new ImageData[] { image.getImageData() };
	// loader.save(testFile, SWT.IMAGE_PNG);
	// }
	//
	// }

	public static void saveCanvasToFile(final Canvas canvas,
			final List<TopicOverTimeNode> allNodes,
			final List<LightweightTopicModel> models, final int topicId,
			String testFile, int numCols, int numRows) {
		File file = new File(testFile);
		if (!file.exists()) {
			Display display = canvas.getDisplay();
			Image image = new Image(display, COL_OFFSET / 2 * (numCols + 1),
					ROW_OFFSET / 2 * numRows);

			GC gc = new GC(image);
			drawMegaCloud(canvas, gc, allNodes, models, topicId);
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { image.getImageData() };
			loader.save(testFile, SWT.IMAGE_PNG);
		}

	}
}
