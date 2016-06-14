/**
 * Prof. Philipp Jenke
 * Hochschule f�r Angewandte Wissenschaften (HAW), Hamburg
 * CG1: Educational Java OpenGL framework with scene graph.
 */

package cgresearch.rendering.jogl.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import cgresearch.core.logging.Logger;
import cgresearch.core.math.*;
import cgresearch.graphics.camera.Camera;
import cgresearch.graphics.misc.AnimationTimer;
import cgresearch.graphics.scenegraph.*;
import cgresearch.rendering.core.RendererOptions;
import cgresearch.rendering.jogl.misc.PickingRenderer;
import cgresearch.rendering.jogl.misc.ViewFrustumCulling;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.GLReadBufferUtil;

/**
 * Implements the 3D rendering functionality.
 * 
 * @author Philipp Jenke
 * 
 */
public class JoglRenderer3D implements Observer {

  /**
   * Constants
   */
  private static final double NEAR_CLIPPING_FACTOR = 0.1;
  private static final double FAR_CLIPPING_FACTOR = 3.0;
  private static final int SCREEN_WIDTH = 640;
  private static final int SCREEN_HEIGHT = 480;
  private static final float OPAQUE = 1.0f;
  private static final Vector CLEAR_COLOR = VectorFactory.createVector3(0, 0, 0); //VectorFactory.createVector3(1, 1, 1);
  private static final int JOGL_NUMBER_OF_LIGHTS = 8;

  /**
   * Screen aspect ration
   */
  private double aspectRatio = 1;

  /**
   * Screenshot filename. A screenshot is saved, when this variable is not the
   * empty string.
   */
  private String screenshotFilename = "";

  /**
   * Reference to the scene graph root node.
   */
  private final CgRootNode rootNode;

  /**
   * Reference to the root node.
   */
  private final JoglRenderObjectManager renderObjectMananger;

  /**
   * GLU instance
   */
  private GLU glu = null;

  /**
   * Set this flag to true, if intrinsic camera parameters need to be updated.
   */
  private boolean updateIntrinsicCameraParametersRequired = true;

  /**
   * Set this flag to true, if extrinsic camera parameters need to be updated.
   */
  private boolean updateExtrinsicCameraParametersRequired = true;

  /**
   * Set this flag to true, if stencil testing method needs to be updated.
   */
  private boolean cameraPositionChanged = true;

  /**
   * Reference to the frame which contains the view.
   */
  private final List<JoglRenderable> renderables = new ArrayList<JoglRenderable>();

  /**
   * This flag indicated that the lights need to be updated.
   */
  private boolean updateLightsRequired = true;

  /**
   * Corner points of the near clipping plane
   */
  private Vector[] nearPlaneCorners = new Vector[4];

  /**
   * Render used for FPS display
   */
  // private TextRenderer renderer = new TextRenderer(new Font("SansSerif",
  // Font.PLAIN, 18), true);

  /**
   * Number of required light sources for plane based soft shadow
   */
  private final int softShadowPlaneCount = 9;

  /**
   * Number of required light sources for sphere based soft shadow
   */
  private final int softShadowSphereCount = 15;

  /**
   * Defines the distance of additional lights for soft shadows
   */
  private final float softShadowOffset = 0.12f;

  /**
   * Defines whether the two sided stencil buffer is used
   */
  // private boolean useTwoSidedStencil = false;

  /**
   * Handles the view frustum culling.
   */
  private final ViewFrustumCulling viewFrustum;

  /**
   * Access to the renderer options interface.
   */
  private RendererOptions rendererOptions;

  /**
   * Constructor.
   */
  public JoglRenderer3D(JoglRenderObjectManager renderObjectManager, CgRootNode rootNode) {
    this.renderObjectMananger = renderObjectManager;
    this.rootNode = rootNode;
    viewFrustum = new ViewFrustumCulling(rootNode);
    new PickingRenderer(rootNode);
    rootNode.addObserver(this);
    Camera.getInstance().addObserver(this);
    glu = new GLU();
    rendererOptions = rootNode.getRendererOptions();
  }

  public void init(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
    gl.glLoadIdentity();

    // Perspective
    final int width = SCREEN_WIDTH;
    final int height = SCREEN_HEIGHT;
    aspectRatio = (float) width / (float) height;
    setPerspectiveProjection(gl);

    // Viewport
    gl.glViewport(0, 0, width, height);

    // Depth
    gl.glEnable(GL2.GL_DEPTH_TEST);

    // which is the front? the one which is drawn counter clockwise
    gl.glFrontFace(GL2.GL_CCW);
    // which one should NOT be drawn
    gl.glCullFace(GL2.GL_BACK);

    // Enable buffers
    gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
    gl.glEnableClientState(GLPointerFunc.GL_TEXTURE_COORD_ARRAY);
    gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);

    // define the color we want to be displayed as the "clipping wall"
    gl.glClearColor((float) (rendererOptions.getClearColor().get(0)), (float) (rendererOptions.getClearColor().get(1)), (float) (rendererOptions.getClearColor().get(2)), OPAQUE);

    // Enable lighting
    gl.glEnable(GL2.GL_LIGHTING);
    gl.glLightModelf(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
    gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

    // Set some lights
    updateLights(gl);

    // Texturing
    gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
    gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);

    // Shading
    gl.glEnable(GLLightingFunc.GL_NORMALIZE);

    updateExtrinsicCameraParameters(drawable);

    // Create render nodes
    Logger.getInstance().debug("Initially create JOGL render nodes for scene graph.");
    renderObjectMananger.update(rootNode, CgNodeStateChange.makeAddChild(null, rootNode));

    // Frame rate
    drawable.getAnimator().setUpdateFPSFrames(3, null);
  }

  /**
   * Set the current lights
   */
  private void updateLights(GL2 gl) {
    if (updateLightsRequired) {
      Logger.getInstance().debug("Updated lights.");

      // Disable inactive lights
      for (int i = 0; i < JOGL_NUMBER_OF_LIGHTS; i++) {
        int lightIndex = getLightIndex(i);
        gl.glDisable(lightIndex);
        gl.glLightfv(lightIndex, GL2.GL_DIFFUSE, new float[] { -1, -1, -1, -1 }, 0);
      }

      for (int i = 0; i < rootNode.getNumberOfLights(); i++) {
        updateLight(gl, i, false);
      }
      updateLightsRequired = false;
    }
  }

  /**
   * Disables the given light
   */
  private void disableLight(GL2 gl, int lightID) {
    gl.glDisable(getLightIndex(lightID));
    int lightIndex = getLightIndex(lightID);
    // gl.glLightfv(lightIndex, GL2.GL_AMBIENT, new float[] { 0, 0, 0, 0 }, 0);
    // // changed
    gl.glLightfv(lightIndex, GL2.GL_DIFFUSE, new float[] { -1, -1, -1, -1 }, 0);
  }

  /**
   * Sets the current light
   * 
   * @param gl
   *          GL object
   * @param lightID
   *          Id of the light source
   * @param drawShadows
   *          Has to be true if scene is drawn with shadows
   */
  private void updateLight(GL2 gl, int lightID, boolean drawShadows) {
    int lightIndex = getLightIndex(lightID);
    LightSource light = rootNode.getLight(lightID);

    disableLight(gl, lightID);

    float lightCount = 1f;
    if (drawShadows) {
      LightSource.ShadowType shadow = light.getShadowType();
      if (shadow == LightSource.ShadowType.PLANE_X || shadow == LightSource.ShadowType.PLANE_Y
          || shadow == LightSource.ShadowType.PLANE_Z) {
        lightCount = (float) softShadowPlaneCount;
      } else if (shadow == LightSource.ShadowType.SPHERE) {
        lightCount = (float) softShadowSphereCount;
      }
    }

    float lightPosition[] = { 1, 1, 1, 1 };
    lightPosition[0] = (float) light.getPosition().get(0);
    lightPosition[1] = (float) light.getPosition().get(1);
    lightPosition[2] = (float) light.getPosition().get(2);
    lightPosition[3] = 0;
    float lightAmbient[] = { 0, 0, 0, 0 };
    float lightSpecular[] = { 0, 0, 0, 0 };
    lightSpecular[0] = (float) light.getSpecularColor().get(0);
    lightSpecular[1] = (float) light.getSpecularColor().get(1);
    lightSpecular[2] = (float) light.getSpecularColor().get(2);
    lightSpecular[3] = 0;
    float lightDiffuse[] = { 0, 0, 0, 0 };
    lightDiffuse[0] = (float) light.getDiffuseColor().get(0);
    lightDiffuse[1] = (float) light.getDiffuseColor().get(1);
    lightDiffuse[2] = (float) light.getDiffuseColor().get(2);
    lightDiffuse[3] = 0;

    if (lightCount > 1) {
      for (int i = 0; i < 3; i++) {
        lightDiffuse[i] /= lightCount;
        lightSpecular[i] /= lightCount;
      }
    }

    gl.glEnable(lightIndex);
    gl.glLightfv(lightIndex, GL2.GL_POSITION, lightPosition, 0);
    gl.glLightfv(lightIndex, GL2.GL_AMBIENT, lightAmbient, 0);
    gl.glLightfv(lightIndex, GL2.GL_SPECULAR, lightSpecular, 0);
    gl.glLightfv(lightIndex, GL2.GL_DIFFUSE, lightDiffuse, 0);

    switch (light.getType()) {
      case DIRECTIONAL:
        lightPosition[3] = -1;
        gl.glLightfv(lightIndex, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightf(lightIndex, GL2.GL_SPOT_CUTOFF, 0);
        break;
      case POINT:
        lightPosition[3] = 1;
        gl.glLightfv(lightIndex, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightf(lightIndex, GL2.GL_SPOT_CUTOFF, 0);
        break;
      case SPOT:
        lightPosition[3] = 1;
        gl.glLightfv(lightIndex, GL2.GL_POSITION, lightPosition, 0);

        float lightDirection[] = { 1, 1, 1, 1 };
        lightDirection[0] = (float) light.getDirection().get(0);
        lightDirection[1] = (float) light.getDirection().get(1);
        lightDirection[2] = (float) light.getDirection().get(2);
        lightDirection[3] = 1;
        gl.glLightfv(lightIndex, GL2.GL_SPOT_DIRECTION, lightDirection, 0);

        gl.glLightf(lightIndex, GL2.GL_SPOT_CUTOFF, (float) (Math.PI * light.getSpotLightAngle() / 180.0));
        break;
      default:
        Logger.getInstance().error("Unsupported light type: " + light.getType());
        break;
    }
  }

  public void resize(GLAutoDrawable drawable, int w, int h) {
    GL2 gl = drawable.getGL().getGL2();
    gl.glViewport(0, 0, w, h);
    aspectRatio = (float) w / (float) h;
    setPerspectiveProjection(gl);
    updateExtrinsicCameraParameters(drawable);
  }

  /**
   * Draw content
   */
  public void draw(GLAutoDrawable drawable) {
    if (rootNode.areShadowsAllowed()) {
      drawWithShadow(drawable);
    } else {
      drawWithoutShadow(drawable);
    }
  }

  /**
   * Draw content without shadows
   */
  private void drawWithoutShadow(GLAutoDrawable drawable) {
    // clear the color buffer and the depth buffer
    GL2 gl = drawable.getGL().getGL2();

    if (rootNode.getUseBlending()) {
      gl.glEnable(GL2.GL_BLEND);
      gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    // Update intrinsic camera settings
    updateIntrinsicCameraParameters(gl);

    // Update extrinsic camera
    updateExtrinsicCameraParameters(drawable);

    // Set some lights
    updateLights(gl);

    // Render all registered renderables.
    for (JoglRenderable renderable : renderables) {
      renderable.draw3D(gl);
    }

    // Render the scene graph
    renderNode(rootNode, gl, false, null, null, null);

    // Render the current camera path
    renderCameraPath(gl);

    // Take screenshot
    checkTakeScreenshot(drawable);

    JoglHelper.hasGLError(gl, "GL rendering");

    // Show framerate
    if (rootNode.isShowFps()) {
      Logger.getInstance().message("FPS: " + drawable.getAnimator().getLastFPS());
    }
  }

  /**
   * Draw content with shadows
   */
  private void drawWithShadow(GLAutoDrawable drawable) {

    // clear the color buffer and the depth buffer
    GL2 gl = drawable.getGL().getGL2();
    gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

    if (rootNode.getUseBlending()) {
      gl.glEnable(GL2.GL_BLEND);
      gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    // Update cam
    updateExtrinsicCameraParameters(drawable);

    // Load projection infinite matrix
    Matrix pInf = getProjectionInfinity();
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
    gl.glLoadMatrixd(pInf.data(), 0);

    // Load Modelview matrix
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

    // Enable depth testing
    gl.glEnable(GL2.GL_DEPTH_TEST);
    gl.glDepthFunc(GL2.GL_LESS);

    // Enable back-face culling
    gl.glEnable(GL2.GL_CULL_FACE);
    gl.glCullFace(GL2.GL_BACK);

    // Enable ambient light only
    gl.glEnable(GL2.GL_LIGHTING);
    for (int i = 0; i < rootNode.getNumberOfLights(); i++) {
      disableLight(gl, i);
    }
    float[] lightAmbientOn = { 1f, 1f, 1f, 1f };
    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lightAmbientOn, 0);

    // Render the scene graph
    renderScene(gl, false, null);

    // Disable depth writes
    gl.glDepthMask(false);

    // Enable additive blending
    gl.glEnable(GL.GL_BLEND);
    gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);

    // "Disable" ambient light
    float[] lightAmbientOff = { 0, 0, 0, 0 };
    gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lightAmbientOff, 0);

    // For all light sources
    for (int i = 0; i < rootNode.getNumberOfLights(); i++) {
      LightSource light = rootNode.getLight(i);

      // if (!useTwoSidedStencil) {
      // // Create a copy of the current light.
      // // This step is required if an animation is moving the current light
      // // source while drawing the shadows.
      // light = light.copy();
      // }

      drawShadowVolumes(gl, light, i);

      LightSource[] lights = null;
      switch (light.getShadowType()) {
        case PLANE_X:
          lights = generateSoftShadowPlane(light, 0);
          break;
        case PLANE_Y:
          lights = generateSoftShadowPlane(light, 1);
          break;
        case PLANE_Z:
          lights = generateSoftShadowPlane(light, 2);
          break;
        case SPHERE:
          lights = generateSoftShadowSphere(light);
          break;
        default:
          // Continue, we already have a hard shadow
          break;
      }
      if (lights != null) {
        for (int j = 0; j < lights.length; j++) {
          drawShadowVolumes(gl, lights[j], i);
        }
      }
    }
    // Disable blending and re-enable depth writes
    gl.glDisable(GL.GL_BLEND);
    gl.glDepthMask(true);

    // Take screenshot
    checkTakeScreenshot(drawable);

    // Show framerate
    if (rootNode.isShowFps()) {
      // CgGlslShader shaderTexture =
      // ResourceManager.getShaderManagerInstance().getResource(Material.SHADER_TEXTURE);
      // JoglShader.use(shaderTexture, gl);
      // renderer.setUseVertexArrays(false);
      // renderer.beginRendering(SCREEN_WIDTH, SCREEN_HEIGHT);
      // renderer.setColor(0f, 0f, 0f, 0.8f);
      // renderer.draw("FPS: " + drawable.getAnimator().getLastFPS(), 0,
      // SCREEN_HEIGHT - 20);
      // renderer.endRendering();
      Logger.getInstance().message("FPS: " + drawable.getAnimator().getLastFPS());
    }

    cameraPositionChanged = false;
    JoglHelper.hasGLError(gl, "GL rendering");
  }

  /**
   * Generates additional lights required for plane based soft shadows
   * 
   * @param light
   *          Current light source
   * @param coord
   *          Positioning lights around this coordinate
   * @return Additional lights
   */
  private LightSource[] generateSoftShadowPlane(LightSource light, int coord) {
    LightSource[] lights = new LightSource[softShadowPlaneCount - 1];

    for (int i = 0; i < lights.length; i++) {
      lights[i] = new LightSource(light.getType(), light.getShadowType(), light.getLightStrength());
      lights[i].setColor(light.getDiffuseColor());
      lights[i].setDirection(light.getDirection());
    }

    float offset = softShadowOffset;

    if (coord == 0) {
      lights[0].setPosition(light.getPosition().add(VectorFactory.createVector3(0, offset, 0)));
      lights[1].setPosition(light.getPosition().add(VectorFactory.createVector3(0, -offset, 0)));
      lights[2].setPosition(light.getPosition().add(VectorFactory.createVector3(0, 0, offset)));
      lights[3].setPosition(light.getPosition().add(VectorFactory.createVector3(0, 0, -offset)));
      lights[4].setPosition(light.getPosition().add(VectorFactory.createVector3(0, offset, offset)));
      lights[5].setPosition(light.getPosition().add(VectorFactory.createVector3(0, -offset, offset)));
      lights[6].setPosition(light.getPosition().add(VectorFactory.createVector3(0, offset, -offset)));
      lights[7].setPosition(light.getPosition().add(VectorFactory.createVector3(0, -offset, -offset)));
    } else if (coord == 1) {
      lights[0].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, 0, 0)));
      lights[1].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, 0, 0)));
      lights[2].setPosition(light.getPosition().add(VectorFactory.createVector3(0, 0, offset)));
      lights[3].setPosition(light.getPosition().add(VectorFactory.createVector3(0, 0, -offset)));
      lights[4].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, 0, offset)));
      lights[5].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, 0, offset)));
      lights[6].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, 0, -offset)));
      lights[7].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, 0, -offset)));
    } else {
      lights[0].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, 0, 0)));
      lights[1].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, 0, 0)));
      lights[2].setPosition(light.getPosition().add(VectorFactory.createVector3(0, offset, 0)));
      lights[3].setPosition(light.getPosition().add(VectorFactory.createVector3(0, -offset, 0)));
      lights[4].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, offset, 0)));
      lights[5].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, offset, 0)));
      lights[6].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, -offset, 0)));
      lights[7].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, -offset, 0)));
    }

    return lights;
  }

  /**
   * Generates additional lights required for plane sphere soft shadows
   * 
   * @param light
   *          Current light source
   * @return Additional lights
   */
  private LightSource[] generateSoftShadowSphere(LightSource light) {
    LightSource[] lights = new LightSource[softShadowSphereCount - 1];

    for (int i = 0; i < lights.length; i++) {
      lights[i] = new LightSource(light.getType());
      lights[i].setColor(light.getDiffuseColor());
      lights[i].setDirection(light.getDirection());
    }

    float offset = softShadowOffset;

    lights[0].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, 0, 0)));
    lights[1].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, 0, 0)));
    lights[2].setPosition(light.getPosition().add(VectorFactory.createVector3(0, offset, 0)));
    lights[3].setPosition(light.getPosition().add(VectorFactory.createVector3(0, -offset, 0)));
    lights[4].setPosition(light.getPosition().add(VectorFactory.createVector3(0, 0, offset)));
    lights[5].setPosition(light.getPosition().add(VectorFactory.createVector3(0, 0, -offset)));
    lights[6].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, offset, 0)));
    lights[7].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, -offset, 0)));
    lights[8].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, 0, offset)));
    lights[9].setPosition(light.getPosition().add(VectorFactory.createVector3(offset, 0, -offset)));
    lights[10].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, offset, 0)));
    lights[11].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, -offset, 0)));
    lights[12].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, 0, offset)));
    lights[13].setPosition(light.getPosition().add(VectorFactory.createVector3(-offset, 0, -offset)));

    return lights;
  }

  /**
   * Draws shadows into the scene in respect to the given light
   */
  private void drawShadowVolumes(GL2 gl, LightSource light, int lightID) {
    // Clear the stencil buffer
    gl.glClear(GL.GL_STENCIL_BUFFER_BIT);

    // Disable color buffer writes
    gl.glColorMask(false, false, false, false);

    // Enable stencil testing
    gl.glEnable(GL.GL_STENCIL_TEST);
    gl.glStencilFunc(GL.GL_ALWAYS, 0, ~0);
    gl.glStencilMask(~0);

    gl.glDisable(GL.GL_CULL_FACE);
    gl.glEnable(GL2.GL_STENCIL_TEST_TWO_SIDE_EXT);

    // Draw shadow volumes
    renderScene(gl, true, light);

    gl.glEnable(GL.GL_CULL_FACE);
    gl.glCullFace(GL.GL_BACK);

    // // Increment stencil buffer value for front-facing polygons that fail the
    // // depth test
    // gl.glCullFace(GL.GL_FRONT);
    // gl.glStencilOp(GL.GL_KEEP, GL.GL_INCR_WRAP, GL.GL_KEEP);
    // renderScene(gl, true, light);
    //
    // // Decrement stencil buffer value for back-facing polygons that fail the
    // // depth test
    // gl.glCullFace(GL.GL_BACK);
    // gl.glStencilOp(GL.GL_KEEP, GL.GL_DECR_WRAP, GL.GL_KEEP);
    // renderScene(gl, true, light);

    // Enable current light source
    updateLight(gl, lightID, true);

    // Set stencil to only render pixel with a 0 value
    gl.glStencilFunc(GL.GL_EQUAL, 0, ~0);
    gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_INCR);
    gl.glDepthFunc(GL.GL_EQUAL);
    gl.glColorMask(true, true, true, true);
    renderScene(gl, false, null);

    // Restore depth test
    gl.glDepthFunc(GL.GL_LESS);
    // Disable stencil tesing
    gl.glDisable(GL.GL_STENCIL_TEST);
  }

  // private void drawBlackSquareFullscreen(GL2 gl) {
  // Vector dir =
  // Camera.getInstance().getRef().subtract(Camera.getInstance().getEye()).getNormalized()
  // .multiply(nearClippingPlane * 1.5);
  // Vector center = Camera.getInstance().getEye().add(dir);
  // Vector x = VectorMatrixFactory.newVector(1, 1,
  // 1).cross(dir).getNormalized();
  // Vector y = x.cross(dir).getNormalized();
  // double length = 10;
  // Vector p0 = center.add(x.multiply(length)).add(y.multiply(length));
  // Vector p1 = center.add(x.multiply(length)).add(y.multiply(-length));
  // Vector p2 = center.add(x.multiply(-length)).add(y.multiply(length));
  // Vector p3 = center.add(x.multiply(-length)).add(y.multiply(-length));
  // gl.glBegin(GL2.GL_QUADS);
  // gl.glVertex3fv(p0.floatData(), 0);
  // gl.glVertex3fv(p1.floatData(), 0);
  // gl.glVertex3fv(p3.floatData(), 0);
  // gl.glVertex3fv(p2.floatData(), 0);
  // gl.glEnd();
  // }

  /**
   * Creates a projection matrix which has no far plane
   */
  private Matrix getProjectionInfinity() {
    Matrix pInf = MatrixFactory.createMatrix(4, 4);
    // Field of view in radians
    double rads = Math.toRadians(Camera.getInstance().getOpeningAngle());
    // Cotangent of the field of view
    double coTanFOV = 1.0 / Math.tan(rads);

    pInf.set(0, 0, coTanFOV / aspectRatio);
    pInf.set(0, 1, 0);
    pInf.set(0, 2, 0);
    pInf.set(0, 3, 0);
    pInf.set(1, 0, 0);
    pInf.set(1, 1, coTanFOV);
    pInf.set(1, 2, 0);
    pInf.set(1, 3, 0);
    pInf.set(2, 0, 0);
    pInf.set(2, 1, 0);
    pInf.set(2, 2, -1);
    pInf.set(2, 3, -1);
    pInf.set(3, 0, 0);
    pInf.set(3, 1, 0);
    pInf.set(3, 2, -2 * Camera.getInstance().getNearClippingPlane());
    pInf.set(3, 3, 0);

    return pInf;
  }

  /**
   * Update the intrinsic camera parameters.
   */
  private void updateIntrinsicCameraParameters(GL2 gl) {
    if (updateIntrinsicCameraParametersRequired) {
      setPerspectiveProjection(gl);
    }
    updateIntrinsicCameraParametersRequired = false;
  }

  /**
   * Set the perspective projection matrix based on the current settings.
   */
  public void setPerspectiveProjection(GL2 gl) {
    if (gl == null) {
      return;
    }
    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluPerspective(Camera.getInstance().getOpeningAngle(), aspectRatio, Camera.getInstance().getNearClippingPlane(),
        Camera.getInstance().getFarClippingPlane());
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
  }

  /**
   * Update the GL matrix stack based on the camera.
   */
  private void updateExtrinsicCameraParameters(GLAutoDrawable drawable) {
    if (updateExtrinsicCameraParametersRequired) {
      GL2 gl = drawable.getGL().getGL2();
      gl.glLoadIdentity();
      final float eyeX = (float) Camera.getInstance().getEye().get(0);
      final float eyeY = (float) Camera.getInstance().getEye().get(1);
      final float eyeZ = (float) Camera.getInstance().getEye().get(2);
      final float refX = (float) Camera.getInstance().getRef().get(0);
      final float refY = (float) Camera.getInstance().getRef().get(1);
      final float refZ = (float) Camera.getInstance().getRef().get(2);
      final float upX = (float) Camera.getInstance().getUp().get(0);
      final float upY = (float) Camera.getInstance().getUp().get(1);
      final float upZ = (float) Camera.getInstance().getUp().get(2);
      glu.gluLookAt(eyeX, eyeY, eyeZ, refX, refY, refZ, upX, upY, upZ);
      updateNearPlaneInformation(gl);
      cameraPositionChanged = true;
      updateExtrinsicCameraParametersRequired = false;
    }
    // if animations are used, we can't use this tweak
    if (AnimationTimer.getInstance().isRunning()) {
      cameraPositionChanged = true;
    }
  }

  private void updateNearPlaneInformation(GL2 gl) {
    // Field of View Y in radians
    double fieldOfViewY = Math.toRadians(Camera.getInstance().getOpeningAngle());
    Vector cPos = Camera.getInstance().getEye();
    Vector cUp = Camera.getInstance().getUp();
    Vector dRef = Camera.getInstance().getRef().subtract(cPos);
    Vector cRight = cUp.cross(dRef).multiply(-1).getNormalized();
    Vector dNear = dRef.multiply(Camera.getInstance().getNearClippingPlane() / dRef.getNorm());
    // Middle of the near plane
    Vector nearMiddle = cPos.add(dNear);

    double halfHeight = Math.tan(fieldOfViewY / 2.0) * Camera.getInstance().getNearClippingPlane();
    Vector heightUp = cUp.multiply(halfHeight);
    Vector heightDown = heightUp.multiply(-1);

    double fieldOfViewX = 2 * Math.atan(Math.tan(fieldOfViewY * 0.5) * aspectRatio);
    double halfWidth = Math.tan(fieldOfViewX / 2.0) * Camera.getInstance().getNearClippingPlane();
    Vector widthRight = cRight.multiply(halfWidth);
    Vector widthLeft = widthRight.multiply(-1);

    // Compute near plane corners
    nearPlaneCorners[0] = heightDown.add(widthLeft).add(nearMiddle); // lower
                                                                     // left
    nearPlaneCorners[1] = heightUp.add(widthLeft).add(nearMiddle); // upper left
    nearPlaneCorners[2] = heightUp.add(widthRight).add(nearMiddle); // upper
                                                                    // right
    nearPlaneCorners[3] = heightDown.add(widthRight).add(nearMiddle); // lower
                                                                      // right

    // Vector[] pyramidNormals = new Vector[5];
    // pyramidNormals[0] = getNormal(rootNode.getLight(0).getPosition(),
    // nearPlaneCorners[1], nearPlaneCorners[0]);
    // pyramidNormals[1] = getNormal(rootNode.getLight(0).getPosition(),
    // nearPlaneCorners[2], nearPlaneCorners[1]);
    // pyramidNormals[2] = getNormal(rootNode.getLight(0).getPosition(),
    // nearPlaneCorners[3], nearPlaneCorners[2]);
    // pyramidNormals[3] = getNormal(rootNode.getLight(0).getPosition(),
    // nearPlaneCorners[0], nearPlaneCorners[3]);
    // pyramidNormals[4] = getNormal(nearPlaneCorners[0], nearPlaneCorners[2],
    // nearPlaneCorners[1]);
    //
    // CgGlslShader shaderTexture =
    // ResourceManager.getShaderManagerInstance().getResource(Material.SHADER_PHONG_SHADING);
    // JoglShader.use(shaderTexture, gl);
    // gl.glBegin(GL2.GL_TRIANGLES);
    // gl.glNormal3fv(pyramidNormals[0].floatData(), 0);
    // gl.glVertex3fv(rootNode.getLight(0).getPosition().floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[1].floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[0].floatData(), 0);
    // gl.glNormal3fv(pyramidNormals[1].floatData(), 0);
    // gl.glVertex3fv(rootNode.getLight(0).getPosition().floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[2].floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[1].floatData(), 0);
    // gl.glNormal3fv(pyramidNormals[2].floatData(), 0);
    // gl.glVertex3fv(rootNode.getLight(0).getPosition().floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[3].floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[2].floatData(), 0);
    // gl.glNormal3fv(pyramidNormals[3].floatData(), 0);
    // gl.glVertex3fv(rootNode.getLight(0).getPosition().floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[0].floatData(), 0);
    // gl.glVertex3fv(nearPlaneCorners[3].floatData(), 0);
    // gl.glEnd();
  }

  // private Vector getNormal(Vector a, Vector b, Vector c) {
  // Vector v1 = b.subtract(a);
  // Vector v2 = c.subtract(a);
  // Vector n = v1.cross(v2);
  // n.normalize();
  // return n;
  // }

  /**
   * Check if a screenshot needs to be taken, do so if yes.
   */
  private void checkTakeScreenshot(GLAutoDrawable drawable) {
    if (screenshotFilename.length() > 0) {
      GLReadBufferUtil screenshot = new GLReadBufferUtil(true, false);
      if (screenshot.readPixels(drawable.getGL(), false)) {
        screenshot.write(new File(screenshotFilename));
        Logger.getInstance().message("Saved screenshot to file " + screenshotFilename);
      } else {
        Logger.getInstance().error("Failed to write screenshot file.");
      }
      screenshotFilename = "";
    }
  }

  /**
   * Render the current camera path.
   */
  private void renderCameraPath(GL2 gl) {
    // Render camera path
    if (Camera.getInstance().showCameraPath()) {
      int RESOLUTION = 500;
      gl.glDisable(GL2.GL_LIGHTING);
      gl.glBegin(GL2.GL_LINE_STRIP);
      gl.glPointSize(2);
      gl.glColor3f(0.15f, 0.15f, 0.15f);
      for (int i = 0; i < RESOLUTION; i++) {
        float t = (float) i / (float) (RESOLUTION + 1);
        Vector p = Camera.getInstance().getCameraPathInterpolation(t);
        gl.glVertex3d(p.get(0), p.get(1), p.get(2));
      }
      gl.glEnd();
    }
  }

  private void renderScene(GL2 gl, boolean renderShadowVolume, LightSource lightSource) {
    renderNode(rootNode, gl, renderShadowVolume, lightSource, new Transformation(), nearPlaneCorners);
  }

  /**
   * Recursive method to render nodes.
   */
  private void renderNode(CgNode node, GL2 gl, boolean renderShadowVolume, LightSource lightSource,
      Transformation transformation, Vector[] nearPlaneCorners) {
    if (node == null) {
      return;
    }
    if (!node.isVisible()) {
      return;
    }

    // Falls nicht im view frustum -> return
    if (rootNode.useViewFrustumCulling()) {
      viewFrustum.computeVisibleScenePart(rootNode);
    }

    if (Camera.getInstance().getCenterViewRequired()) {
      setViewFromBoundingBox();
    }

    // Draw node
    JoglRenderNode renderNode = renderObjectMananger.getRenderNode(node);
    if (renderNode != null) {
      if (!renderShadowVolume) {
        renderNode.draw3D(gl);
      } else if (lightSource != null) {
        renderNode.draw3D(gl, lightSource, transformation, nearPlaneCorners, cameraPositionChanged);
      }
    }

    // Draw children
    for (int childIndex = 0; childIndex < node.getNumChildren(); childIndex++) {
      if (renderShadowVolume && node.getContent() instanceof Transformation) {
        Transformation t = (Transformation) node.getContent();
        if (t != null) {
          t.multiplyTransformation(transformation.getTransformation());
          renderNode(node.getChildNode(childIndex), gl, renderShadowVolume, lightSource, t, nearPlaneCorners);
        } else {
          Logger.getInstance().error("Transformation node is null.");
        }
      } else if (node.getContent() instanceof Animation) {
        // Special case: animation node, only render current time step.
        if (AnimationTimer.getInstance().getValue() == childIndex) {
          renderNode(node.getChildNode(childIndex), gl, renderShadowVolume, lightSource, transformation,
              nearPlaneCorners);
        }
      } else {
        // Non-animation node
        renderNode(node.getChildNode(childIndex), gl, renderShadowVolume, lightSource, transformation,
            nearPlaneCorners);
      }
    }

    if (renderNode != null) {
      renderNode.afterDraw(gl);
    }

  }

  public void setClipplingPlanes(double near, double far) {
    Camera.getInstance().setClipping(near, far);
    updateIntrinsicCameraParametersRequired = true;
  }

  /**
   * Get the bounding box of all visible scene content.
   */
  public BoundingBox getCurrentBoundingBox() {
    BoundingBox boundingBox = rootNode.getBoundingBox();
    for (JoglRenderable renderable : renderables) {
      if (renderable.getBoundingBox() != null && renderable.getBoundingBox().isInitialized()) {
        boundingBox.unite(renderable.getBoundingBox());
      }
    }
    return boundingBox;
  }

  /**
   * Setter.
   */
  public void setScreenshotFilename(String filename) {
    screenshotFilename = filename;
  }

  /**
   * Setter.
   */
  public void addRenderable(JoglRenderable renderable) {
    renderables.add(renderable);
  }

  /**
   * Returns the light index constant for an index
   */
  private int getLightIndex(int i) {
    switch (i) {
      case 0:
        return GLLightingFunc.GL_LIGHT0;
      case 1:
        return GLLightingFunc.GL_LIGHT1;
      case 2:
        return GLLightingFunc.GL_LIGHT2;
      case 3:
        return GLLightingFunc.GL_LIGHT3;
      case 4:
        return GLLightingFunc.GL_LIGHT4;
      case 5:
        return GLLightingFunc.GL_LIGHT5;
      case 6:
        return GLLightingFunc.GL_LIGHT6;
      case 7:
        return GLLightingFunc.GL_LIGHT7;
      default:
        return GLLightingFunc.GL_LIGHT0;
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof Camera) {
      updateExtrinsicCameraParametersRequired = true;
    } else if (o instanceof CgRootNode) {
      updateLightsRequired = true;
    }
  }

  /**
   * Setup the camera based to 'see' the accumulated bounding box of the scene.
   */
  public void setViewFromBoundingBox() {
    BoundingBox boundingBox = getCurrentBoundingBox();
    if (boundingBox.isInitialized()) {
      final double diameter = boundingBox.getDiameter();
      setClipplingPlanes(diameter * NEAR_CLIPPING_FACTOR, diameter * FAR_CLIPPING_FACTOR);
      Camera.getInstance().getCurrentController().fitToBoundingBox(boundingBox);
    } else {
      Logger.getInstance().message("Cannot center view - no visible nodes with valid bounding boxes.");
    }
  }
}