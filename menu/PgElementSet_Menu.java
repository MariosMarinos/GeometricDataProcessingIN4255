package menu;

import jv.geom.PgPointSet_Menu;
import jv.geom.PgElementSet;
import jv.object.PsDebug;
import jv.object.PsDialog;
import jv.object.PsObject;
import jv.project.PvDisplayIf;
import jv.project.PvViewerIf;
import jvx.project.PjWorkshop_Dialog;
import workshop.*;

public class PgElementSet_Menu extends PgPointSet_Menu {
	
	private enum MenuEntry{
		MyWorkshop			("MyWorkshop..."),
		Registration		("Surface Registration..."),
		Differential_coordinates("Differential Coordinates..."),
		// Additional entries...
		;
		protected final String name;
		MenuEntry(String name) { this.name  = name; }
		
		public static MenuEntry fromName(String name){
			for (MenuEntry entry : MenuEntry.values()) {
				if(entry.name.equals(name)) return entry;
			}
			return null;
		}
	}	
	
	protected PgElementSet m_elementSet;
	
	protected PvViewerIf m_viewer;

	public void init(PsObject anObject) {
		super.init(anObject);
		m_elementSet = (PgElementSet)anObject;
		
		String menuDev = "My Workshops";
		addMenu(menuDev);
		for (MenuEntry entry : MenuEntry.values()) {
			addMenuItem(menuDev, entry.name);
		}
	}
	
	public boolean applyMethod(String aMethod) {
		if (super.applyMethod(aMethod))
			return true;

		if (PsDebug.NOTIFY) PsDebug.notify("trying method = "+aMethod);

		PvDisplayIf currDisp = null;
		if (getViewer() == null) {
			if (PsDebug.WARNING) PsDebug.warning("missing viewer");
		} else {
			currDisp = getViewer().getDisplay();
			if (currDisp == null) PsDebug.warning("missing display.");
		}

		PsDialog dialog;
		MenuEntry entry = MenuEntry.fromName(aMethod);
		if(entry == null) return false;
		switch (entry) {
		case MyWorkshop:
			MyWorkshop ws = new MyWorkshop();
			ws.setGeometry(m_elementSet);
			if (currDisp == null) {
				if (PsDebug.WARNING) PsDebug.warning("missing display.");
			} else
				ws.setDisplay(currDisp);
			dialog = new PjWorkshop_Dialog(false);
			dialog.setParent(ws);
			dialog.update(ws);
			dialog.setVisible(true);
			break;
		case Registration:
			Registration reg = new Registration();
			reg.setGeometry(m_elementSet);
			if (currDisp == null) {
				if (PsDebug.WARNING) PsDebug.warning("missing display.");
			} else
				reg.setDisplay(currDisp);
			dialog = new PjWorkshop_Dialog(false);
			dialog.setParent(reg);
			dialog.update(reg);
			dialog.setVisible(true);
			break;
		case Differential_coordinates:
			Differential_coordinates Differential_coordinates = new Differential_coordinates();
			Differential_coordinates.setGeometry(m_elementSet);
			if (currDisp == null) {
				if (PsDebug.WARNING) PsDebug.warning("missing display.");
			} else
				Differential_coordinates.setDisplay(currDisp);
			dialog = new PjWorkshop_Dialog(false);
			dialog.setParent(Differential_coordinates);
			dialog.update(Differential_coordinates);
			dialog.setVisible(true);
			break;
		}
		
		return true;
	}
	
	public PvViewerIf getViewer() { return m_viewer; }

	public void setViewer(PvViewerIf viewer) { m_viewer = viewer; }		
	
}	
