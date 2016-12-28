package ec.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ec.wsc.TreeNode;

public class Graph {
	public Map<String, GraphNode> nodeMap = new HashMap<String, GraphNode>();
	public List<GraphEdge> edgeList = new ArrayList<GraphEdge>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("digraph g {");
		for (GraphEdge e : edgeList) {
			builder.append(e.toString());
			builder.append("; ");
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Used in the toTree method.
	 *
	 * @author sawczualex
	 */
	public class NodePair {
		public GraphNode original;
		public TreeNode copy;

		public NodePair(GraphNode original, TreeNode copy) {
			this.original = original;
			this.copy = copy;
		}
	}

	public TreeNode toTree(String rootNodeName) {
		Queue<NodePair> queue = new LinkedList<NodePair>();
		GraphNode endNode = nodeMap.get(rootNodeName);
		// Create new root
		TreeNode root = new TreeNode(endNode.getService());

		// For each end graph node predecessor, create a corresponding tree node
		for(GraphEdge predEdge : endNode.getIncomingEdgeList()) {
			GraphNode pred = predEdge.getFromNode();
			TreeNode treePred = new TreeNode(pred.getService());
			// Connect tree node to tree
			root.getChildren().add(treePred);
			treePred.setParent(root);
			// Add child to queue
			queue.offer(new NodePair(pred, treePred));
		}

		while (!queue.isEmpty()) {
			NodePair pair = queue.poll();
			// For each end graph node predecessor, create a corresponding tree node
			for(GraphEdge predEdge : pair.original.getIncomingEdgeList()) {
				GraphNode pred = predEdge.getFromNode();
				TreeNode treePred = new TreeNode(pred.getService());
				// Connect tree node to tree
				pair.copy.getChildren().add(treePred);
				treePred.setParent(pair.copy);
				// Add child to queue
				queue.offer(new NodePair(pred, treePred));
			}
		}

		return root;
	}
}
