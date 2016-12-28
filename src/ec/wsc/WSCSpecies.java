package ec.wsc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Species;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.util.Parameter;
import ec.graph.Graph;
import ec.graph.GraphEdge;
import ec.graph.GraphNode;

public class WSCSpecies extends Species {

	private static final long serialVersionUID = 1L;

	@Override
	public Parameter defaultBase() {
		return new Parameter("wscspecies");
	}

	@Override
	public Individual newIndividual(EvolutionState state, int thread) {
	    WSCInitializer init = (WSCInitializer) state.initializer;

	    Graph graph = createNewGraph(state, init.startNode, init.endNode, init.relevant);
	    // Turn graph to tree, and return that tree. End node will be the root
	    TreeNode treeRoot = graph.toTree("end");
	    TreeIndividual tree = (TreeIndividual) super.newIndividual(state, thread);

	    tree.setRoot(treeRoot);
	    return tree;
	}

	public Graph createNewGraph(EvolutionState state, Service start, Service end, Set<Service> relevant) {
		GraphNode startNode = new GraphNode(start);
		GraphNode endNode = new GraphNode(end);

		WSCInitializer init = (WSCInitializer) state.initializer;

		Graph newGraph = new Graph();

		Set<String> currentEndInputs = new HashSet<String>();
		Map<String,GraphEdge> connections = new HashMap<String,GraphEdge>();

		// Connect start node
		connectCandidateToGraphByInputs(startNode, connections, newGraph, currentEndInputs, init);

		Set<Service> seenNodes = new HashSet<Service>();
		List<Service> candidateList = new ArrayList<Service>();

		addToCandidateList(start, seenNodes, relevant, candidateList, init);

		Collections.shuffle(candidateList, init.random);

		finishConstructingGraph(currentEndInputs, endNode, candidateList, connections, init, newGraph, seenNodes, relevant);

		return newGraph;
	}

	public void finishConstructingGraph(Set<String> currentEndInputs, GraphNode end, List<Service> candidateList, Map<String,GraphEdge> connections,
	        WSCInitializer init, Graph newGraph, Set<Service> seenNodes, Set<Service> relevant) {

		// While end cannot be connected to graph
		while(!checkCandidateNodeSatisfied(init, connections, newGraph, end, end.getInputs(), null)){
			connections.clear();

            // Select node
            int index;

            candidateLoop:
            for (index = 0; index < candidateList.size(); index++) {
                Service candidate = candidateList.get(index);
                // For all of the candidate inputs, check that there is a service already in the graph
                // that can satisfy it

                GraphNode candNode = new GraphNode(candidate);
                if (!checkCandidateNodeSatisfied(init, connections, newGraph, candNode, candidate.getInputs(), null)) {
                    connections.clear();
                	continue candidateLoop;
                }

                // Connect candidate to graph, adding its reachable services to the candidate list
                connectCandidateToGraphByInputs(candNode, connections, newGraph, currentEndInputs, init);
                connections.clear();

                addToCandidateList(candidate, seenNodes, relevant, candidateList, init);

                break;
            }

            candidateList.remove(index);
            Collections.shuffle(candidateList, init.random);
        }

        connectCandidateToGraphByInputs(end, connections, newGraph, currentEndInputs, init);
        connections.clear();
        init.removeDanglingNodes(newGraph, end);
	}

	private boolean checkCandidateNodeSatisfied(WSCInitializer init,
			Map<String, GraphEdge> connections, Graph newGraph,
			GraphNode candidate, Set<String> candInputs, Set<GraphNode> fromNodes) {

		Set<String> candidateInputs = new HashSet<String>(candInputs);
		Set<String> startIntersect = new HashSet<String>();

		// Check if the start node should be considered
		GraphNode start = newGraph.nodeMap.get("start");

		if (fromNodes == null || fromNodes.contains(start)) {
    		for(String output : start.getOutputs()) {
    			Set<String> inputVals = init.taxonomyMap.get(output).servicesWithInput.get(candidate.getService());
    			if (inputVals != null) {
    				candidateInputs.removeAll(inputVals);
    				startIntersect.addAll(inputVals);
    			}
    		}

    		if (!startIntersect.isEmpty()) {
    			GraphEdge startEdge = new GraphEdge(startIntersect);
    			startEdge.setFromNode(start);
    			startEdge.setToNode(candidate);
    			connections.put(start.getName(), startEdge);
    		}
		}


		for (String input : candidateInputs) {
			boolean found = false;
			for (Service s : init.taxonomyMap.get(input).servicesWithOutput) {
			    if (fromNodes == null || fromNodes.contains(s)) {
    				if (newGraph.nodeMap.containsKey(s.getName())) {
    					Set<String> intersect = new HashSet<String>();
    					intersect.add(input);

    					GraphEdge mapEdge = connections.get(s.getName());
    					if (mapEdge == null) {
    						GraphEdge e = new GraphEdge(intersect);
    						e.setFromNode(newGraph.nodeMap.get(s.getName()));
    						e.setToNode(candidate);
    						connections.put(e.getFromNode().getName(), e);
    					} else
    						mapEdge.getIntersect().addAll(intersect);

    					found = true;
    					break;
    				}
			    }
			}
			// If that input cannot be satisfied, move on to another candidate
			// node to connect
			if (!found) {
				// Move on to another candidate
				return false;
			}
		}
		return true;
	}

	public void connectCandidateToGraphByInputs(GraphNode candidate, Map<String,GraphEdge> connections, Graph graph, Set<String> currentEndInputs, WSCInitializer init) {
		graph.nodeMap.put(candidate.getName(), candidate);
		graph.edgeList.addAll(connections.values());
		candidate.getIncomingEdgeList().addAll(connections.values());

		for (GraphEdge e : connections.values()) {
			GraphNode fromNode = graph.nodeMap.get(e.getFromNode().getName());
			fromNode.getOutgoingEdgeList().add(e);
		}
		for (String o : candidate.getOutputs()) {
			currentEndInputs.addAll(init.taxonomyMap.get(o).endNodeInputs);
		}
	}

	public void addToCandidateList(Service n, Set<Service> seenNode, Set<Service> relevant, List<Service> candidateList, WSCInitializer init) {
		seenNode.add(n);
		List<TaxonomyNode> taxonomyOutputs;
		if (n.getName().equals("start")) {
			taxonomyOutputs = new ArrayList<TaxonomyNode>();
			for (String outputVal : n.getOutputs()) {
				taxonomyOutputs.add(init.taxonomyMap.get(outputVal));
			}
		}
		else
			taxonomyOutputs = init.serviceMap.get(n.getName()).getTaxonomyOutputs();

		for (TaxonomyNode t : taxonomyOutputs) {
			// Add servicesWithInput from taxonomy node as potential candidates to be connected
			for (Service current : t.servicesWithInput.keySet()) {
				if (!seenNode.contains(current) && relevant.contains(current)) {
					candidateList.add(current);
					seenNode.add(current);
				}
			}
		}
	}

	public int checkSatisfiedByInputs(WSCInitializer init, Set<Service> services, Set<String> inputs, List<Service> satisfiedByStart) {
		for (Service s : services) {
			if (init.isSubsumed(s.getInputs(), inputs))
				satisfiedByStart.add(s);
		}
		return satisfiedByStart.size();
	}

//	public Set<Service> findPredecessors(WSCInitializer init, Set<String> inputs, Service s) {
//		Set<Service> predecessors = new HashSet<Service>();
//
//		// Get only inputs that are not subsumed by the given composition inputs
//		Set<String> inputsNotSatisfied = init.getInputsNotSubsumed(s.getInputs(), inputs);
//		Set<String> inputsToSatisfy = new HashSet<String>(inputsNotSatisfied);
//
//		// Find services to satisfy all inputs
//		for (String i : inputsNotSatisfied) {
//			if (inputsToSatisfy.contains(i)) {
//				List<Service> candidates = init.taxonomyMap.get(i).servicesWithOutput;
//				Collections.shuffle(candidates, init.random);
//
//				Service chosen = null;
//				candLoop:
//				for(Service cand : candidates) {
//					if (init.relevant.contains(cand) && cand.layer < s.layer) {
//						predecessors.add(cand);
//						chosen = cand;
//						break candLoop;
//					}
//				}
//
//				inputsToSatisfy.remove(i);
//
//				// Check if other outputs can also be fulfilled by the chosen candidate, and remove them also
//				Set<String> subsumed = init.getInputsSubsumed(inputsToSatisfy, chosen.outputs);
//				inputsToSatisfy.removeAll(subsumed);
//			}
//		}
//		return predecessors;
//	}

}