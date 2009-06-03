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
package org.lexgrid.extension.loaders.icdgem.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.LexGrid.messaging.LgMessageDirectorIF;

/**
 * Common bits for the Text -> foo loaders.
 * 
 * @author <A HREF="mailto:armbrust.daniel@mayo.edu">Dan Armbrust</A>
 * @version subversion $Revision: 5882 $ checked in on $Date: 2007-05-16
 *          21:55:43 +0000 (Wed, 16 May 2007) $
 */
public class TextUtility {
    public static Concept getParent(Concept[] concepts, int curPos) {
        int depth = concepts[curPos].getDepth();
        for (int i = curPos; i >= 0; i--) {
            if (concepts[i].getDepth() < depth) {
                return concepts[i];
            }
        }
        return null;
    }

    public static CodingScheme readAndVerifyConcepts(String fileLocation, LgMessageDirectorIF messages, String token, ICDGEMProperties props) throws Exception {
        CodingScheme codingScheme = new CodingScheme();
        messages.info("TextUtility: readAndVerifyConcepts: Reading file into memory");
        try {
            ArrayList<Concept> concepts = new ArrayList<Concept>();
            ArrayList<Association> associations = new ArrayList<Association>();
            BufferedReader fileReader = new BufferedReader(new FileReader(fileLocation));
            String line = fileReader.readLine();

            int lineNo = 1;
            while (line != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    // read the "special" first line
                    StringTokenizer tokenizer = new StringTokenizer(line, token);

                    if (tokenizer.countTokens() < 4) {
                        messages
                                .fatalAndThrowException("TextUtility: readAndVerifyConcepts: FATAL ERROR - The beginning of the file must contain a token seperated"
                                        + " line that contain (in this order) 'codingSchemeName,codingSchemeId,defaultLanguage,formalName'"
                                        + "\nThe line may contain the following values (in this order) 'version,source,description,copyright'");
                    }
                    codingScheme.codingSchemeName = tokenizer.nextToken().trim();
                    codingScheme.codingSchemeId = tokenizer.nextToken().trim();
                    codingScheme.defaultLanguage = tokenizer.nextToken().trim();
                    codingScheme.formalName = tokenizer.nextToken().trim();

                    if (tokenizer.hasMoreTokens()) {
                        codingScheme.representsVersion = tokenizer.nextToken().trim();
                    }
                    if (tokenizer.hasMoreTokens()) {
                        codingScheme.source = tokenizer.nextToken().trim();
                    }
                    if (tokenizer.hasMoreTokens()) {
                        codingScheme.entityDescription = tokenizer.nextToken().trim();
                    }
                    if (tokenizer.hasMoreTokens()) {
                        codingScheme.copyright = tokenizer.nextToken().trim();
                    }

                    lineNo++;
                    line = fileReader.readLine();
                    break;
                }
                lineNo++;
                line = fileReader.readLine();
            }

            // read the rest of the lines
            while (line != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    if (line.startsWith("Relation")) {
                        Association assoc = new Association(line);
                        if (assoc.isValid() && !associations.contains(assoc)) {
                            associations.add(assoc);
                        } else {
                            messages.info("TextUtility: readAndVerifyConcepts: WARNING - Line number " + lineNo
                                    + " is missing required information.  Skipping.");
                        }
                    } else {
                        Concept temp = new Concept(line, token, props.getIcdGemType());
                        if (temp.getName() != null) {
                            concepts.add(temp);
                        } else {
                            messages.info("TextUtility: readAndVerifyConcepts: WARNING - Line number " + lineNo
                                    + " is missing required information.  Skipping.");
                        }
                    }

                }
                lineNo++;
                line = fileReader.readLine();
            }

            Concept[] allConcepts = (Concept[]) concepts.toArray(new Concept[concepts.size()]);
            concepts = null;

            for (int i = 0; i < allConcepts.length; i++) {
                // see if this code already exists - if so, the names must
                // match.
                for (int j = 0; j < i; j++) {
                    if (allConcepts[j].getCode().equals(allConcepts[i].getCode())
                            && (((allConcepts[i].getName() == null && allConcepts[j].getName() != null) || 
                            		(allConcepts[i].getName() != null && allConcepts[j].getName() == null)) || 
                            		((allConcepts[i].getName() != null && allConcepts[j].getName() != null) && 
                            				(!allConcepts[i].getName().equals(allConcepts[j].getName()))))) {
                        // codes match, names don't, fatal error.
                        messages.fatalAndThrowException("TextUtility: readAndVerifyConcepts: FATAL ERROR - Concept code " + allConcepts[i].getCode()
                                + " occurs twice with different names.  This is illegal.");

                    }
                }
            }

            codingScheme.concepts = allConcepts;
            codingScheme.associations = (Association[]) associations.toArray(new Association[associations.size()]);

            return codingScheme;
        } catch (FileNotFoundException e) {
            messages.fatalAndThrowException("TextUtility: readAndVerifyConcepts: File not found", e);
            // this is actually unreachable
            return null;
        }
    }
}