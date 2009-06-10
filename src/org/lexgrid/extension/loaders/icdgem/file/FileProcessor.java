package org.lexgrid.extension.loaders.icdgem.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.LexGrid.messaging.LgMessageDirectorIF;
import org.lexgrid.extension.loaders.icdgem.utils.Association;
import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;
import org.lexgrid.extension.loaders.icdgem.utils.CodingScheme;
import org.lexgrid.extension.loaders.icdgem.utils.ComplexConcept;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;
import org.lexgrid.extension.loaders.icdgem.utils.RootConcept;

public class FileProcessor {
	
	public static CodingScheme process(String fileName, ICDGEMProperties props) {
		LgMessageDirectorIF md = props.getMessageDirector();
		GemFileEntry gfe = null;
		ArrayList<GemFileEntry> subSet = new ArrayList<GemFileEntry>();
		RootConcept root = new RootConcept(props); 
		
		CodingScheme cs = new CodingScheme();
		cs.setCopyright(props.getCsCopyright());
		cs.setCsId(props.getCsUri());
		cs.setCsName(props.getCsLocalName());
		cs.setDefaultLanguage(props.getEnglishName());
		cs.setEntityDescription("??");
		cs.setFormalName(props.getCsUri());
		cs.setSource(props.getCsSource());
		cs.setRepresentsVersion("??");
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
			String line = null;
			boolean done = false;
			GemFileEntry prevGfe = null;
            while (!done) {
    			line = fileReader.readLine();
    			if(line == null || line.length() == 0) {
    				done = true;
    			} else {
                	line = line.trim();
                	gfe = new GemFileEntry(line, props);
                	if(prevGfe == null) { // first line in file
                		prevGfe = gfe;
                	} else {
                		String curSrcCon = gfe.getSourceCode();
                		String prevSrcCon = prevGfe.getSourceCode();
                		if(curSrcCon.equalsIgnoreCase(prevSrcCon) == true) { // still within a 'set'
                			subSet.add(prevGfe);
                			prevGfe = gfe;
                		} else { // we've entered a new set of codes
                			subSet.add(prevGfe);
                			processSubSet(cs, root, subSet, props);
                			// when done, clear subset
                			subSet.clear();
                			prevGfe = gfe;
                		}
                	}
    			}
            }
			
		} catch (FileNotFoundException e) {
			md.error("FileProcessor: process: " + e.getMessage());
		} catch (IOException e) {
			md.error("FileProcessor: process: " + e.getMessage());
		}
		
		
		
		
		return cs;
	}
	
	/* processSubSet will process a subset of mappings such as the following:
	 *  
	 * 80608 S12400A 10111
	 * 80608 S12401A 10111
	 * 80608 S14125A 10112
	 * 80608 S12500A 10121
	 * 80608 S12501A 10121
	 * 80608 S14126A 10122
	 * 80608 S12600A 10131
	 * 80608 S12601A 10131
	 * 80608 S14127A 10132
	 * 
	 * The sub-set could also be as simple as a single code mapping entry.
	 * The above is an example of a complex code mapping entry.
	 */
	private static void processSubSet(CodingScheme cs, RootConcept root, ArrayList<GemFileEntry> subSet, ICDGEMProperties props) {
	    
		if(subSet.size() == 1) {
			GemFileEntry gfe = subSet.get(0);
			processSimpleEntry(cs, root, gfe, props);
		} else if(subSet.get(0).getCombinationFlag() == 0) { // multipule single mappings
			GemFileEntry gfe = null;
			for(int i=0; i<subSet.size(); ++i) {
				gfe = subSet.get(i);
				processSimpleEntry(cs, root, gfe, props);
			}
		} else {  // complex mapping
			Collections.sort(subSet);
			ArrayList<GemScenario> scenarios = getScenarios(subSet);
			processScenarios(cs, root, scenarios, props);
		}
	}
	
	private static void processScenarios(CodingScheme cs, RootConcept root, ArrayList<GemScenario> scenarios, ICDGEMProperties props) {
		GemScenario scenario = null;
		for(int i=0; i<scenarios.size(); ++i) {
			scenario = scenarios.get(i);
			processSingleScenario(cs, root, scenario, props);
		}
	}

	/* a single scenario example:
	 * 80608 S12400A 10111
	 * 80608 S12401A 10111
	 * 80608 S14125A 10112
	 * 
	 * should produce: 
	 *                        (80608)
	 *                        /     \ 
	 *             (mapsTo)  /       \ 
	 *                      /         \
	 *   (S12400A AND S14125A)        (S12401A AND S14125A)
	 *           /  \                         /  \
	 *          /    \(contains)             /    \
	 *         /      \                     /      \
	 *  (S12400A)    (S14125A)        (S12401A)    (S14125A)
	 *        
	 *  Another example:  Note the choice list is out of order.                
	 *                  057L4DZ 0040  10112
     *                  057L4DZ 0045  10114
     *                  057L4DZ 0062  10111
     *                  057L4DZ 0065  10113
     *
     *  And Another (even the scenario numbering can be out of order):
     *  
     *  304   0B110F4 10133
     *  304   0B110Z4 10133
     *  304   0B113F4 10133
     *  304   0B113Z4 10133
     *  304   0B114F4 10133
     *  304   0B114Z4 10133
     *  304   0CTS0ZZ 10111
     *  304   0CTS0ZZ 10121
     *  304   0CTS0ZZ 10131
     *  304   0CTS4ZZ 10111
     *  304   0CTS4ZZ 10121
     *  304   0CTS4ZZ 10131
     *  304   0CTS7ZZ 10111
     *  304   0CTS7ZZ 10121
     *  304   0CTS7ZZ 10131
     *  304   0CTS8ZZ 10111
     *  304   0CTS8ZZ 10121
     *  304   0CTS8ZZ 10131
     *  304   0GTG0ZZ 10123
     *  304   0GTG4ZZ 10123
     *  304   0GTH0ZZ 10123
     *  304   0GTH4ZZ 10123
     *  304   0GTK0ZZ 10123
     *  304   0GTK4ZZ 10123
     *  304   0WB60ZZ 10112
     *  304   0WB60ZZ 10122
     *  304   0WB60ZZ 10132
     *  304   0WB63ZZ 10112
     *  304   0WB63ZZ 10122
     *  304   0WB63ZZ 10132
     *  304   0WB64ZZ 10112
     *  304   0WB64ZZ 10122
     *  304   0WB64ZZ 10132
     *  304   0WB6XZZ 10112
     *  304   0WB6XZZ 10122
     *  304   0WB6XZZ 10132
	 */          
	private static void processSingleScenario(CodingScheme cs, RootConcept root, GemScenario scenario, ICDGEMProperties props) {
		ArrayList<GemChoiceList> choiceLists = getChoiceLists(scenario);
		
		// if choice lists is only 1 element don't process using tree, 
		// just AND all the internal elements togeather
		if(choiceLists.size() == 1) {
			processSingleChoiceList(cs, root, choiceLists.get(0), props); 
		} else {
			// split the choice lists into 'top' an 'other' nodes
			GemChoiceList top = null;
			ArrayList<GemChoiceList> others = new ArrayList<GemChoiceList>();
			for(int i=0; i<choiceLists.size(); ++i) {
				if(i == 0) {
					top = choiceLists.get(i);
				} else {
					others.add(choiceLists.get(i));
				}
			}
			processMultipuleChoiceLists(cs, root, top, others, props);
		}
	}
	
	private static void processMultipuleChoiceLists(CodingScheme cs, RootConcept root, GemChoiceList tops, ArrayList<GemChoiceList> others, ICDGEMProperties props) {
		GemTree gt = null;
		GemTreeNode gtn = null;
		GemFileEntry gfe = null;
		GemChoiceList gcl = null;
		ArrayList<GemTreeNode> newNodes = null;
		
		for(int i=0; i<tops.getSize(); ++i) {
			gfe = tops.getMember(i);
			gtn = new GemTreeNode(gfe.getTargetConcept());
			gt = new GemTree(gtn);
			for(int j=0; j<others.size(); ++j) {
				gcl = others.get(j);
				newNodes = convertChoiceListToTreeNodes(gcl);
				gt.addNewLevel(newNodes);
			}
			processTree(cs, root, gt, props);
		}
	}
	
	private static void processTree(CodingScheme cs, RootConcept root, GemTree gt, ICDGEMProperties props) {
		ArrayList<GemComboEntry> combos = gt.getCombinations();
		ComplexConcept comCon = null;
		GemComboEntry gce = null;
		for(int i=0; i<combos.size(); ++i) {
			// create complex concept
			gce = combos.get(i);
			comCon = new ComplexConcept(gce.toString(), props);
			
			// add to coding scheme
			cs.addConcpet(comCon);
			
			// reference concept
			BaseConcept src = gce.getPart(0);
			
			// create has SubType association
			Association hasSubType = new Association(ICDGEMConstants.ASSOCIATION_HAS_SUBTYPE, root.getSourceCodingScheme(), root.getCode(), 
					src.getSourceCodingScheme(), src.getCode());
			cs.addHasSubTypeAssociation(hasSubType); 
			
			// create mapsTo association
			Association mapsTo = new Association(ICDGEMConstants.ASSOCIATION_MAPS_TO, src.getSourceCodingScheme(), src.getCode(), 
					comCon.getSourceCodingScheme(), comCon.getCode());
			cs.addMapsToAssociation(mapsTo);
			
			// create contains associations
			Association contains = null;
			BaseConcept tgt = null;
			for(int j=0; i<gce.getSize(); ++i) {
				tgt = gce.getPart(j);
				contains = new Association(ICDGEMConstants.ASSOCIATION_CONTAINS, comCon.getSourceCodingScheme(), comCon.getCode(),
						tgt.getSourceCodingScheme(), tgt.getCode());
				cs.addContainsAssociation(contains);
			}
		}

		
	}
	
	private static ArrayList<GemTreeNode> convertChoiceListToTreeNodes(GemChoiceList cl) {
		ArrayList<GemTreeNode> treeNodes = new ArrayList<GemTreeNode>();
		GemTreeNode gtn = null;
		GemFileEntry gfe = null;
		for(int i=0; i<cl.getSize(); ++i) {
			gfe = cl.getMember(i);
			gtn = new GemTreeNode(gfe.getTargetConcept());
			treeNodes.add(gtn);
		}
		return treeNodes;
	}
	
	private static void processSingleChoiceList(CodingScheme cs, RootConcept root, GemChoiceList cl, ICDGEMProperties props) {
		StringBuffer complexCode = new StringBuffer();
		GemFileEntry gfe = null;
		for(int i=0; i<cl.getSize(); ++i) {
			gfe = cl.getMember(i);
			if(i == 0) {
				complexCode.append(gfe.getTargetCode());
			} else {
				complexCode.append(" AND ");
				complexCode.append(gfe.getTargetCode());
			}
		}
		
		// get a reference to the source concept we are mapping
		BaseConcept src = cl.getMember(0).getSourceConcept();
		
		// create concept
		ComplexConcept comCon = new ComplexConcept(complexCode.toString(), props); 
		cs.addConcpet(comCon);
		
		// create has SubType association
		Association hasSubType = new Association(ICDGEMConstants.ASSOCIATION_HAS_SUBTYPE, root.getSourceCodingScheme(), root.getCode(), 
				src.getSourceCodingScheme(), src.getCode());
		cs.addHasSubTypeAssociation(hasSubType); 
		
		// create mapsTo association
		Association mapsTo = new Association(ICDGEMConstants.ASSOCIATION_MAPS_TO, src.getSourceCodingScheme(), src.getCode(), 
				comCon.getSourceCodingScheme(), comCon.getCode());
		cs.addMapsToAssociation(mapsTo);
		
		// create contains associations
		Association contains = null;
		BaseConcept tgt = null;
		for(int i=0; i<cl.getSize(); ++i) {
			gfe = cl.getMember(i);
			tgt = gfe.getTargetConcept();
			contains = new Association(ICDGEMConstants.ASSOCIATION_CONTAINS, comCon.getSourceCodingScheme(), comCon.getCode(),
					tgt.getSourceCodingScheme(), tgt.getCode());
			cs.addContainsAssociation(contains);
		}
	}
	
	private static ArrayList<GemChoiceList> getChoiceLists(GemScenario scenario) {
		ArrayList<GemChoiceList> choiceLists = new ArrayList<GemChoiceList>();
		GemChoiceList gcl = null;
		GemFileEntry gfe = null;
		int curCL = -1;
		int prevCL = -1;		
		for(int i = 0; i<scenario.getSize(); ++i) {
			gfe = scenario.getMember(i);
			curCL = gfe.getChoiceList();
			if(curCL != prevCL) {
				gcl = new GemChoiceList();
				choiceLists.add(gcl);
			}
			gcl.addMember(gfe);
			prevCL = curCL;
		}
		return choiceLists;
	}
	
	private static ArrayList<GemScenario> getScenarios(ArrayList<GemFileEntry> subSet) {
		ArrayList<GemScenario> scenarios = new ArrayList<GemScenario>();
		GemFileEntry gfe = null;
		GemScenario gs = null;
		boolean done = false;
		int i = 0;
		int curScenario = -1;
		int prevScenario = -1;
		while(!done) {
			gfe = subSet.get(i);
			curScenario = gfe.getScenario();
			if(prevScenario != curScenario) {
				gs = new GemScenario();
				scenarios.add(gs);
			}
			prevScenario = curScenario;
			gs.addMember(gfe);
			
			++i;
			if(i == subSet.size()) {
				done = true;
			}
		}
		return scenarios;
	}
	
	private static void processSimpleEntry(CodingScheme cs, RootConcept root, GemFileEntry gfe, ICDGEMProperties props) {
		// create a hasSubType association
		// Association(String relationName, String sourceCodingScheme, String sourceCode, String targetCodingScheme, String targetCode)
		Association hasSubType = new Association(ICDGEMConstants.ASSOCIATION_HAS_SUBTYPE, root.getSourceCodingScheme(), root.getCode(), 
				gfe.getSourceCodingScheme(), gfe.getSourceCode());
		cs.addHasSubTypeAssociation(hasSubType);
		
		// create a mapsTo association
		Association mapsTo = null;
		if(gfe.getNoMapFlag() == 1) {
			mapsTo = new Association(ICDGEMConstants.ASSOCIATION_MAPS_TO, gfe.getSourceCodingScheme(), gfe.getSourceCode(), 
					props.getCsLocalName(), ICDGEMConstants.NO_MAP);
		} else {
			mapsTo = new Association(ICDGEMConstants.ASSOCIATION_MAPS_TO, gfe.getSourceCodingScheme(), gfe.getSourceCode(), 
					gfe.getTargetCodingScheme(), gfe.getTargetCode());
		}
		cs.addMapsToAssociation(mapsTo);
	}	
	
	public static void printCs(CodingScheme cs) {
		System.out.println("FileProcessor: printCs: entry");
		System.out.println("FileProcessor: printCs: number of concepts: " + cs.getConcepts().size());
		ArrayList<BaseConcept> cons = cs.getConcepts();
		BaseConcept con = null;
		for(int i=0; i<cons.size(); ++i) {
			con = cons.get(i);
			System.out.println("  [" + i + "]: " + con.toString());			
		}
		
		
		System.out.println("FileProcessor: printCs: number of hasSubType associations: " + cs.getHasSubTypeAssociations().size());
		System.out.println("FileProcessor: printCs: number of mapsTo associations: " + cs.getMapsToAssociations().size());
		System.out.println("FileProcessor: printCs: number of contains associations: " + cs.getContainsAssociations().size());
		System.out.println("FileProcessor: printCs: exit");
	}
	
	public static void main(String[] args) {
		String inFile = "C:/ibm/eclipse341b/workspace/lgICDGEMLoaderExtension/resources/testData/icdGem/small_2009_I9gem.txt";
		ICDGEMProperties props = new ICDGEMProperties(ICDGEMConstants.ICD9_TO_10_CM_DESC, "1.01", null);
		CodingScheme cs = FileProcessor.process(inFile, props);
		printCs(cs);
		
	}
	
}
