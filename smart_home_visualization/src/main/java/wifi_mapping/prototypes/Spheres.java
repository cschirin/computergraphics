package wifi_mapping.prototypes;

import cgresearch.AppLauncher;
import cgresearch.JoglAppLauncher;
import cgresearch.core.assets.ResourcesLocator;
import cgresearch.core.math.Vector;
import cgresearch.core.math.VectorFactory;
import cgresearch.graphics.bricks.CgApplication;
import cgresearch.graphics.datastructures.trianglemesh.ITriangleMesh;
import cgresearch.graphics.datastructures.trianglemesh.TriangleMeshFactory;
import cgresearch.graphics.material.Material;
import cgresearch.graphics.scenegraph.CgNode;

/**
 * Prototype 1: 3 Spheres
 * Created by christian on 14.06.16.
 */
public class Spheres extends CgApplication {

    private CgNode outerNode;
    private CgNode middleNode;
    private CgNode innerNode;

    /** Constructor. */
    private Spheres() {
        setUpRootNode();
        makeSpheres();
    }

    private void setUpRootNode() {
        //Enable transparency
        getCgRootNode().setUseBlending(true);
    }

    private void makeSpheres() {
        makeInnerSphere();
        makeMidSphere();
        makeOuterSphere();
    }

    public Vector getOuterColour() {
        return outerColour;
    }


    private Vector outerColour;

    public Vector getInnerColour() {
        return innerColour;
    }

    private Vector innerColour;

    private void makeOuterSphere() {
        ITriangleMesh outerSphere = TriangleMeshFactory.createSphere(VectorFactory.createVector3(0, 0, 0), 3, 100);
        outerSphere.getMaterial().setShaderId(Material.SHADER_PHONG_SHADING);
        //Vector outerColour = new Vector(0, 0, 139 / 255.0); //dark blue
        //Vector outerColour = new Vector(81 / 255.0, 102 / 255.0, 0); //green
        outerColour = new Vector(53 / 255.0 , 0, 148 / 255.0); //"blue" "purple" "burple"????  (Visual studio logo colour)
        outerSphere.getMaterial().setReflectionDiffuse(outerColour);
        outerSphere.getMaterial().setTransparency(0.5);
        outerNode = new CgNode(outerSphere, "OuterSphere");
        getCgRootNode().addChild(outerNode);
    }

    private void makeMidSphere() {
        ITriangleMesh middleSphere = TriangleMeshFactory.createSphere(VectorFactory.createVector3(0, 0, 0), 2, 100);
        middleSphere.getMaterial().setShaderId(Material.SHADER_PHONG_SHADING);
        //Vector middleColour = new Vector(255 / 255.0, 170 / 255.0, 0); //mint green
        //Vector middleColour = new Vector(0, 191 / 255.0, 168 / 255.0); //turquoise
        Vector middleColour = new Vector(226 / 255.0 , 0, 255 / 255.0); //violet
        middleSphere.getMaterial().setReflectionDiffuse(middleColour);
        middleSphere.getMaterial().setTransparency(0.50);
        middleNode = new CgNode(middleSphere, "MidSphere");
        getCgRootNode().addChild(middleNode);
    }

    private void makeInnerSphere() {
        ITriangleMesh innerSphere = TriangleMeshFactory.createSphere(VectorFactory.createVector3(0, 0, 0), 1, 100);
        innerSphere.getMaterial().setShaderId(Material.SHADER_PHONG_SHADING);
        //Vector innerColour = new Vector(255 / 255.0, 170 / 255.0, 0); //orange
        //Vector innerColour = new Vector(20 / 255.0 , 0, 255 / 255.0); //blue
        innerColour = new Vector(255 / 255.0, 0, 137 / 255.0); //magenta
        innerSphere.getMaterial().setReflectionDiffuse(innerColour);
        innerNode = new CgNode(innerSphere, "InnerSphere");
        getCgRootNode().addChild(innerNode);
    }

    public static void main(String[] args) {
        ResourcesLocator.getInstance().parseIniFile("resources.ini");
        CgApplication app = new Spheres();
        JoglAppLauncher appLauncher = JoglAppLauncher.getInstance();
        appLauncher.create(app);
        appLauncher.setRenderSystem(AppLauncher.RenderSystem.JOGL);
        appLauncher.setUiSystem(AppLauncher.UI.JOGL_SWING);
        appLauncher.addCustomUi(new SpheresGui((Spheres)app));
    }

    public void updateColour() {
        //TODO lerp for all mesh colours from inner to outer
        outerNode.getContent().getMaterial().setReflectionDiffuse(outerColour);
        innerNode.getContent().getMaterial().setReflectionDiffuse(innerColour);
    }

}
