package sdcl.ics.uci.edu.lda.topicModelComparer.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.cloudio.TagCloud;
import org.eclipse.zest.cloudio.TagCloudViewer;
import org.eclipse.zest.cloudio.Word;
import org.eclipse.zest.cloudio.layout.DefaultLayouter;
import org.eclipse.zest.cloudio.layout.ILayouter;

import sdcl.ics.uci.edu.lda.topicModelComparer.model.LightweightTopicModel;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.Model;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.MultiModelReader;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeLinker;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.TopicOverTimeLinks;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.WordListCreator;

public class TopicModelSimpleVisualizer {

	static TopicOverTimeLinks totLinks = null;

	public static void main(String[] args) throws Exception {

		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		LightweightTopicModel topicModel = null;
		try {
			topicModel = MultiModelReader.readTestModel();
		} catch (Exception e1) {

			e1.printStackTrace();
		}
		// createCloudsForModel(topicModel, shell);
		List<LightweightTopicModel> topicModels = MultiModelReader
				.readSeveralTestModels();

		createCloudsForMultipleModels(topicModels, shell);

		shell.open();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		display.dispose();
	}

	public static Composite createCloudsForModel(
			LightweightTopicModel topicModel, Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL);
		Composite cloudComp = new Composite(sc, SWT.NONE);
		sc.setContent(cloudComp);
		GridLayout gridLayout = new GridLayout();
		int numTopics = topicModel.topicToTerm.length;
		gridLayout.numColumns = numTopics;
		cloudComp.setLayout(gridLayout);

		Model model;
		List<TagCloudViewer> viewers;
		List<ILayouter> layouters;
		model = new Model(topicModel);
		viewers = new ArrayList<TagCloudViewer>();
		layouters = new ArrayList<ILayouter>();
		List<LightweightTopicModel> models = new ArrayList<LightweightTopicModel>();
		models.add(topicModel);

		for (int i = 0; i < numTopics; i++) {
			TagCloud cloud = new TagCloud(cloudComp, SWT.NONE, 5, 1280);
			cloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			cloud.setBounds(0, 0, 300, 300);
			TagCloudViewer oneViewer = new TagCloudViewer(cloud);
			viewers.add(oneViewer);
			ILayouter oneLayouter = new DefaultLayouter(20, 10);
			layouters.add(oneLayouter);
			List<Word> words = WordListCreator.createWordsForTopic(i,
					topicModel, cloud, i, false);
			cloud.setWords(words, null);
			drawTopicStatsInCloud(cloud, models, i, i, 0);
		}
		cloudComp.setSize(numTopics * 320, 300);
		sc.setSize(numTopics * 320, 300);
		return sc;
	}

	public static Composite createCloudsForMultipleModels(
			List<LightweightTopicModel> topicModels, Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL);
		Composite cloudComp = new Composite(sc, SWT.NONE);
		sc.setContent(cloudComp);
		GridLayout gridLayout = new GridLayout();

		TopicOverTimeLinker totLinker = new TopicOverTimeLinker();
		totLinks = totLinker.smarterCreateToTLinks(topicModels);
		// totLinks.printMatrix();
		// totLinks = totLinker.createToTLinksGreedily(topicModels);

		gridLayout.numColumns = totLinks.numTopics;
		cloudComp.setLayout(gridLayout);

		List<TagCloudViewer> viewers;
		List<ILayouter> layouters;
		// model = new Model(topicModel);

		viewers = new ArrayList<TagCloudViewer>();
		layouters = new ArrayList<ILayouter>();

		for (int i = 0; i < totLinks.numModels; i++) {
			LightweightTopicModel topicModel = topicModels.get(i);
			for (int j = 0; j < totLinks.numTopics; j++) {
				int numTopicToDraw = totLinks.topicMatrix[i][j];
				if (numTopicToDraw != -1) {
					TagCloud cloud = new TagCloud(cloudComp, SWT.NONE, 5, 1280);
					cloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							true));
					cloud.setBounds(0, 0, 300, 300);
					TagCloudViewer oneViewer = new TagCloudViewer(cloud);
					viewers.add(oneViewer);
					ILayouter oneLayouter = new DefaultLayouter(20, 10);
					layouters.add(oneLayouter);
					List<Word> words = WordListCreator.createWordsForTopic(
							numTopicToDraw, topicModel, cloud, j, false);
					cloud.setWords(words, null);
					drawTopicStatsInCloud(cloud, topicModels, numTopicToDraw,
							j, i);
				} else {
					TagCloud cloud = new TagCloud(cloudComp, SWT.NONE, 5, 1280);
					cloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							true));
					cloud.setBounds(0, 0, 300, 300);
					TagCloudViewer oneViewer = new TagCloudViewer(cloud);
					viewers.add(oneViewer);
					ILayouter oneLayouter = new DefaultLayouter(20, 10);
					layouters.add(oneLayouter);
					List<Word> words = new ArrayList<Word>();
					cloud.setWords(words, null);
				}
			}
		}

		cloudComp.setSize(totLinks.numTopics * 320, 300 * topicModels.size());
		sc.setSize(totLinks.numTopics * 320, 300);
		return sc;
	}

	private static void drawTopicStatsInCloud(final TagCloud cloud,
			final List<LightweightTopicModel> topicModels, final int numTopic,
			int column, int numModel) {
		LightweightTopicModel topicModel = topicModels.get(numModel);
		final int divergence = (int) topicModel.aggregatedTopicDivergence[numTopic];
		final int clusterSize = topicModel.originClusterSize[numTopic];

		double totDivergence = -1;
		if (totLinks != null && numModel + 1 < totLinks.numModels) {
			LightweightTopicModel toModel = topicModels.get(numModel + 1);
			int toTopic = totLinks.topicMatrix[numModel + 1][column];
			if (toTopic != -1) {
				int toModelInt = numModel + 1;
				// System.out.println("geting divergence from model:" + numModel
				// + "topic:" + numTopic + ". to model:" + toModelInt
				// + ". topic" + toTopic);
				totDivergence = TopicOverTimeLinker.getKLDivergence(topicModel,
						toModel, numTopic, toTopic);
			}
		}
		final String finalTotDivergence = ("" + totDivergence).substring(0, 4);

		PaintListener testPaintListener = new PaintListener() {
			public void paintControl(PaintEvent e) {

				Font font = new Font(cloud.getDisplay(), new FontData(
						"Helvetica", 10, SWT.NORMAL));
				e.gc.setFont(font);
				e.gc.drawText("Top#:" + numTopic, 250, 0);
				e.gc.drawText("Div:" + divergence, 250, 20);
				e.gc.drawText("Size:" + clusterSize, 250, 40);
				if (!finalTotDivergence.equals("-1.0")) {
					e.gc.drawText("totD:" + finalTotDivergence, 125, 250);
				}
				font.dispose();

			}
		};

		cloud.addPaintListener(testPaintListener);
	}
}
