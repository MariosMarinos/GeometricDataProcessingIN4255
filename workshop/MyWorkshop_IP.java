package workshop;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import jv.number.PuDouble;
import jv.object.PsDebug;
import jv.object.PsConfig;
import jv.object.PsDialog;
import jv.object.PsUpdateIf;
import jvx.project.PjWorkshop_IP;

import java.awt.*;
import java.awt.event.*;

public class MyWorkshop_IP extends PjWorkshop_IP implements ActionListener {

	protected Button m_bcalculateGenus;//name of the button
	protected Button m_bcalculateconnect;

	MyWorkshop m_ws;
	PsDebug m_debug;

	public MyWorkshop_IP() {
		super();
		if(getClass() == MyWorkshop_IP.class)
			init();
	}
	
	public void init() {
		super.init();
		setTitle("My Workshop");  //settitle
	}
	
	public String getNotice() {
		return "Not sure about (3)";
	}
	
	public void setParent(PsUpdateIf parent) { //set parent??
		super.setParent(parent);
		m_ws = (MyWorkshop)parent;
	
		addSubTitle("Task 1");  
		
		m_bcalculateGenus = new Button("get genus and volume"); 
		m_bcalculateGenus.addActionListener(this);

		m_bcalculateconnect = new Button("calculate connectivity"); 
		m_bcalculateconnect.addActionListener(this);

		Panel panel1 = new Panel(new FlowLayout(FlowLayout.CENTER)); 
		panel1.add(m_bcalculateGenus);
		panel1.add(m_bcalculateconnect);
		add(panel1);// set panel flow layout:  arrange the components in a line, one after another (in a flow)
		
		validate();
	}
	
	/**
	 * Handle action events fired by buttons etc.
	 */
	public void actionPerformed(ActionEvent event) {//actions that called from press button
		Object source = event.getSource();
		if (source == m_bcalculateGenus) {
			m_ws.calculateGenus();
			m_ws.VolumeOfMesh();
			// // m_ws.m_geom.update(m_ws.m_geom);
			// m_btextArea.setText(gentext);
			return;
		}
		else if (source == m_bcalculateconnect) {
			int components = m_ws.NumofConnect();
			m_debug.message("The connect component number is "+ Integer.toString(components)+"\n");
			return;
		}
	}
	
	/**
	 * Get information which bottom buttons a dialog should create
	 * when showing this info panel.
	 */
	protected int getDialogButtons()		{
		return PsDialog.BUTTON_OK;
	}
}
