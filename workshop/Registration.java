package workshop;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import jv.geom.PgBndPolygon;
import jv.geom.PgElementSet;
import jv.geom.PgPolygonSet;
import jv.geom.PgVectorField;
import jv.geom.PuCleanMesh;
import jv.number.PdColor;
import jv.object.PsConfig;

import jv.object.PsDebug;
import jv.object.PsObject;
import jv.project.PgGeometry;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;
import jv.vecmath.PuMath;
import java.lang.Math;
import jv.viewer.PvDisplay;
import jv.project.PvGeometryIf;
import java.lang.Object;

import jvx.project.PjWorkshop;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
/**
 *  Workshop for surface registration
 */

public class Registration extends PjWorkshop {	
	/** First surface to be registered. */	
	PgElementSet	m_surfP;	
	/** Second surface to be registered. */
	PgElementSet	m_surfQ;	
	PsDebug m_debug;	


	/** Constructor */
	public Registration() {
		super("Surface Registration");
		if (getClass() == Registration.class) {
			init();
		}
	}	
	/** Initialization */
	public void init() {
		super.init();
	}
		
	public void setGeometries(PgElementSet surfP, PgElementSet surfQ) {
		m_surfP = surfP;
		m_surfQ = surfQ;
	}

	public Double distance(PdVector vP, PdVector vQ, int Method, int vertexInd){
		//Method=0: distance points; Method=1: distance planes; 
		Double disPQ = 0d;
		Double xP = vP.getEntry(0);
		Double yP = vP.getEntry(1);
		Double zP = vP.getEntry(2);
		Double xQ = vQ.getEntry(0);
		Double yQ = vQ.getEntry(1);
		Double zQ = vQ.getEntry(2);
		if (Method == 0){
			disPQ = (xP-xQ)*(xP-xQ)+(yP-yQ)*(yP-yQ)+(zP-zQ)*(zP-zQ);
		}	
		else if (Method == 1){
			PdVector Norm =m_surfQ.getVertexNormal(vertexInd);
			Double xNorm = Norm.getEntry(0);
			Double yNorm = Norm.getEntry(1);
			Double zNorm = Norm.getEntry(2);
			//distance=((Pi-Qi)T*ni)^2
			disPQ = ((xP-xQ)*xNorm+(yP-yQ)*yNorm+(zP-zQ)*zNorm)*((xP-xQ)*xNorm+(yP-yQ)*yNorm+(zP-zQ)*zNorm);
		}		
		return disPQ;	
	}

	public void RigidRegistration(int n, int k, int Method){
		//n: number of random chosen points
		PdVector[] vertsP = m_surfP.getVertices();	
		// m_debug.message("\nP ver1: ("+Double.toString(vertsP[0].getEntry(0))+","+Double.toString(vertsP[0].getEntry(1))+","+Double.toString(vertsP[0].getEntry(2))+")");
		PdVector[] vertsQ = m_surfQ.getVertices();	
		// m_debug.message("Q ver1: ("+Double.toString(vertsQ[0].getEntry(0))+","+Double.toString(vertsQ[0].getEntry(1))+","+Double.toString(vertsQ[0].getEntry(2))+")\n");

		int[] indexP = randomIndex(vertsP.length,n);//i of Pi
		PdVector[] Pi = new PdVector[n];
		PdVector[] Qi = new PdVector[n];

		if (Method == 0){
			m_debug.message("\npoints to points");
		}	
		else if (Method == 1){
			m_debug.message("\npoints to planes");
		}

        for (int v = 0; v < n; v++) {
		 	Pi[v] = vertsP[indexP[v]];
			Double minDis = distance (Pi[v],vertsQ[0],0,0);
			int minId = 0;//initial values
			for (int r = 1; r < vertsQ.length; r++){
				Double thisDis = distance (Pi[v],vertsQ[r],0,r);
				if (minDis > thisDis) {
					minDis = thisDis;
					minId = r;
				}				
			}
			Qi[v] = vertsQ[minId];			
		}

		//Find median
		Double[] disPairs = new Double[Pi.length];//distances of pairs
		Double median = 0d;
		for (int v = 0; v < Pi.length; v++) {
			disPairs[v] = distance(Pi[v],Qi[v],0,v);			
		}
		Double[] disPairs2 = disPairs;
		Arrays.sort(disPairs2);		
		if (disPairs2.length % 2 == 0) {
			median = ((disPairs2[disPairs2.length / 2 - 1] + disPairs2[disPairs2.length / 2])) / 2;
		}else {
			median = disPairs2[disPairs2.length / 2];
		}
		/**
 		*  Remove Pairs
 		*/
		m_debug.message("median distance:"+Double.toString(median)+"\n");
		List<Integer> removedIndex = new ArrayList<>();//marked the removed pairs. simplified 
		for (int v = 0; v < Pi.length; v++){
			if(disPairs[v]>k*median){
				removedIndex.add(v);
			}
		}
		PdVector[] Norm =m_surfQ.getVertexNormals();
		PdVector[] Piprime = new PdVector[Pi.length-removedIndex.size()];
		PdVector[] Qiprime = new PdVector[Pi.length-removedIndex.size()];	
		PdVector[] NormPrime = new PdVector[Pi.length-removedIndex.size()];	
		int iremoved=0;	
		for (int r = 0; r < Pi.length; r++){
			if (!removedIndex.contains(r)){
				Piprime[iremoved] = Pi[r];
				Qiprime[iremoved] = Qi[r];
				NormPrime[iremoved] = Norm[r];
				iremoved++;
			}
		}
		/**
 		*  Optimal
 		*/
		int nlength=Piprime.length;
		double[][] R = new double[3][3];
		Double[] topt = new Double[3];

		if (Method == 0){
			Double[] centroifP = average(Piprime);// 
			Double[] centroifQ = average(Qiprime);
			double[][] arrayM = new double[3][3];
			for (int i = 0; i < nlength; i++) {
				for (int j=0;j<3;j++){
					for (int r=0;r<3;r++){
						arrayM[j][r] += ((Piprime[i].getEntry(j)-centroifP[j])*(Qiprime[i].getEntry(r)-centroifQ[r]))/nlength;
					}
				}		
			}
			Matrix M = new Matrix(arrayM);
			SingularValueDecomposition mm = M.svd();
			Matrix U = mm.getU();
			Matrix V = mm.getV();
			double[][] ss = {{1,0d,0d},{0d,1,0d},{0d,0d,V.times(U.transpose()).det()}};
			Matrix S = new Matrix(ss);			
			//matrix R
			Matrix matrixR = V.times(S).times(U.transpose());
			R = matrixR.getArray();
			//topt=q_aver-R*p_aver
			double[][] transCentroP= {{centroifP[0]},{centroifP[1]},{centroifP[2]}};
			Matrix cenP = new Matrix(transCentroP);
			Matrix Rp = matrixR.times(cenP);
			topt[0] = centroifQ[0]-Rp.get(0,0);
			topt[1] = centroifQ[1]-Rp.get(1,0);
			topt[2] = centroifQ[2]-Rp.get(2,0);
		}
		else if (Method ==1){
			// PdMatrix A = new PdMatrix(6,6);
			double[][] A = new double[6][6];
			double[][] b = new double[6][1];
			for (int i = 0; i < nlength; i++) {
				double p0= Piprime[i].getEntry(0);
				double p1= Piprime[i].getEntry(1);
				double p2= Piprime[i].getEntry(2);
				double n0= NormPrime[i].getEntry(0);
				double n1= NormPrime[i].getEntry(1);
				double n2= NormPrime[i].getEntry(2);		
				double[] AMultiplier = {p1*n2-p2*n1,p2*n0-p0*n2,p0*n1-p1*n0,n0,n1,n2};
				double bMultiplier = (p0-Qiprime[i].getEntry(0))*n0+(p1-Qiprime[i].getEntry(1))*n1+(p2-Qiprime[i].getEntry(2))*n2;
				for (int j=0;j<6;j++){
					for (int r=0;r<6;r++){
						A[j][r] += AMultiplier[j]*AMultiplier[r];
					}
					b[j][0] += 0-(bMultiplier*AMultiplier[j]);
				}				
			}
			Matrix Amat = new Matrix(A);
			Matrix bmat = new Matrix(b);
			Matrix X= Amat.solve(bmat);
			double[] Rr={X.get(0,0),X.get(1,0),X.get(2,0)}; 
			double[][] rhat = {{1,-Rr[2],Rr[1]},{Rr[2],1,-Rr[0]},{-Rr[1],Rr[0],1}};
			Matrix Rhat= new Matrix(rhat);
			SingularValueDecomposition RR = Rhat.svd();
			Matrix U = RR.getU();
			Matrix V = RR.getV();
			double[][] RRr = {{1,0d,0d},{0d,1,0d},{0d,0d,U.times(V.transpose()).det()}};
			Matrix S = new Matrix(RRr);			
			//matrix R
			Matrix matrixR = U.times(S).times(V.transpose());
			R = matrixR.getArray();
			//topt=q_aver-R*p_aver
			topt[0] = X.get(3,0);
			topt[1] = X.get(4,0);
			topt[2] = X.get(5,0);

		}
		m_debug.message("Ropt: ("+Double.toString(R[0][0])+","+Double.toString(R[0][1])+","+Double.toString(R[0][2])+")");
		m_debug.message("      ("+Double.toString(R[1][0])+","+Double.toString(R[1][1])+","+Double.toString(R[1][2])+")");
		m_debug.message("      ("+Double.toString(R[2][0])+","+Double.toString(R[2][1])+","+Double.toString(R[2][2])+")");
		m_debug.message("topt: ("+Double.toString(topt[0])+","+Double.toString(topt[1])+","+Double.toString(topt[2])+")\n");
		/**
 		*  Transform the P to Q
 		*/
		 // the double array is v.m_data 
		int nov = vertsP.length;
		Double[] transedPointQ = new Double[3];
		// PdVector[] vertsQPrime = new PdVector[nov];
		 for (int i=0; i<nov; i++) {
			 for (int j=0;j<3;j++){
				 //Q'=R*pi+t
				 transedPointQ[j] = R[j][0]*vertsP[i].getEntry(0)+R[j][1]*vertsP[i].getEntry(1)+R[j][2]*vertsP[i].getEntry(2)+topt[j];
			 }
			 m_surfP.setVertex(i, transedPointQ[0],transedPointQ[1],transedPointQ[2]);
		 }

		 //\\Q'i-qi\\^2
		 PdVector[] vertsQPrime = m_surfP.getVertices();//new surface
		
		/**
 		*  Evaluation: error function
 		*/
		Double f = 0d;
		for (int i=0; i<nov; i++){
			f += distance(vertsQPrime[i],vertsQ[i],0,i);
		}
		m_debug.message("n="+Integer.toString(n)+" k="+Integer.toString(k)+"\n Square Error: "+(Double.toString(f)));
		 return;

	}

	public static int[] randomIndex(int len, int n){  
		//len the length of list and n the chosen number
		int[] result = new int[n];  
		int count = 0;  
		while(count < n) {  
			int num = (int) (Math.random() * len);  //float between 0~1
			boolean flag = true;  
			for (int j = 0; j < n; j++) {  
				if(num == result[j]){  
					flag = false;  
					break;  
				}  
			}  
			if(flag){  
				result[count] = num;  
				count++;  
			}  
		}  
		return result;  
	}  
	public static Double[] average (PdVector[]  Vec){
		Double sumX =0d;
		Double sumY =0d;
		Double sumZ =0d;
		for (int i = 0; i < Vec.length; i++) {
			sumX += Vec[i].getEntry(0);
			sumY += Vec[i].getEntry(1);
			sumZ += Vec[i].getEntry(2);
		}
		Double[] averVec = {sumX/Vec.length,sumY/Vec.length,sumZ/Vec.length};
		return averVec;
	}
}
