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


public class GemComboEntry {
	private ArrayList<BaseConcept> _parts;
	
	public GemComboEntry() {
		_parts = new ArrayList<BaseConcept>();
	}
	
	public void addPart(BaseConcept part) {
		_parts.add(part);
	}
	
	public BaseConcept getPart(int i) {
		if(i < 0 || i >= _parts.size()) {
			return null;
		}
		return _parts.get(i);
	}
	
	public int getSize() {
		return _parts.size();
	}
	
	public String toString() {
		StringBuffer rv = new StringBuffer();
		BaseConcept part = null;
		for(int i=0; i<_parts.size(); ++i) {
			part = _parts.get(i);
			if(i== 0) {
				rv.append(part.getCode());
			} else {
				rv.append(" AND ");
				rv.append(part.getCode());
			}
		}
		return rv.toString();
	}

}
