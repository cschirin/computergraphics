/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * Lecture demo program.
 */
package cgresearch.core.math.deprecated;

import cgresearch.core.math.Matrix;
import cgresearch.core.math.Vector;
import cgresearch.core.math.Vector;

/**
 * Interface class for matrices in 3-space.
 * 
 * @author Philipp Jenke
 * 
 */
public interface Matrix3Deprecated {
  /**
   * Set the row 'index'. Attentions: sets the reference, no copy!
   */
  public void setRow(final int rowIndex, Vector row);

  /**
   * Compute the product of this matrix and a vector.
   */
  public Vector multiply(final Vector other);

  /**
   * Getter.
   */
  public Vector getRow(final int rowIndex);

  /**
   * Getter
   */
  public Vector getColumn(int columnIndex);

  /**
   * Return the double array representing the matrix. Use with caution. Use
   * read-only.
   * 
   * @return Array representation of the matrix.
   */
  public double[] data();

  /**
   * Return the double array representing the matrix transformed into a
   * homogenous 4x4 matrix.
   * 
   * @return Array representation of the matrix.
   */
  public double[] data4x4();

  /**
   * Return the transposed of the matrix.
   */
  public Matrix getTransposed();

  /**
   * Set the value at the specified row and column index.
   * 
   * @param columnIndex
   *          Index of the column.
   * @param rowIndex
   *          Index of the row.
   * @param d
   *          New value.
   */
  public void set(int rowIndex, int columnIndex, double d);

  /**
   * Add another matrix, return the result.
   */
  public Matrix add(Matrix vectorProduct);

  /**
   * Getter for an individual values.
   * 
   * @param colIndex
   *          Index of the column.
   * @param rowIndex
   *          Index of the row.
   * @return Value.
   */
  public double get(int rowIndex, int columnIndex);

  /**
   * Copy from other matrix
   * 
   * @param rotation
   */
  public void copy(Matrix other);

  /**
   * Matrix multiplication.
   */
  public Matrix multiply(Matrix other);

  /**
   * Return the inverse of the matrix, return null if not invertible.
   */
  public Matrix getInverse();

  /**
   * Return the determinant of the matrix.
   */
  public double getDeterminant();

  /**
   * Multiply with scalar.
   */
  public Matrix multiply(double d);
}
