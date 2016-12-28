package ec.wsc;

import java.util.Collections;
import java.util.List;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPNode;
import ec.util.Parameter;

public class WSCCrossoverPipeline extends BreedingPipeline {

	private static final long serialVersionUID = 1L;

	@Override
	public Parameter defaultBase() {
		return new Parameter("wsccrossoverpipeline");
	}

	@Override
	public int numSources() {
		return 2;
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {
		WSCInitializer init = (WSCInitializer) state.initializer;

		Individual[] inds1 = new Individual[inds.length];
		Individual[] inds2 = new Individual[inds.length];

		int n1 = sources[0].produce(min, max, 0, subpopulation, inds1, state, thread);
		int n2 = sources[1].produce(min, max, 0, subpopulation, inds2, state, thread);

        if (!(sources[0] instanceof BreedingPipeline)) {
            for(int q=0;q<n1;q++)
                inds1[q] = (Individual)(inds1[q].clone());
        }

        if (!(sources[1] instanceof BreedingPipeline)) {
            for(int q=0;q<n2;q++)
                inds2[q] = (Individual)(inds2[q].clone());
        }

        if (!(inds1[0] instanceof TreeIndividual))
            // uh oh, wrong kind of individual
            state.output.fatal("WSCCrossoverPipeline didn't get a TreeIndividual. The offending individual is in subpopulation "
            + subpopulation + " and it's:" + inds1[0]);

        if (!(inds2[0] instanceof TreeIndividual))
            // uh oh, wrong kind of individual
            state.output.fatal("WSCCrossoverPipeline didn't get a TreeIndividual. The offending individual is in subpopulation "
            + subpopulation + " and it's:" + inds2[0]);

        int nMin = Math.min(n1, n2);

        // Perform crossover
        for(int q=start,x=0; q < nMin + start; q++,x++) {
    		TreeIndividual t1 = ((TreeIndividual)inds1[x]);
    		TreeIndividual t2 = ((TreeIndividual)inds2[x]);

    		// Find all nodes from both candidates
    		List<TreeNode> allT1Nodes = t1.getAllTreeNodes();
            List<TreeNode> allT2Nodes = t2.getAllTreeNodes();

            // Shuffle them so that the crossover is random
            Collections.shuffle( allT1Nodes, init.random );
            Collections.shuffle( allT2Nodes, init.random );

            // For each t1 node, see if it can be replaced by a t2 node
            TreeNode[] nodes = findReplacement(init, allT1Nodes, allT2Nodes);
            TreeNode nodeT1 = nodes[0];
            TreeNode nodeT2 = nodes[1];

            // Swap nodes in individuals
            swapNodes(nodeT1, nodeT2);

	        inds[q] = t1;
	        inds[q].evaluated=false;

	        if (q+1 < inds.length) {
	        	inds[q+1] = t2;
	        	inds[q+1].evaluated=false;
	        }
        }
        return n1;
	}

	public TreeNode[] findReplacement(WSCInitializer init, List<TreeNode> nodes, List<TreeNode> replacements) {
	    TreeNode[] result = new TreeNode[2];
	    outer:for (TreeNode node : nodes) {
	        for (TreeNode replacement : replacements) {
	            /* Check if the inputs of replacement are subsumed by the inputs of the
	             * node and the outputs of the node are subsumed by the outputs of the
	             * replacement. This will ensure that the replacement has equivalent
	             * functionality to the replacement.*/
	        	if (node.getService().getName().equals(replacement.getService().getName()) && !node.getService().getName().equals("end")) {
	        		result[0] = node;
	        		result[1] = replacement;
	        		break outer;
	        	}
	        }
	    }
	    return result;
	}

	public void swapNodes(TreeNode nodeT1, TreeNode nodeT2) {
		// Get parents
		TreeNode parentT1 = nodeT1.getParent();
		TreeNode parentT2 = nodeT2.getParent();

		// Disconnect child nodes from their parents
		parentT1.getChildren().remove(nodeT1);
		parentT2.getChildren().remove(nodeT2);

		// Connect child nodes to the other parents
		nodeT1.setParent(parentT2);
		nodeT2.setParent(parentT1);
		parentT2.getChildren().add(nodeT1);
		parentT1.getChildren().add(nodeT2);
	}
}
