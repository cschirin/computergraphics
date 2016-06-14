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

    /** Constructor .*/
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

    private void makeOuterSphere() {
        ITriangleMesh mSphere = TriangleMeshFactory.createSphere(VectorFactory.createVector3(0, 0, 0), 3, 100);
        mSphere.getMaterial().setShaderId(Material.SHADER_PHONG_SHADING);
        mSphere.getMaterial().addShaderId(Material.SHADER_COLOR);
        //Vector oColor = new Vector(0, 0, 139 / 255.0); //dark blue
        Vector oColor = new Vector(81 / 255.0, 102 / 255.0, 0); //green
        mSphere.getMaterial().setReflectionDiffuse(oColor);
        mSphere.getMaterial().setTransparency(0.5);
        CgNode node = new CgNode(mSphere, "OuterSphere");
        getCgRootNode().addChild(node);
    }

    private void makeMidSphere() {
        ITriangleMesh mSphere = TriangleMeshFactory.createSphere(VectorFactory.createVector3(0, 0, 0), 2, 100);
        mSphere.getMaterial().setShaderId(Material.SHADER_PHONG_SHADING);
        mSphere.getMaterial().addShaderId(Material.SHADER_COLOR);
        //Vector mColor = new Vector(255 / 255.0, 170 / 255.0, 0); //mint green
        Vector mColor = new Vector(0, 191 / 255.0, 168 / 255.0); //turquoise
        mSphere.getMaterial().setReflectionDiffuse(mColor);
        mSphere.getMaterial().setTransparency(0.50);
        CgNode node = new CgNode(mSphere, "MidSphere");
        getCgRootNode().addChild(node);
    }

    private void makeInnerSphere() {
        ITriangleMesh iSphere = TriangleMeshFactory.createSphere(VectorFactory.createVector3(0, 0, 0), 1, 100);
        iSphere.getMaterial().setShaderId(Material.SHADER_PHONG_SHADING);
        iSphere.getMaterial().addShaderId(Material.SHADER_COLOR);
        //Vector iColor = new Vector(255 / 255.0, 170 / 255.0, 0); //orange
        Vector iColor = new Vector(20 / 255.0 , 0, 255 / 255.0); //blue
        iSphere.getMaterial().setReflectionDiffuse(iColor);
        CgNode node = new CgNode(iSphere, "InnerSphere");
        getCgRootNode().addChild(node);
    }

    public static void main(String[] args) {
        ResourcesLocator.getInstance().parseIniFile("resources.ini");
        CgApplication app = new Spheres();
        JoglAppLauncher appLauncher = JoglAppLauncher.getInstance();
        appLauncher.create(app);
        appLauncher.setRenderSystem(AppLauncher.RenderSystem.JOGL);
        appLauncher.setUiSystem(AppLauncher.UI.JOGL_SWING);
    }

}
