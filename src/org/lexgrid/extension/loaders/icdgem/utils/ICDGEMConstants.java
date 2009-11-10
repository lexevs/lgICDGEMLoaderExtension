/*
 * Copyright: (c) 2004-2007 Mayo Foundation for Medical Education and 
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

/**
 * Constants used by the ICD-10 Loader.
 * 
 * @author Michael Turk (turk.michael@mayo.edu)
 * 
 */
public class ICDGEMConstants {
    
	public static final int CON_TYPE_10_CM  = 0;
	public static final int CON_TYPE_10_PCS = 1;
	public static final int CON_TYPE_9_CM   = 2;
	public static final int CON_TYPE_9_PCS  = 3;	
	public static final int CON_TYPE_ROOT   = 99;
	public static final int CON_TYPE_COMPLEX  = 4;
	
	public static final int ICD9_TO_10_CM  = 0;
	public static final int ICD9_TO_10_PCS = 1;
	public static final int ICD10_TO_9_CM  = 2;
	public static final int ICD10_TO_9_PCS = 3;
	
	public static final String ICD9_TO_10_CM_DESC = "i9to10cm";
	public static final String ICD9_TO_10_PCS_DESC = "i9to10pcs";
	public static final String ICD10_TO_9_CM_DESC = "i10to9cm";
	public static final String ICD10_TO_9_PCS_DESC = "i10to9pcs";
	
    public static final String URI_DELIM = ":";
    public static final String DOT = ".";    
    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String RELATIONS_CONTAINER_NAME = "relations";
    
    public static final String ASSOCIATION_HAS_SUBTYPE = "hasSubtype";
    public static final String ASSOCIATION_MAPS_TO = "mapsTo";
    public static final String ASSOCIATION_CONTAINS = "contains";
    public static final String ASSOCIATION_PART_OF = "partOf";
    public static final String ASSOCIATION_ISA = "Is_A";    
    public static final String NO_MAP = "NO MAP";
    
	public static final String PROPS_ICD9_TO_10_CM  = "icd9to10cm.properties";
	public static final String PROPS_ICD9_TO_10_PCS = "icd9to10pcs.properties";
	public static final String PROPS_ICD10_TO_9_CM  = "icd10to9cm.properties";
	public static final String PROPS_ICD10_TO_9_PCS = "icd10to9pcs.properties";
	public static final String PROPS_ICD_GEM_LOADER = "icdGemLoader.properties";
    
}