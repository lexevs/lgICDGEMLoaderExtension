package org.lexgrid.extension.loaders.icdgem.file;

import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;
import org.lexgrid.extension.loaders.icdgem.utils.ICDConceptFactory;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

public class GemFileEntry implements Comparable<GemFileEntry>{
	public static final int ON = 1;
	public static final int OFF = 0;
	
	private BaseConcept _sourceCon;
	private BaseConcept _targetCon;
	private int _approximateFlag;
	private int _noMapFlag;
	private int _combinationFlag;
	private int _scenario;
	private int _choiceList;
	
	/*
	 * [sourceCode]  [targetCode]  [approximateFlag noMapFlag combinationFlag scenario chocieList]
	 * 80603  S12000A  10111
	 */
	public GemFileEntry(String line, ICDGEMProperties props) {
		String sCode = null;
		String tCode = null;
		int sCodeType = props.getSrcConType();
		int tCodeType = props.getTgtConType();
		final char SPACE = ' ';
		int start = 0;
		int end = line.indexOf(SPACE);
		sCode = line.substring(start, end);
		_sourceCon = ICDConceptFactory.createConcept(sCodeType, sCode, props);
		
		start = getIndexOfFirstNonSpaceChar(end, line);
		end = line.indexOf(SPACE, start);
		tCode = line.substring(start, end);
		_targetCon = ICDConceptFactory.createConcept(tCodeType, tCode, props);
		
		start = getIndexOfFirstNonSpaceChar(end, line);
		_approximateFlag = getIntegerFromStringAtIndex(line, start);
		
		++start;
		_noMapFlag = getIntegerFromStringAtIndex(line, start); 
		
		++start;
		_combinationFlag = getIntegerFromStringAtIndex(line, start); 

		++start;
		_scenario = getIntegerFromStringAtIndex(line, start);
		
		++start;
		_choiceList = getIntegerFromStringAtIndex(line, start); 
		
		
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("source: ");
		buf.append(_sourceCon.getCode());
		buf.append(" target: ");
		buf.append(_targetCon.getCode());
		buf.append(" aF: ");
		buf.append(_approximateFlag);
		buf.append(" nmF: ");
		buf.append(_noMapFlag);
		buf.append(" cF: ");
		buf.append(_combinationFlag);
		buf.append(" s: ");
		buf.append(_scenario);
		buf.append(" cl: ");
		buf.append(_choiceList);
		return buf.toString();
	}
	
	public BaseConcept getSourceConcept() {
		return _sourceCon;
	}
	
	public BaseConcept getTargetConcept() {
		return _targetCon;
	}
	
	public String getSourceCode() {
		return _sourceCon.getCode();
	}
	
	public String getTargetCode() {
		return _targetCon.getCode();
	}
	
	public String getSourceCodingScheme() {
		return _sourceCon.getSourceCodingScheme();
	}
	
	public String getTargetCodingScheme() {
		return _targetCon.getSourceCodingScheme();
	}
	
	public int getApproximateFlag() {
		return _approximateFlag;
	}
	
	public int getNoMapFlag() {
		return _noMapFlag;
	}
	
	public int getCombinationFlag() {
		return _combinationFlag;
	}
	
	public boolean isComplex() {
		boolean rv = false;
		if(getCombinationFlag() == 1) {
			rv = true;
		}
		return rv;
	}
	
	public int getScenario() {
		return _scenario;
	}
	
	public int getChoiceList() {
		return _choiceList;
	}
	
	private int getIntegerFromStringAtIndex(String myString, int index) {
		StringBuffer buf = new StringBuffer(1);
		buf.append(myString.charAt(index));
		int rv = Integer.parseInt(buf.toString());
		return rv;
	}
	
	private int getIndexOfFirstNonSpaceChar(int startLookingAt, String myString) {
		final char SPACE = ' ';
		boolean done = false;
		int index = startLookingAt;
		while(!done) {
			char temp = myString.charAt(index);
			if(temp == SPACE) {
				++index;
			} else {
				done = true;
			}
		}
		return index;
	}
	
	public static void main(String args[]) {
		ICDGEMProperties props = new ICDGEMProperties(ICDGEMConstants.ICD10_TO_9_CM_DESC, "1.01", null);
		GemFileEntry gfe1 = new GemFileEntry("81603 S12301A 10243", props);
		System.out.println("gfe1: " + gfe1.toString());
	}

	public int compareTo(GemFileEntry gfe) {
		int rv = 0;
		if(gfe._scenario == this._scenario) {
			if(gfe._choiceList == this._choiceList) {
				rv = 0;
			} else if(gfe._choiceList < this._choiceList) {
				rv = 1;
			} else {
				rv = -1;
			}
		} else if (gfe._scenario < this._scenario) {
			rv = 1;
		} else {
			rv = -1;
		}
		return rv;
	}

}
