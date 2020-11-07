package bjut.ai.bn;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import bjut.ai.bn.score.Scores;
import bjut.ai.bn.score.Score;
public class BNGraph
    implements Comparable  , Cloneable  {

  public double K2Score;

  private java.util.HashMap<Integer, BNNode> Vex = null;

  public BNGraph() {
    this.Vex = new java.util.HashMap<Integer, BNNode> ();
  }

  public double calcTotalK2Score(Score s) {
    double score = 0;
    int i = 0;
    int VexCount = this.Vex.size();
    BNNode node;
    for (i = 0; i < VexCount; i++) {
      node = this.Vex.get(i);
      node.calcK2Score(s);
      score += node.GetK2Score();
    }
    this.K2Score = score;
    return score;
  }

  @Override
public int compareTo(Object o) {
    return this.K2Score > ( (BNGraph) o).K2Score ? 1 :
        (this.K2Score == ( (BNGraph) o).K2Score ? 0 : -1);
  }

  @Override
public Object clone()  {
    BNGraph gclone = new BNGraph(this.getVexNum());
    BNNode node;
    int i=0;
    int j=0;
    for(i=0;i<this.getVexNum();i++)
    {
      node = new BNNode(i);
      gclone.AddNode(node);
    }
    for(i=0;i<this.getVexNum();i++)
    {
      java.util.ArrayList<Integer> parent = this.GetNode(i).GetParentNodesIndex();
      for(j=0;j<parent.size();j++)
      {
        gclone.AddArc(parent.get(j),i);
      }
    }
    return gclone;
  }


  public BNGraph(int VexCount) {
    int i = 0;
    this.Vex = new java.util.HashMap<Integer, BNNode> ();
    for (i = 0; i < VexCount; i++) {
      BNNode n = new BNNode(i);
      this.Vex.put(i, n);
    }
  }

  @Override
public String toString() {
    java.lang.StringBuilder sb = new StringBuilder();
    int i = 0;
    int j = 0;
    int VexCount = this.Vex.size();
    BNNode node;
    sb.append("===================================\r\n");
    sb.append("The number of nodes£º" + VexCount);
    sb.append("\r\n");
    sb.append("The score of the graph£º" + this.K2Score + "\r\n");
   // sb.append("The results with best   is£º");
    int ArcCount = 0;
    for (i = 0; i < VexCount; i++) {
      node = this.Vex.get(i);
      sb.append("Node£º" + (node.GetNodeId()+1) );
      sb.append("\r\n");
      java.util.ArrayList<Integer> parentIndex = node.GetParentNodesIndex();
      sb.append("    Parent node sets£º" );
      for(j=0;j<parentIndex.size();j++)
      {
        sb.append( (parentIndex.get(j) + 1)  + " ");
      }
      ArcCount += parentIndex.size();

      sb.append("\r\n");
      java.util.ArrayList<Integer> childIndex = node.GetChildNodesIndex();
      sb.append("    Child node sets£º" );
      for(j=0;j<childIndex.size();j++)
      {
        sb.append( (childIndex.get(j) + 1) +  " ");
      }
      sb.append("\r\n");
    }
    sb.append("The number of edges£º" + ArcCount + "\r\n");
    sb.append("===================================\r\n");
    return sb.toString();
  }

  public String tograph() {
	    java.lang.StringBuilder sb = new StringBuilder();
	    int i = 0;
	    int j = 0;
	    int VexCount = this.Vex.size();
	    BNNode node;
	    sb.append("===================================\r\n");
	    sb.append("The number of nodes£º" + VexCount);
	    sb.append("\r\n");
	    sb.append("The Grapgh with best CTE score is£º"+ "\r\n");
	    //sb.append("The score of the graph£º" + this.K2Score + "\r\n");
	    int ArcCount = 0;
	    for (i = 0; i < VexCount; i++) {
	      node = this.Vex.get(i);
	      java.util.ArrayList<Integer> childIndex = node.GetChildNodesIndex();
	      for(j=0;j<childIndex.size();j++)
	      {
	        sb.append((node.GetNodeId()+1)+"->" + (childIndex.get(j) + 1) +  "\n");
	      }
	    }
	    sb.append("===================================\r\n");
	    return sb.toString();
	  }
  
  public String tograph1() {
	    java.lang.StringBuilder sb = new StringBuilder();
	    int i = 0;
	    int j = 0;
	    int VexCount = this.Vex.size();
	    BNNode node;
	    int ArcCount = 0;
	    for (i = 0; i < VexCount; i++) {
	      node = this.Vex.get(i);	 
	      java.util.ArrayList<Integer> childIndex = node.GetChildNodesIndex();
	      for(j=0;j<childIndex.size();j++)
	      {
	        sb.append((node.GetNodeId()+1)+"->" + (childIndex.get(j) + 1) +  "\n");
	      }
	    }
	    return sb.toString();
	  }

  
  /**
   * Add node
   *
   * @param node BNNode
   *
   */
  public void AddNode(BNNode node) {
    this.Vex.put(node.GetNodeId(), node);
  }

  /**
   * Get node
   *
   * @param nodeid int
   *
   */
  public BNNode GetNode(int nodeid) {
    BNNode node = null;
    node = this.Vex.get(nodeid);
    return node;
  }

  /**
   * add Node From Node To
   *
   * @param NodeFrom BNNode
   * @param NodeTo BNNode
   */
  public void AddArc(BNNode NodeFrom, BNNode NodeTo) {
    NodeFrom.AddChildNode(NodeTo);
    NodeTo.AddParentNode(NodeFrom);
  }

  public void delArc(BNNode NodeFrom, BNNode NodeTo) {
	    NodeFrom.RemoveChildNode(NodeTo);
	    NodeTo.RemoveParentNode(NodeFrom);
	  }
  
  /**
   * add arc
   *
   * @param ifrom int 
   * @param ito int 
   */
  public void AddArc(int ifrom, int ito) {

    BNNode NodeFrom = this.Vex.get(ifrom);
    BNNode NodeTo = this.Vex.get(ito);
    this.AddArc(NodeFrom, NodeTo);
  }
  
  public void delArc(int ifrom, int ito) {

	    BNNode NodeFrom = this.Vex.get(ifrom);
	    BNNode NodeTo = this.Vex.get(ito);
	    this.delArc(NodeFrom, NodeTo);
	  }
  
  public int[][] GetArcArray()
  {
    int VexNum = Vex.size();
    int[][] arc = new int[VexNum][VexNum];
    int ifrom = 0;
    int ito = 0;
    int i = 0,j=0;
    for(i=0;i<VexNum;i++)
    {
      for(j=0;j<VexNum;j++)
      {
        arc[i][j] = 0;
      }
    }
    for (ifrom = 0; ifrom < VexNum; ifrom++)
    {
      BNNode node = this.GetNode(ifrom);
      java.util.ArrayList<Integer> child = node.GetChildNodesIndex();
      for(i=0;i<child.size();i++)
      {
        ito = child.get(i);
        arc[ifrom][ito] = 1;
      }
    }
    return arc;
  }
  public String GetArcArrayString()
  {
    int [][] arr = this.GetArcArray();
    java.lang.StringBuilder sb = new StringBuilder();
    sb.append("\r\n\r\n");
    int size= arr.length;
    for(int i=0;i<size;i++)
    {
      for(int j=0;j<size;j++)      {
        sb.append(arr[i][j] + "  ");
      }
      sb.append("\r\n");
    }
    sb.append("\r\n\r\n");
    return sb.toString();
  }
  public int GetArcNumber()
  {
	int num=0;
    int VexNum = Vex.size();
    int[][] arc = new int[VexNum][VexNum];
    int ifrom = 0;
    int ito = 0;
    int i = 0,j=0;
    for(i=0;i<VexNum;i++)
    {
      for(j=0;j<VexNum;j++)
      {
        arc[i][j] = 0;
      }
    }
    for (ifrom = 0; ifrom < VexNum; ifrom++)
    {
      BNNode node = this.GetNode(ifrom);
      java.util.ArrayList<Integer> child = node.GetChildNodesIndex();
      for(i=0;i<child.size();i++)
      {
        ito = child.get(i);
        arc[ifrom][ito] = 1;
        num++;
      }
    }
    return num;
  }

  public static BNGraph GetGraphStandAlarm() { 
   BNGraph graph = BNGraph.LoadGraphFromFile("F:\\sim1s.txt",Scores.VEXNUM);
   return graph;

  }
  
  public static BNGraph GetGraph1() {
	    int VexNum = 5; 	
	   BNGraph g = BNGraph.LoadGraphFromFile("F:\\sim1sq1.txt",VexNum);     
	    return g;
	  }
  
  public static BNGraph GetGraph2() {
	    int VexNum = 5; 
	    
	   BNGraph g = BNGraph.LoadGraphFromFile("F:\\sim1sq2.txt",VexNum);  
	    
	    return g;
	  }
  
  public static BNGraph GetGraph(BNGraph a) {
	    return a;
	  }
  

  public static BNGraph GetGraphStandAlarm_mlwong()
 {
     BNGraph graph = BNGraph.LoadGraphFromFile("stand_alarm_hk_mlwong.txt",Scores.VEXNUM);
     return graph;
 }

  public static BNGraph GetGraphBestAlarm()
 {
     BNGraph graph = BNGraph.LoadGraphFromFile("best_alarm_graph_byk2.txt",Scores.VEXNUM);
     return graph;
 }
   

  public static BNGraph GetGraphStandInsurance()
  {
      BNGraph graph = BNGraph.LoadGraphFromFile("D:\\data\\Insurance_graph.txt",Scores.VEXNUM);
      return graph;
  }


  public static BNGraph GetGraphStandAsia()
    {
        BNGraph graph = BNGraph.LoadGraphFromFile("D:\\data\\stand_asia_graph.txt",Scores.VEXNUM);
        return graph;
  }

  public static BNGraph GetGraphStandHailFinder()
    {
        BNGraph graph = BNGraph.LoadGraphFromFile("HailFinder_graph.txt",Scores.VEXNUM);
        return graph;
  }


  public static BNGraph GetGraphStandChild()
    {
        BNGraph graph = BNGraph.LoadGraphFromFile("D:\\data\\stand_child_graph.txt",Scores.VEXNUM);
        return graph;
  }

  public static BNGraph GetGraphStandBarley()
   {
       BNGraph graph = BNGraph.LoadGraphFromFile("stand_barley_graph.txt",Scores.VEXNUM);
       return graph;
 }



public static BNGraph LoadGraphFromFile(String strFileName,int VexNum)
  {
    int [][] arr = new int[VexNum][VexNum];
    int row = 0;
    int col =0;
    try
     {
       BufferedReader br = new BufferedReader(new FileReader(strFileName));
       String line = br.readLine();
       while(line != null && row < VexNum)
       {
         String[] arrline = line.split(",");
         for(col=0;col<VexNum;col++)
         {
           arr[row][col] =Integer.parseInt(arrline[col]);
         }
         line = br.readLine();
         row++;
       }
       br.close();
     }
     catch (Exception ex)
     {
       ex.printStackTrace();
     }


    BNGraph g = new BNGraph(VexNum);

    int ifrom = 0;
    int ito = 0;
    for (ifrom = 0; ifrom < VexNum; ifrom++) {
      for (ito = 0; ito < VexNum; ito++) {
        if (arr[ifrom][ito] != 0) {
          g.AddArc(ifrom, ito);
        }
      }
    }

    return g;

  }

public static String CompareGraph(BNGraph g1,BNGraph g2, int edge)
{
  int [][] arr1 = g1.GetArcArray();
  int [][] arr2 = g2.GetArcArray();
  int VexNum = g1.getVexNum();
  java.lang.StringBuilder sb = new StringBuilder();
  java.lang.StringBuilder sbAdd = new StringBuilder();
  java.lang.StringBuilder sbDel = new StringBuilder();
  //
  int fals=0;
  double pre=0.00;
  double rec=0.00;
  double f1=0.00;
  
  
  //
  sb.append("g1score£º" + g1.K2Score + "   g2score:" + g2.K2Score + "\r\n");
  for(int i=0;i<VexNum;i++)
  {
    for(int j=0;j<VexNum;j++)
    {
        if (arr1[i][j] != arr2[i][j])
        {
            if (arr1[i][j] == 0 )
            {

                if(arr1[j][i] == arr2[i][j])
                {
                    if(i <= j)
                    {
                        sb.append("reverse£º" + (i + 1) + " -> " + (j + 1) + "\r\n");
                    }
                }
                else
                {
                    sbAdd.append("add£º" + (i + 1) + " -> " + (j + 1) + "\r\n");
                }
            }
            else
            {
                if(arr1[i][j] == arr2[j][i])
                {
                    if(i<j)
                    {
                        sb.append("reverse£º" + (i + 1) + " -> " + (j + 1) + "\r\n");
                        fals++;
                    }
                }
                else
                {
                    sbDel.append("delete£º" + (i + 1) + " -> " + (j + 1) + "\r\n");
                }
            }

        }
    }
  }
  pre=(edge-fals)/(double)edge ;
  rec=(edge-fals)/(double)edge ;
  f1=2*(pre+rec)/pre*rec ;
  System.out.println("Accuracy£º" + pre );
  return sb.toString() + sbAdd.toString() + sbDel.toString();
}


  public static String CompareGraph2(BNGraph g1,BNGraph g2)
    {
      int [][] arr1 = g1.GetArcArray();
      int [][] arr2 = g2.GetArcArray();
      int VexNum = g1.getVexNum();
      java.lang.StringBuilder sb = new StringBuilder();
      java.lang.StringBuilder sbAdd = new StringBuilder();
      java.lang.StringBuilder sbDel = new StringBuilder();
      sb.append(g2.K2Score + ",");
      int ireverse =0;
      int iadd =0;
      int idel =0;
      for(int i=0;i<VexNum;i++)
      {
        for(int j=0;j<VexNum;j++)
        {
            if (arr1[i][j] != arr2[i][j])
            {
                if (arr1[i][j] == 0 )
                {
                    if(arr1[j][i] == arr2[i][j])
                    {
                        if(i <= j)
                        {
                            ireverse ++;
                        }
                    }
                    else
                    {
                        iadd++;
                    }
                }
                else
                {
                    if(arr1[i][j] == arr2[j][i])
                    {
                        if(i<j)
                        {
                            ireverse++;
                        }                    }
                    else
                    {
                        idel++;
                    }
                }

            }
        }
      }

      sb.append(iadd + "," );
      sb.append(idel + "," + ireverse +  "");
      return sb.toString() ;
  }

  public double getScore()
  {
	  return this.K2Score;
  }

  public int getVexNum() {
    return this.Vex.size();
  }


  public void DelArc(BNNode NodeFrom, BNNode NodeTo) {
    NodeFrom.RemoveChildNode(NodeTo);
    NodeTo.RemoveParentNode(NodeFrom);
  }

  public void DelArc(int iFrom, int iTo) {
    BNNode NodeFrom = this.GetNode(iFrom);
    BNNode NodeTo = this.GetNode(iTo);
    NodeFrom.RemoveChildNode(NodeTo);
    NodeTo.RemoveParentNode(NodeFrom);
  }



  public String toDrawString()
  {
    java.lang.StringBuilder sb = new StringBuilder();
    int i=0;
    int j=0;
    int VexCount = this.Vex.size();
    BNNode node = null;
    System.err.println("todrawstring:" + VexCount);
    sb.append("digraph G {\r\n");
    for (i = 0; i < VexCount; i++) {
      node = this.Vex.get(i);
      java.util.ArrayList<Integer> childIndex = node.GetChildNodesIndex();
      for(j=0;j<childIndex.size();j++)
      {
        //
        sb.append( (node.GetNodeId()+1)
                   + " -> "
                   + (childIndex.get(j) + 1)
                   + " ; \r\n"
           );
      }
    }
    sb.append("}\r\n");
    return sb.toString();
  }
  
  public static BNGraph GetStandSimNetwork(int fileindx,int vexnum) throws IOException{
	  BNGraph g = new BNGraph(vexnum);
	  String []row = new String[vexnum];
	  int[][] standNetwork = new int[vexnum][vexnum];
	  BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\g"+fileindx+".txt"));
	  String line = br.readLine();
	  int count = 0;
	  while(line!=null){
		  row = line.split(",");  
		  for(int i=0;i<vexnum;i++){
			  standNetwork[count][i]=new Integer(row[i]);
		  }
		  count++;
		  line = br.readLine();
	  }
	  br.close();
	  int ifrom = 0;
	  int ito = 0;
	  for (ifrom = 0; ifrom < vexnum; ifrom++) {
	    for (ito = 0; ito < vexnum; ito++) {
	      if (standNetwork[ifrom][ito] != 0) {
	        g.AddArc(ifrom, ito);
	      }
	    }
	  }
	  return g;
  


  }

}
