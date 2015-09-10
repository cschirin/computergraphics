/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * Lecture demo program.
 */
package cgresearch.apps.curves;

import java.util.ArrayList;
import java.util.List;

import cgresearch.AppLauncher;
import cgresearch.AppLauncher.RenderSystem;
import cgresearch.AppLauncher.UI;
import cgresearch.core.assets.ResourcesLocator;
import cgresearch.core.math.IVector3;
import cgresearch.core.math.VectorMatrixFactory;
import cgresearch.graphics.bricks.CgApplication;
import cgresearch.graphics.datastructures.curves.MonomialCurve;
import cgresearch.graphics.datastructures.trianglemesh.ITriangleMesh;
import cgresearch.graphics.datastructures.trianglemesh.TriangleMeshFactory;
import cgresearch.graphics.material.Material;
import cgresearch.graphics.scenegraph.CgNode;
import cgresearch.graphics.scenegraph.CoordinateSystem;
import cgresearch.ui.IApplicationControllerGui;

/**
 * Interpolation of 3 points with an monom curve of degree 2.
 * 
 */
public class MonomInterpolation extends CgApplication {

  private final CurveFrameGui gui;

  /**
   * Constructor.
   */
  public MonomInterpolation() {
    List<IVector3> interpolationPoints = new ArrayList<IVector3>();
    interpolationPoints.add(VectorMatrixFactory.newIVector3(1, 1, -0.5));
    interpolationPoints.add(VectorMatrixFactory.newIVector3(0.5, 1, 1));
    interpolationPoints.add(VectorMatrixFactory.newIVector3(-0.5, -0.5, 0.5));
    MonomialCurve curve = computeInterpolatedCurve(interpolationPoints);
    getCgRootNode().addChild(new CgNode(curve, "Interpolated monom curve"));

    gui = new CurveFrameGui();
    gui.registerCurve(curve, "Monom-Curve");

    for (int i = 0; i < interpolationPoints.size(); i++) {
      ITriangleMesh sphere =
          TriangleMeshFactory
              .createSphere(interpolationPoints.get(i), 0.05, 20);
      sphere.getMaterial().setShaderId(Material.SHADER_PHONG_SHADING);
      sphere.getMaterial().setReflectionDiffuse(Material.PALETTE0_COLOR3);
      CgNode node = new CgNode(sphere, "Interpolation point " + i);
      getCgRootNode().addChild(node);
    }

    getCgRootNode().addChild(new CoordinateSystem());
  }

  private MonomialCurve computeInterpolatedCurve(
      List<IVector3> interpolationPoints) {
    MonomialCurve monomialCurve = new MonomialCurve(2);
    IVector3 c0 = interpolationPoints.get(0);
    IVector3 c1 =
        interpolationPoints.get(0).multiply(-3)
            .add(interpolationPoints.get(1).multiply(4))
            .subtract(interpolationPoints.get(2));
    IVector3 c2 =
        interpolationPoints.get(2).subtract(interpolationPoints.get(0))
            .subtract(c1);
    monomialCurve.setControlPoint(0, c0);
    monomialCurve.setControlPoint(1, c1);
    monomialCurve.setControlPoint(2, c2);
    return monomialCurve;
  }

  /**
   * Getter.
   */
  private IApplicationControllerGui getUi() {
    return gui;
  }

  /**
   * Program entry point.
   */
  public static void main(String[] args) {
    ResourcesLocator.getInstance().parseIniFile("resources.ini");
    MonomInterpolation app = new MonomInterpolation();
    AppLauncher.getInstance().create(app);
    AppLauncher.getInstance().setRenderSystem(RenderSystem.JOGL);
    AppLauncher.getInstance().setUiSystem(UI.JOGL_SWING);
    AppLauncher.getInstance().addCustomUi(app.getUi());
  }
}