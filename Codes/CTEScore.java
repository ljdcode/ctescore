    package bjut.ai.bn.score;

import java.util.ArrayList;
import java.util.Collections;

import smile.stat.distribution.KernelDensity;
import java.util.HashMap;

import bjut.ai.bn.score.Entropy;
import bjut.ai.bn.BNGraph;
import bjut.ai.bn.learning.acob.AlarmReader;
import Jama.*;

public class CTEScore extends Score{
	
    public static int VEXNUM = 5;
    public static CTEScore INSTANCE = null
    private String[][] Records;
    private HashMap<String, Double> cacheResult;
    
	public static CTEScore gettransfer(String dir, int DatasetNum, int VexNum) {
        CTEScore.INSTANCE = new CTEScore(dir, DatasetNum, VexNum);
	    CTEScore.VEXNUM = VexNum;
	    return CTEScore.INSTANCE;
	 }
    
    public CTEScore(String fileName, int size, int nodeNums) {
    	AlarmReader ar = new AlarmReader(fileName, size, nodeNums);
        this.Records = ar.GetDataSet();
        cacheResult = new HashMap<String, Double>();
        CTEScore.VEXNUM=nodeNums;
    }
    
	public String[][] getRecord() {
		return this.Records;
	}
	
	@Override
	public double calcScore(int index, ArrayList<Integer> parent) {
		return this.calctransfer(index, parent);
	}
	
	@Override
	public void clearCache()
	{
		cacheResult = new HashMap<String, Double>();
	}
	
	@Override
	public double calcGraphScore(BNGraph g)
	{
		double score = 0.0;
		double size = g.getVexNum();
		for (int i = 0; i < size; i++) {
			ArrayList parent = g.GetNode(i).GetParentNodesIndex();
			double nodescore = this.calcScore(i, parent);
			score += nodescore;
		}
		return score;
		
	}
	
	private double calctransfer(int index, ArrayList<Integer> parent, double omega, double lamda, int regression) {
		double score=0;
		int regression=0;
		Collections.sort(parent);
		String key = ""+index+":"+parent.toString();
		if(cacheResult.containsKey(key)) {
			return cacheResult.get(key);
		}
		Entropy entropy = new Entropy();
		int size=this.Records.length, nodeNums=this.Records[0].length;
		double[][] Data = new double[size][nodeNums];
        for(int a=0;a<size;a++){
            for(int b=0;b<nodeNums;b++){
                Data[a][b] = Double.parseDouble(this.Records[a][b]);
            }
        }
        double[][] x = new double[size][1];
       // double[] x = new double[size];
        for(int a=0;a<size;a++) {
        	x[a][0] = Data[a][index];
        	//x[a] = Data[a][index];
        }
        for(int a=0;a<parent.size();a++) {
        	double[][] y = new double[size][1];
        	//double[] y = new double[size];
        	for(int b=0;b<size;b++) {
        		y[b][0] = Data[b][parent.get(a)];
        	}
//        	//Use JIT tool box
//        	TransferEntropyCalculatorGaussian calc=null;//employ Gaussian kernal estimator
//		    try {
//				calc = new TransferEntropyCalculatorGaussian();
//			} catch (InstantiationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		    try {
//				calc.initialise();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			try {
//				calc.setObservations(y, x);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			double te=0;
//			try {
//				te = calc.computeAverageLocalOfObservations()*10000;
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}//end
			
        	double te = entropy.get_te(x, y);
        	double ce = entropy.conditionalH(x, y);
        	score += -omega*ce+lamda*te;
        }
		if(parent.size()>0) {
			if(regression==1)
			{
			double[] E = this.LinearRegression(index, parent);
			KernelDensity kd = new KernelDensity(E);
			for(double i:E) {
				score += kd.logp(i);
			}
			}
			else
			score -= 1.0*parent.size()*Math.log(this.Records.length)/2;
			cacheResult.put(key, score);
		}else {
			if(regression==1)
			{
			double[] E=new double[this.Records.length];
			for(int i=0;i<E.length;i++) {
				E[i] = Double.valueOf(this.Records[i][index]);
			}
			KernelDensity kd = new KernelDensity(E);
			for(double i:E) {
				score += kd.logp(i);
			}
			}
			score -= 1.0*parent.size()*Math.log(this.Records.length)/2;
			cacheResult.put(key, score);
		}
		
		return score;
	}
	

	private double[] LinearRegression(int index, ArrayList<Integer> parent) {
		int size=this.Records.length, nodeNums=parent.size()*2+1;
		double[][] x = new double[size][nodeNums];
		double[][] y = new double[size][1];
        for(int a=0;a<size;a++){
        	x[a][0] = 1;
            for(int b=1;b<nodeNums-1;b++){
            	
            	if(b-1>=parent.size()) {
            		x[a][b] = Math.pow(Double.parseDouble(this.Records[a][parent.get(b-1-parent.size())]), 2);
            	}else {
            		x[a][b] = Double.parseDouble(this.Records[a][parent.get(b-1)]);
            	}
            }
            y[a][0] = Double.parseDouble(this.Records[a][index]);
        }
        Matrix X = new Matrix(x);
        Matrix Y = new Matrix(y);
        Matrix theta = X.transpose();
        theta = theta.times(X);
        theta = this.pseudoinverse(theta);
        theta = theta.times(X.transpose());
        theta = theta.times(Y);
        double[] E = new double[size];
        Matrix outcome = Y.minus(X.times(theta));
        for(int i=0;i<size;i++) {
        	E[i] = outcome.get(i, 0);
        }
        return E;
	}
	private Matrix pseudoinverse(Matrix A) {
		SingularValueDecomposition svd =A.svd();
		Matrix S = svd.getS();
		Matrix V = svd.getV().transpose();
		Matrix U = svd.getU();
		Matrix sinv = CTEScore.UnaryNotZeroElement(S);
		Matrix inv = V.times(sinv).times(U.transpose());
		return inv;
	}
	private static Matrix UnaryNotZeroElement(Matrix x) {
		double[][] array=x.getArray();
		for(int i=0;i<array.length;i++){
			for(int j=0;j<array[i].length;j++){
				if(array[i][j]!=0){
					array[i][j]=1.0/array[i][j];
				}
			}
		}
		return new Matrix(array);
	}
}
