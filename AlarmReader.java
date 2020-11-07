package bjut.ai.bn.learning.acob;

import java.io.*;
import java.util.*;

public class AlarmReader {
	  public String FileName;
	  public String[][] DataSet;
	  public int RecordNum;
	  public int AttributeNum;


	  public AlarmReader(String FileName, int RecordNum, int AttributeNum)
	  {
	    this.FileName = FileName;
	    this.RecordNum = RecordNum;
	    this.AttributeNum = AttributeNum;
	    DataSet = new String[this.RecordNum][this.AttributeNum];
	  }
	  public TreeSet[] getColumnValue()
	  {
	    TreeSet<String>[] ts = new TreeSet[AttributeNum];
	    for(int k = 0; k < AttributeNum; k++)
	    {
	      ts[k] = new TreeSet();
	    }
	    for(int i = 0; i < RecordNum; i++)
	    {
	      for(int j = 0; j < AttributeNum; j++)
	      {
	    	  ts[j].add(DataSet[i][j]);
	      }
	    }
	    return ts;
	  }

	  public String[][] GetDataSet()
	  {
	    int row = 0;

	    try
	    {
	      BufferedReader br = new BufferedReader(new FileReader(FileName));
	      String line = br.readLine();
	      while(line != null && row < RecordNum)
	      {
	          line = line.trim();
	          line=line.replaceAll(","," ");
	        
	        DataSet[row] = line.split("[\\s]+");
	        line = br.readLine();
	        row++;
	      }
	      br.close();
	    }
	    catch (Exception ex)
	    {
	      ex.printStackTrace();
	    }
	    return DataSet;
	  }

	  //for test only
	  public void outArray(String[][] s)
	  {
	      for(int i = 0; i < s.length; i++)
	      {
	          for(int j = 0; j < s[i].length; j++)
	          {
	              System.out.print(s[i][j]+"  ");
	          }
	          System.out.println();
	      }
	  }
	  public static void main(String[] args)
	  {

	    AlarmReader ar = new AlarmReader("F:\\sim1.txt", 2000, 5);
	    ar.outArray(ar.GetDataSet());	    
	    TreeSet[] ts = ar.getColumnValue();
	    System.out.println("");

	  }
}
