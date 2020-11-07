package bjut.ai.bn.learning.acob.multithreads;
import java.util.ArrayList;
import java.util.Random;

import bjut.ai.bn.BNGraph;
import bjut.ai.bn.score.Scores;
import bjut.ai.bn.score.Score;
import bjut.ai.bn.score.Bayes;

public class AntE {
	private double[][] m_heuristicInfo; 
	private double m_tao0;
	private double m_alpha = 0.4F; 
	private double m_beta = 0.6F;
	private double m_rou;
	private double m_q0;
	private BNGraph m_bnGraph;
	private int m_i, m_j;
	private final Random RANDOM;
	private Scores.TYPE type;
	private double m_mi = 1.0;
	private double base; 
	private int vexnum;
	private Score score = null;
	private double[][] globalPh;
	private double[][] fglobalPh;
	private double[] noParVexK2Value;
	private ArrayList<BNGraph> BestSolutions;

	
	
		/**
	 * 
	 * @param apheromone
	 *            double[][]
	 * @param analpha
	 *            double
	 * @param abeta
	 *            double
	 * @param arou
	 *            double
	 * @param aq0
	 *            double
	 * @param atao0
	 *            double
	 * @param aVexNum
	 *            int
	 */
	public AntE(double analpha, double abeta, double arou, double aq0,
			double atao0, int aVexNum, int ID, Scores.TYPE type1,
			double[][] InitHArray, Score score, double[][] globalPh,
			double[] noParVexK2Value, ArrayList<BNGraph> bestsolutions, Bayes p) {
		this.score = score;
		this.vexnum = aVexNum;
		m_beta = abeta;
		m_rou = arou;
		m_q0 = aq0;
		m_tao0 = atao0;
		m_i = -1;
		m_j = -1;
		this.noParVexK2Value = new double[noParVexK2Value.length];
		System.arraycopy(noParVexK2Value, 0, this.noParVexK2Value, 0,
				this.noParVexK2Value.length);
		
		this.m_alpha = analpha;
		m_bnGraph = new BNGraph(aVexNum);
		RANDOM = new Random();
		m_heuristicInfo = new double[aVexNum][aVexNum];
		this.type = type1;
		this.setInitH(InitHArray);
		
		for(int i=0;i<vexnum;i++)
		{
			for(int j=0;j<vexnum;j++)
			{
			      if(i!=j)
			    	  this.m_heuristicInfo[i][j]=InitHArray[i][j];
			      else
			    	  this.m_heuristicInfo[i][j]=Double.NEGATIVE_INFINITY;
			}
		}
		this.globalPh = globalPh;
		fglobalPh=new double[aVexNum][aVexNum];
		this.BestSolutions = bestsolutions;
	
		
	}

	public void setScore(Score s) {
		this.score = s;
	}


	private void updatePheromone() {
		if(globalPh.length==0)
			fglobalPh[m_i][m_j]=m_tao0;
		else
		  fglobalPh[m_i][m_j]=globalPh[m_i][m_j];
		globalPh[m_i][m_j] = (1 - m_rou) * globalPh[m_i][m_j] + m_rou * m_tao0;
	}
	
	private void updateab() {
		if(globalPh[m_i][m_j]<=m_tao0)
		{
		this.m_alpha=fglobalPh[m_i][m_j]/globalPh[m_i][m_j];
		}
		if(globalPh[m_i][m_j]>=m_tao0)
		{
		this.m_beta=fglobalPh[m_i][m_j]/globalPh[m_i][m_j];
		}
		
	}
	
	
	private void printPheromone() {
		for(int i=0;i<=m_i;i++)
		{
			for(int j=0;j<=m_j;j++)
			{
				System.out.println(globalPh[i][j]);
			}
		}
	}
	
	/**
	 * GlobalPheromoneMatrix is ACOB's
	 * 
	 * @param GlobalPheromoneMatrix
	 */
	private void updatePheromone(double[][] GlobalPheromoneMatrix) {
		GlobalPheromoneMatrix[m_i][m_j] = (1 - m_rou)
				* GlobalPheromoneMatrix[m_i][m_j] + m_rou * m_tao0;
	}


	public void setInitH(double[][] array) {
		for (int m = 0; m < array.length; m++) {
			System.arraycopy(array[m], 0, this.m_heuristicInfo[m], 0, vexnum);
			this.m_bnGraph.GetNode(m).K2Score = noParVexK2Value[m];	
		}		
	}

	/**
	 * circle
	 */
	private void eliminateCircle() {
		ArrayList<Integer> ancestorslist = null;
		ArrayList<Integer> descendantslist = null;

		ancestorslist = m_bnGraph.GetNode(m_j).GetAncestorNodesIndex();
		descendantslist = m_bnGraph.GetNode(m_i).GetDescendantNodesIndex();

		for (int i = 0, size1 = ancestorslist.size(); i < size1; i++) {
			for (int j = 0, size2 = descendantslist.size(); j < size2; j++) {
				m_heuristicInfo[ancestorslist.get(i).intValue()][descendantslist
						.get(j).intValue()] = Double.NEGATIVE_INFINITY;
			}
		}

	}

	/**
	 * 
	 * 
	 * @param ParentK2score
	 *            double
	 * @param parentlist
	 *            ArrayList
	 */
	private void updateHeuristicInfo(double ParentK2score,
			final ArrayList<Integer> parentlist) {

		for (int k = 0; k < vexnum; k++) {
			if (m_heuristicInfo[m_i][k] > (Double.NEGATIVE_INFINITY)) {
				double afterAddParentK2score = 0;
				parentlist.add(m_bnGraph.GetNode(k).GetNodeId());
				int index = parentlist.size();
				afterAddParentK2score = this.score.calcScore(m_bnGraph.GetNode(
						m_i).GetNodeId(), parentlist);
				m_heuristicInfo[m_i][k] =  afterAddParentK2score - ParentK2score;
				parentlist.remove(index - 1);
			}
		}
	}

	/**
	 * 
	 * @param base
	 *            double
	 */
	public void CIConstrainByValue(double base) {
		Scores.CITestByValue(base);
		for (int i = 0; i < vexnum; i++) {
			for (int j = 0; j < vexnum; j++) {
				if (Scores.ChiSquare[i][j] == 0) {
					this.m_heuristicInfo[i][j] = Double.NEGATIVE_INFINITY;
				}
			}
		}

	}

	/**
	 * AntE
	 */
	public void run() {
		ArrayList<Integer> parentlist = null;
		if (this.type.toString() == "CInew")
			this.CIConstrainByValue(this.base);
		do {
			this.choose_ijNew();

			if (m_heuristicInfo[m_i][m_j] > 0) {
				m_bnGraph.AddArc(m_j, m_i);
				m_heuristicInfo[m_i][m_j] = Double.NEGATIVE_INFINITY;
				parentlist = m_bnGraph.GetNode(m_i).GetParentNodesIndex();
				double ParentK2score = this.score.calcScore(m_bnGraph.GetNode(
						m_i).GetNodeId(), parentlist);
				this.m_bnGraph.GetNode(m_i).K2Score = ParentK2score;
				this.eliminateCircle();
				if (this.type == Scores.TYPE.AIO || this.type == Scores.TYPE.AIO )
					System.out.print("" );
				else
					this.updateHeuristicInfo(ParentK2score, parentlist);
				this.updatePheromone();				
				
			}
		} while (!stopchoose());
		this.m_bnGraph.K2Score = this.m_bnGraph.calcTotalK2Score(this.score);

		this.BestSolutions.add(this.m_bnGraph);

		
	}

	public void run1(Bayes p) {
		ArrayList<Integer> parentlist = null;
		do {
			this.choose_ijNew();
			
			if (m_heuristicInfo[m_i][m_j] >0) {

				m_bnGraph.AddArc(m_j, m_i);
				m_heuristicInfo[m_i][m_j] = Double.NEGATIVE_INFINITY;
				parentlist = m_bnGraph.GetNode(m_i).GetParentNodesIndex();
				//
				double ParentK2score = this.score.calcScore(m_bnGraph.GetNode(
						m_i).GetNodeId(), parentlist);

				this.m_bnGraph.GetNode(m_i).K2Score = ParentK2score;
				this.eliminateCircle();
				this.updateHeuristicInfo(ParentK2score, parentlist); //
				this.updatePheromone();
				
			}
			
		} while (!stopchoose());
		this.m_bnGraph.K2Score = this.m_bnGraph.calcTotalK2Score(this.score);
		this.BestSolutions.add(this.m_bnGraph);
		
	}

	/**
	 * 
	 * @param temp
	 *            double[][]
	 * @param mi
	 *            double
	 * @param mj
	 *            double
	 */
	private void findMax(final double[][] temp) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < temp.length; i++) {
			for (int j = 0; j < temp.length; j++) {
				if (temp[i][j] > max) {
					m_i = i;
					m_j = j;
					max = temp[i][j];
				}
			}
		}
	}

	/**
	 * choose_ij() ACS
	 */
	private void choose_ij() {
		double q = RANDOM.nextDouble();
		double sumvalue = 0.0;
		double value = 0.0;
		double probability[][];
		if (q <= m_q0) {
			probability = new double[vexnum][vexnum];
			for (int i = 0; i < vexnum; i++) {
				for (int j = 0; j < vexnum; j++) {
					if (i != j && m_heuristicInfo[i][j] > 0) {
						probability[i][j] = Math.pow(m_heuristicInfo[i][j],
								m_beta)
								* globalPh[i][j];
					} else {
						probability[i][j] = Double.NEGATIVE_INFINITY;
					}
				}
			}
			this.findMax(probability);
		} else { // exploration
			probability = new double[vexnum][vexnum];
			for (int i = 0; i < vexnum; i++) {
				for (int j = 0; j < vexnum; j++) {
					if (i != j && m_heuristicInfo[i][j] > 0) {
						probability[i][j] = Math.pow(m_heuristicInfo[i][j],
								m_beta)
								* globalPh[i][j];
						sumvalue += probability[i][j]; 
					} else {
						probability[i][j] = 0.0;
					}
				}
			}
			for (int i = 0; i < vexnum; i++)
				for (int j = 0; j < vexnum; j++)
					probability[i][j] /= sumvalue; 
			q = RANDOM.nextDouble();
			while (q <= 0.0)
				q = RANDOM.nextDouble();
			loop1: for (int i = 0; i < vexnum; i++)
				for (int j = 0; j < vexnum; j++) {
					value += probability[i][j];
					if (value >= q) {
						m_i = i;
						m_j = j;
						break loop1;
					}
				}
		}
	}

	private void initPart1(double[][] probability) {
		double sumvalue = 0.0;
		for (int i = 0; i < vexnum; i++) {
			for (int j = 0; j < vexnum; j++) {
				if (i != j && m_heuristicInfo[i][j] > 0) {
					probability[i][j] = Math.pow(m_heuristicInfo[i][j], m_beta)
							* globalPh[i][j];
					sumvalue += probability[i][j];
				} else {
					probability[i][j] = Double.NEGATIVE_INFINITY;
				}
			}
		}

	}


	private void choose_ijNew() {
		double q = RANDOM.nextDouble();
		double sumvalue = 0.0;
		double value = 0.0;
		double[][] probability = new double[vexnum][vexnum];

		if (q <= m_q0) {
			this.initPart1(probability);
			this.findMax(probability);

		} else { // exploration
			probability = new double[vexnum][vexnum];
			for (int i = 0; i < vexnum; i++) {
				for (int j = 0; j < vexnum; j++) {
					if (i != j && m_heuristicInfo[i][j] > 0) {
						probability[i][j] = Math.pow(m_heuristicInfo[i][j],
								m_beta)
								* Math.pow(
										globalPh[i][j],
										m_alpha);
						sumvalue += probability[i][j]; 
					} else {
						probability[i][j] = 0.0;
					}
				}
			}
			for (int i = 0; i < vexnum; i++)
				for (int j = 0; j < vexnum; j++) {
					probability[i][j] /= sumvalue; 
				}

			q = RANDOM.nextDouble();
			while (q <= 0.0)
				q = RANDOM.nextDouble();
			loop1: for (int i = 0; i < vexnum; i++)
				for (int j = 0; j < vexnum; j++) {
					value += probability[i][j]; 
					if (value >= q) {
						m_i = i;
						m_j = j;
						break loop1;
					}
				}

		}
	}

	protected boolean stopchoose() {
		for (int i = 0; i < vexnum; i++)
			for (int j = 0; j < vexnum; j++)
				if (m_heuristicInfo[i][j] > 0.0) {
					return false;
				}
		return true;
	}

	/**
	 * getBNgraph 
	 * 
	 * @return BNGraph
	 */
	public BNGraph getBNgraph() {
		return this.m_bnGraph;
	}

	/**
	 * getBNGraphScore 
	 * 
	 * @return double
	 */
	public double getBNGraphScore() {
		return this.m_bnGraph.getScore();
	}
	


  
}
