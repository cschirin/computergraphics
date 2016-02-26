/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * Lecture demo program.
 */
package cgresearch.graphics.scenegraph.icons;

import cgresearch.graphics.datastructures.trianglemesh.ITriangleMesh;
import cgresearch.graphics.scenegraph.CgNode;
import cgresearch.graphics.scenegraph.IconLoader;

/**
 * Icon type for mesh nodes.
 * 
 * @author Philipp Jenke
 * 
 */
public class IconTypeMesh extends IIconTypeStrategy {

    /**
     * Constructor
     */
    public IconTypeMesh() {
        image = IconLoader.getIcon("icons/mesh.png");
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * edu.haw.cg.scenegraph.icons.IIconTypeStrategy#fits(edu.haw.cg.scenegraph
     * .gui.SceneGraphViewerNode)
     */
    @Override
    public boolean fits(CgNode node) {
        return node.getContent() instanceof ITriangleMesh;
    }

}
