package bjut.ai.bn.score;

import java.util.ArrayList;

import bjut.ai.bn.BNGraph;
public abstract class Score {
	
	
	public double calcScore(int index, ArrayList<Integer> parent) {
		return Double.NaN;
	}
	public void clearCache()
	{
		
	}
	public double calcGraphScore(BNGraph g)
	{
		return Double.NaN;
	}
}
