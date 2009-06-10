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
