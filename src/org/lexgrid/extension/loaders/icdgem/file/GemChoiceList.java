package org.lexgrid.extension.loaders.icdgem.file;

import java.util.ArrayList;

public class GemChoiceList {
	ArrayList<GemFileEntry> _choices;
	
	public GemChoiceList() {
		_choices = new ArrayList<GemFileEntry>();
	}
	
	public int getSize() {
		return _choices.size();
	}
	
	public GemFileEntry getMember(int i) {
		if(i < 0 || i >= _choices.size()) {
			return null;
		}
		return _choices.get(i);
	}
	
	public void addMember(GemFileEntry member) {
		_choices.add(member);
	}
}
