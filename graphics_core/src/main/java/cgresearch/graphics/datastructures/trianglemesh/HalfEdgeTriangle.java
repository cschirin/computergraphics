package cgresearch.graphics.datastructures.trianglemesh;

import cgresearch.core.math.Vector;

/**
 * A facet has a reference to one of oits half edges. This datastructure
 * represents a general mesh (triangle, quad, ...). However, we only use
 * triangle meshes here.
 * 
 * @author Philipp Jenke
 *
 */
public class HalfEdgeTriangle implements ITriangle {

  /**
   * One of the half edges around the facet.
   */
  private HalfEdge halfEdge;

  /**
   * Facet normal
   */
  private Vector normal;

  /**
   * Triangle is visible?
   */
  private boolean isVisible = true;

  public HalfEdge getHalfEdge() {
    return halfEdge;
  }

  public void setHalfEdge(HalfEdge halfEdge) {
    this.halfEdge = halfEdge;
  }

  @Override
  public String toString() {
    return "Triangular Facet";
  }

  public Vector getNormal() {
    return normal;
  }

  public void setNormal(Vector normal) {
    this.normal = normal;
  }

  /**
   * Compute the area of the facet. Area of the facet.
   * 
   * @return Area of the triangle.
   */
  public double getArea() {
    Vector v0 = halfEdge.getStartVertex().getPosition();
    Vector v1 = halfEdge.getNext().getStartVertex().getPosition();
    Vector v2 = halfEdge.getNext().getNext().getStartVertex().getPosition();
    return v1.subtract(v0).cross(v2.subtract(v0)).getNorm() / 2.0;
  }

  /**
   * Compute the centroid (center of mass) of the triangle.
   * 
   * @return Centroid of the triangle.
   */
  public Vector getCentroid() {
    Vector v0 = halfEdge.getStartVertex().getPosition();
    Vector v1 = halfEdge.getNext().getStartVertex().getPosition();
    Vector v2 = halfEdge.getNext().getNext().getStartVertex().getPosition();
    return (v0.add(v1).add(v2)).multiply(1.0 / 3.0);
  }

  @Override
  public int getA() {
    throw new UnsupportedOperationException("Not implemented for half edge triangle mesh");
  }

  @Override
  public int getB() {
    throw new UnsupportedOperationException("Not implemented for half edge triangle mesh");
  }

  @Override
  public int getC() {
    throw new UnsupportedOperationException("Not implemented for half edge triangle mesh");
  }

  @Override
  public int getTextureCoordinate(int index) {
    return -1;
  }

  @Override
  public int get(int i) {
    throw new UnsupportedOperationException("Not implemented for half edge triangle mesh");
  }

  /**
   * Set the global index for a vertex.
   * 
   * @param i
   *          Triangle index, in 0-2;
   * @param vertexIndex
   *          Global vertex index;
   */
  public void setVertexIndex(int i, int vertexIndex) {
    throw new UnsupportedOperationException("Not implemented for half edge triangle mesh");
  }

  @Override
  public int getOther(int a, int b) {
    throw new UnsupportedOperationException("Not implemented for half edge triangle mesh");
  }

  @Override
  public boolean contains(int vertexIndex) {
    throw new UnsupportedOperationException("Not implemented for half edge triangle mesh");
  }

  @Override
  public boolean isVisible() {
    return isVisible;
  }

  @Override
  public void setVisible(boolean visible) {
    this.isVisible = visible;
  }
}
