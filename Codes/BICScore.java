package bjut.ai.bn.score;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import bjut.ai.bn.learning.acob.AlarmReader;
import bjut.ai.bn.BNGraph;

public class BICScore extends Score {
	
	public static double maxMI;
	public static double minMI;
	public static double[][] Inf;
	public static int[][] ChiSquare;
	public static PrintWriter out;
	public static int VEXNUM = 5;
	public static Scores INSTANCE = null;
	public static int cacheCount = 0;
	public static int count = 0;
	public static int actualCalcCount = 0;
	public static TreeSet[] NodeInfo;
	private String[][] Records;
	private HashMap<String, Double> hm; 
	public HashMap<String, Double> cacheResult;
	private int ri;

	 public BICScore(String fileName, int size, int nodeNums) {
	 AlarmReader ar = new AlarmReader(fileName, size, nodeNums);
	 this.Records = ar.GetDataSet();
	 BICScore.NodeInfo = ar.getColumnValue();
	 BICScore.VEXNUM=nodeNums;
	 this.cacheResult = new HashMap<String, Double>();
	 }
	 
		@Override
		public double calcGraphScore(BNGraph g) {
			double score = 0.0;
			double size = g.getVexNum();
			for (int i = 0; i < size; i++) {
				ArrayList parent = g.GetNode(i).GetParentNodesIndex();
				double nodescore = this.calcScore(i, parent, g);
				score += nodescore;
			}
			
			return score;
			
		}

	
	public double calcScore(int index, ArrayList<Integer> parent, BNGraph g) {

		return this.calcK2Cache(index, parent, g);

	
	}
	
	public double calcK2Cache(int index, ArrayList<Integer> parent, BNGraph g) {
		double result = 0.0;
		int[] indexArray = new int[parent.size() + 1];
		for (int i = 0; i < parent.size(); i++) {
			indexArray[i + 1] = parent.get(i);
		}
		Arrays.sort(indexArray);
		indexArray[0] = index;
		String indexString = this.convertToString(indexArray);
		if (this.cacheResult.containsKey(indexString)) {
			result = this.cacheResult.get(indexString);
			BICScore.cacheCount++;
		} else {
			result = this.calcK2Tool(index, indexArray, g);
			this.cacheResult.put(indexString, result);
		}
		return result;
	}

	private String convertToString(int[] array) {
		StringBuilder sb = new StringBuilder();
		for (int k : array) {
			sb.append(k);
			sb.append(",");
		}
		return sb.toString();
	}
	
	private double calcK2Tool(int index, int[] parentIJK, BNGraph g) {
		int[] parent = new int[parentIJK.length - 1];
		System.arraycopy(parentIJK, 1, parent, 0, parent.length);

		ri = NodeInfo[index].size();
		hm = BICScore.getCount(this.Records, parentIJK);
		double ijk_result = this.calcPart2(hm);

		hm = BICScore.getCount(Records, parent); 
		double ij_result = this.calcPart1(hm);
		
		double l1_result = this.calcPart3(g);
		
		double result = ij_result + ijk_result;
		
		 System.out.println(result);
		 
		return result;

	}
	
	private static HashMap getCount(String[][] record, int[] indexs) {
		HashMap hm = new HashMap();
		StringBuilder sb;
		Object tempcount;
		for (int i = 0; i < record.length; i++) {
			sb = new StringBuilder();
			for (int j = 0; j < indexs.length; j++) {

				sb.append(record[i][indexs[j]]);
				sb.append(";");
			}
			String temp = sb.toString();
			int count = 0;
			if ((tempcount = hm.get(temp)) != null) {
				count = (Integer) tempcount;
			}
			hm.put(temp, ++count);
		}

		return hm;
	}
	
	private double calcPart1(HashMap h1) // ij
	{
		double ij_result = 0;
		Collection c = h1.values();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			int Nij = (Integer) it.next();
			double temp1 = BICScore.calcLog(ri - 1);
			double temp2 = BICScore.calcLog(Nij + ri - 1);
			double temp3 = temp1 - temp2;
			ij_result = ij_result + temp3;
		}
		c = null;
		it = null;
		return ij_result;
	}

	private double calcPart2(HashMap h2) 
	{

		double ijk_result = 0;
		Collection c = h2.values();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			int temp_k = (Integer) it.next();
			double temp_result = BICScore.calcLog(temp_k);
			ijk_result = ijk_result + temp_result;
		}
		c = null;
		it = null;
		return ijk_result;
	}

	private double calcPart3(BNGraph g) 
	{
		double l1_result=0.0;
	    double lam=0.2;
		
		l1_result=g.getVexNum()*lam;
		return l1_result;
		
		
	}
	
	/**
	 * 计算log(n!)
	 * 
	 * @param n
	 *            int
	 * @return double
	 */
	private static double calcLog(int n) {
		double result = 0;
		for (int i = 1; i <= n; i++) {
			result = result + java.lang.Math.log10(i);
		}
		return result;
	}
	
	/**
	 * 计算互信息
	 * 
	 */
	public static void calcInf(String[][] data) {
		System.out.println("计算互信息");
		BICScore.Inf = new double[BICScore.VEXNUM][BICScore.VEXNUM];
		int row = 0;
		int col = 0;
		for (row = 0; row < BICScore.VEXNUM; row++) {
			for (col = 0; col < BICScore.VEXNUM; col++) {
				// 计算上三角
				if (row < col) {
					//
					Inf[row][col] = BICScore.calcInd(row, col, data);
				} else {
					Inf[row][col] = Inf[col][row];
				}
			}
		}
		BICScore.maxMI = BICScore.getMaxMI();
		BICScore.minMI = BICScore.getMinMI();
		 System.out.print("\r\n最大互信息：" + BICScore.maxMI);
		 System.out.print("\r\n最小互信息：" + BICScore.minMI);
	}
	public static double getMaxMI() {
		double infMax = 0;
		double[][] inf = BICScore.Inf;
		for (int i = 0; i < inf.length; i++) {
			for (int j = 0; j < inf.length; j++) {
				if (inf[i][j] > infMax) {
					infMax = inf[i][j];
				}
			}
		}
		return infMax;

	}

	public static double getMinMI() {
		double infMin = Double.POSITIVE_INFINITY;
		double[][] inf = BICScore.Inf;
		for (int i = 0; i < inf.length; i++) {
			for (int j = 0; j < inf.length; j++) {
				if ((inf[i][j] < infMin) && inf[i][j] != 0) {
					infMin = inf[i][j];
				}
			}
		}
		return infMin;

	}
	

	public static double calcInd(int x, int y, String[][] data) {
		double result = 0.0;
		int[] query = { x, y };
		int[] queryResult = BICScore.getCountBrutal(data, query);
		double[][] arrayProb = constructProbArray(x, y, queryResult, data);
		double[] px = constructProbX(arrayProb);// 列数
		double[] py = constructProbY(arrayProb);// 行数

		for (int i = 0; i < arrayProb.length; i++) {
			for (int j = 0; j < arrayProb[0].length; j++) {
				if (arrayProb[i][j] != 0.0) {
					double temp = Math.log10(arrayProb[i][j] / px[j] / py[i]);
					temp *= arrayProb[i][j];
					result += temp;
				}
			}
		}
		return result;
	}
	
	private static double[][] constructProbArray(int x, int y,
			int[] queryResult, String[][] data) {
		int count = 0;
		int jIndex = BICScore.NodeInfo[x].size();
		int iIndex = BICScore.NodeInfo[y].size();
		double num = data.length;
		double[][] arrayProb = new double[iIndex][jIndex];
		for (int j = 0; j < jIndex; j++) {
			for (int i = 0; i < iIndex; i++) {
				arrayProb[i][j] = queryResult[count++] / num;
			}
		}
		return arrayProb;
	}

	private static double[] constructProbX(double[][] arrayProb) {
		double[] px = new double[arrayProb[0].length];
		for (int j = 0; j < arrayProb[0].length; j++) {
			for (int i = 0; i < arrayProb.length; i++) {
				px[j] += arrayProb[i][j];
			}
		}
		return px;
	}

	private static double[] constructProbY(double[][] arrayProb) {
		double[] py = new double[arrayProb.length];
		for (int i = 0; i < arrayProb.length; i++) {
			for (int j = 0; j < arrayProb[0].length; j++) {
				py[i] += arrayProb[i][j];
			}
		}
		return py;
	}
	
	public static int[] getCountBrutal(String[][] record, int[] indexs) {

		int resultLength = 1;
		for (int i = 0; i < indexs.length; i++) {
			int size = NodeInfo[indexs[i]].size();
			resultLength = resultLength * size;
		}
		 System.out.println("\n查询数组长度"+resultLength);
		int[] result = new int[resultLength];
		for (int j = 0; j < record.length; j++) {
			int[] temp = new int[indexs.length];
			for (int k = 0; k < indexs.length; k++) {
				temp[k] = Integer.parseInt(record[j][indexs[k]]);
			}
			int index = BICScore.calcStringToIndex(temp, indexs);

			result[index]++;
		}

		return result;
	}

	private static int calcStringToIndex(int[] array, int[] indexs) {
		int index = 0;
		for (int i = 0; i < array.length; i++) {
			int temp = 1;
			for (int j = i + 1; j < array.length; j++) {
				temp = temp * NodeInfo[indexs[j]].size();
			}
			index = index + array[i] * temp;
		}

		return index;
	}
	
	
	
	public static void main(String[] args) {
		try {

			BNGraph g = BNGraph.GetGraphStandAlarm();
			Score BIC = new BICScore("F:\\sim1-4.txt", 10000, 5);

			double score = BIC.calcGraphScore(g);
	        System.out.print(score);
	        
		
		    
		    BNGraph g1 = BNGraph.GetGraph1();
		    BNGraph g2 = BNGraph.GetGraph2();
		    double score1 = BIC.calcGraphScore(g1);
	    double score2 = BIC.calcGraphScore(g2);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
}
