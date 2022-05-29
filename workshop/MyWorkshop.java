package workshop;

import java.awt.Color;
import java.sql.Array;
import java.util.Arrays;

import java.util.List;
import java.util.ArrayList;

import jv.geom.PgElementSet;
import jv.object.PsDebug;
import jv.project.PgGeometry;
import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;
import jv.vecmath.PdMatrix;
import jv.vecmath.PiVector;
import jvx.project.PjWorkshop;
import jvx.numeric.PnStiffDiriConforming;

import util.Util;
import java.lang.Math;

public class MyWorkshop extends PjWorkshop {

	PgElementSet m_geom;
	PgElementSet m_geomSave;
	PsDebug m_debug;
	
	public MyWorkshop() {
		super("My Workshop");
		init();
	}
	
	@Override
	public void setGeometry(PgGeometry geom) {
		super.setGeometry(geom);
		m_geom 		= (PgElementSet)super.m_geom;
		m_geomSave 	= (PgElementSet)super.m_geomSave;
	}
	
	public void init() {		
		super.init();
	}
	
	public double SignedVolumeOfTriangle(PdVector p1, PdVector p2, PdVector p3) {//p1: point1. three points of a triangle 
		double xyz = p1.getEntry(0)*p2.getEntry(1)*p3.getEntry(2);
		double zxy = p2.getEntry(0)*p3.getEntry(1)*p1.getEntry(2);
		double yzx = p3.getEntry(0)*p1.getEntry(1)*p2.getEntry(2);
		double xzy = p1.getEntry(0)*p3.getEntry(1)*p2.getEntry(2);
		double yxz = p2.getEntry(0)*p1.getEntry(1)*p3.getEntry(2);
		double zyx = p3.getEntry(0)*p2.getEntry(1)*p1.getEntry(2);
		return (1.0f/6.0f)*(xyz + yzx + zxy - xzy - yxz - zyx);
	}

	public void VolumeOfMesh()
    {
		//calculate volume 
		PiVector[] eles = m_geom.getElements();
		PdVector[] verts = m_geom.getVertices();	
        double volume = 0.0;
        for (int i = 0; i < eles.length; i += 1)
        {
            PdVector p1 = verts[eles[i].getEntry(0)];
            PdVector p2 = verts[eles[i].getEntry(1)];
            PdVector p3 = verts[eles[i].getEntry(2)];
            volume += SignedVolumeOfTriangle(p1, p2, p3);
	
        }
        m_debug.message("calculate result: "+ Double.toString(Math.abs(volume)));
		//verification
		double autovol = m_geom.getVolume();
		m_debug.message("verification by build-in function: "+ Double.toString(autovol)+"\n");
		return;
    }

	public void calculateGenus() {
		int nov = m_geom.getNumVertices();
		int noe = m_geom.getNumEdges();
		int nof = m_geom.getNumElements();
		int genus = 1- (nov - noe + nof)/2;
		m_debug.message("The genus is "+ Integer.toString(genus)+"\n");
		return;
	}

	public int NumofConnect() {
		PiVector[] neighbours = m_geom.getNeighbours();
		int numtriangles = neighbours.length; 
		return countComponents(numtriangles, neighbours);
		//get int arrary of faces
	}

	public int countComponents(int n, PiVector[] neighbours) {				
        List<Integer>[] NeighborofFace = new List[n];                        
		int[][] faces = new int[n][3];

        for (int i = 0; i < n; i++) {
			NeighborofFace[i] = new ArrayList<>();   
			faces[i] = neighbours[i].getEntries();		
			for (int j =0; j<3; j++){
					NeighborofFace[i].add(faces[i][j]);			
			}
		}
        int components = 0;
        boolean[] visited = new boolean[n];				//mark whether have visted 
		//from Internet...Not sure
        for (int v = 0; v < n; v++) {
			components += dfs(v, NeighborofFace, visited);		
		}
		return components;
    }
	//source: https://www.cainiaojc.com/note/qagqjz.html
    int dfs(int u, List<Integer>[] NeighborofFace, boolean[] visited) {      //whether or not is a component
        if (visited[u]) return 0;              //no leaf leaft
        visited[u] = true;
        for (int v : NeighborofFace[u]) dfs(v, NeighborofFace, visited);              
        return 1;
    }


}