package bjut.ai.bn.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import bjut.ai.bn.learning.acob.AlarmReader;
import bjut.ai.bn.BNGraph;
import Jama.*;
public class BICGScore extends Score
{
	public static int VEXNUM = 5;
	public static BICGScore INSTANCE = null;
	public static enum TYPE {
		ORI, CI, CInew, HF, OP, AIO, SA, PC
	};
    private String[][] Records;
    private HashMap<String, Double> cacheResult;
	public static BICGScore getBICGScore(String dir, int DatasetNum, int VexNum) {
        BICGScore.INSTANCE = new BICGScore(dir, DatasetNum, VexNum);
	    BICGScore.VEXNUM = VexNum;
	    return BICGScore.INSTANCE;
	 }


	public BICGScore(String fileName, int size, int nodeNums) {
	    AlarmReader ar = new AlarmReader(fileName, size, nodeNums);
        this.Records = ar.GetDataSet();
        cacheResult = new HashMap<String, Double>();
        BICGScore.VEXNUM=nodeNums;
	}

	
	public String[][] getRecord() {
		return this.Records;
	}
	
	@Override
	public void clearCache() {
		cacheResult = new HashMap<String, Double>();
	}

    private double calcBICGScore(int i, ArrayList<Integer> PAi) {
		double score=0;
		Collections.sort(PAi);
		String key = ""+i+":"+PAi.toString();
		if(cacheResult.containsKey(key)) {
			return cacheResult.get(key);
		}
		int size=this.Records.length, nodeNums=this.Records[0].length;
		double[][] Data = new double[size][nodeNums];
        for(int a=0;a<size;a++){
            for(int b=0;b<nodeNums;b++){
                Data[a][b] = Double.parseDouble(this.Records[a][b]);
            }
        }
		Matrix dataMatrix = new Matrix(Data);
		int T=dataMatrix.getRowDimension();
		Matrix X = dataMatrix.getMatrix(0,T-1,i,i);
		if(PAi!=null&&PAi.size()!=0) {
			int D = PAi.size();
			int[] pai = new int[D];
			for(int t=0;t<D;t++) {
				pai[t] = PAi.get(t);
			}
			Matrix PA = dataMatrix.getMatrix(0,T-1,pai);
			Matrix H = PA.times(pdinv(PA.transpose().times(PA))).times(PA.transpose());
			Matrix E = X.minus(H.times(X));
			double sigma2=0;
			for(int a=0;a<E.getRowDimension();a++) {
				for(int b=0;b<E.getColumnDimension();b++) {
					sigma2 += Math.pow(E.get(a, b), 2);
				}
			}
			sigma2 = sigma2/T;
			score = T*Math.log(sigma2)+D*Math.log(T);
		}else {
			double sigma2=0;
			for(int a=0;a<X.getRowDimension();a++) {
				for(int b=0;b<X.getColumnDimension();b++) {
					sigma2 += Math.pow(X.get(a, b), 2);
				}
			}
			sigma2 = sigma2/T;
			score = T*Math.log(sigma2);
		}
		cacheResult.put(key, -score);
		return -score;
    }
    
	private Matrix pdinv(Matrix a) {
		int numData=a.getRowDimension();
		CholeskyDecomposition chol= new CholeskyDecomposition(a);
		if(chol.isSPD()) {
			Matrix U = chol.getL().transpose();//涓婁笁瑙�
			Matrix eyeMatrix = new Matrix(numData, numData);
			for(int i=0;i<numData;i++) {
				eyeMatrix.set(i, i, 1);
			}
			Matrix invU = eyeMatrix.times(U.inverse());
			Matrix Ainv = invU.times(invU.transpose());
			return Ainv;
		}else {
		//	System.out.println("Matrix is not positive definite in pdinv, inverting using svd");
			SingularValueDecomposition svd = new SingularValueDecomposition(a);
			Matrix U=svd.getU(),V=svd.getU(), S=svd.getS();
			for(int i=0;i<U.getRowDimension();i++) {
				for(int j=0;j<V.getColumnDimension();j++) {
					S.set(i, j, 1/S.get(i, j));
				}
			}
			return V.times(S).times(U.transpose());
		}
	}
	
	@Override
	public double calcGraphScore(BNGraph g) {
		double score = 0.0;
		double size = g.getVexNum();
		for (int i = 0; i < size; i++) {
			ArrayList parent = g.GetNode(i).GetParentNodesIndex();
			double nodescore = this.calcScore(i, parent);
			score += nodescore;
		}
		return score;
		
	}

	@Override
	public double calcScore(int index, ArrayList<Integer> parent) {
		return this.calcBICGScore(index, parent);
	
	}

	public static void main(String[] args) {
		try {
			
			BNGraph g = BNGraph.GetGraphStandAlarm();
			Score BICGScore = new BICGScore("F:\\sim1.txt", 2000, 5);
			double score = BICGScore.calcGraphScore(g);
		    System.out.print(score);
		   
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
