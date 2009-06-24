package org.lexgrid.extension.loaders.icdgem.utils;

public class RootConcept extends BaseConcept {
	
	public RootConcept(ICDGEMProperties props) {
        super.init("@", "top thing", props.getCsLocalName());
	}
	
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Code: ");
    	sb.append(super.getCode());
    	sb.append(" ");
    	sb.append("Type: ");
    	sb.append("root");
        return sb.toString();
    }
}
