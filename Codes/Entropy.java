package bjut.ai.bn.score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.PriorityQueue;
import java.util.Comparator;

//import org.apache.commons.math3.special.Gamma;

public class Entropy {
	private static final double GAMMA = 0.577215664901532860606512090082;
	private static final double GAMMA_MINX = 1.e-12;
	private static final double DIGAMMA_MINNEGX = -1250;
	private static final double C_LIMIT = 49;
	private static final double S_LIMIT = 1e-5;
	private static final double minist = 1e-32;
	/***
	 * double[][] x:		n x d data
	 * double[][] y:		n x d data
	 * double minist:		mine distance usually >0
	 * String norm:			what type distance you want to compute:max or euclidean
	 * String estimator:	'ksg' or 'naive'
	 * ***/
	public double get_h(double[][] x) { //Default parameters
		int k=1;
	    double minist=1*10^-16;
		int n=x.length,d=x[0].length;
		double log_c_d = 0;
		double[] distances = getDistance(x);
        double sum_log_dist=0;
        for(double ds:distances) {
        	sum_log_dist += Math.log(ds*2);;
        }
        double h = -digamma(k) + digamma(n) + log_c_d + (d / (double)n) * sum_log_dist;
		return h;
	}
	
	public double get_h1(double[][] x, double minist, String norm, int k) {//Selected parameters 
		int kn=k;
		int n=x.length,d=x[0].length;
		double log_c_d = 0;
		if(norm.equals("euclidean")) {
			log_c_d = (d/2.0) * Math.log(Math.PI) -Math.log(gamma(d/2.0 +1, 0.00001));
		}
		double[] distances = getDistance(x);
        double sum_log_dist=0;
        for(double ds:distances) {
        	sum_log_dist += Math.log(ds*2);;
        } 
        double h = -digamma(k) + digamma(n) + log_c_d + (d / (double)n) * sum_log_dist;
		return h;
	}
	
	public double get_h2(double[][] x, int k, String norm) {//the k'th neighbor with different distance
		int n=x.length,d=x[0].length;
		double log_c_d = 0;
		if(norm.equals("euclidean")) {
			log_c_d = (d/2.0) * Math.log(Math.PI) -Math.log(gamma(d/2.0 +1, 0.00001));
		}
		double[] distances = getDistance(x, k+1, norm);
        double sum_log_dist=0;
        for(double ds:distances) {
        	sum_log_dist += Math.log(ds*2);;
        }
        System.out.println(d/n);
        double h = -digamma(k) + digamma(n) + log_c_d + (d / (double)n) * sum_log_dist;
        if (h<0)
        	h=0;
		return h;
	}
	
	public double get_mi(double[][] x, double[][] y, double minist, String norm, String estimator) {
		double mi=0;
		int k=1;
		double[][] xy=new double[x.length][x[0].length+y[0].length];
		for(int i=0;i<x.length;i++) {
			for(int j=0;j<x[0].length+y[0].length;j++) {
				if(j<x[0].length) {
					xy[i][j] = x[i][j];
				}else {
					xy[i][j] = y[i][j-x[0].length];
				}
			}
		}
		if(estimator.equals("naive")) {
			double hx = get_h1(x, minist, norm, k);
			double hy = get_h1(y, minist, norm, k);
			double hxy = get_h1(xy, minist, norm, k);
			mi = hx+hy-hxy;
		}else {//ksg
			double[] epsilon=getDistance1(xy, minist, norm);
			int n = x.length;
			int[] nx = new int[n], ny = new int[n];
			double nx_ny_digamma_mean=0;
			for(int i=0;i<n;i++) {
				nx[i] = getNeighbourMount(x[i], x, epsilon[i], norm);
				ny[i] = getNeighbourMount(y[i], y, epsilon[i], norm);
				nx_ny_digamma_mean += (digamma(nx[i])+digamma(ny[i]))/n;
			}
			mi = digamma(k)-nx_ny_digamma_mean+digamma(n);
		}
		return mi;
	}
	private int getNeighbourMount(double[] point, double[][] x, double r, String norm) {
		int n=x.length,d=x[0].length;
		int count=0;
		for(int i=0;i<n;i++) {
			if(norm.equals("max")) {
				double dis=Double.MIN_VALUE;
				for(int w=0;w<d;w++) {
					dis = Math.max(dis, Math.abs(x[i][w]-point[w]));
				}
				if(dis<r) {
					count++;
				}
			}else {//euclidean
				double dis=0;
				for(int w=0;w<d;w++) {
					dis += Math.pow(x[i][w]-point[w], 2);
				}
				if(Math.pow(dis, 0.5)<r) {
					count++;
				}
			}
		}
		return count;
	}
	private double[] getDistance(double[][] x) {//Selected parameters 
		int n=x.length,d=x[0].length;
		double minist=0.0001;
		double[] distances = new double[n];
		for(int i=0;i<n;i++) {
			double min = Double.MAX_VALUE;
			for(int j=0;j<n;j++) {
				if(i!=j) {
					//maximum-coordinate-difference distance
						double dis=Double.MIN_VALUE;
						for(int w=0;w<d;w++) {
							dis = Math.max(dis, Math.abs(x[i][w]-x[j][w]));
						}
						min = Math.min(min, dis);				
				}
			}
			if(min<minist) {min=minist;}
			distances[i] = min;
		}
		return distances;
	}
	private double[] getDistance(double[][] x, int k, String norm) {
		int n=x.length,d=x[0].length;
		double[] distances = new double[n];
		for(int i=0;i<n;i++) {
			PriorityQueue<Double> heap = new PriorityQueue<Double>(k, new Comparator<Double>() {
			    @Override
			    public int compare(Double o1, Double o2) {
			        return o2.compareTo(o1);
			    }
			});
			for(int j=0;j<n;j++) {
				double dis;
				if(norm.equals("max")) {//maximum-coordinate-difference distance
					dis=Double.MIN_VALUE;
					for(int w=0;w<d;w++) {
						dis = Math.max(dis, Math.abs(x[i][w]-x[j][w]));
					}
				}
				else {//euclidean
					dis=0;
					for(int w=0;w<d;w++) {
						dis += Math.pow(x[i][w]-x[j][w], 2);
					}
					dis = Math.pow(dis, 0.5);
				}
				if(heap.size()<k) {
					heap.add(dis);
				}
				else {
					if(heap.peek()>dis) {
						heap.poll();
						heap.add(dis);
					}
				}
			}
			if(heap.peek()<minist) {
				distances[i] = minist;
			}else {
				distances[i] = heap.peek();
			}
		}
		return distances;
	}
	private double[] getDistance1(double[][] x, double minist, String norm) {//Selected parameters
		int n=x.length,d=x[0].length;
		double[] distances = new double[n];
		for(int i=0;i<n;i++) {
			double min = Double.MAX_VALUE;
			for(int j=0;j<n;j++) {
				if(i!=j) {
					if(norm.equals("max")) {//maximum-coordinate-difference distance
						double dis=Double.MAX_VALUE;
						for(int w=0;w<d;w++) {
							dis = Math.min(dis, Math.abs(x[i][w]-x[j][w]));
						}
						min = Math.min(min, dis);
					}
					else {//euclidean
						double dis=0;
						for(int w=0;w<d;w++) {
							dis += Math.pow(x[i][w]-x[j][w], 2);
						}
						min = Math.min(min, Math.pow(dis, 0.5));
					}
					
				}
			}
			if(min<minist) {min=minist;}
			distances[i] = min;
		}
		return distances;
	}
	public static double gamma(double x, double setAbsRelaErr) {
		//setAbsRelaErr
		if(x < 0) {
			return gamma(x + 1, setAbsRelaErr) / x;
		}
		if(Math.abs(1.0 - x) < 0.00001) {
			return 1;
		}
		if(Math.abs(0.5 - x) < 0.00001) {
			return Math.sqrt(3.1415926);
		}
		
		if(x > 1.0) {
			return (x - 1) * gamma(x - 1, setAbsRelaErr);
		}
		
		double res = 0.0;
		double temp = 1.0;
		double check = 0.0;
		int i = 1;
		while(Math.abs((check - temp) / temp) > setAbsRelaErr){
			check = temp;
			temp *= i / (x - 1 + i);
			i++;
		}
		res = temp * Math.pow(i, x - 1);
		return res;
	}
	public static double digamma(double x) {
	    double value = 0;
	    while (true){
	        if (x >= 0 && x < GAMMA_MINX) {
	            x = GAMMA_MINX;
	        }
	        if (x < DIGAMMA_MINNEGX) {
	            x = DIGAMMA_MINNEGX + GAMMA_MINX;
	            continue;
	        }
	        if (x > 0 && x <= S_LIMIT) {
	            return value + -GAMMA - 1 / x;
	        }

	        if (x >= C_LIMIT) {
	            double inv = 1 / (x * x);
	            return value + Math.log(x) - 0.5 / x - inv
	                    * ((1.0 / 12) + inv * (1.0 / 120 - inv / 252));
	        }
	        value -= 1 / x;
	        x = x + 1;
	    }
	}
	
	public double conditionalH(double[][] x, double[][] y) {//calculate H(X|Y)
		double CH=0.0;
		double[][] xy = new double[x.length][2];
		for(int i=0;i<x.length;i++) {
			xy[i][0] = x[i][0];
			xy[i][1] = y[i][0];
		}
		CH=get_h(xy)-get_h(y);
		return CH;
	
	}
	
	
	public double transferH(double[][] x, double[][] y) {//calculate T(Y->X)
		double TH=0.0;
		double[][] xhou = new double[x.length][1];
		double[][] xqian = new double[x.length][1];
		double[][] yqian = new double[x.length][1];
		
		for(int i=0;i<x.length/2;i++) {
			xqian[i][0] = x[i][0];
			yqian[i][0] = y[i][0];
		}
		for(int i=x.length/2;i<x.length;i++) {
			xhou[i][0] = x[i][0];
		}
		
		double[][] xxqian = new double[x.length][2];
		double[][] xqianyqian = new double[x.length][2];
		double[][] xxqianyqian = new double[x.length][3];
		
		for(int i=0;i<x.length;i++) {
			xxqian[i][0] = xhou[i][0];
			xxqian[i][1] = xqian[i][0];
			xqianyqian[i][0] = xqian[i][0];
			xqianyqian[i][1] = yqian[i][0];
			xxqianyqian[i][0] = xhou[i][0];
			xxqianyqian[i][1] = xqian[i][0];
			xxqianyqian[i][2] = yqian[i][0];
		}
		TH=get_h(xqianyqian)+get_h(xxqian)-get_h(xxqianyqian)-get_h(xqian);
		return TH;
	
	}
	public double get_ch(double[][] x, double[][] y) {
		double[][] xy=new double[x.length][x[0].length+y[0].length];
		for(int i=0;i<x.length;i++) {
			for(int j=0;j<x[0].length+y[0].length;j++) {
				if(j<x[0].length) {
					xy[i][j] = x[i][j];
				}else {
					xy[i][j] = y[i][j-x[0].length];
				}
			}
		}
		return this.get_h(xy);
	}
	
	public double get_ch1(double[][] x, double[][] y, double minist, int k, String norm) {
		double[][] xy=new double[x.length][x[0].length+y[0].length];
		for(int i=0;i<x.length;i++) {
			for(int j=0;j<x[0].length+y[0].length;j++) {
				if(j<x[0].length) {
					xy[i][j] = x[i][j];
				}else {
					xy[i][j] = y[i][j-x[0].length];
				}
			}
		}
		return this.get_h1(xy, minist, norm, k);
	}
	
	public double get_te(double[][] x, double[][] y) {
		int n=x.length/2,dx=x[0].length,dy=y[0].length;
		if(n/2>y.length) {
			return Double.NaN;
		}
		double[][] X=new double[n][dx],W=new double[n][dx],Y=new double[n][dy];
		for(int i=0;i<X.length*2;i++) {
			if(i<X.length) {
				for(int j=0;j<dx;j++) {
					X[i][j]=x[i][j];
				}
				for(int j=0;j<dy;j++) {
					Y[i][j]=y[i][j];
				}
			}else {
				for(int j=0;j<dx;j++) {
					W[i-X.length][j]=x[i][j];
				}
			}
		}
		double[][] WX = this.concrate(W, X);
		double[][] XY = this.concrate(X, Y);
		double[][] WXY = this.concrate(WX, Y);
		double hwx=this.get_h(WX);
		double hxy=this.get_h(XY);
		double hx=this.get_h(X);
		double hwxy=this.get_h(WXY);
		double te=hwx+hxy-hx-hwxy;
		if(te<0) {
			te=0;
		}
		return hwx+hxy-hx-hwxy;
	}
	private double[][] concrate(double[][]x, double[][]y){
		double[][] xy = new double[x.length+y.length][x[0].length];
		for(int i=0;i<x.length+y.length;i++) {
			if(i<x.length) {
				for(int j=0;j<x[0].length;j++) {
					xy[i][j] = x[i][j];
				}
			}else {
				for(int j=0;j<x[0].length;j++) {
					xy[i][j] = y[i-x.length][j];
				}
			}
		}
		return xy;
	}
	public double get_te1(double[][] x, double[][] y, double minist, int k, String norm) {
		int n=x.length/2,dx=x[0].length,dy=y[0].length;
		if(n/2>y.length) {
			return Double.NaN;
		}
		double[][] X=new double[n][dx],W=new double[n][dx],Y=new double[n][dy];
		for(int i=0;i<X.length*2;i++) {
			if(i<X.length) {
				for(int j=0;j<dx;j++) {
					X[i][j]=x[i][j];
				}
				for(int j=0;j<dy;j++) {
					Y[i][j]=y[i][j];
				}
			}else {
				for(int j=0;j<dx;j++) {
					W[i-X.length][j]=x[i][j];
				}
			}
		}
		double hwx=this.get_ch1(W, X, minist, k, norm);
		double hxy=this.get_ch1(X, Y, minist, k, norm);
		double hx=this.get_h1(X, minist, norm, k);
		double[][] WXY = new double[X.length][dx*2+dy];
		for(int i=0;i<X.length;i++) {
			for(int j=0;j<dx*2+dy;j++) {
				if(j<dx) {
					WXY[i][j]=W[i][j];
				}else {
					if(j<2*dx) {
						WXY[i][j]=X[i][j-dx];
					}else {
						WXY[i][j]=Y[i][j-2*dx];
					}
				}
			}
		}
		double hwxy=this.get_h1(WXY, minist, norm, k);
		return hwx+hxy-hx-hwxy;
	}
	
	
	public static void main(String[] args) {
		String filepath = "D:\\sim1.txt";
		int n=5; //number of nodes
		double[][] x=null;
		int lines=0;
		try {
			File file = new File(filepath);
			FileReader fileReader = new FileReader(file);
			LineNumberReader r = new LineNumberReader(fileReader);
			r.skip(Long.MAX_VALUE);
			lines = Math.min(10000, r.getLineNumber());
			x = new double[lines][n];
			r.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			File file = new File(filepath);
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			int t=lines;
			while(lines>0) {//依次读取文件的每一行
				lines--;
				String line = reader.readLine();
				String[] num = line.trim().split(",");
				for(int i=0;i<n;i++) {
				x[t-lines-1][i] = Double.valueOf(num[i]);
				}
			}
			reader.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		//测试
		double [][] a=new double[x.length][1], b=new double[x.length][1], c=new double[x.length][1]
				, d=new double[x.length][1], e=new double[x.length][1], f=new double[x.length+x.length][1], g=new double[x.length][1];
		for(int i=0;i<x.length;i++) {
			a[i][0] = x[i][0];
			b[i][0] = x[i][1];
			c[i][0] = x[i][2];
			d[i][0] = x[i][3];
			e[i][0] = x[i][4];
		}
		for(int i=0;i<x.length*2;i++) {
			if (i<x.length)
			f[i][0] = x[i][0];
			if (i>=x.length&&i<x.length*2)
			f[i][0] = x[i-x.length][0];

		}
		System.out.println("begin");
		//double mi=new continuous().get_mi(a,b,0.001,"euclidean","ksg");
		
		double h1=new Entropy().get_h(a);
		double h2=new Entropy().get_h(b);
		double h3=new Entropy().get_h(c);
		double h4=new Entropy().get_h(d);
		double h5=new Entropy().get_h(e);
		double h6=new Entropy().get_h(f);
		double h7=new Entropy().get_h2(f,1,"max");

//		double h12=new Entropy().transferH(a,b);
//		double h13=new Entropy().transferH(a,c);
//		double h14=new Entropy().transferH(a,d);
//		double h15=new Entropy().transferH(a,e);
//		double h23=new Entropy().transferH(b,c);
//		double h24=new Entropy().transferH(b,d);
//		double h25=new Entropy().transferH(b,e);
//		double h34=new Entropy().transferH(c,d);
//		double h35=new Entropy().transferH(c,e);
//		double h45=new Entropy().transferH(d,e);
//		double h21=new Entropy().transferH(b,a);
//		double h31=new Entropy().transferH(c,a);
//		double h41=new Entropy().transferH(d,a);
//		double h51=new Entropy().transferH(e,a);
//		double h32=new Entropy().transferH(c,b);
//		double h42=new Entropy().transferH(d,b);
//		double h52=new Entropy().transferH(e,b);
//		double h43=new Entropy().transferH(d,c);
//		double h53=new Entropy().transferH(e,c);
//		double h54=new Entropy().transferH(e,d);
		System.out.println(h1+","+h2+","+h3+","+h4+","+h5+","+h6+","+h7);
		
//		System.out.println(0+","+h12+","+h13+","+h14+","+h15);
//		System.out.println(h21+","+0+","+h23+","+h24+","+h25);
//		System.out.println(h31+","+h32+","+0+","+h34+","+h35);
//		System.out.println(h41+","+h42+","+h43+","+0+","+h45);
//		System.out.println(h51+","+h52+","+h53+","+h54+","+0);
//		double h121=h12-h21;
//		double h232=h23-h32;
//		double h343=h34-h43;
//		double h454=h45-h54;
//		double h151=h15-h51;
//		System.out.println(h121+","+h232+","+h343+","+h454+","+h151);
		
		
		//System.out.println(mi);
		
		
		
		System.out.println("end");
	}
}
