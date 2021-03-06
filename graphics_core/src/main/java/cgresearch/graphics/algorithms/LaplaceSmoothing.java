package cgresearch.graphics.algorithms;

import java.util.ArrayList;
import java.util.List;

import cgresearch.core.math.Matrix;
import cgresearch.core.math.Vector;
import cgresearch.core.math.VectorFactory;
import cgresearch.graphics.datastructures.trianglemesh.ITriangleMesh;

/**
 * Implements Laplacian smoothing by solving the linear system corresponding to
 * the Laplace matrix.
 * 
 * @author Philipp Jenke
 *
 */
public class LaplaceSmoothing {

  /**
   * Apply several iterations of Laplace smoothing.
   * 
   * @param mesh
   *          Mesh to be smoothed.
   */
  public static void smooth(ITriangleMesh mesh, int numberOfIterations) {
    if (mesh == null) {
      throw new IllegalArgumentException();
    }
    for (int iteration = 0; iteration < numberOfIterations; iteration++) {
      smooth(mesh);
    }
  }

  /**
   * Apply one step of Laplacian smoothing.
   * 
   * @param mesh
   *          Mesh to be smoothed.
   */
  public static void smooth(ITriangleMesh mesh) {
    if (mesh == null) {
      throw new IllegalArgumentException();
    }

    // Compute smoothed positions
    Matrix L = TriangleMeshTools.createLaplacian(mesh);
    List<Vector> newVertexPositions = new ArrayList<Vector>();
    for (int rowIndex = 0; rowIndex < L.getNumberOfRows(); rowIndex++) {
      Vector v = VectorFactory.createVector3(0, 0, 0);
      for (int columnIndex = 0; columnIndex < L.getNumberOfColumns(); columnIndex++) {
        if (L.get(rowIndex, columnIndex) != 0 && rowIndex != columnIndex) {
          v = v.add(mesh.getVertex(columnIndex).getPosition());
        }
      }
      v = v.multiply(1.0 / L.get(rowIndex, rowIndex));
      newVertexPositions.add(v);
    }

    // Set new vertex positions
    for (int vertexIndex = 0; vertexIndex < mesh.getNumberOfVertices(); vertexIndex++) {
      mesh.getVertex(vertexIndex).getPosition().copy(newVertexPositions.get(vertexIndex));
    }
    mesh.updateRenderStructures();
  }

}
