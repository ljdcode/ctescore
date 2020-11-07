package bjut.ai.bn;

import bjut.ai.bn.score.Scores;
import bjut.ai.bn.score.Score;
import java.util.Random;
/**
 * HillClimbing
 * Company: BJUT - AI
 * @author Jinduo
 * @version 1.0
 */
public class Neighborhood {
	
	private final Random RANDOM;

	private Score ScoreMetric = null;
	public BNGraph gold = null;
	public BNGraph gopt = null;
	int VexNum = 0;
	private double dAddScore = Double.NEGATIVE_INFINITY;
	private int nodeIdAddFrom = -1;
	private int nodeIdAddTo = -1;
	private double dDelScore = Double.NEGATIVE_INFINITY;
	private int nodeIdDelFrom = -1;
	private int nodeIdDelTo = -1;
	private double dReverseScore = Double.NEGATIVE_INFINITY;
	private int nodeIdRevFrom = -1;
	private int nodeIdRevTo = -1;
    
	private double dMoveScore=Double.NEGATIVE_INFINITY;
	private int nodeIdMoveFrom1=-1;
	private int nodeIdMoveTo1=-1;
	private int nodeIdMoveFrom2=-1;
	private int nodeIdMoveTo2=-1;
	private double dAddKGScore = Double.NEGATIVE_INFINITY;
	private int nodeIdKGAddFrom = -1;
	private int nodeIdKGAddTo = -1;
	
	private double dDelKGScore = Double.NEGATIVE_INFINITY;
	private int nodeIdKGDelFrom = -1;
	private int nodeIdKGDelTo = -1;
	
	private int[][] CI; 
	private double[][] heuristic; 
	private double[][] Inf;
	private double MaxInf = Double.NEGATIVE_INFINITY; 

	private int FirstK = 150;

	private array3D[] SortedHeuritic;

	private array3D[] SortedInf; 
	private int[][] arrFirstK;
	private int[][] arrLastK;
	private double Qd = 0.5;

	public Neighborhood(BNGraph g, Score s) {
		 RANDOM = new Random();
		this.ScoreMetric = s;
		this.gold = g;
		this.gold.calcTotalK2Score(s);
		this.gopt = (BNGraph) g.clone();
		VexNum = gopt.getVexNum();
		this.CI = Scores.ChiSquare;
		this.Inf = Scores.Inf;

		this.heuristic = new double[VexNum][VexNum];
		this.arrFirstK = new int[VexNum][VexNum];
		this.arrLastK = new int[VexNum][VexNum];

	}

	private void updateHeuristic(int row) {
		for (int j = 0; j < VexNum; j++) {
			java.util.ArrayList<Integer> p = gopt.GetNode(row).GetParentNodesIndex();
			if (p.contains(new Integer(j))) {
				this.heuristic[row][j] = Double.NEGATIVE_INFINITY;
			} else {
				double dold = Scores.INSTANCE.calcScore(row, p);
				p.add(j);
				double dnew = Scores.INSTANCE.calcScore(row, p);
				this.heuristic[row][j] = (dnew - dold)* (1 + this.Inf[row][j] / MaxInf);
			}
		}
	}

	private void calcHeuristic() {
		int i, j;
		this.MaxInf = Scores.maxMI;
		for (i = 0; i < VexNum; i++) {
			for (j = 0; j < VexNum; j++) {
				if (i == j) {
					this.heuristic[i][j] = Double.NEGATIVE_INFINITY;
				} else {
					if (this.CI[i][j] == 1) {
						java.util.ArrayList<Integer> p = gopt.GetNode(i).GetParentNodesIndex();
						if (p.contains(new Integer(j))) {
							this.heuristic[i][j] = 0;
						} else {
							double dold = Scores.INSTANCE.calcScore(i, p);
							p.add(j);
							double dnew = Scores.INSTANCE.calcScore(i, p);
							this.heuristic[i][j] = (dnew - dold)* (1 + this.Inf[i][j] / MaxInf);
						}
					} else {
						this.heuristic[i][j] = Double.NEGATIVE_INFINITY;
					}
				}
			}
		}
	}
	
	public BNGraph NeighborEmployedBN() {

		
		this.dAddScore = Double.NEGATIVE_INFINITY;
		this.dDelScore = Double.NEGATIVE_INFINITY;
		calcAddition();
		calcDeletion();
        
		double d = 0;
		String sMaxType = "";
		if(this.dAddScore>this.dDelScore){
			d=this.dAddScore;
			sMaxType = "a";
		} 
		else{
			d=this.dDelScore;
			sMaxType = "d";
		}
	    if (d <= 0) {
		} else {
			if (sMaxType == "a") {
				this.gopt.AddArc(this.nodeIdAddFrom, this.nodeIdAddTo);
			} else {
				this.gopt.DelArc(this.nodeIdDelFrom, this.nodeIdDelTo);				
			} 
		}
		
	gopt.calcTotalK2Score(this.ScoreMetric);
	
	if (gopt.K2Score > gold.K2Score) {
		return gopt;
	} else {
		return gold;
	}
	

	}
	
	public BNGraph NeighborEmployedBN1() {
		
			this.dAddScore = Double.NEGATIVE_INFINITY;
			this.dDelScore = Double.NEGATIVE_INFINITY;
			this.dReverseScore = Double.NEGATIVE_INFINITY;
            this.dMoveScore = Double.NEGATIVE_INFINITY;
			calcAddition();
			calcDeletion();
			calcReverse();
            calcMove();
            
			double d = 0;
			String sMaxType = "";
		    double[] scoreArr = {this.dAddScore,this.dDelScore,this.dReverseScore,this.dMoveScore}; 
			for(int i=0;i<scoreArr.length;i++){
				if(scoreArr[i]>d)
					d=scoreArr[i];
			}
            if(d == scoreArr[0])
            	sMaxType = "a";
            else if(d == scoreArr[1])
            	sMaxType = "d";
            else if(d == scoreArr[2])
            	sMaxType = "r";
            else
            	sMaxType = "m";
		    if (d <= 0) {
			} else {
				if (sMaxType == "a") {
					this.gopt.AddArc(this.nodeIdAddFrom, this.nodeIdAddTo);
				} else if (sMaxType == "d") {
					this.gopt.DelArc(this.nodeIdDelFrom, this.nodeIdDelTo);
				} else if (sMaxType == "r") {
					this.gopt.DelArc(this.nodeIdRevFrom, this.nodeIdRevTo);
					this.gopt.AddArc(this.nodeIdRevTo, this.nodeIdRevFrom);
				} else {
					this.gopt.DelArc(this.nodeIdMoveFrom1,this.nodeIdMoveTo1);
					this.gopt.DelArc(this.nodeIdMoveFrom2,this.nodeIdMoveTo2);
					this.gopt.AddArc(this.nodeIdMoveFrom2,this.nodeIdMoveTo1);
					this.gopt.AddArc(this.nodeIdMoveFrom1,this.nodeIdMoveTo2);
				}
			}
			
		//}
		gopt.calcTotalK2Score(this.ScoreMetric);
		
		if (gopt.K2Score > gold.K2Score) {

			return gopt;
		} else {
			return gold;
		}
		
	}

	public BNGraph NeighborOnlookerBN() {
			this.dAddScore = Double.NEGATIVE_INFINITY;
			this.dDelScore = Double.NEGATIVE_INFINITY;
			this.dAddKGScore = Double.NEGATIVE_INFINITY;
			this.dDelKGScore = Double.NEGATIVE_INFINITY;
			double q = RANDOM.nextDouble();
			if(q <= this.Qd){
				calcAddition_KG();
				calcDeletion_KG();
			}else{
			    calcAddition();
			    calcDeletion();
			}
			double d = 0;
			String sMaxType = "";
		    double[] scoreArr = {this.dAddScore,this.dDelScore,this.dAddKGScore,this.dDelKGScore}; 
			for(int i=0;i<scoreArr.length;i++){
				if(scoreArr[i]>d)
					d=scoreArr[i];
			}
            if(d == scoreArr[0])
            	sMaxType = "a";
            else if(d == scoreArr[1])
            	sMaxType = "d";
            else if(d == scoreArr[2])
            	sMaxType = "aKG";
            else
            	sMaxType = "dKG";
		    if (d <= 0) {
			} else {
				if (sMaxType == "a") {
					this.gopt.AddArc(this.nodeIdAddFrom, this.nodeIdAddTo);

				} else if (sMaxType == "d") {
					this.gopt.DelArc(this.nodeIdDelFrom, this.nodeIdDelTo);

				} else if (sMaxType == "aKG") {
					this.gopt.AddArc(this.nodeIdKGAddFrom, this.nodeIdKGAddTo);
				} else {
                    this.gopt.DelArc(this.nodeIdKGDelFrom, this.nodeIdKGDelTo);
				}
			}
		gopt.calcTotalK2Score(this.ScoreMetric);
		if (gopt.K2Score > gold.K2Score) {
			return gopt;
		} else {
			return gold;
		}
	}
	

	public BNGraph OptimizeBN_CI() { 
		int MAXLOOP = 1000; 
		int i = 0;
		int iBeginAddArc = 5;

		this.sortInf();

		while (i < MAXLOOP) {
			i++;

			this.dAddScore = Double.NEGATIVE_INFINITY;
			this.dDelScore = Double.NEGATIVE_INFINITY;
			this.dReverseScore = Double.NEGATIVE_INFINITY;

			if (iBeginAddArc >= 5) {
				calcAddition_CI();
			}
			calcDeletion_CI();
			calcReverse();

			double d = 0;
			String sMaxType = "";
			if (this.dAddScore > this.dDelScore) {
				if (this.dReverseScore > this.dAddScore) {
					d = this.dReverseScore;
					sMaxType = "r";
				} else {
					d = this.dAddScore;
					sMaxType = "a";
				}
			} else {
				if (this.dReverseScore > this.dDelScore) {
					d = this.dReverseScore;
					sMaxType = "r";
				} else {
					d = this.dDelScore;
					sMaxType = "d";
				}
			}

			if (d <= 0) {
				break;
			} else {
				if (sMaxType == "a") {
					this.gopt.AddArc(this.nodeIdAddFrom, this.nodeIdAddTo);

				} else if (sMaxType == "d") {
					this.gopt.DelArc(this.nodeIdDelFrom, this.nodeIdDelTo);

				} else if (sMaxType == "r") {
					this.gopt.DelArc(this.nodeIdRevFrom, this.nodeIdRevTo);
					this.gopt.AddArc(this.nodeIdRevTo, this.nodeIdRevFrom);


				} else {
				}

			}
		}

		gopt.calcTotalK2Score(this.ScoreMetric);
		if (gopt.K2Score > gold.K2Score) {
			return gopt;
		} else {

			return gold;
		}
	}

	private void InifFB() {
		int i, j;
		for (i = 0; i < VexNum; i++) {
			for (j = 0; i < VexNum; j++) {
				if (i != j) {
					if (this.CI[i][j] == 0) {
						gopt.GetNode(i).AddForbiddenParent(gopt.GetNode(j));
						;
					}
				}
			}
		}
	}

	private void calcAddition() {

		int i = 0;
		int j = 0;
		int k = 0;
		BNNode node;
		java.util.TreeSet<Integer> tsFB = new java.util.TreeSet<Integer>();
		do{
			tsFB.clear();
		    i=RANDOM.nextInt(VexNum);
			node = gopt.GetNode(i);
			java.util.ArrayList<Integer> parent = node.GetParentNodesIndex();
			java.util.ArrayList<Integer> descendant = node.GetDescendantNodesIndex();
			java.util.ArrayList<Integer> forbidden = node.getForbiddenParent();
			for (k = 0; k < parent.size(); k++) {
				tsFB.add(parent.get(k));
			}
			for (k = 0; k < descendant.size(); k++) {
				tsFB.add(descendant.get(k));
			}
			for (k = 0; k < forbidden.size(); k++) {
				tsFB.add(forbidden.get(k));
			}

			j=RANDOM.nextInt(VexNum);

		}while(j == i || tsFB.contains(j)); 
			
			double scoreold = gopt.calcTotalK2Score(this.ScoreMetric);

			gopt.AddArc(j, i);
			double scorenew = gopt.calcTotalK2Score(this.ScoreMetric);
			if (scorenew - scoreold > 0) {

							this.dAddScore = scorenew - scoreold;
							this.nodeIdAddFrom = j;
							this.nodeIdAddTo = i;	
						
							

			} else {

						node.AddForbiddenParent(gopt.GetNode(j));
						
					}
	
			gopt.DelArc(j, i);		

	}
	private void calcAddition_KG(){
		int i1 = 0;
		int j1 = 0;
		int k = 0;
		BNNode node;
        double q = RANDOM.nextDouble();
        double sumvalue = 0.0;
        double value = 0.0;
        java.util.TreeSet<Integer> tsFB = new java.util.TreeSet<Integer>();
        double[][] probability = new double[VexNum][VexNum];
            for (int i = 0; i < VexNum; i++)
            {
                for (int j = 0; j < VexNum; j++)
                {
                    if (i != j && heuristic[i][j] > 0)
                    {
                        probability[i][j] = heuristic[i][j];
                        sumvalue += probability[i][j]; 
                    }
                    else
                    {
                        probability[i][j] = 0.0;
                    }
                }
            }
            for (int i = 0; i < VexNum; i++)
                for (int j = 0; j < VexNum; j++)
                {
                    probability[i][j] /= sumvalue; 
                }
         do{
        	value = 0.0;
            while (q <= 0.0)
                q = RANDOM.nextDouble();
            loop1:
            for (int i = 0; i < VexNum; i++)
                for (int j = 0; j < VexNum; j++)
                {
                    value += probability[i][j];
                    if (value >= q)
                    {
                    	i1 = i;
                    	j1 = j;	
                        break loop1;
                    }
                }
			node = gopt.GetNode(i1);
			java.util.ArrayList<Integer> parent = node.GetParentNodesIndex();
			java.util.ArrayList<Integer> descendant = node.GetDescendantNodesIndex();
			java.util.ArrayList<Integer> forbidden = node.getForbiddenParent();
			for (k = 0; k < parent.size(); k++) {
				tsFB.add(parent.get(k));
			}
			for (k = 0; k < descendant.size(); k++) {
				tsFB.add(descendant.get(k));
			}
			for (k = 0; k < forbidden.size(); k++) {
				tsFB.add(forbidden.get(k));
			}
         }while(j1 == i1 || tsFB.contains(j1));

			double scoreold = gopt.calcTotalK2Score(this.ScoreMetric);

			gopt.AddArc(j1, i1);
			double scorenew = gopt.calcTotalK2Score(this.ScoreMetric);
			if (scorenew - scoreold > 0) {

							this.dAddKGScore = scorenew - scoreold;
							this.nodeIdKGAddFrom = j1;
							this.nodeIdKGAddTo= i1;
					
			} else {

						node.AddForbiddenParent(gopt.GetNode(j1));
					}

			 gopt.DelArc(j1, i1);		
			 
	}
	private class array3D implements Comparable {
		int i;
		int j;
		double v;

		public array3D(int row, int col, double value) {
			this.i = row;
			this.j = col;
			this.v = value;
		}

		@Override
		public int compareTo(Object o) {
			return this.v > ((array3D) o).v ? 1
					: (this.v == ((array3D) o).v ? 0 : -1);
		}
	}

	private void sortInf() {
	
		java.util.ArrayList<array3D> arr = new java.util.ArrayList<array3D>();
		int i, j;
		array3D temp;
		for (i = 0; i < VexNum; i++) {
			for (j = 0; j < VexNum; j++) {
				if (i == j) {
					continue;
				}

				temp = new array3D(i, j, this.Inf[i][j]);
				arr.add(temp);
		
			}
		}
		java.util.Collections.sort(arr);
	
		int arrsize = arr.size();
		if (this.FirstK < arrsize) {
			for (i = 0; i < this.FirstK; i++) {
				temp = arr.get(i);
				this.arrFirstK[temp.i][temp.j] = 1;
			}
			for (i = 0; i < this.FirstK; i++) {
				temp = arr.get(arrsize - 1 - i);
				this.arrLastK[temp.i][temp.j] = 1;
			}
		}
	}

	private void sortHeuritic() {
	
		java.util.PriorityQueue<array3D> q = new java.util.PriorityQueue<array3D>();
		int i, j, k = 0;
		array3D temp;
		for (i = 0; i < VexNum; i++) {
			for (j = 0; j < VexNum; j++) {
				if (this.heuristic[i][j] != Double.NEGATIVE_INFINITY) {
					temp = new array3D(i, j, this.heuristic[i][j]);
					q.add(temp);
				}
			}
		}
		SortedHeuritic = new array3D[q.size()];
		while (!q.isEmpty()) {
			temp = q.poll();
			SortedHeuritic[k] = temp;
			k++;
		}
		for (i = 0; i < VexNum; i++) {
			for (j = 0; j < VexNum; j++) {
				this.arrFirstK[i][j] = 0;
				this.arrLastK[i][j] = 0;
			}
		}
		for (i = 0; i < this.FirstK && i < this.SortedHeuritic.length; i++) {
			temp = this.SortedHeuritic[i];
			this.arrFirstK[temp.i][temp.j] = 1;
		}
		for (i = 0; i < this.FirstK && i < this.SortedHeuritic.length; i++) {
			temp = this.SortedHeuritic[this.SortedHeuritic.length - 1 - i];
			this.arrLastK[temp.i][temp.j] = 1;
		}
	}

	private boolean IsBelongFirstK(int xi, int xj) {
		boolean bResult = false;
		if (this.arrFirstK[xi][xj] == 1) {
			bResult = true;
		}

		return bResult;
	}

	private boolean IsBelongLastK(int xi, int xj) {
		boolean bResult = false;
		if (this.arrLastK[xi][xj] == 1) {
			bResult = true;
		}
		
		return bResult;


	}

	private void calcAddition_CI() {
		
		int i = 0;
		int j = 0;
		int k = 0;
		int m = 0;
		for (i = 0; i < VexNum; i++) {
			BNNode node = gopt.GetNode(i);
			java.util.ArrayList<Integer> parent = node.GetParentNodesIndex();
			java.util.ArrayList<Integer> descendant = node.GetDescendantNodesIndex();
			java.util.ArrayList<Integer> forbidden = node.getForbiddenParent();

			java.util.TreeSet<Integer> tsFB = new java.util.TreeSet<Integer>();
			for (k = 0; k < parent.size(); k++) {
				tsFB.add(parent.get(k));
			}
			for (k = 0; k < descendant.size(); k++) {
				tsFB.add(descendant.get(k));
			}
			for (k = 0; k < forbidden.size(); k++) {
				tsFB.add(forbidden.get(k));
			}


			for (j = 0; j < VexNum; j++) {
				if (j == i || tsFB.contains(j)
						|| (!this.IsBelongFirstK(i, j))) {
				
					m++;
				} else {
	
					double scoreold = gopt.calcTotalK2Score(this.ScoreMetric);
			

					gopt.AddArc(j, i);

					double scorenew = gopt.calcTotalK2Score(this.ScoreMetric);

					if (scorenew - scoreold > 0) {
						if (scorenew - scoreold > this.dAddScore) {
							this.dAddScore = scorenew - scoreold;
							this.nodeIdAddFrom = j;
							this.nodeIdAddTo = i;
						}
					} else {
						node.AddForbiddenParent(gopt.GetNode(j));
					}
					gopt.DelArc(j, i);
				}
			}
		}

	}

	private void calcDeletion() {

		int i = 0;
		int j = 0;
		BNNode node;
		java.util.ArrayList<Integer> parent;
		do{
        i=RANDOM.nextInt(VexNum);

			node = gopt.GetNode(i);
			parent = node.GetParentNodesIndex();
		}while(parent.size()<1);

			j=RANDOM.nextInt(parent.size());

		    double scoreold = gopt.calcTotalK2Score(this.ScoreMetric);

			int parentNodeId = parent.get(j);

				gopt.DelArc(parentNodeId, i);

				double scorenew = gopt.calcTotalK2Score(this.ScoreMetric);
				if (scorenew - scoreold > 0) {
	
					node.AddForbiddenParent(gopt.GetNode(parentNodeId));
				
				}
				this.dDelScore = scorenew - scoreold;
		
				this.nodeIdDelFrom = parentNodeId;
				this.nodeIdDelTo = i;

			gopt.AddArc(parentNodeId, i);

	}
	//Knowledge-Guided addition operator
	private void calcDeletion_KG(){
		int i1 = 0;
		int j1 = 0;
		BNNode node;
        double q = RANDOM.nextDouble();
        double sumvalue = 0.0;
        double value = 0.0;
        java.util.ArrayList<Integer> parent;
        double[][] probability = new double[VexNum][VexNum];
            for (int i = 0; i < VexNum; i++)
            {
                for (int j = 0; j < VexNum; j++)
                {
                    if (i != j && heuristic[i][j] > 0)
                    {
                        probability[i][j] = 1/heuristic[i][j];
                        sumvalue += probability[i][j];
                    }
                    else
                    {
                        probability[i][j] = 0.0;
                    }
                }
            }
            for (int i = 0; i < VexNum; i++)
                for (int j = 0; j < VexNum; j++)
                {
                    probability[i][j] /= sumvalue; 
                }
         do{
        	 value = 0.0;
            while (q <= 0.0)
                q = RANDOM.nextDouble();
            loop1:
            for (int i = 0; i < VexNum; i++)
                for (int j = 0; j < VexNum; j++)
                {
                    value += probability[i][j]; //累加直到到达q
                    if (value >= q)
                    {
                    	i1 = i;
                    	j1 = j;	
                        break loop1;
                    }
                }

			node = gopt.GetNode(i1);
			parent = node.GetParentNodesIndex();
         }while(parent.size()<1);

			double scoreold = gopt.calcTotalK2Score(this.ScoreMetric);
	
			int parentNodeId = parent.get(j1);
			gopt.DelArc(parentNodeId, i1);
			double scorenew = gopt.calcTotalK2Score(this.ScoreMetric);
			if (scorenew - scoreold > 0) {
				node.AddForbiddenParent(gopt.GetNode(parentNodeId));
			}
	
				this.dDelKGScore = scorenew - scoreold;
		
				this.nodeIdDelFrom = parentNodeId;
		
				this.nodeIdDelTo = i1;
	
			gopt.AddArc(parentNodeId, i1);
	}
	
	private void calcDeletion_CI() {

		
		int i = 0;
		int j = 0;

		for (i = 0; i < VexNum; i++) {
			BNNode node = gopt.GetNode(i);
			java.util.ArrayList<Integer> parent = node.GetParentNodesIndex();

			for (j = 0; j < parent.size(); j++) {
				if (!this.IsBelongLastK(i, parent.get(j))) {
					continue;
				}
		

				double scoreold = gopt.calcTotalK2Score(this.ScoreMetric);

				int parentNodeId = parent.get(j);

				gopt.DelArc(parentNodeId, i);

				double scorenew = gopt.calcTotalK2Score(this.ScoreMetric);
				if (scorenew - scoreold > 0) {
				
					node.AddForbiddenParent(gopt.GetNode(parentNodeId));
				}

			
				if (scorenew - scoreold > this.dDelScore) {
					this.dDelScore = scorenew - scoreold;
					this.nodeIdDelFrom = parentNodeId;
					this.nodeIdDelTo = i;
				}


				gopt.AddArc(parentNodeId, i);
			}
		}
	}

	private void calcReverse() {

		int fig=0;
		int i = 0;
		int j = 0;
		while(fig==0){
		
		i=RANDOM.nextInt(VexNum);

			BNNode node = gopt.GetNode(i);
			java.util.ArrayList<Integer> parent = node.GetParentNodesIndex();
			int parent_size = parent.size();
			if(parent_size > 0){
		    j=RANDOM.nextInt(parent.size());	
			BNNode nodeParent = gopt.GetNode(parent.get(j));
			int nodeParentId = nodeParent.GetNodeId();
			java.util.ArrayList<Integer> parentForbidden = nodeParent.getForbiddenParent();
			if(!parentForbidden.contains(new Integer(i))){
					double dDelOld = gopt.calcTotalK2Score(this.ScoreMetric);
					gopt.DelArc(nodeParent, node);
					java.util.ArrayList<Integer> xjDescendant = nodeParent.GetDescendantNodesIndex();
					if (!xjDescendant.contains(new Integer(i))) {
						double dDelNew = gopt.calcTotalK2Score(this.ScoreMetric);
						double d1 = dDelNew - dDelOld;
						if (d1 > 0) {
							node.AddForbiddenParent(nodeParent);
						}
						double dAddOld = gopt.calcTotalK2Score(this.ScoreMetric);
						gopt.AddArc(node, nodeParent);
						java.util.ArrayList<Integer> xjParent = nodeParent.GetParentNodesIndex();
						double dAddNew = gopt.calcTotalK2Score(this.ScoreMetric);
						double d2 = dAddNew - dAddOld;
						if (d2 < 0) {
							nodeParent.AddForbiddenParent(node);
						}
						double d = d1 + d2;
						if(d > 0){
							this.dReverseScore = d;
							this.nodeIdRevFrom = nodeParentId;
							this.nodeIdRevTo = i;
						}
						gopt.DelArc(node, nodeParent);
						fig=1;
					}
					gopt.AddArc(nodeParent, node);
				}
			//} // for j
		} // for parent_size
		}
//		}
	}
	private void calcMove(){
		//int MaxIter=1;
		//for(int iter=0;iter<MaxIter;iter++){
		int i = 0;
		int k = 0;
		int j = 0;
		int l = 0;
		int fig=0;
		int p=0;
		BNNode node_i,node_j;
		java.util.ArrayList<Integer> parent_i = new java.util.ArrayList<Integer>();
		java.util.ArrayList<Integer> parent_j = new java.util.ArrayList<Integer>();
		//java.util.ArrayList<Integer> descendant_i = new java.util.ArrayList<Integer>();
		//java.util.ArrayList<Integer> descendant_j = new java.util.ArrayList<Integer>();
		//java.util.ArrayList<Integer> parent_i,parent_j;
		while(fig==0){
		 //parent_i.clear();
		 //parent_j.clear();
		 do{
            i=RANDOM.nextInt(VexNum);  
            node_i = gopt.GetNode(i);
            parent_i = node_i.GetParentNodesIndex();
		  }while(parent_i.size()<1);
		 do{
	         j=RANDOM.nextInt(VexNum);  
	         node_j = gopt.GetNode(j);
	         parent_j = node_j.GetParentNodesIndex();
		  }while(parent_j.size()<1);
		 
		 ////////////////////////
			java.util.ArrayList<Integer> descendant_i = node_i.GetDescendantNodesIndex();
			java.util.ArrayList<Integer> descendant_j = node_j.GetDescendantNodesIndex();
			java.util.TreeSet<Integer> tsFB_i = new java.util.TreeSet<Integer>();
			java.util.TreeSet<Integer> tsFB_j = new java.util.TreeSet<Integer>();
			for (p = 0; p < parent_i.size(); p++) {
				tsFB_i.add(parent_i.get(p));
			}
			for (p = 0; p < descendant_i.size(); p++) {
				tsFB_i.add(descendant_i.get(p));
			}
			for (p = 0; p < parent_j.size(); p++) {
				tsFB_j.add(parent_j.get(p));
			}
			for (p = 0; p < descendant_j.size(); p++) {
				tsFB_j.add(descendant_j.get(p));
			}
		 ///////////////////////
		 k=RANDOM.nextInt(parent_i.size());
		 l=RANDOM.nextInt(parent_j.size()); 
		 BNNode nodeParent_k = gopt.GetNode(parent_i.get(k));
		 BNNode nodeParent_l = gopt.GetNode(parent_j.get(l));
		 if((!tsFB_j.contains(parent_i.get(k)))&&(!(tsFB_i.contains(parent_j.get(l))))&&(!(node_i.equals(nodeParent_l)))&&(!(node_j.equals(nodeParent_k)))){
			// if(tsFB_j.contains(k)||(tsFB_i.contains(l))||(i==l))
		// if((!parent_i.contains(nodeParent_l.GetNodeId()))&&(!parent_j.contains(nodeParent_k.GetNodeId()))
	    //			&&(!node_i.equals(nodeParent_l))&&(!node_j.equals(nodeParent_k))&&(!(nodeParent_k.equals(nodeParent_l)))&&(!(node_i.equals(node_j)))){
			 double dDelOld = gopt.calcTotalK2Score(this.ScoreMetric);
			 gopt.DelArc(nodeParent_k, node_i);
			 gopt.DelArc(nodeParent_l, node_j);
			 double dDelNew = gopt.calcTotalK2Score(this.ScoreMetric);
			 double d1 = dDelNew - dDelOld;
			 double dAddOld = gopt.calcTotalK2Score(this.ScoreMetric);
			 gopt.AddArc(nodeParent_l,node_i);
			 gopt.AddArc(nodeParent_k,node_j);
			 double dAddNew = gopt.calcTotalK2Score(this.ScoreMetric);
			 double d2 = dAddNew - dAddOld;
			 double d = d1 + d2;
			 if (d > 0){
			 //if (d > this.dMoveScore) {
					this.dMoveScore = d;
					this.nodeIdMoveFrom1=nodeParent_k.GetNodeId();
					this.nodeIdMoveTo1=i;
					this.nodeIdMoveFrom2=nodeParent_l.GetNodeId();
					this.nodeIdMoveTo2=j;
			 }
			 gopt.DelArc(nodeParent_l,node_i);
			 gopt.DelArc(nodeParent_k,node_j);
			 gopt.AddArc(nodeParent_k, node_i);
			 gopt.AddArc(nodeParent_l, node_j);
			 fig=1;
		 }
		}
//	}
	}
	
	public void Move(int i,int j){


		int k = 0;
		int l = 0;
		int fig=0;
		int p=0;
		BNNode node_i,node_j;
		java.util.ArrayList<Integer> parent_i = new java.util.ArrayList<Integer>();
		java.util.ArrayList<Integer> parent_j = new java.util.ArrayList<Integer>();
		//java.util.ArrayList<Integer> descendant_i = new java.util.ArrayList<Integer>();
		//java.util.ArrayList<Integer> descendant_j = new java.util.ArrayList<Integer>();
		//java.util.ArrayList<Integer> parent_i,parent_j;
		while(fig==0){
		 //parent_i.clear();
		 //parent_j.clear();
		 do{
  
            node_i = gopt.GetNode(i);
            parent_i = node_i.GetParentNodesIndex();
		  }while(parent_i.size()<1);
		 do{
 
	         node_j = gopt.GetNode(j);
	         parent_j = node_j.GetParentNodesIndex();
		  }while(parent_j.size()<1);
		 ////////////////////////
			java.util.ArrayList<Integer> descendant_i = node_i.GetDescendantNodesIndex();
			java.util.ArrayList<Integer> descendant_j = node_j.GetDescendantNodesIndex();
			java.util.TreeSet<Integer> tsFB_i = new java.util.TreeSet<Integer>();
			java.util.TreeSet<Integer> tsFB_j = new java.util.TreeSet<Integer>();
			for (p = 0; p < parent_i.size(); p++) {
				tsFB_i.add(parent_i.get(p));
			}
			for (p = 0; p < descendant_i.size(); p++) {
				tsFB_i.add(descendant_i.get(p));
			}
			for (p = 0; p < parent_j.size(); p++) {
				tsFB_j.add(parent_j.get(p));
			}
			for (p = 0; p < descendant_j.size(); p++) {
				tsFB_j.add(descendant_j.get(p));
			}
		 ///////////////////////
		 k=RANDOM.nextInt(parent_i.size());
		 l=RANDOM.nextInt(parent_j.size()); 
		 BNNode nodeParent_k = gopt.GetNode(parent_i.get(k));
		 BNNode nodeParent_l = gopt.GetNode(parent_j.get(l));
		 if((!tsFB_j.contains(parent_i.get(k)))&&(!(tsFB_i.contains(parent_j.get(l))))&&(!(node_i.equals(nodeParent_l)))&&(!(node_j.equals(nodeParent_k)))){
			// if(tsFB_j.contains(k)||(tsFB_i.contains(l))||(i==l))
		// if((!parent_i.contains(nodeParent_l.GetNodeId()))&&(!parent_j.contains(nodeParent_k.GetNodeId()))
	    //			&&(!node_i.equals(nodeParent_l))&&(!node_j.equals(nodeParent_k))&&(!(nodeParent_k.equals(nodeParent_l)))&&(!(node_i.equals(node_j)))){
			 double dDelOld = gopt.calcTotalK2Score(this.ScoreMetric);
			 gopt.DelArc(nodeParent_k, node_i);
			 gopt.DelArc(nodeParent_l, node_j);
			 double dDelNew = gopt.calcTotalK2Score(this.ScoreMetric);
			 double d1 = dDelNew - dDelOld;
			 double dAddOld = gopt.calcTotalK2Score(this.ScoreMetric);
			 gopt.AddArc(nodeParent_l,node_i);
			 gopt.AddArc(nodeParent_k,node_j);
			 double dAddNew = gopt.calcTotalK2Score(this.ScoreMetric);
			 double d2 = dAddNew - dAddOld;
			 double d = d1 + d2;
			 if (d > 0){
			 //if (d > this.dMoveScore) {
					this.dMoveScore = d;
					this.nodeIdMoveFrom1=nodeParent_k.GetNodeId();
					this.nodeIdMoveTo1=i;
					this.nodeIdMoveFrom2=nodeParent_l.GetNodeId();
					this.nodeIdMoveTo2=j;
			 }
			 gopt.DelArc(nodeParent_l,node_i);
			 gopt.DelArc(nodeParent_k,node_j);
			 gopt.AddArc(nodeParent_k, node_i);
			 gopt.AddArc(nodeParent_l, node_j);
			 fig=1;
		 }
		}
//	}
	}
	public void Move1(int i,int j){


		int k = 0;
		int l = 0;
		int fig=0;
		int p=0;
		BNNode node_i,node_j;
		java.util.ArrayList<Integer> parent_i = new java.util.ArrayList<Integer>();
		java.util.ArrayList<Integer> parent_j = new java.util.ArrayList<Integer>();

		while(fig==0){
	
		 do{
        //    i=RANDOM.nextInt(VexNum);  
            node_i = gopt.GetNode(i);
            parent_i = node_i.GetParentNodesIndex();
		  }while(parent_i.size()<1);
		 do{
			
	         node_j = gopt.GetNode(j);
	         parent_j = node_j.GetParentNodesIndex();
		  }while(parent_j.size()<1);
		 ////////////////////////
			java.util.ArrayList<Integer> descendant_i = node_i.GetDescendantNodesIndex();
			java.util.ArrayList<Integer> descendant_j = node_j.GetDescendantNodesIndex();
			java.util.TreeSet<Integer> tsFB_i = new java.util.TreeSet<Integer>();
			java.util.TreeSet<Integer> tsFB_j = new java.util.TreeSet<Integer>();
			for (p = 0; p < parent_i.size(); p++) {
				tsFB_i.add(parent_i.get(p));
			}
			for (p = 0; p < descendant_i.size(); p++) {
				tsFB_i.add(descendant_i.get(p));
			}
			for (p = 0; p < parent_j.size(); p++) {
				tsFB_j.add(parent_j.get(p));
			}
			for (p = 0; p < descendant_j.size(); p++) {
				tsFB_j.add(descendant_j.get(p));
			}
		 ///////////////////////
		 k=RANDOM.nextInt(parent_i.size());
		 l=RANDOM.nextInt(parent_j.size()); 
		 BNNode nodeParent_k = gopt.GetNode(parent_i.get(k));
		 BNNode nodeParent_l = gopt.GetNode(parent_j.get(l));
		 if((!tsFB_j.contains(parent_i.get(k)))&&(!(tsFB_i.contains(parent_j.get(l))))&&(!(node_i.equals(nodeParent_l)))&&(!(node_j.equals(nodeParent_k)))){
			// if(tsFB_j.contains(k)||(tsFB_i.contains(l))||(i==l))
		// if((!parent_i.contains(nodeParent_l.GetNodeId()))&&(!parent_j.contains(nodeParent_k.GetNodeId()))
	    //			&&(!node_i.equals(nodeParent_l))&&(!node_j.equals(nodeParent_k))&&(!(nodeParent_k.equals(nodeParent_l)))&&(!(node_i.equals(node_j)))){
			 double dDelOld = gopt.calcTotalK2Score(this.ScoreMetric);
			 gopt.DelArc(nodeParent_k, node_i);
			 gopt.DelArc(nodeParent_l, node_j);
			 double dDelNew = gopt.calcTotalK2Score(this.ScoreMetric);
			 double d1 = dDelNew - dDelOld;
			 double dAddOld = gopt.calcTotalK2Score(this.ScoreMetric);
			 gopt.AddArc(nodeParent_l,node_i);
			 gopt.AddArc(nodeParent_k,node_j);
			 double dAddNew = gopt.calcTotalK2Score(this.ScoreMetric);
			 double d2 = dAddNew - dAddOld;
			 double d = d1 + d2;
			 if (d > 0){
			 //if (d > this.dMoveScore) {
					this.dMoveScore = d;
					this.nodeIdMoveFrom1=nodeParent_k.GetNodeId();
					this.nodeIdMoveTo1=i;
					this.nodeIdMoveFrom2=nodeParent_l.GetNodeId();
					this.nodeIdMoveTo2=j;
			 }
			 gopt.DelArc(nodeParent_l,node_i);
			 gopt.DelArc(nodeParent_k,node_j);
			 gopt.AddArc(nodeParent_k, node_i);
			 gopt.AddArc(nodeParent_l, node_j);
			 fig=1;
		 }
		}
//	}
	}
}
