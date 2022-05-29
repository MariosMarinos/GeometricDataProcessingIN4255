package workshop;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jv.object.PsDebug;
import jv.object.PsDialog;
import jv.object.PsUpdateIf;
import jv.vecmath.PdMatrix;
import jv.vecmath.PdVector;
import jvx.project.PjWorkshop_IP;

public class Differential_coordinates_IP extends PjWorkshop_IP implements ActionListener {
    protected Differential_coordinates differential_coordinates;

    protected Button btnReset;

    protected Button calcLaplaceMatrix;

    protected Button calcSparseG;

    public Differential_coordinates_IP () {
        super();
        if (getClass() == Differential_coordinates_IP.class)
            init();
    }

    public String getNotice() {
        return "Practical Assignment 2";
    }

    public void setParent(PsUpdateIf parent) {
    	try {
        super.setParent(parent);

        differential_coordinates = (Differential_coordinates) parent;

        addSubTitle("Gradients of Linear Polynomial:");

        calcLaplaceMatrix = new Button("Compute sparse Laplace Matrix L");
        calcLaplaceMatrix.addActionListener(this);
        Panel panel = new Panel(new FlowLayout(FlowLayout.LEFT));
        panel.add(calcLaplaceMatrix);
        add(panel);

        calcSparseG = new Button("Compute sparse Matrix G");
        calcSparseG.addActionListener(this);
        panel.add(calcSparseG);
        add(panel);

        btnReset = new Button("Reset");
        btnReset.addActionListener(this);
        panel.add(btnReset);

        this.add(panel);

        validate();
    	} catch(Exception E){
			StackTraceElement[] stacktrace = E.getStackTrace();
			for (StackTraceElement elem : stacktrace)
				PsDebug.message(elem.toString());
			PsDebug.warning(E.toString());
		}
    }

    public void init() {
        super.init();
        setTitle("Differential Coordinates");
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == calcLaplaceMatrix) {
            //	    this.testTriangleToGradient();
            differential_coordinates.calcLaplaceSelected();
            differential_coordinates.m_geom.update(differential_coordinates.m_geom);
            PsDebug.message("Laplace matrix was calculated.");
        } else if (source == calcSparseG) {
            differential_coordinates.calcLinearPolGradients();
            differential_coordinates.m_geom.update(differential_coordinates.m_geom);
            PsDebug.message("G matrix was calculated.");
            return;
        }
        else if (source == btnReset) {
            differential_coordinates.reset();
            differential_coordinates.m_geom.update(differential_coordinates.m_geom);
            }
        }


    private void testTriangleToGradient() {
        PdVector[] mesh_1 = new PdVector[]{new PdVector(0.0,0.0,0.0), new PdVector(1.0,1.0,0.0), new PdVector(0.0,1.0,1.0)};
        double[][] expect_matrix = {{-1.0, 2.0, -1.0}, {-2.0, 1.0, 1.0}, {-1.0, -1.0, 2.0}};
        PdMatrix expectedMatrix = new PdMatrix(expect_matrix);
        expectedMatrix.multScalar(1.0/3.0);

        PsDebug.message("Expect: " + expectedMatrix.toShortString());
        PsDebug.message("Got: " + differential_coordinates.calcGradient(mesh_1));

        PdVector[] mesh_2 = new PdVector[]{
                new PdVector(-0.523035,0.4749694,0.436263),
                new PdVector(0.528191,0.492968,0.448928),
                new PdVector(-0.714874,1.3084,-0.42234)};
        expect_matrix = new double[][]{
                {-0.947366, 0.950318, -0.00295215},
                {-0.70442, 0.120702, 0.583718},
                {-0.692358, -0.0951279, -0.59723}};
        expectedMatrix = new PdMatrix(expect_matrix);

        PsDebug.message("Expect: " + expectedMatrix.toShortString());
        PsDebug.message("Got: " + differential_coordinates.calcGradient(mesh_2));
    }

    protected int getDialogButtons(){
        return PsDialog.BUTTON_OK;
    }
}
