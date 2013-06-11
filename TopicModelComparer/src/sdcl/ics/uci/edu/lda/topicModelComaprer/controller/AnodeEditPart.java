package sdcl.ics.uci.edu.lda.topicModelComaprer.controller;

import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;

import sdcl.ics.uci.edu.lda.topicModelComparer.model.NodeModel;

public class AnodeEditPart extends AbstractGraphicalEditPart {

	/** The figure's anchor. */
	private ChopboxAnchor m_anchor;

	protected IFigure createFigure() {
		System.out.println("Called HelloEditPart.createFigure()");
		IFigure rectangle = new RectangleFigure();
		rectangle.setBackgroundColor(new Color(null, 200, 200, 200));
		m_anchor = new ChopboxAnchor(rectangle);
		return rectangle;
	}

	protected void createEditPolicies() {
		System.out.println("Called HelloEditPart.createEditPolicies()");
	}

	protected void refreshVisuals() {
		NodeModel node = (NodeModel) getModel();
		// This is where the actual drawing is done,
		// Simply a rectangle with text
		Rectangle bounds = new Rectangle(50, 50, 50, 50);
		getFigure().setBounds(bounds);
		Label label = new Label(node.getLabel());
		label.setTextAlignment(PositionConstants.CENTER);
		label.setBounds(bounds.crop(IFigure.NO_INSETS));
		getFigure().add(label);
	}

	public void propertyChange(PropertyChangeEvent evt) {
	}

	protected List getModelSourceConnections() {
		List sourceConnections = ((NodeModel) getModel())
				.getSourceConnections();
		return sourceConnections;
	}

	protected List getModelTargetConnections() {
		List targetConnection = ((NodeModel) getModel()).getTargetConnections();
		return targetConnection;
	}

	protected ConnectionEditPart createConnection(Object iModel) {
		NodeConnectionEditPart connectPart = (NodeConnectionEditPart) getRoot()
				.getViewer().getEditPartRegistry().get(iModel);
		if (connectPart == null) {
			connectPart = new NodeConnectionEditPart();
			connectPart.setModel(iModel);
		}
		return connectPart;
	}
}
