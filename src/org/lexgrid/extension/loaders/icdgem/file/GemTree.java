package org.lexgrid.extension.loaders.icdgem.file;

import java.util.ArrayList;
import java.util.Stack;

import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

public class GemTree {
	private GemTreeNode _root;
	
	public GemTree(GemTreeNode root) {
		_root = root;
	}
	
	private ArrayList<GemTreeNode> getLeafNodes() {
		ArrayList<GemTreeNode> leafNodes = new ArrayList<GemTreeNode>();
		addLeafNode(_root, leafNodes);
		return leafNodes;
	}
	
	private void addLeafNode(GemTreeNode node, ArrayList<GemTreeNode> leafNodes) {
		if(node.getChildren() == null) {
			leafNodes.add(node);
		} else {
			ArrayList<GemTreeNode> childNodes = node.getChildren();
			for(int i=0; i<childNodes.size(); ++i) {
				GemTreeNode child = childNodes.get(i);
				addLeafNode(child, leafNodes);
			}
		}
	}
		
	public void addNewLevel(ArrayList<GemTreeNode> newLeafs) {
		ArrayList<GemTreeNode> curLeafNodes = this.getLeafNodes();
		ArrayList<GemTreeNode> newLeafNodes = new ArrayList<GemTreeNode>();
		GemTreeNode curLeaf;
		GemTreeNode newLeaf;
		for(int i=0; i<curLeafNodes.size(); ++i) {
			curLeaf = curLeafNodes.get(i);
			for(int j=0; j<newLeafs.size(); ++j) {
				newLeaf = new GemTreeNode(newLeafs.get(j), curLeaf);
				curLeaf.addChild(newLeaf);
				newLeafNodes.add(newLeaf);
			}			
		}
	}
	
	public ArrayList<String> getCombinations() {
		Stack<GemTreeNode> combination = new Stack<GemTreeNode>();
		ArrayList<String> returnValue = new ArrayList<String>();
		StringBuffer buf;
		GemTreeNode leaf;
		ArrayList<GemTreeNode> leafNodes = this.getLeafNodes();
		for(int i=0; i<leafNodes.size(); ++i) {
			leaf = leafNodes.get(i);
			combination.clear();
			buf = new StringBuffer();
			getCombination(combination, leaf);
			getTargetCodeValues(combination, buf);
			returnValue.add(buf.toString());
		}
		return returnValue;
	}
	
	private void getTargetCodeValues(Stack<GemTreeNode> stack, StringBuffer buf) {
		GemTreeNode node;
		boolean done = false;
		while(!done) {
			node = stack.pop();
			BaseConcept temp = node.getValue();
			buf.append(temp.getCode());
			if(stack.empty()) {
				done = true;
			} else {
				buf.append(" AND ");
			}
		}
	}
	
	private void getCombination(Stack<GemTreeNode> stack, GemTreeNode node) {
		GemTreeNode parent = node.getParent();
		if(parent == null) {
			return;
		}
		stack.push(node);
		getCombination(stack, parent);
	}
	
	public GemTreeNode getRootNode() {
		return _root;
	}
	
	public static void test1() {
		
		ICDGEMProperties props = new ICDGEMProperties(ICDGEMConstants.ICD10_TO_9_CM_DESC, "1.01", null);
		GemFileEntry gfe1 = new GemFileEntry("80606 S12400A 10111", props);
		GemFileEntry gfe2 = new GemFileEntry("80606 S12401A 10111", props);
		GemFileEntry gfe3 = new GemFileEntry("80606 S14115A 10112", props);
		
		GemTreeNode root = new GemTreeNode(gfe1.getSourceConcept());
		GemTree gtree = new GemTree(root);
		
		ArrayList<GemTreeNode> tgtCodes = new ArrayList<GemTreeNode>();
		GemTreeNode tempTgtCode = new GemTreeNode(gfe1.getTargetConcept());
		tgtCodes.add(tempTgtCode);
		tempTgtCode = new GemTreeNode(gfe2.getTargetConcept());
		tgtCodes.add(tempTgtCode);
		gtree.addNewLevel(tgtCodes);
		
		tgtCodes = new ArrayList<GemTreeNode>();
		tempTgtCode = new GemTreeNode(gfe3.getTargetConcept());
		tgtCodes.add(tempTgtCode);
		gtree.addNewLevel(tgtCodes);
		
		ArrayList<String> combos = gtree.getCombinations();
		System.out.println("Combinations for " + root.getValue() + ": ");
		for(int i=0; i<combos.size(); ++i) {
			String temp = combos.get(i);
			System.out.println("  [" + i + "] " + temp);
		}
	}
	
	public static void main(String[] args) {
		GemTree.test1();
	}
}