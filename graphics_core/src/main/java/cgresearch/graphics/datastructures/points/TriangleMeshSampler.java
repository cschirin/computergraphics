/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * Lecture demo program.
 */
package cgresearch.graphics.datastructures.points;

import java.util.Random;

import cgresearch.core.math.Vector;
import cgresearch.core.math.VectorFactory;
import cgresearch.graphics.datastructures.trianglemesh.ITriangle;
import cgresearch.graphics.datastructures.trianglemesh.ITriangleMesh;
import cgresearch.graphics.datastructures.trianglemesh.IVertex;

/**
 * Creates a point cloud from a triangle mesh.
 * 
 * @author Philipp Jenke
 * 
 */
public class TriangleMeshSampler {

  /**
   * Sample random points on all triangles of the mesh.
   */
  public static IPointCloud sample(ITriangleMesh mesh, int numberOfSamples) {
    IPointCloud pointCloud = new PointCloud();
    int RANDOM_INT = 100000;
    for (int i = 0; i < numberOfSamples; i++) {
      int triangleIndex = new Random().nextInt(mesh.getNumberOfTriangles());
      ITriangle t = mesh.getTriangle(triangleIndex);
      Vector va = mesh.getVertex(t.getA()).getPosition();
      Vector vb = mesh.getVertex(t.getB()).getPosition();
      Vector vc = mesh.getVertex(t.getC()).getPosition();
      float alpha = new Random().nextInt(RANDOM_INT) / (float) RANDOM_INT;
      float beta = 1;
      if (alpha > 0.99) {
        beta = 0;
      } else {
        while (alpha + beta > 1) {
          beta = new Random().nextInt(RANDOM_INT) / (float) RANDOM_INT;
        }
      }
      float gamma = 1 - alpha - beta;
      Vector position = va.multiply(alpha).add(vb.multiply(beta).add(vc.multiply(gamma)));
      pointCloud.addPoint(new Point(position, VectorFactory.createVector3(0.5, 0.5, 0.5), t.getNormal()));
    }
    return pointCloud;
  }

  /**
   * Convert a triangle mesh to a point cloud. Keep the vertices only.
   */
  public static IPointCloud convert(ITriangleMesh mesh) {
    IPointCloud pointCloud = new PointCloud();
    for (int vertexIndex = 0; vertexIndex < mesh.getNumberOfVertices(); vertexIndex++) {
      IVertex vertex = mesh.getVertex(vertexIndex);
      pointCloud
          .addPoint(new Point(vertex.getPosition(), VectorFactory.createVector3(0, 0, 0), vertex.getNormal()));
    }
    return pointCloud;
  }
}
