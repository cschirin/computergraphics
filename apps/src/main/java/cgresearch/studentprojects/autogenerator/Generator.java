package cgresearch.studentprojects.autogenerator;

import cgresearch.JoglAppLauncher;
import cgresearch.AppLauncher.RenderSystem;
import cgresearch.AppLauncher.UI;
import cgresearch.core.assets.ResourcesLocator;
import cgresearch.graphics.bricks.CgApplication;

public class Generator extends CgApplication {

	/**
	 * Constructor
	 */

	public Generator() {

	}

	/**
	 * Program entry point.
	 */
	public static void main(String[] args) {

		ResourcesLocator.getInstance().parseIniFile("resources.ini");
		Generator frame = new Generator();
		JoglAppLauncher appLauncher = JoglAppLauncher.getInstance();
		appLauncher.create(frame);
		appLauncher.setRenderSystem(RenderSystem.JOGL);
		appLauncher.setUiSystem(UI.JOGL_SWING);
		// GeneratorGUI gui = new GeneratorGUI(frame.getCgRootNode());
		// GeneratorGUI2D gui = new GeneratorGUI2D(frame.getCgRootNode());
		GeneratorGUI2DNew gui = new GeneratorGUI2DNew(frame.getCgRootNode());
		// GeneratorGUIBut gui = new GeneratorGUIBut(frame.getCgRootNode());
		// GeneratorGUIButNew gui = new
		// GeneratorGUIButNew(frame.getCgRootNode());
		appLauncher.addCustomUi(gui);

	}
}
