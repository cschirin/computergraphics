/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * Lecture demo program.
 */
package cgresearch.graphics.datastructures.trianglemesh;

import cgresearch.core.math.Vector;
import cgresearch.graphics.scenegraph.ICgNodeContent;

/**
 * This interface describes the valid operations for a triangle mesh structure.
 * 
 * @author Philipp Jenke
 * 
 */
public abstract class ITriangleMesh extends ICgNodeContent {

  /**
   * Add a new triangle to the mesh with the vertex indices a, b, c. The index
   * of the first vertex is 0.
   * 
   * @param t
   *          Container object to represent a triangle.
   */
  public abstract void addTriangle(ITriangle t);

  /**
   * Add a triangle between the given vertex indices to the triangle mesh.
   * 
   * @param indexA
   *          Index of the first incident vertex.
   * @param indexB
   *          Index of the second incident vertex.
   * @param indexC
   *          Index of the third incident vertex.
   */
  public abstract void addTriangle(int indexA, int indexB, int indexC);

  /**
   * Add a new vertex to the vertex list. The new vertex is appended to the end
   * of the list.
   * 
   * @param v
   *          Vertex to be added.
   * 
   * @return Index of the vertex in the vertex list.
   */
  public abstract int addVertex(IVertex v);

  /**
   * Getter.
   * 
   * @return Number of triangles in the mesh.
   */
  public abstract int getNumberOfTriangles();

  /**
   * Getter.
   * 
   * @return Number of vertices in the triangle mesh.
   */
  public abstract int getNumberOfVertices();

  /**
   * Getter.
   * 
   * @param index
   *          Index of the triangle to be accessed.
   * @return Triangle at the given index.
   */
  public abstract ITriangle getTriangle(int index);

  /**
   * Getter
   * 
   * @param index
   *          Index of the vertex to be accessed.
   * @return Vertex at the given index.
   */
  public abstract IVertex getVertex(int index);

  /**
   * Clear mesh - remove all triangles and vertices.
   */
  public abstract void clear();

  /**
   * Compute the triangle normals.
   */
  public abstract void computeTriangleNormals();

  /**
   * Compute the vertex normals.
   */
  public abstract void computeVertexNormals();

  /**
   * Add an additional texture coordinate, return index of the coordinate in the
   * data structure.
   * 
   * @param texCoord3f
   *          New texture coordinate.
   * @return Index of the texture coordinate.
   */
  public abstract int addTextureCoordinate(Vector texCoord3f);

  /**
   * Getter.
   * 
   * @return Number of texture coordinates in the data structure.
   */
  public abstract int getNumberOfTextureCoordinates();

  /**
   * Getter
   * 
   * @param index
   *          Index of the texture coordinate
   * @return Texture coordinate at the specified index.
   */
  public abstract Vector getTextureCoordinate(int index);

  /**
   * Fit the mesh into the unit box centered at the origin.
   */
  public abstract void fitToUnitBox();

  /**
   * Invert the norma directions.
   */
  public abstract void invertFaceNormals();

  /**
   * Unit the content of the second mesh (only vertices and triangles) from the
   * second mesh with the this-mesh. The result is saved in the this-mesh.
   */
  public abstract void unite(ITriangleMesh createSphere);

  /**
   * Remove the triangle.
   * 
   * @param triangleIndex
   *          Triangle index.
   */
  public abstract void removeTriangle(int triangleIndex);

  /**
   * Split each triangle of the mesh in 4 triangles.
   */
  public abstract void split();

  /**
   * Clone the other mesh.
   * 
   * @param mesh
   */
  public abstract void copyFrom(ITriangleMesh mesh);

  /**
   * Show hide an individual triangle. Attention: not checked if this helps the
   * performance.
   * 
   * @param triangleIndex
   *          Index of the triangle.
   * @param visible
   *          Visibility flag.
   */
  public abstract void setTriangleVisible(int triangleIndex, boolean visible);
}
