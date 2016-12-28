package ec.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ec.gp.GPNode;
import ec.wsc.TreeNode;
import ec.wsc.Service;
import ec.wsc.TaxonomyNode;

public class GraphNode implements Cloneable {
	private List<GraphEdge> incomingEdgeList = new ArrayList<GraphEdge>();
	private List<GraphEdge> outgoingEdgeList = new ArrayList<GraphEdge>();
	private List<TaxonomyNode> taxonomyOutputs = new ArrayList<TaxonomyNode>();
	private Service serv;

	public GraphNode(Service serv) {
		this.serv = serv;
	}

	public List<GraphEdge> getIncomingEdgeList() {
		return incomingEdgeList;
	}

	public List<GraphEdge> getOutgoingEdgeList() {
		return outgoingEdgeList;
	}

	public double[] getQos() {
		return serv.getQos();
	}

	public Set<String> getInputs() {
		return serv.getInputs();
	}

	public Set<String> getOutputs() {
		return serv.getOutputs();
	}

	public String getName() {
		return serv.getName();
	}

	public GraphNode clone() {
		return new GraphNode(serv);
	}

	public Service getService() {
		return serv;
	}

	public List<TaxonomyNode> getTaxonomyOutputs() {
		return taxonomyOutputs;
	}

	@Override
	public String toString(){
		return serv.getName();
	}

	@Override
	public int hashCode() {
		return serv.getName().hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GraphNode) {
			GraphNode o = (GraphNode) other;
			return serv.getName().equals(o.serv.getName());
		}
		else
			return false;
	}
}
