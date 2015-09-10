/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * Lecture demo program.
 */
package cgresearch.rendering.jogl.core;

import java.util.Observable;
import java.util.Observer;

import com.jogamp.opengl.GL2;

import cgresearch.core.math.IMatrix3;
import cgresearch.core.math.IVector3;
import cgresearch.core.math.VectorMatrixFactory;
import cgresearch.graphics.datastructures.curves.ICurve;
import cgresearch.graphics.datastructures.primitives.Line3D;
import cgresearch.graphics.datastructures.trianglemesh.ITriangleMesh;
import cgresearch.graphics.datastructures.trianglemesh.Triangle;
import cgresearch.graphics.datastructures.trianglemesh.TriangleMesh;
import cgresearch.graphics.datastructures.trianglemesh.TriangleMeshFactory;
import cgresearch.graphics.datastructures.trianglemesh.TriangleMeshTransformation;
import cgresearch.graphics.datastructures.trianglemesh.Vertex;
import cgresearch.graphics.material.Material;
import cgresearch.graphics.scenegraph.CgNode;
import cgresearch.graphics.scenegraph.Transformation;

/**
 * A render node for a curve.
 * 
 * @author Philipp Jenke
 * 
 */
public class RenderContentCurve extends JoglRenderContent
    implements Observer {

  /**
   * Reference to the rendered curve
   */
  private final CgNode cgNode;

  /**
   * Transformation of the current curve point.
   */
  private Transformation transformation =
      new Transformation();

  boolean showControlPolygon = true;

  /**
   * Constructor
   */
  public RenderContentCurve(CgNode cgNode) {
    this.cgNode = cgNode;
    getCurve().addObserver(this);

    // Mesh for the curve
    ITriangleMesh curveMesh = createCurveMesh();
    CgNode curveNode = new CgNode(curveMesh, "curve");
    cgNode.addChild(curveNode);

    // Mesh for control points
    ITriangleMesh controlPointsmesh =
        createControlPointsMesh();
    CgNode controlPointsNode =
        new CgNode(controlPointsmesh, "control points");
    if (showControlPolygon) {
      cgNode.addChild(controlPointsNode);
    }

    // CgNode for evaluation
    CgNode evalNode = createEvalNode();
    cgNode.addChild(evalNode);

    // Initialize transformation
    updateTransformation();
  }

  private void updateTransformation() {
    transformation.reset();
    transformation.addTranslation(getCurve().eval(
        getCurve().getParameter()));
    IMatrix3 T =
        VectorMatrixFactory
            .createCoordinateFrameX(getCurve().derivative(
                getCurve().getParameter()));
    transformation.addTransformation(T);

  }

  private CgNode createEvalNode() {
    ITriangleMesh mesh = createEvalMesh();
    CgNode transformationNode =
        new CgNode(transformation, "transformation");
    CgNode cgNode = new CgNode(mesh, "eval");
    transformationNode.addChild(cgNode);
    return transformationNode;
  }

  private ICurve getCurve() {
    return (ICurve) cgNode.getContent();
  }

  /**
   * Create the mesh which represents the control polygon and points.
   */
  private ITriangleMesh createControlPointsMesh() {
    // Control points
    ITriangleMesh mesh = new TriangleMesh();
    mesh.getMaterial().setReflectionDiffuse(
        VectorMatrixFactory.newIVector3(0.6, 0.6, 0.9));
    for (int i = 0; i <= getCurve().getDegree(); i++) {
      mesh.unite(TriangleMeshFactory.createSphere(
          getCurve().getControlPoint(i), 0.03f, 10));
    }

    // Control polygon
    for (int i = 1; i <= getCurve().getDegree(); i++) {
      Line3D line =
          new Line3D(getCurve().getControlPoint(i - 1),
              getCurve().getControlPoint(i));
      mesh.unite(TriangleMeshFactory.createLine3D(line, 10,
          0.01f));

    }

    mesh.computeTriangleNormals();
    mesh.computeVertexNormals();

    mesh.getMaterial().setReflectionDiffuse(
        Material.PALETTE2_COLOR0);
    mesh.getMaterial().setShaderId(
        Material.SHADER_PHONG_SHADING);
    return mesh;
  }

  /**
   * Create a mesh to display the curve at e given parameter.
   */
  private ITriangleMesh createEvalMesh() {
    ITriangleMesh mesh = new TriangleMesh();

    // Eval-position
    mesh.unite(TriangleMeshFactory.createSphere(
        VectorMatrixFactory.newIVector3(0, 0, 0), 0.1f, 10));

    // Derivative arrow
    ITriangleMesh arrowMesh =
        TriangleMeshFactory.createArrow();
    TriangleMeshTransformation.scale(arrowMesh, 0.6);
    mesh.unite(arrowMesh);

    mesh.computeTriangleNormals();
    mesh.computeVertexNormals();

    mesh.getMaterial().setReflectionDiffuse(
        Material.PALETTE2_COLOR2);
    mesh.getMaterial().setShaderId(
        Material.SHADER_PHONG_SHADING);

    return mesh;
  }

  /**
   * Create a triangle mesh to represent the curve.
   */
  private ITriangleMesh createCurveMesh() {
    int resolution = 50;
    int circleResolution = 10;
    double radius = 0.02;

    ITriangleMesh mesh = new TriangleMesh();

    for (int i = 0; i < resolution; i++) {
      double t = (double) i / (double) (resolution - 1);
      IVector3 center = getCurve().eval(t);
      IVector3 tangent = getCurve().derivative(t);
      IMatrix3 frame =
          VectorMatrixFactory.createCoordinateFrameX(
              tangent).getTransposed();
      IVector3 dx = frame.getRow(1).multiply(radius);
      IVector3 dy = frame.getRow(2).multiply(radius);
      for (int j = 0; j < circleResolution; j++) {
        double alpha =
            (double) j * 2.0 * Math.PI
                / (double) (circleResolution + 1);
        IVector3 p =
            center.add(dx.multiply(Math.cos(alpha)).add(
                dy.multiply(Math.sin(alpha))));
        mesh.addVertex(new Vertex(p));
      }

      for (int j = 0; j < circleResolution; j++) {
        if (i > 0) {
          int i00 = (i - 1) * circleResolution + j;
          int i01 =
              (i - 1) * circleResolution + (j + 1)
                  % circleResolution;
          int i10 = i * circleResolution + j;
          int i11 =
              i * circleResolution + (j + 1)
                  % circleResolution;
          mesh.addTriangle(new Triangle(i00, i01, i11));
          mesh.addTriangle(new Triangle(i00, i10, i11));
        }
      }
    }

    mesh.computeTriangleNormals();
    mesh.computeVertexNormals();

    mesh.getMaterial().setReflectionDiffuse(
        Material.PALETTE2_COLOR4);
    mesh.getMaterial().setShaderId(
        Material.SHADER_PHONG_SHADING);
    return mesh;
  }

  /**
   * @param parameter
   */
  public void setParameter(double parameter) {
    updateTransformation();
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  @Override
  public void update(Observable o, Object arg) {
    updateTransformation();
  }

  @Override
  public void updateRenderStructures() {
  }

  @Override
  public void draw3D(GL2 gl) {
  }

  @Override
  public void afterDraw(GL2 gl) {
  }
}