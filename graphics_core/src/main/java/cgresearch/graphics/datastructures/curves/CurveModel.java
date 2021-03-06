package cgresearch.graphics.datastructures.curves;

import java.util.Observable;

import cgresearch.core.math.VectorFactory;
import cgresearch.graphics.datastructures.curves.BasisFunctionBezier;
import cgresearch.graphics.datastructures.curves.BasisFunctionHermite;
import cgresearch.graphics.datastructures.curves.Curve;
import cgresearch.graphics.datastructures.curves.BasisFunctionLagrange;
import cgresearch.graphics.datastructures.curves.BasisFunctionMonomial;

public class CurveModel extends Observable {

  public static enum CurveType {
    MONOM, HERMITE, LAGRANGE, BEZIER
  };

  private final Curve curve;

  /**
   * Generate generic curve model
   */
  public CurveModel() {
    this(new Curve(new BasisFunctionBezier(), 3));
    generateCurve(CurveType.BEZIER);
  }

  /**
   * Generate model from existing curve.
   */
  public CurveModel(Curve curve) {
    this.curve = curve;
  }

  /**
   * Generate a curve of the given type.
   */
  public void generateCurve(CurveType type) {
    switch (type) {
    case BEZIER:
      curve.setBasisFunctions(new BasisFunctionBezier());
      curve.setControlPoints(VectorFactory.createVector3(-0.5, -0.5, 0.5),
          VectorFactory.createVector3(-0.25, 0.5, -0.5),
          VectorFactory.createVector3(0.25, -0.5, 0.5),
          VectorFactory.createVector3(0.5, 0.5, 0.5));
      break;
    case LAGRANGE:
      curve.setBasisFunctions(new BasisFunctionLagrange());
      curve.setControlPoints(VectorFactory.createVector3(-0.5, -0.5, 0.5),
          VectorFactory.createVector3(-0.25, 0.5, -0.5),
          VectorFactory.createVector3(0.25, -0.5, 0.5),
          VectorFactory.createVector3(0.5, 0.5, 0.5));
      break;
    case HERMITE:
      curve.setBasisFunctions(new BasisFunctionHermite());
      curve.setControlPoints(VectorFactory.createVector3(-0.5, -0.5, 0.5),
          VectorFactory.createVector3(-0.5, 1, -1),
          VectorFactory.createVector3(0.5, -1, 1),
          VectorFactory.createVector3(0.5, 0.5, 0.5));
      break;
    case MONOM:
      curve.setBasisFunctions(new BasisFunctionMonomial());
      curve.setControlPoints(VectorFactory.createVector3(-0.5, -0.5, 0.5),
          VectorFactory.createVector3(-0.25, 0.5, -0.5),
          VectorFactory.createVector3(0.25, -0.5, 0.5),
          VectorFactory.createVector3(0.5, 0.5, 0.5));
      break;
    }
    setChanged();
    notifyObservers();
  }

  public Curve getCurve() {
    return curve;
  }

  public void setT(double t) {
    curve.setParameter(t);
    setChanged();
    notifyObservers();
  }

  public double getT() {
    return curve.getParameter();
  }
}
