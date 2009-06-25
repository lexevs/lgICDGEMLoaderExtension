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
