package org.lexgrid.extension.loaders.icdgem.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.LexGrid.messaging.LgMessageDirectorIF;
import org.lexgrid.extension.loaders.icdgem.utils.Association;
import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;
import org.lexgrid.extension.loaders.icdgem.utils.CodingScheme;
import org.lexgrid.extension.loaders.icdgem.utils.ComplexConcept;
import org.lexgrid.extension.loaders.icdgem.utils.ICDConceptFactory;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;
import org.lexgrid.extension.loaders.icdgem.utils.RootConcept;

public class FileProcessor {
	
	public static CodingScheme process(String fileName, ICDGEMProperties props) {
		LgMessageDirectorIF md = props.getMessageDirector();
		GemFileEntry gfe = null;
		ArrayList<Association> hasSubTypeAssociations = new ArrayList<Association>();
		ArrayList<Association> MapsToAssociations = new ArrayList<Association>();
		ArrayList<Association> containsAssociations = new ArrayList<Association>();
		ArrayList<BaseConcept> concepts = new ArrayList<BaseConcept>();
		
		ArrayList<GemFileEntry> subSet = new ArrayList<GemFileEntry>();
		
		ArrayList<BaseConcept> complexConceptParts = new ArrayList<BaseConcept>();
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
    			if(line == null) {
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
			processSimpleEntry(cs, root, subSet, props);
		} else {
			ArrayList<GemScenario> scenarios = getScenarios(subSet);
			processScenarios(cs, root, scenarios, props);
		}
		BaseConcept top = null;
		GemFileEntry gfe = null;
		String code = null;
		int conType = -99;
		
		for(int i = 0; i < subSet.size(); ++i) {
			gfe = subSet.get(i);
			if (i == 0) {
				top = gfe.getSourceConcept();
			}
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
	 */          
	private static void processSingleScenario(CodingScheme cs, RootConcept root, GemScenario scenario, ICDGEMProperties props) {
		
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
	
	private static void processSimpleEntry(CodingScheme cs, RootConcept root, ArrayList<GemFileEntry> subSet, ICDGEMProperties props) {
		GemFileEntry gfe = subSet.get(0); // a simple entry should always be a list of length 1;
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
	
	private static Association createHasSubTypeAssociation(RootConcept root, String tgtCode, ICDGEMProperties props, int srcCon) {
		return new Association(
				ICDGEMConstants.ASSOCIATION_HAS_SUBTYPE, // relation name
				props.getCsLocalName(), // sourceCodingScheme
				root.getCode(),         // sourceCode
				props.getCsLocalName(), // targetCodingScheme
				tgtCode);               // targetCode)
	}

}
