package org.lexgrid.extension.loaders.icdgem.file;

import java.util.ArrayList;

import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;

public class GemTreeNode {
	private BaseConcept _value;
	private GemTreeNode _parent;
	private ArrayList<GemTreeNode> _children; 
	
	public GemTreeNode(BaseConcept value) {
		_value = value;
		_parent = null;
		_children = null;
	}
	
	public void setParent(GemTreeNode parent) {
		_parent = parent;
	}
	
	public GemTreeNode(GemTreeNode node, GemTreeNode parent) {
		_value = node.getValue();
		_parent = parent;
		_children = node.getChildren();
	}
	
	
	public void addChild(GemTreeNode child) {
		if(_children == null) {
			_children = new ArrayList<GemTreeNode>();
		}
		_children.add(child);
	}
	
	public BaseConcept getValue() {
		return _value;
	}
	
	public ArrayList<GemTreeNode> getChildren() {
		return _children;
	}
	
	public GemTreeNode getParent() {
		return _parent;
	}
	
}
