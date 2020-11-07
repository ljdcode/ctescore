package bjut.ai.bn;

import java.util.Collections;

import bjut.ai.bn.score.Score;
public class BNNode implements Cloneable {
  private int NodeId = 0;
  private java.util.HashMap<Integer, BNNode> NodeParent = null;
  private java.util.HashMap<Integer, BNNode> NodeChild = null;
  private boolean IsVisited = false;

  private java.util.HashMap<Integer, BNNode> NodeFP = null;

  public double K2Score = 0;
  public BNNode(int id) {
    this.NodeId = id;
    this.NodeParent = new java.util.HashMap<Integer, BNNode> ();
    this.NodeChild = new java.util.HashMap<Integer, BNNode> ();
    this.NodeFP = new java.util.HashMap<Integer, BNNode> ();
  }


  @Override
public Object clone()
{

    BNNode nclone =null;
    try
    {
      nclone = (BNNode)super.clone();
      nclone.NodeParent = (java.util.HashMap<Integer, BNNode>)this.NodeParent.
          clone();
      nclone.NodeChild = (java.util.HashMap<Integer, BNNode>)this.NodeChild.
          clone();
      nclone.NodeFP = (java.util.HashMap<Integer, BNNode>)this.NodeFP.clone();
    }
    catch(Exception ex)
    {}
  return nclone;
}


  /**
   * get node id
   *
   * @return int
   */
  public int GetNodeId() {
    return this.NodeId;
  }

  /**
   * get parent node id
   *
   * @return java.util.ArrayList<Integer>
   */
  public java.util.ArrayList<Integer> GetParentNodesIndex() {
    BNNode node = null;
    java.util.ArrayList<Integer> arrParentNodeIndex = new java.util.ArrayList<
        Integer> ();
    java.util.Iterator<Integer> iter = this.NodeParent.keySet().iterator();
    while (iter.hasNext()) {
      node = this.NodeParent.get(iter.next());
      arrParentNodeIndex.add(node.GetNodeId());
    }
    Collections.sort(arrParentNodeIndex);
    return arrParentNodeIndex;
  }

  /**
   * get parent set
   *
   * @return java.util.ArrayList<BNNode>
   */
  public java.util.ArrayList<BNNode> GetParentNodes() {
    BNNode node = null;
    java.util.ArrayList<BNNode> arrParentNode = new java.util.ArrayList<BNNode> ();
    java.util.Iterator<Integer> iter = this.NodeParent.keySet().iterator();
    while (iter.hasNext()) {
      node = this.NodeParent.get(iter.next());
      arrParentNode.add(node);
    }

    return arrParentNode;
  }

  /**
   * get ancestor node
   *
   * @return java.util.ArrayList<Integer>
   */
  public java.util.ArrayList<Integer> GetAncestorNodesIndex() {
    BNNode node = null;
    java.util.ArrayList<Integer> arrAncestorNodeIndex = new java.util.ArrayList<
        Integer> ();

    java.util.HashMap<Integer,
        BNNode> nodeAncestor = new java.util.HashMap<Integer, BNNode> ();
    nodeAncestor.put(this.NodeId, this);
    this.AddAncestorNodetoHashMap(this, nodeAncestor);

    java.util.Iterator<Integer> iter = nodeAncestor.keySet().iterator();
    while (iter.hasNext()) {
      node = nodeAncestor.get(iter.next());
      arrAncestorNodeIndex.add(node.GetNodeId());
    }
    return arrAncestorNodeIndex;
  }

  private void AddAncestorNodetoHashMap(BNNode node, java.util.HashMap<Integer,
                                        BNNode> nodeAncestor) {
    java.util.ArrayList<BNNode> parent = node.GetParentNodes();
    for (int i = 0; i < parent.size(); i++) {
      BNNode parentNode = parent.get(i);
      int parentNodeId = parentNode.GetNodeId();
      if (!nodeAncestor.containsKey(parentNodeId)) {
        if (!nodeAncestor.containsKey(parentNodeId)) {
          nodeAncestor.put(parentNodeId, parentNode);
        }
        AddAncestorNodetoHashMap(parentNode, nodeAncestor);
      }
    }
  }


  public String GetParentNodeString() {

    java.lang.StringBuilder sb = new StringBuilder();
    BNNode node;
    java.util.Iterator<Integer> iter = this.NodeParent.keySet().iterator();
    while (iter.hasNext()) {
      node = this.NodeParent.get(iter.next());
      sb.append(node.toString());
      sb.append(" ");
    }
    return sb.toString();
  }


  public java.util.ArrayList<Integer> GetChildNodesIndex() {
    BNNode node = null;
    java.util.ArrayList<Integer> arrChildNodeIndex = new java.util.ArrayList<
        Integer> ();

    java.util.Iterator<Integer> iter = this.NodeChild.keySet().iterator();
    while (iter.hasNext()) {
      node = this.NodeChild.get(iter.next());
      arrChildNodeIndex.add(node.GetNodeId());
    }

    return arrChildNodeIndex;
  }


  public java.util.ArrayList<BNNode> GetChildNodes() {
    BNNode node = null;
    java.util.ArrayList<BNNode> arrChildNode = new java.util.ArrayList<BNNode> ();
    java.util.Iterator<Integer> iter = this.NodeChild.keySet().iterator();
    while (iter.hasNext()) {
      node = this.NodeChild.get(iter.next());
      arrChildNode.add(node);
    }

    return arrChildNode;
  }


  public java.util.ArrayList<Integer> GetDescendantNodesIndex() {
    BNNode node = null;
    java.util.ArrayList<Integer> arrChildNodeIndex = new java.util.ArrayList<
        Integer> ();

    java.util.HashMap<Integer,
        BNNode> nodeDescendant = new java.util.HashMap<Integer, BNNode> ();
    nodeDescendant.put(this.NodeId, this);
    this.AddDescendantNodetoHashMap(this, nodeDescendant);

    java.util.Iterator<Integer> iter = nodeDescendant.keySet().iterator();
    while (iter.hasNext()) {
      node = nodeDescendant.get(iter.next());
      arrChildNodeIndex.add(node.GetNodeId());
    }
    return arrChildNodeIndex;
  }

  private void AddDescendantNodetoHashMap(BNNode node,
                                          java.util.HashMap<Integer, BNNode>
      nodeDescendant) {
    java.util.ArrayList<BNNode> child = node.GetChildNodes();
    for (int i = 0; i < child.size(); i++) {
      BNNode childNode = child.get(i);
      int childNodeId = childNode.GetNodeId();
      if (!nodeDescendant.containsKey(childNodeId)) {
        nodeDescendant.put(childNodeId, childNode);
        AddDescendantNodetoHashMap(childNode, nodeDescendant);
      }
    }
  }

 
  public String GetChildNodeString() {

    java.lang.StringBuilder sb = new StringBuilder();
    BNNode node;
    java.util.Iterator<Integer> iter = this.NodeChild.keySet().iterator();
    while (iter.hasNext()) {
      node = this.NodeChild.get(iter.next());
      sb.append(node.toString());
      sb.append(" ");
    }
    return sb.toString();
  }

  /**
   * AddParentNode
   *
   * @param p BNNode
   */
  public void AddParentNode(BNNode parent) {
    if (!this.NodeParent.containsKey(parent.GetNodeId())) {
      this.NodeParent.put(parent.GetNodeId(), parent);
    }
  }

  public void RemoveParentNode(BNNode parent) {
    if (this.NodeParent.containsKey(parent.NodeId)) {
      this.NodeParent.remove(parent.NodeId);
    }
  }

  /**
   * AddForbiddenParent
   *
   * @param p BNNode
   */
  public void AddForbiddenParent(BNNode node) {
    if (!this.NodeFP.containsKey(node.GetNodeId())) {
      this.NodeFP.put(node.GetNodeId(), node);
    }
  }

  public java.util.ArrayList<Integer> getForbiddenParent() {
    BNNode node = null;
    java.util.ArrayList<Integer> arrFB = new java.util.ArrayList<Integer> ();
    java.util.Iterator<Integer> iter = this.NodeFP.keySet().iterator();
    while (iter.hasNext()) {
      node = this.NodeFP.get(iter.next());
      arrFB.add(node.GetNodeId());
    }

    return arrFB;
  }

  /**
   * AddChildNode
   *
   * @param c BNNode
   */
  public void AddChildNode(BNNode child) {
    if (!this.NodeChild.containsKey(child.GetNodeId())) {
      this.NodeChild.put(child.GetNodeId(), child);
    }
  }

  public void RemoveChildNode(BNNode child) {
    if (this.NodeChild.containsKey(child.NodeId)) {
      this.NodeChild.remove(child.NodeId);
    }
  }

  @Override
public String toString() {
    return Integer.toString(this.NodeId);
  }

  public void SetVisited(boolean visited) {
    this.IsVisited = visited;
  }

  public boolean GetVisited() {
    return this.IsVisited;
  }

  public void calcK2Score(Score s) {
    this.K2Score = s.calcScore(this.NodeId,this.GetParentNodesIndex());
  }

  public double GetK2Score() {
    return this.K2Score;
  }
}
