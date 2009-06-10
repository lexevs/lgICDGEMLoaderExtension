package org.lexgrid.extension.loaders.icdgem.file;

import java.util.ArrayList;

import org.lexgrid.extension.loaders.icdgem.file.GemFileEntry;

public class GemScenario {
	
	private ArrayList<GemFileEntry> _scenarioMembers;
	
	public GemScenario() {
		_scenarioMembers = new ArrayList<GemFileEntry>();
	}
	
	public void addMember(GemFileEntry member) {
		_scenarioMembers.add(member);
	}
	
	public int getSize() {
		return _scenarioMembers.size();
	}
	
	public GemFileEntry getMember(int index) {
		if(index < 0 || index >= _scenarioMembers.size() ) {
			return null;
		}
		return _scenarioMembers.get(index);
	}
	
	
	
}
