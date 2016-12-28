package ec.wsc;

import ec.util.*;

import java.util.HashSet;
import java.util.Set;

import ec.*;
import ec.gp.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;

public class WSC extends Problem implements SimpleProblemForm {

	private static final long serialVersionUID = 1L;

	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
		if (!ind.evaluated) {
			WSCInitializer init = (WSCInitializer) state.initializer;

			TreeIndividual treeInd = (TreeIndividual) ind;
			Set<String> seenServices = new HashSet<String>();

			double[] qos = new double[4];
			qos[WSCInitializer.TIME] = retrieveLongestTime(treeInd.getRoot(), seenServices);
			qos[WSCInitializer.AVAILABILITY] = 1.0;
			qos[WSCInitializer.RELIABILITY] = 1.0;

			for(String serviceName : seenServices) {
				Service s = init.serviceMap.get(serviceName);
				double[] servQos = s.getQos();
				qos[WSCInitializer.COST] += servQos[WSCInitializer.COST];
				qos[WSCInitializer.AVAILABILITY] *= servQos[WSCInitializer.AVAILABILITY];
				qos[WSCInitializer.RELIABILITY] *= servQos[WSCInitializer.RELIABILITY];
			}

			treeInd.setAvailability(qos[WSCInitializer.AVAILABILITY]);
			treeInd.setReliability(qos[WSCInitializer.RELIABILITY]);
			treeInd.setTime(qos[WSCInitializer.TIME]);
			treeInd.setCost(qos[WSCInitializer.COST]);
			double[] objectives = calculateFitness(qos[WSCInitializer.AVAILABILITY], qos[WSCInitializer.RELIABILITY], qos[WSCInitializer.TIME], qos[WSCInitializer.COST], init);

			// the fitness better be SimpleFitness!
			MultiObjectiveFitness f = ((MultiObjectiveFitness) ind.fitness);
			f.setObjectives(state, objectives);
			ind.evaluated = true;
		}
	}

	private double retrieveLongestTime(TreeNode current, Set<String> seenServices) {
		double longestTimeSoFar = 0.0;

		for (TreeNode child : current.getChildren()) {
			double t = retrieveLongestTime(child, seenServices);
			if (t > longestTimeSoFar)
				longestTimeSoFar = t;
		}
		String name = current.getService().getName();
		if(!name.equals("start") && !name.equals("end"))
			seenServices.add(name);
		return longestTimeSoFar + current.getService().getQos()[WSCInitializer.TIME];
	}

	private double[] calculateFitness(double a, double r, double t, double c, WSCInitializer init) {
		a = normaliseAvailability(a, init);
		r = normaliseReliability(r, init);
		t = normaliseTime(t, init);
		c = normaliseCost(c, init);

		double[] objectives = new double[2];
		//objectives[GraphInitializer.AVAILABILITY] = a;
        //objectives[GraphInitializer.RELIABILITY] = r;
        //objectives[WSCInitializer.TIME] = t;
        //objectives[WSCInitializer.COST] = c;
		objectives[0] = t + c;
		objectives[1] = a + r;

		return objectives;
	}

	private double normaliseAvailability(double availability, WSCInitializer init) {
		if (init.maxAvailability - init.minAvailability == 0.0)
			return 1.0;
		else
			//return (availability - init.minAvailability)/(init.maxAvailability - init.minAvailability);
			return (init.maxAvailability - availability)/(init.maxAvailability - init.minAvailability);
	}

	private double normaliseReliability(double reliability, WSCInitializer init) {
		if (init.maxReliability - init.minReliability == 0.0)
			return 1.0;
		else
			//return (reliability - init.minReliability)/(init.maxReliability - init.minReliability);
			return (init.maxReliability - reliability)/(init.maxReliability - init.minReliability);
	}

	private double normaliseTime(double time, WSCInitializer init) {
		// If the time happens to go beyond the normalisation bound, set it to the normalisation bound
		if (time > init.maxTime)
			time = init.maxTime;

		if (init.maxTime - init.minTime == 0.0)
			return 1.0;
		else
			//return (init.maxTime - time)/(init.maxTime - init.minTime);
			return (time - init.minTime)/(init.maxTime - init.minTime);
	}

	private double normaliseCost(double cost, WSCInitializer init) {
		// If the cost happens to go beyond the normalisation bound, set it to the normalisation bound
		if (cost > init.maxCost)
			cost = init.maxCost;

		if (init.maxCost - init.minCost == 0.0)
			return 1.0;
		else
			//return (init.maxCost - cost)/(init.maxCost - init.minCost);
			return (cost - init.minCost)/(init.maxCost - init.minCost);
	}
}