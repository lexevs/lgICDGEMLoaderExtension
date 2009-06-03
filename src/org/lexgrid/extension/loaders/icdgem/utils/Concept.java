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

import java.util.StringTokenizer;

/**
 * Borrowed from lgConverter project,
 * edu.mayo.informatics.lexgrid.convert.directConversions.TextCommon
 * turk.michael@mayo.edu
 * 
 * Holder for concepts loaded from Text files.
 * 
 * @author <A HREF="mailto:armbrust.daniel@mayo.edu">Dan Armbrust</A>
 * @version subversion $Revision: 7190 $ checked in on $Date: 2008-02-15
 *          17:25:58 +0000 (Fri, 15 Feb 2008) $
 */
public class Concept {
    private String code;
    private String name;
    private String description;
    private int depth;

    public Concept(String code, String name, String description, int depth, int type) {
        this.description = description;
        this.depth = depth;
        setCodeAndName(code, name, type);
    }
    
    private Concept() {
    	code = "@";
    	name = "Top Thing";
    	description = "Points to all concepts that aren't children of any other concepts";
    	depth = -1;
    }
    
    public static Concept createRootConcept() {
    	return new Concept();
    }
    
    private void setCodeAndName(String code, String name, int type) {
    	if(type == ICDGEMConstants.CON_TYPE_10_CM) {
    		if(code.length() == 3) {
                this.code = code;
                this.name = name;    		    			
    		} else {
    			StringBuffer sb = new StringBuffer(code.length() + 1);
    			sb.append(code.substring(0, 3));
    			sb.append('.');
    			sb.append(code.substring(3));
    			this.code = sb.toString();
    			this.name = sb.toString();
    		}
    	} else if (type == ICDGEMConstants.CON_TYPE_10_PCS) {
            this.code = code;
            this.name = name;    		
    	} else if (type == ICDGEMConstants.CON_TYPE_9_CM) {
			StringBuffer sb = new StringBuffer(code.length() + 1);    		
            sb.append(code.substring(0, 3));
            sb.append('.');
            sb.append(code.substring(3));
            this.code = sb.toString();
            this.name = sb.toString();    		
    	} else if (type == ICDGEMConstants.CON_TYPE_9_PCS) {
			StringBuffer sb = new StringBuffer(code.length() + 1);    		
            sb.append(code.substring(0, 2));
            sb.append('.');
            sb.append(code.substring(2));
            this.code = sb.toString();
            this.name = sb.toString();    		
    	} else {
            this.code = code;
            this.name = name;    		
    		IllegalArgumentException e = new IllegalArgumentException("Invalid type: " + type);
    		e.printStackTrace();
    	}    	
    }
    
    public Concept(String line, String token, int type) {
        depth = 0;

        StringTokenizer tokenizer = new StringTokenizer(line, token);
        if (tokenizer.hasMoreElements()) {
        	String tempCode = tokenizer.nextToken();
        	String tempName = tempCode;
        	setCodeAndName(tempCode, tempName, type);
        }
        if (tokenizer.hasMoreElements()) {
            description = tokenizer.nextToken();
        }
    }
    
    
    public String getCode() {
    	return code;
    }
    
    public String getName() {
    	return name;
    }
    
    public String getDescription() {
    	return description;
    }
    
    public int getDepth() {
    	return depth;
    }

    public String toString() {
    	final String NEW_LINE = System.getProperty("line.separator"); 
    	StringBuffer sb = new StringBuffer();
    	sb.append("Code: ");
    	sb.append(code);
    	sb.append(NEW_LINE);
    	sb.append("Name: ");
    	sb.append(name);
    	sb.append(NEW_LINE);
    	sb.append("Description: ");
    	sb.append(description);
    	sb.append(NEW_LINE);
    	sb.append("Depth: ");
    	sb.append(depth);
    	sb.append(NEW_LINE);
        return sb.toString();
    }
}