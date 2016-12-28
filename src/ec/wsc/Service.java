package ec.wsc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ec.wsc.TaxonomyNode;

public class Service implements Cloneable {
	private List<TaxonomyNode> taxonomyOutputs = new ArrayList<TaxonomyNode>();
	private String name;
	private double[] qos;
	private Set<String> inputs;
	private Set<String> outputs;

	public Service(String name, double[] qos, Set<String> inputs, Set<String> outputs) {
		this.name = name;
		this.qos = qos;
		this.inputs = inputs;
		this.outputs = outputs;
	}

	public double[] getQos() {
		return qos;
	}

	public Set<String> getInputs() {
		return inputs;
	}

	public Set<String> getOutputs() {
		return outputs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TaxonomyNode> getTaxonomyOutputs() {
		return taxonomyOutputs;
	}

	public Service clone() {
		return new Service(name, qos, inputs, outputs);
	}

	@Override
	public String toString(){
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Service) {
			Service o = (Service) other;
			return name.equals(o.name);
		}
		else
			return false;
	}
}
