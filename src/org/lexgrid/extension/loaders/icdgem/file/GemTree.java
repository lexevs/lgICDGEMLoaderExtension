/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.lexgrid.extension.loaders.icdgem.file;

import java.util.ArrayList;

import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

public class GemTree {
	private GemTreeNode _root;
	private BaseConcept _sourceCon;
	
	public GemTree(GemTreeNode root, BaseConcept sourceCon) {
		_root = root;
		_sourceCon = sourceCon;
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
	
	public BaseConcept getSourceConcept() {
		return _sourceCon;
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
	
	public ArrayList<GemComboEntry> getCombinations() {
		ArrayList<GemComboEntry> returnValue = new ArrayList<GemComboEntry>();
		GemTreeNode leaf;
		ArrayList<GemTreeNode> leafNodes = this.getLeafNodes();
		GemComboEntry gce = null;
		for(int i=0; i<leafNodes.size(); ++i) {
			leaf = leafNodes.get(i);
			gce = new GemComboEntry();
			getCombination(gce, leaf);
			returnValue.add(gce);
		}
		return returnValue;
	}
	
	private void getCombination(GemComboEntry gce, GemTreeNode node) {
		GemTreeNode parent = node.getParent();
		if(parent == null) {
			return;
		}
		gce.addPart(node.getValue());
		getCombination(gce, parent);
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
		GemTree gtree = new GemTree(root, gfe1.getSourceConcept());
		
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
		
		ArrayList<GemComboEntry> combos = gtree.getCombinations();
		System.out.println("Combinations for " + root.getValue() + ": ");
		for(int i=0; i<combos.size(); ++i) {
			GemComboEntry temp = combos.get(i);
			System.out.println("  [" + i + "] " + temp);
		}
	}
	
	public static void main(String[] args) {
		GemTree.test1();
	}
}
