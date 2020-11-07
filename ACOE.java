package bjut.ai.bn.learning.acob.multithreads;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import bjut.ai.bn.learning.acob.AlarmReader;
import bjut.ai.bn.BNGraph;
import bjut.ai.bn.CommonTools;
import bjut.ai.bn.HillClimbing;
import bjut.ai.bn.score.K2;
import bjut.ai.bn.score.BICGScore;
import bjut.ai.bn.score.SELF;
import bjut.ai.bn.score.Score;
import bjut.ai.bn.score.Bayes;


public class ACOE {

	public double[][] tempH = null;
	public double[] noParVexK2Value = new double[SELF.VEXNUM];
	public double GlobalPheromoneMatrix[][] = new double[SELF.VEXNUM][SELF.VEXNUM];

	public int bestOccur = 0;
	public long[] bestOccurTime = new long[200];
	private static int solutionCount = 0;
	public ArrayList<BNGraph> BestSolutions;
	public static int LogK2ScoreCount = 0;
	public int vexnum;
	public Score score = null;
	public BNGraph BestSolution;

	private BNGraph gk2sn;
	private int tStep;
	private int tMaxStep;
	private int antNum;
	private double alpha;
	private double beta;
	private double rou;
	private double q0;
	private double tao0; 
    private int currentI; 
    private double K; 
	private K2.TYPE type;
	private double[][] allPh;
	private java.util.ArrayList<Double> BestScore;
	private java.util.TreeSet<Integer> setOpted; 
	private java.util.Random AnnealRandom = new Random();



	public ACOE(BNGraph gk2sn, double alpha, double beta, double rou,
			double q0, int tStep, int tMaxStep, int antNum, K2.TYPE type1,
			Score score, Bayes p, double th, double k) {

		this.score = score;

		this.gk2sn = gk2sn;
		this.vexnum = gk2sn.getVexNum();
		this.tempH = new double[vexnum][vexnum];
		this.tao0 = 1 / (vexnum * Math.abs(this.score
				.calcGraphScore(this.gk2sn)));
		this.BestSolution = gk2sn;

		this.antNum = antNum;
		this.tMaxStep = tMaxStep;
		this.tStep = tStep;
		this.alpha = alpha;
		this.beta = beta;
		this.rou = rou;
		this.q0 = q0;
		this.BestSolutions = new ArrayList<BNGraph>();
		this.type = type1;
		
		this.K=k; 
		
		switch (type) {
		case ORI:
			System.err.println("Default model");
			this.BestScore = new ArrayList<Double>();
			this.setOpted = new TreeSet<Integer>();
			this.initheuristicInfo();
			this.initalGlobalPheromene(this.tao0);
			break;
		case SA:
			System.err.println("Simulate Anneal");
			this.BestScore = new ArrayList<Double>();
			this.setOpted = new TreeSet<Integer>();
			this.initheuristicInfo();
			this.initalGlobalPheromene(this.tao0);
			break;

		case CI:
			System.err.println("CI-test");
			this.BestScore = new ArrayList<Double>();
			this.setOpted = new TreeSet<Integer>();

			this.initheuristicInfo();
			this.initalGlobalPheromene(this.tao0);
			CIConstrain(((K2) score).getRecord());
			break;
			
		case PC:
			this.BestScore = new ArrayList<Double>();
			this.setOpted = new TreeSet<Integer>();
			this.initheuristicInfo();
			this.initalGlobalPheromene(this.tao0);
		    this.pConstrain(p);
			

			break;

		}
	
	}

	public void setScoreMetric(Score s) {
		this.score = s;
	}

	public void initalGlobalPheromene(double tao0) {
		for (int i = 0; i < this.GlobalPheromoneMatrix.length; i++) {
			for (int j = 0; j < this.GlobalPheromoneMatrix[i].length; j++) {
				if (i != j) {
					this.GlobalPheromoneMatrix[i][j] = tao0;
				} else {
					this.GlobalPheromoneMatrix[i][j] = Double.NEGATIVE_INFINITY;
				}

			}
		}
	}

	public void initheuristicInfo() {
		BNGraph temp = new BNGraph(this.vexnum);
		ArrayList<Integer> anodelist = new ArrayList<Integer>(); 
		System.out.println("Initialization heuristic information matrix.");
		long start = System.currentTimeMillis();
		for (int i = 0; i < this.vexnum; i++) {
			noParVexK2Value[i] = this.score.calcScore(i, anodelist);

			for (int j = 0; j < this.vexnum; j++) {
				if (i != j) {
					anodelist.add(j);
					this.tempH[i][j] = this.score.calcScore(temp.GetNode(i)
							.GetNodeId(), anodelist)
							- noParVexK2Value[i];
					anodelist.remove(0);
				} else
					this.tempH[i][j] = Double.NEGATIVE_INFINITY;
				
				
				System.out.println("Heuristic information: brain region"+(i+1)+"---brain region"+(j+1)+" :" +this.tempH[i][j]);
			}
		}

		long end = System.currentTimeMillis();
		System.out.format("End, total[%d]ms\n", end - start);
		System.out.format("Waiting for ACO algorithm......");
		
	}
	
	

	
	/**
	 * ¼ÓÈëCI-test
	 */
	public void CIConstrain(String[][] data) {
		K2.calcInf(data);
		K2.CITest(data);
		System.out.println("CI-test");
		for (int i = 0; i < K2.VEXNUM; i++) {
			for (int j = 0; j < K2.VEXNUM; j++) {
				if (K2.ChiSquare[i][j] == 0) {
					this.tempH[i][j] = Double.NEGATIVE_INFINITY;
				}
			}
		}
	}
	
	

	public void pConstrain(Bayes p) {
		for (int i = 0; i < SELF.VEXNUM; i++) {
			for (int j = 0; j < SELF.VEXNUM; j++) {	
				if(p.kappa[i][j]>=0.25)
				{
				System.out.print("1"+",");
				}
				else
					System.out.print("0"+",");
				if (p.kappa[i][j] <K) {
					this.tempH[i][j] = Double.NEGATIVE_INFINITY;  			
				}
				
			}System.out.println("");
		}
	}
	
	
	public BNGraph getBestSolution() {
		Collections.sort(this.BestSolutions);
		return this.BestSolutions.get(this.BestSolutions.size() - 1);
	}

	/**
	 * Update
	 * 
	 * @param g
	 *            BNGraph
	 */
	private void updateGlobalPheromone(final BNGraph g) {
		double bestSolutionCost = Math.abs(1 / g.calcTotalK2Score(this.score));
		ArrayList al = new ArrayList();
		for (int i = 0; i < K2.VEXNUM; i++) {
			al = g.GetNode(i).GetParentNodesIndex();
			for (int j = 0; j < al.size(); j++) {
				this.GlobalPheromoneMatrix[i][(Integer) al.get(j)] = (1 - this.rou)
						* this.GlobalPheromoneMatrix[i][(Integer) al.get(j)]
						+ this.rou * bestSolutionCost;
			}
		}
	}

	/**
	 * print
	 */
	private void printPheromone2(int m_i, int m_j) {
		for(int i=0;i<m_i;i++)
		{
		for(int j=0;j<m_j;j++)
		   {
			System.out.print(GlobalPheromoneMatrix[j][i]);
			System.out.print(" ");
			}
		System.out.print("\n");
		}
		
	}
	
	public void printPheromone(int m_i, int times, double sdMatrix[][]) {
		        double sum=0.0;
				double min=0.0;
                int count=0;
                double f=0.0;
                double deta=0.0;
                int m_j=m_i;
			 
			for(int i=0;i<m_i;i++)
			{
			for(int j=0;j<m_j;j++)
			   {
				if(GlobalPheromoneMatrix[j][i]>=0)
				{
			       sum+=GlobalPheromoneMatrix[j][i];
			       count++;
				}
				
			   }
			       
			}
			min=sum/count;
			
			for(int i=0;i<m_i;i++)
			{
			for(int j=0;j<m_j;j++)
			   {
				if(GlobalPheromoneMatrix[j][i]>=0)
				{
					f += Math.sqrt((GlobalPheromoneMatrix[j][i] -min) *(GlobalPheromoneMatrix[j][i] -min));
			     	
					}
				
			   }
			       
			}
			deta=f/ Math.sqrt(count);
			for(int i=0;i<m_i;i++)
			{
			for(int j=0;j<m_j;j++)
			   {
				GlobalPheromoneMatrix[j][i]=(GlobalPheromoneMatrix[j][i]-min)/(deta);
			   }
			}		
					
			for(int i=0;i<m_i;i++)
			{
			for(int j=0;j<m_j;j++)
			   {
				sdMatrix[j][i]+=GlobalPheromoneMatrix[j][i];

				}
			}

			for(int i=0;i<m_i;i++)
			{
			for(int j=0;j<m_j;j++)
			   {
				System.out.print(sdMatrix[j][i]/times);  
				System.out.print(" ");
				}
			System.out.print("\n");
			}
			
		}
	
	/**
	 * BayesianNet
	 * 
	 * @return BNGraph
	 */
	public BNGraph findBestBayesianNetwithlocaloptimization(Bayes p) {
		long start = System.currentTimeMillis();
		BNGraph G_b;
		this.BestScore.clear();
		this.setOpted.clear();
		currentI=0;
		
		for (int t = 0; t < tMaxStep; t++) {
			ACOE.solutionCount = 0;
			for (int k = 0; k < antNum; k++) {				
				
				AntE temp = new AntE(this.alpha, this.beta,
						this.rou, this.q0,
						this.tao0, SELF.VEXNUM, k,
						this.type, this.tempH,
						this.score, this.GlobalPheromoneMatrix,
						this.noParVexK2Value, this.BestSolutions,p);
				temp.run();
		
				this.allPh=this.GlobalPheromoneMatrix;
				
				ACOE.solutionCount++;
			}
           
			
			if (this.type == K2.TYPE.OP || this.type == K2.TYPE.SA) {
				if ((t % tStep) == 0 && t != 0) {
					System.out.println("unfinished");
				}
			} else {
				if ((t % tStep) == 0 && t != 0) {
					for (int i = 0; i < this.BestSolutions.size(); i++) {
						this.BestSolutions.set(i, this
								.HillClimbing(this.BestSolutions.get(i)));						
					}

				}
			}
			
			
			G_b = this.getBestSolution();
			long end = System.currentTimeMillis();
			long interTime = end - start;
			bestOccurTime[t] = interTime;
			if (G_b.K2Score > this.BestSolution.K2Score) {
				this.BestSolution = G_b;
				bestOccur = t;
			}

			this.updateGlobalPheromone(this.BestSolution);
			currentI=t;
		
			if (this.canStop()) {
				
				break;
			}

		}
		
		if (this.type == K2.TYPE.OP || this.type == K2.TYPE.SA) {
		} else {
			this.localOptimizate();
		}

		return this.BestSolution;
	}

	

	public BNGraph findBestBayesianNet(Bayes p) {
		long start = System.currentTimeMillis();
		BNGraph G_b;
		this.BestScore.clear();
		this.setOpted.clear();
		currentI=0;
		
		for (int t = 0; t < tMaxStep; t++) {
			ACOE.solutionCount = 0;
			for (int k = 0; k < antNum; k++) {
				AntE temp = new AntE(this.alpha, this.beta,
						this.rou, this.q0,
						this.tao0, SELF.VEXNUM, k,
						this.type, this.tempH,
						this.score, this.GlobalPheromoneMatrix,
						this.noParVexK2Value, this.BestSolutions, p);
				
				//temp.run1(p);
				temp.run();
				
				this.allPh=this.GlobalPheromoneMatrix;
				
				ACOE.solutionCount++;
			}

			if ((t % tStep) == 0 && t != 0) {
				for (int i = 0; i < this.BestSolutions.size(); i++) {
					this.BestSolutions.set(i, this
							.HillClimbing(this.BestSolutions.get(i)));						
				}

			}
			
			G_b = this.getBestSolution();

			if (G_b.K2Score > this.BestSolution.K2Score) {
				this.BestSolution = G_b;
				bestOccur = t;
			}

			this.updateGlobalPheromone(this.BestSolution);

			if (this.canStop()) {
				
				break;
			}

			currentI++;
		}
		return this.BestSolution;
		
		
	}

	static int MaxEqualStep = 5;

	private boolean canStop() {
		boolean stop = false;

		int current = currentI;
		if (current - this.bestOccur +1 > ACOE.MaxEqualStep) {
			System.err.println("Searching is end:" + current);
			stop = true;
		}

		return stop;
	}
	
	/**
	 * 
	 * 
	 * @param G_k
	 *            BNGraph
	 * @return BNGraph
	 */

	public BNGraph HillClimbing(BNGraph G_k) {
		HillClimbing hill = new HillClimbing(G_k, this.score);
		if (this.type == K2.TYPE.OP) {
			G_k = hill.OptimizeBN_CI();
		} else {
			G_k = hill.OptimizeBN();
		}
		return G_k;
	}


	public void localOptimizate() {
		BNGraph temp = new BNGraph(this.vexnum);
		for (int i = 0; i < this.BestSolutions.size(); i++) {
			this.BestSolutions.set(i, this.HillClimbing(this.BestSolutions
					.get(i)));						
		}
		temp = this.getBestSolution();
		if (temp.getScore() > this.BestSolution.getScore()) {
			this.BestSolution = temp;
		}

	}
	
	

}
