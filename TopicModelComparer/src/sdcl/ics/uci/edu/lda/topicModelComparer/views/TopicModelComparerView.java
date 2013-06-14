package sdcl.ics.uci.edu.lda.topicModelComparer.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.cloudio.TagCloud;
import org.eclipse.zest.cloudio.TagCloudViewer;
import org.eclipse.zest.cloudio.Word;
import org.eclipse.zest.cloudio.layout.DefaultLayouter;
import org.eclipse.zest.cloudio.layout.ILayouter;

import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.LightweightTopicModel;
import sdcl.ics.uci.edu.lda.topicModelComaprer.controller.GraphicalPartFactory;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.Model;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.MultiModelReader;
import sdcl.ics.uci.edu.lda.topicModelComparer.model.WordListCreator;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class TopicModelComparerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "TopicModelComparerView.views.tagCloudView";

	// Use a standard Viewer for the Draw2d canvas
	// private ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();

	// Use standard RootEditPart as holder for all other edit parts
	private RootEditPart rootEditPart = new FreeformGraphicalRootEditPart();

	// Custom made EditPartFactory, will automatically be called to create edit
	// parts for model elements
	private EditPartFactory editPartFactory = new GraphicalPartFactory();
	// The model
	private Model model;
	private List<TagCloudViewer> viewers;
	private List<ILayouter> layouters;

	// // Use a standard Viewer for the Draw2d canvas
	private TagCloudViewer viewer;
	private TagCloudViewer viewer2;

	// private TagCloud tagCloud;
	private ILayouter layouter;
	private ILayouter layouter2;

	// private TestModel model;

	/**
	 * The constructor.
	 */
	public TopicModelComparerView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewers = new ArrayList<TagCloudViewer>();
		layouters = new ArrayList<ILayouter>();
		LightweightTopicModel topicModel = null;
		try {
			topicModel = MultiModelReader.readTestModel();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		model = new Model(topicModel);

		// // Initialize the viewer, 'parent' is the
		// // enclosing RCP windowframe
		// viewer.createControl(parent);
		// viewer.setRootEditPart(rootEditPart);
		// viewer.setEditPartFactory(editPartFactory);
		//
		// // Inject the model into the viewer, the viewer will
		// // traverse the model automatically
		// viewer.setContents(model);
		//
		// // Set the view's background to white
		// viewer.getControl().setBackground(new Color(null, 255, 255, 255));

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		Composite cloudComp = new Composite(parent, SWT.VERTICAL);
		cloudComp.setLayout(new GridLayout(10, true));
		// cloudComp.setLayout(new RowLayout(SWT.VERTICAL));
		// cloudComp.setLayoutData(new RowData(100, 100));
		GridData cloudComLayoutData = new GridData(SWT.FILL, SWT.TOP, true,
				true);
		cloudComLayoutData.minimumHeight = 200;
		cloudComp.setLayoutData(cloudComLayoutData);

		int numTopics = topicModel.topicToTerm.length;
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
		}

		// TagCloud cloud = new TagCloud(cloudComp, SWT.NONE, 5, 1280);
		// cloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// // cloud.setBackground(new Color(null, 0, 0, 0));
		// cloud.setBounds(0, 0, 300, 300);
		// viewer = new TagCloudViewer(cloud);
		// layouter = new DefaultLayouter(20, 10);
		// // layouter = new CharacterLayouter(20,10);
		// viewer.setLayouter(layouter);
		// List<Word> words;
		//
		// words = WordListCreator.createWordsForTopic(0, topicModel, cloud);
		// cloud.setWords(words, null);
		//
		// TagCloud cloud2 = new TagCloud(cloudComp, SWT.NONE, 5, 1280);
		// cloud2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// cloud2.setBounds(0, 0, 300, 300);
		// viewer2 = new TagCloudViewer(cloud2);
		// layouter2 = new DefaultLayouter(20, 10);
		// // layouter = new CharacterLayouter(20,10);
		// viewer2.setLayouter(layouter2);
		// List<Word> words2;
		//
		// words2 = WordListCreator.createWordsForTopic(1, topicModel, cloud2);
		// cloud2.setWords(words2, null);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}