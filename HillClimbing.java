package bjut.ai.bn;

import bjut.ai.bn.score.Scores;
import bjut.ai.bn.score.Score;
/**
 * HillClimbing
 * Company: BJUT - AI
 * 
 * @author Jinduo
 * @version 1.0
 */
public class HillClimbing {

	private Score ScoreMetric = null;
	private BNGraph gold = null;
	private BNGraph gopt = null;
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
	private int[][] CI; 
	private double[][] heuristic; 
	private double[][] Inf;
	private double MaxInf = Double.NEGATIVE_INFINITY; 
	private int FirstK = 150;
	private array3D[] SortedHeuritic;
	private array3D[] SortedInf; 
	private int[][] arrFirstK;
	private int[][] arrLastK;

	public HillClimbing(BNGraph g, Score s) {
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

	public BNGraph OptimizeBN() {
		int MAXLOOP = 1000; 
		int i = 0;
		while (i < MAXLOOP) {
			i++;

			this.dAddScore = Double.NEGATIVE_INFINITY;
			this.dDelScore = Double.NEGATIVE_INFINITY;
			this.dReverseScore = Double.NEGATIVE_INFINITY;

			calcAddition();
			calcDeletion();
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
	
	public BNGraph OptimizeBNcte() {
		int MAXLOOP = 1000; 
		int i = 0;
		while (i < MAXLOOP) {
			i++;

			this.dAddScore = Double.NEGATIVE_INFINITY;
			this.dDelScore = Double.NEGATIVE_INFINITY;
			this.dReverseScore = Double.NEGATIVE_INFINITY;

			calcAddition();
			calcDeletion();
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
				if (j == i || tsFB.contains(j)) {
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
				// }
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
			java.util.ArrayList<Integer> descendant = node
					.GetDescendantNodesIndex();
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
		int k = 0;

		for (i = 0; i < VexNum; i++) {
			BNNode node = gopt.GetNode(i);
			java.util.ArrayList<Integer> parent = node.GetParentNodesIndex();

			
			for (j = 0; j < parent.size(); j++) {
				
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

	private void calcDeletion_CI() {


		int i = 0;
		int j = 0;
		int k = 0;

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

	
		int i = 0;
		int j = 0;
		int k = 0;
		for (i = 0; i < VexNum; i++) {
			BNNode node = gopt.GetNode(i);
			java.util.ArrayList<Integer> parent = node.GetParentNodesIndex();
			for (j = 0; j < parent.size(); j++) {
				BNNode nodeParent = gopt.GetNode(parent.get(j));
				int nodeParentId = nodeParent.GetNodeId();
				java.util.ArrayList<Integer> parentForbidden = nodeParent
						.getForbiddenParent();
				if (!parentForbidden.contains(i)) {

					double dDelOld = gopt.calcTotalK2Score(this.ScoreMetric);
					gopt.DelArc(nodeParent, node);
					java.util.ArrayList<Integer> xjDescendant = nodeParent
							.GetDescendantNodesIndex();
					if (!xjDescendant.contains(new Integer(i))) {
						double dDelNew = gopt.calcTotalK2Score(this.ScoreMetric);
						double d1 = dDelNew - dDelOld;
						if (d1 > 0) {
							node.AddForbiddenParent(nodeParent);
						}

						double dAddOld = gopt.calcTotalK2Score(this.ScoreMetric);
						gopt.AddArc(node, nodeParent);
						java.util.ArrayList<Integer> xjParent = nodeParent
								.GetParentNodesIndex();
						double dAddNew = gopt.calcTotalK2Score(this.ScoreMetric);
						double d2 = dAddNew - dAddOld;
						if (d2 < 0) {
							nodeParent.AddForbiddenParent(node);
						}
						double d = d1 + d2;
						if (d > this.dReverseScore) {
							this.dReverseScore = d;
							this.nodeIdRevFrom = nodeParentId;
							this.nodeIdRevTo = i;
						}
						gopt.DelArc(node, nodeParent);
					}
					gopt.AddArc(nodeParent, node);
				}
			} 
		}

	}
}
