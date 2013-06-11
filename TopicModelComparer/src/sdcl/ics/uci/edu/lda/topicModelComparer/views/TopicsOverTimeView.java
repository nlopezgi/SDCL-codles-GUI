package sdcl.ics.uci.edu.lda.topicModelComparer.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.zest.cloudio.TagCloud;
import org.eclipse.zest.cloudio.TagCloudViewer;
import org.eclipse.zest.cloudio.Word;
import org.eclipse.zest.cloudio.layout.ILayouter;

import sdcl.ics.uci.edu.lda.topicModelComparer.model.LightweightTopicModel;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.MultiModelReader;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeLinker;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeLinks;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeTree;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeTree.TopicOverTimeNode;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.WordListCreator;
import sdcl.ics.uci.edu.lda.topicModelComparer.views.TopicOverTimeCanvasCreator.ToTCanvasData;

public class TopicsOverTimeView {

	static TopicOverTimeLinks totLinks = null;

	static int currentModel = 0;
	static List<LightweightTopicModel> topicModels = null;
	static List<TagCloud> clouds;
	static List<TagCloudViewer> viewers;
	static List<ILayouter> layouters;
	static int NUM_COLS = 6;
	static int NUM_ROWS = 4;
	public final static int CLOUD_WIDTH = 300;
	public final static int CLOUD_HEIGHT = 270;
	// Not sure whty but some clouds are left smaller in the pop-up windows, so
	// this allows us to hack them to the right size
	final static int CLOUD_BUFFER = 60;

	public static void main(String[] args) throws Exception {
		topicModels = MultiModelReader.readSeveralTestModels();
		final int numTopics = topicModels.get(0).topicToTerm.length;

		Display display = new Display();
		final Shell shell = new Shell(display);
		// shell.setLayout(new FillLayout());
		GridLayout layout = new GridLayout();
		// set the layout of the shell
		shell.setLayout(layout);

		Group actionGroup = new Group(shell, SWT.NONE);

		actionGroup.setLayout(new FillLayout());

		Button buttonAnimateOneStep = new Button(actionGroup, SWT.PUSH);
		buttonAnimateOneStep.setText("Animate");
		buttonAnimateOneStep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				animateAllTest();
			}
		});

		Button buttonCreateTotForTopic = new Button(actionGroup, SWT.PUSH);

		final Text toTSelectText = new Text(actionGroup, SWT.NONE);
		toTSelectText.setText("0");
		final Label totThresholdLabel = new Label(actionGroup, SWT.NONE);
		totThresholdLabel.setText("Divergence threshold (0-1000)");
		final Text toTThreshold = new Text(actionGroup, SWT.NONE);
		toTThreshold.setText("200");

		buttonCreateTotForTopic.setText("ToT tree from topic:");
		buttonCreateTotForTopic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					int topic = Integer.parseInt(toTSelectText.getText());
					int threshold = Integer.parseInt(toTThreshold.getText());
					if (topic >= 0 && topic < totLinks.numTopics) {
						createTopicOverTimeDetailFrameWithBranching(shell,
								topic, threshold);
					} else {
						MessageDialog.openError(shell, "Error",
								"Topic number is not valid");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					MessageDialog.openError(shell, "Error",
							"Topic and threshold must be valid numbers");
				}
			}
		});

		Button buttonCreateAllToTImages = new Button(actionGroup, SWT.PUSH);
		buttonCreateAllToTImages.setText("All ToTs to image files");
		buttonCreateAllToTImages.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {

					int threshold = Integer.parseInt(toTThreshold.getText());

					// createTopicOverTimeDetailFrameWithBranching(shell,
					// topic, threshold);
					createImagesForAllToTs(shell, threshold, numTopics);

				} catch (Exception ex) {
					ex.printStackTrace();
					MessageDialog.openError(shell, "Error",
							"Threshold must be valid numbers");
				}
			}
		});

		Button buttonSwithcToClassClouds = new Button(actionGroup, SWT.PUSH);
		buttonSwithcToClassClouds.setText("Switch to/from class clouds");
		buttonSwithcToClassClouds.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openClassCloudsFrame(shell, topicModels.get(0));
			}

		});

		Group cloudGroup = new Group(shell, SWT.NONE);
		cloudGroup.setLayout(new FillLayout());

		getTopicsOverTime(topicModels);
		createCloudsForModel(topicModels.get(0), cloudGroup, false);
		//

		shell.open();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		display.dispose();
	}

	public static void createTopicOverTimeDetailFrameWithBranching(Shell shell,
			int topic, int threshold) {
		Shell frame = new Shell(shell, SWT.SHELL_TRIM);
		frame.setLayout(new FillLayout());

		Group detailCloudGroup = new Group(frame, SWT.NONE);
		detailCloudGroup.setLayout(new FillLayout());

		TopicOverTimeLinker totLinker = new TopicOverTimeLinker();
		TopicOverTimeTree totTree = totLinker.createToTTreeForTopic(topic,
				topicModels, threshold);
		TopicOverTimeCanvasCreator.createTopicOverTimeCompositeWithBranching(
				detailCloudGroup, topic, 0, totTree, topicModels, topic);

		frame.setSize(1500, 800);
		frame.open();
	}

	public static void createImagesForAllToTs(Shell shell, int threshold,
			int numTopics) {
		Shell frame = new Shell(shell, SWT.SHELL_TRIM);
		frame.setLayout(new FillLayout());

		List<Integer> topcisDrawn = new ArrayList<Integer>();
		for (int i = 0; i < numTopics; i++) {
			Group detailCloudGroup = new Group(frame, SWT.NONE);
			detailCloudGroup.setLayout(new FillLayout());
			if (!topcisDrawn.contains(i)) {
				TopicOverTimeLinker totLinker = new TopicOverTimeLinker();
				TopicOverTimeTree totTree = totLinker.createToTTreeForTopic(i,
						topicModels, threshold);
				ToTCanvasData ret = TopicOverTimeCanvasCreator
						.createTopicOverTimeCompositeWithBranching(
								detailCloudGroup, i, 0, totTree, topicModels, i);
				String testFile = "D:/nlopezgi/devProjects/topicLocation/NewExperimentData/LDAModels/topicsOverTimeModels/aggregateModels/calico/testAggregateModels/generatedToTs/test-"
						+ i + ".png";
				TopicOverTimeCanvasCreator.saveCanvasToFile(ret.canvas,
						ret.allNodes, topicModels, i, testFile, ret.numCols,
						ret.numRows);
				topcisDrawn.addAll(ret.topicsInFirstRow);
			}
		}
		// frame.setSize(1500, 800);
		frame.dispose();
	}

	public static void animateAllTest() {
		if (totLinks != null) {
			int nextModel = currentModel + 1;
			if (currentModel + 1 == totLinks.numModels) {
				nextModel = 0;
			}
			// for (int i = 0; i < 1; i++) {
			for (int i = 0; i < totLinks.numTopics; i++) {
				int nextTopic = getNextTopic(i, nextModel);
				// int nextTopic = getNextTopic(7, nextModel);
				LightweightTopicModel model = topicModels.get(nextModel);
				if (clouds.size() > i && nextTopic != -1) {
					TagCloud cloudToMutate = clouds.get(i);
					Composite cloudComp = cloudToMutate.getParent();

					createTransformedCloud(cloudToMutate, model, nextTopic, i,
							cloudComp);
					// transformCloud(cloudToMutate, model, nextTopic, i);
				}
			}
			currentModel = nextModel;
		}
	}

	private static void openClassCloudsFrame(Shell shell,
			LightweightTopicModel topicModel) {
		Shell frame = new Shell(shell, SWT.SHELL_TRIM);
		frame.setLayout(new FillLayout());

		Group cloudGroup = new Group(frame, SWT.NONE);
		cloudGroup.setLayout(new FillLayout());
		createCloudsForModel(topicModel, cloudGroup, true);

		// frame.setSize(1500, 800);
		frame.open();
	}

	public static void testAddWord() {
		TagCloud cloud = clouds.get(0);
		cloud.testAddNewWord();

	}

	/**
	 * Transforms the current cloud to match the topic in the given column,
	 * changing the weights of existing words, removing words no loner present
	 * and adding new words. Final layout is ok if not too much changed, but the
	 * method does not reduce in the tree model the size of the words, meaning
	 * lots of space is left unusable, for clouds changing a lot, it does not
	 * layout nicely in the end. Try using the createTransformedCloud instead
	 * 
	 * @param cloud
	 * @param model
	 * @param topic
	 * @param column
	 */
	public static void transformCloud(TagCloud cloud,
			LightweightTopicModel model, int topic, int column) {
		List<Word> newWords = WordListCreator.createLightweightWordsForTopic(
				topic, model);
		List<Color> colors = WordListCreator.getColorsForNewWords(column);

		// TODO: MUST DECIDE WHEN TO RELAYOUT BASED ON THE AMOUNT OF CHANGE AND
		// THE SPARSENESS OF THE CLOUD!!!
		cloud.mutateTagCloud(newWords, colors, false);
	}

	/**
	 * modifies the passed cloud using words from the topic in the given column
	 * using as hint the layout of the words in the passed cloud before the
	 * modification.
	 * 
	 * @param cloud
	 * @param model
	 * @param topic
	 * @param column
	 */
	public static void createTransformedCloud(TagCloud cloud,
			LightweightTopicModel model, int topic, int column,
			Composite cloudComp) {
		// Get the current location of words
		Map<Word, Point> locationHints = cloud.getCurrentWordHints();
		List<Word> newWords = WordListCreator.createLightweightWordsForTopic(
				topic, model);
		List<Color> colors = WordListCreator.getColorsForNewWords(column);

		// Mutate the cloud
		cloud.mutateTagCloud(newWords, colors, false);

		// re-layout with hints
		cloud.layoutCloudWithHints(null, true, locationHints);
	}

	public static int getNextTopic(int column, int model) {
		if (totLinks != null) {
			return totLinks.topicMatrix[model][column];
		}
		return -1;
	}

	/**
	 * Creates the initial set of clouds for the currentModel (the latest one)
	 * and lays them out in a grid.
	 * 
	 * @param topicModel
	 * @param parent
	 * @return
	 */
	public static Composite createCloudsForModel(
			LightweightTopicModel topicModel, Composite parent,
			boolean classClouds) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL);
		Composite cloudComp = new Composite(sc, SWT.NONE);
		sc.setContent(cloudComp);
		GridLayout gridLayout = new GridLayout();

		int numTopics = topicModel.topicToTerm.length;
		gridLayout.numColumns = 5;
		cloudComp.setLayout(gridLayout);

		// Model model;

		// model = new Model(topicModel);
		viewers = new ArrayList<TagCloudViewer>();

		List<LightweightTopicModel> models = new ArrayList<LightweightTopicModel>();
		models.add(topicModel);
		clouds = new ArrayList<TagCloud>();
		for (int i = 0; i < numTopics; i++) {
			final TagCloud cloud = new TagCloud(cloudComp, SWT.NONE, 5, 1280);
			cloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			cloud.setBounds(0, 0, CLOUD_WIDTH, CLOUD_HEIGHT);
			final TagCloudViewer oneViewer = new TagCloudViewer(cloud);
			viewers.add(oneViewer);

			List<Word> words = WordListCreator.createWordsForTopic(i,
					topicModel, cloud, i, classClouds);
			cloud.setWords(words, null);
			drawTopicStatsInCloud(cloud, i);

			// A simple label provider (see above)
			oneViewer
					.setLabelProvider(new CustomLabelProvider(cloud.getFont()));

			// Demo of an selection listener
			oneViewer
					.addSelectionChangedListener(new ISelectionChangedListener() {

						@Override
						public void selectionChanged(SelectionChangedEvent event) {
							IStructuredSelection selection = (IStructuredSelection) oneViewer
									.getSelection();
							// cloud.testRepaintCurrentWord(null);
							System.out.println("Selection: " + selection);
						}
					});
			clouds.add(cloud);
		}
		cloudComp.setSize(NUM_COLS * CLOUD_WIDTH, NUM_ROWS * CLOUD_HEIGHT);
		sc.setSize(NUM_COLS * CLOUD_WIDTH, NUM_ROWS * CLOUD_HEIGHT);
		return sc;
	}

	private static void drawTopicStatsInCloud(final TagCloud cloud,
			final int topicIndex) {

		PaintListener testPaintListener = new PaintListener() {
			public void paintControl(PaintEvent e) {
				int numTopic = totLinks.topicMatrix[currentModel][topicIndex];
				if (numTopic != -1) {
					LightweightTopicModel topicModel = topicModels
							.get(currentModel);
					int divergence = (int) topicModel.aggregatedTopicDivergence[numTopic];
					int clusterSize = topicModel.originClusterSize[numTopic];
					Font font = new Font(cloud.getDisplay(), new FontData(
							"Helvetica", 10, SWT.NORMAL));
					e.gc.setFont(font);
					e.gc.drawText("Div:" + divergence, CLOUD_WIDTH - 10, 0);
					e.gc.drawText("Size:" + clusterSize, CLOUD_WIDTH - 10, 20);

					if (currentModel < topicModels.size() - 1) {
						double totDivergence;
						LightweightTopicModel toModel = topicModels
								.get(currentModel + 1);
						int toTopic = totLinks.topicMatrix[currentModel + 1][topicIndex];
						if (toTopic != -1) {
							int toModelInt = currentModel + 1;
							totDivergence = TopicOverTimeLinker
									.getKLDivergence(topicModel, toModel,
											numTopic, toTopic);
							String finalTotDivergence = ("" + totDivergence);
							finalTotDivergence = finalTotDivergence.substring(
									0, 4);

							e.gc.drawText("totD:" + finalTotDivergence,
									CLOUD_WIDTH / 2, CLOUD_HEIGHT - 20);
						}

					}

					font.dispose();
				}

			}
		};

		cloud.addPaintListener(testPaintListener);
	}

	public static void getTopicsOverTime(List<LightweightTopicModel> topicModels) {
		TopicOverTimeLinker totLinker = new TopicOverTimeLinker();
		// totLinks = totLinker.createToTLinksGreedily(topicModels);
		totLinks = totLinker.smarterCreateToTLinks(topicModels);
	}

	public static void printNodes(List<TopicOverTimeNode> nodes) {
		String str = "";
		for (TopicOverTimeNode node : nodes) {
			str += "{m:" + node.model + "t:" + node.topic + "},";
		}
		System.out.println(str);

	}

}
