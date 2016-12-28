package ec.wsc;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	private TreeNode parent;
	private List<TreeNode> children = new ArrayList<TreeNode>();
	private Service service;

	public TreeNode(Service s){
		service = s;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode p) {
		parent = p;
	}

	public List<TreeNode> getChildren(){
		return children;
	}

	public void setChildren(List<TreeNode> c){
		children = c;
	}

	public Service getService(){
		return service;
	}

	public void setService(Service s){
		service = s;
	}
}
