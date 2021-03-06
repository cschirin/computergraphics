package cgresearch.graphics.camera;

import java.io.Serializable;

import cgresearch.core.math.Vector;
import cgresearch.core.math.VectorFactory;

/**
 * Key point of a camera path
 * 
 * @author Philipp Jenke
 *
 */
public class CameraPathKeypoint implements Serializable {
  /**
   * Version 1: Initial setup.
   */
  private static final long serialVersionUID = 1;

  /**
   * All information about the key point is saved in the camera object.
   */
  private Vector pos = VectorFactory.createVector(3);
  private Vector up = VectorFactory.createVector(3);
  private Vector ref = VectorFactory.createVector(3);

  /**
   * Constructor.
   */
  public CameraPathKeypoint(Vector pos, Vector up, Vector ref) {
    this.pos.copy(pos);
    this.up.copy(up);
    this.ref.copy(ref);
  }

  /**
   * Getter.
   */
  public Vector getPos() {
    return pos;
  }

  /**
   * Getter.
   */
  public Vector getUp() {
    return up;
  }

  /**
   * Getter.
   */
  public Vector getRef() {
    return ref;
  }
}
