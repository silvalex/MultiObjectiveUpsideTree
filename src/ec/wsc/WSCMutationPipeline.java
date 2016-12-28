package ec.wsc;

import java.util.List;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import ec.graph.Graph;

public class WSCMutationPipeline extends BreedingPipeline {

	private static final long serialVersionUID = 1L;

	@Override
	public Parameter defaultBase() {
		return new Parameter("wscmutationpipeline");
	}

	@Override
	public int numSources() {
		return 1;
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {
		WSCInitializer init = (WSCInitializer) state.initializer;

		int n = sources[0].produce(min, max, start, subpopulation, inds, state, thread);

        if (!(sources[0] instanceof BreedingPipeline)) {
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());
        }

        if (!(inds[start] instanceof TreeIndividual))
            // uh oh, wrong kind of individual
            state.output.fatal("WSCMutationPipeline didn't get a TreeIndividual. The offending individual is in subpopulation "
            + subpopulation + " and it's:" + inds[start]);

        // Perform mutation
        for(int q=start;q<n+start;q++) {
            TreeIndividual tree = (TreeIndividual)inds[q];
            WSCSpecies species = (WSCSpecies) tree.species;

            // Randomly select a node in the tree to be mutation
            List<TreeNode> allNodes = tree.getAllTreeNodes();
            int selectedIndex = init.random.nextInt(allNodes.size());
            TreeNode selectedNode = allNodes.get( selectedIndex );

            while(selectedNode.getService().getName().equals("start") || selectedNode.getService().getName().equals("end")) {
                selectedIndex = init.random.nextInt(allNodes.size());
                selectedNode = allNodes.get( selectedIndex );
            }

            Service startNode = init.startNode;
            Service endNode = selectedNode.getService();

            // Create the replacement subtree
            Graph newGraph = species.createNewGraph(state, startNode, endNode, init.relevant);
            TreeNode newNode = newGraph.toTree(endNode.getName());

            // Replace the old tree with the new one
            tree.replaceNode( selectedNode, newNode );
            tree.evaluated=false;
        }
        return n;
	}

}
