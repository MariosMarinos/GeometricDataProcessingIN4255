package workshop;

import jv.geom.PgElementSet;
import jv.object.PsDebug;
import jv.project.PgGeometry;
import jv.vecmath.PdMatrix;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;
import jvx.numeric.PnSparseMatrix;
import jvx.project.PjWorkshop;

public class Differential_coordinates extends PjWorkshop {
    PgElementSet m_geom;
    PgElementSet m_geomSave;

    public Differential_coordinates() {
        super("Gradient calculations");
		if (getClass() == Differential_coordinates.class) {
			init();
		}
    }

    public void init() {
        super.init();
    }

    @Override
    public void setGeometry(PgGeometry geom) {
        super.setGeometry(geom);
        m_geom 		= (PgElementSet)super.m_geom;
        m_geomSave 	= (PgElementSet)super.m_geomSave;
    }
    
    /**
     * Get the M_v matrix for all the meshes
     * @return The M_v matrix
     */
    private PnSparseMatrix getMatrixM() {
        int n = m_geom.getNumElements() * 3;
    	PnSparseMatrix MatrixM = new PnSparseMatrix(n, n);

    	for(int triangle_index = 0; triangle_index < m_geom.getNumElements(); triangle_index++) {
            int pos = triangle_index*3;
            
            MatrixM.setEntry(pos, pos, m_geom.getAreaOfElement(triangle_index));
            MatrixM.setEntry(pos+1, pos+1, m_geom.getAreaOfElement(triangle_index));
            MatrixM.setEntry(pos+2, pos+2, m_geom.getAreaOfElement(triangle_index));
    	}
    	return MatrixM;
    }

    /**
     * Computes a 3x3 gradient matrix that maps the linear polynomial over a triangle to its gradient vector.
     */
    public PdMatrix calcGradient(PdVector[] vertices) {
        // initialize the PdMatrix, the gradient matrix and the corresponding vectors.
        PdMatrix mesh_gradient = new PdMatrix(3, 3);

        PdVector p1 = vertices[0];
        PdVector p2 = vertices[1];
        PdVector p3 = vertices[2];
        // calculate the area manually with: area = 0.5 * ||(p2 - p1) x (p3 - p1)||
        PdVector V = PdVector.subNew(p2, p1);
        PdVector W = PdVector.subNew(p3, p1);

        double area = PdVector.crossNew(V, W).length() * 0.5;
        // construct the scalar
        double scalar = 1.0 / (2 * area);
        // Calculate the normal
        PdVector normal = PdVector.crossNew(V, W);
        normal.normalize();

        PdVector e1 = PdVector.subNew(p3, p2);
        PdVector e2 = PdVector.subNew(p1, p3);
        PdVector e3 = PdVector.subNew(p2, p1);

        // Set up the gradient matrix: to (N * e1, N * e2, N * e3)
        mesh_gradient.setColumn(0, PdVector.crossNew(normal, e1));
        mesh_gradient.setColumn(1, PdVector.crossNew(normal, e2));
        mesh_gradient.setColumn(2, PdVector.crossNew(normal, e3));
        // multiply the whole 3x3 matrix with a scalar which is the 1/2area(T) * (N * e1, N * e2, N * e3)
        mesh_gradient.multScalar(scalar);

        return mesh_gradient;
    }
    /**
     * Computes matrix G for a triangle mesh
     * Where G maps a continuous linear polynomial over all triangles of a mesh to its gradient vectors
     */
    public PnSparseMatrix calcLinearPolGradients() {
        if (m_geom == null)
            return null;
        // initialize the sparse G matrix according to assignment
        int m = m_geom.getNumElements() * 3;
        int n = m_geom.getNumVertices();
        PnSparseMatrix matrix_G = new PnSparseMatrix(m, n, 3);

        PiVector[] triangles = m_geom.getElements();

        for(int triangle_index = 0; triangle_index < triangles.length; triangle_index++) {
            PiVector triangle = triangles[triangle_index];

//            int[] global_indeces = triangle.getEntries();

            PdMatrix gradient = calcGradient(new PdVector[]{
                    m_geom.getVertex(triangle.getEntry(0)),
                    m_geom.getVertex(triangle.getEntry(1)),
                    m_geom.getVertex(triangle.getEntry(2))});

            // add the gradient to the sparse matrix G
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    matrix_G.addEntry((3 * triangle_index) + j, triangle.getEntry(i), gradient.getEntry(j, i));
                }
            }
        }
        return matrix_G;
    }

    /**
     * Computes the Cotangent matrix according to the formula in slides: S = G^{T} * M_{V} * G
     * @return the Cotangent matrix S which is symmetric (n x n).
     */
    public PnSparseMatrix calcCotangentMatrix(PnSparseMatrix matrixG, PnSparseMatrix matrixM){
        // initialize the Cotangent matrix S
        // calculate the G transposed as it is in the slides.
        PnSparseMatrix MatrixGTranspose = PnSparseMatrix.transposeNew(matrixG);
        // calculate according to formula from lectures: S = G^{T} * M_{V} * G
        PnSparseMatrix middle_matrix = PnSparseMatrix.multMatrices(MatrixGTranspose, matrixM, null);
        PnSparseMatrix S = PnSparseMatrix.multMatrices(middle_matrix, matrixG, null);
        return S;
    }

    public void calcLaplaceSelected() {
        PnSparseMatrix matrixG = calcLinearPolGradients();
        System.out.println("Matrix G:");
        System.out.println(matrixG.toString());

        PnSparseMatrix matrixM = getMatrixM();

        PnSparseMatrix cotangent_matrix_S = calcCotangentMatrix(matrixG, matrixM);
        System.out.println("Matrix S:");
        System.out.println(cotangent_matrix_S.toString());

//        PnSparseMatrix Laplace_matrix_L =

        m_geom.update(m_geom);
    }

    /**
     * Reset the geometry to its standard shape
     */
    public void reset() {
    	m_geom.setVertices(m_geomSave.getVertices().clone());
    	m_geom.update(m_geom);
    }
}
