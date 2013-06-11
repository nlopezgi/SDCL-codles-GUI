package sdcl.ics.uci.edu.lda.topicModelComparer.views;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.cloudio.ICloudLabelProvider;

public class CustomLabelProvider extends BaseLabelProvider implements
		ICloudLabelProvider {

	private Font font;

	public CustomLabelProvider(Font font) {
		this.font = font;
	}

	@Override
	public String getLabel(Object element) {
		return element.toString();
	}

	@Override
	public double getWeight(Object element) {
		return Math.random();
	}

	@Override
	public Color getColor(Object element) {
		return Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	}

	@Override
	public FontData[] getFontData(Object element) {
		return font.getFontData();
	}

	@Override
	public float getAngle(Object element) {
		return (float) (-90 + Math.random() * 180);
	}

	@Override
	public String getToolTip(Object element) {
		if (element == null) {
			return "";
		}
		return element.toString();
	}
}
