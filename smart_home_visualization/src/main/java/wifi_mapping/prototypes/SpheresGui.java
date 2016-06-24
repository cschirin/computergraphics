package wifi_mapping.prototypes;

import cgresearch.core.math.Vector;
import cgresearch.ui.IApplicationControllerGui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * Created by christian on 24.06.16.
 */
public class SpheresGui extends IApplicationControllerGui implements ActionListener {


    private Spheres app;

    private JTextField rInnerTextField;
    private JTextField gInnerTextField;
    private JTextField bInnerTextField;

    private JTextField rOuterTextField;
    private JTextField gOuterTextField;
    private JTextField bOuterTextField;

    public SpheresGui(Spheres app) {
        this.app = app;
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        //Build the GUI
        JButton button = new JButton("Apply");
        button.addActionListener(this);
        add(button);
        // Build the text fields
        Vector defaultInnerColour = app.getInnerColour();
        rInnerTextField = new JTextField(Double.toString(defaultInnerColour.get(0)));
        gInnerTextField = new JTextField(Double.toString(defaultInnerColour.get(1)));
        bInnerTextField = new JTextField(Double.toString(defaultInnerColour.get(2)));

        Vector defaultOuterColour = app.getOuterColour();
        rOuterTextField = new JTextField(Double.toString(defaultOuterColour.get(0)));
        gOuterTextField = new JTextField(Double.toString(defaultOuterColour.get(1)));
        bOuterTextField = new JTextField(Double.toString(defaultOuterColour.get(2)));
        add(rInnerTextField); add(gInnerTextField); add(bInnerTextField);
        add(rOuterTextField); add(gOuterTextField); add(bOuterTextField);
    }

    @Override
    public String getName() {
        return "Spheres";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double rInner = Double.parseDouble(rInnerTextField.getText());
        double gInner = Double.parseDouble(gInnerTextField.getText());
        double bInner = Double.parseDouble(bInnerTextField.getText());
        app.getInnerColour().set(rInner, gInner, bInner);

        double rOuter = Double.parseDouble(rOuterTextField.getText());
        double gOuter = Double.parseDouble(gOuterTextField.getText());
        double bOuter = Double.parseDouble(bOuterTextField.getText());
        app.getOuterColour().set(rOuter, gOuter, bOuter);

        // TODO change this to the observer pattern supported by CgApplication
        app.updateColour();
    }
}
