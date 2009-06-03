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
    
    /* misc helper constants */
	public static final int CON_TYPE_10_CM  = 0;
	public static final int CON_TYPE_10_PCS = 1;
	public static final int CON_TYPE_9_CM   = 2;
	public static final int CON_TYPE_9_PCS  = 3;
	public static final int CON_TYPE_ROOT   = 99;
	
	public static final String CON_TYPE_10_CM_DESC  = "i10cm";
	public static final String CON_TYPE_10_PCS_DESC = "i10pcs";
	public static final String CON_TYPE_9_CM_DESC   = "i9cm";
	public static final String CON_TYPE_9_PCS_DESC  = "i9pcs";
	public static final String CON_TYPE_ROOT_DESC   = "root";	
	
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
    public static final String RELATIONS_CONTAINER_NAME = "relations";
    public static final String ASSOCIATION_HASSUBTYPE = "hasSubtype";
    public static final String ASSOCIATION_ISA = "Is_A";

    /* config properties names/ids */
    public static final String PROP_FILE_NAME = "icdGemLoader.properties";
    public static final String PROP_LOADER_NAME = "icdgem.loader.name";
    public static final String PROP_LOADER_DESCR = "icdgem.loader.descr";
    public static final String PROP_LOADER_VERSION = "icdgem.loader.version";
    public static final String PROP_LOADER_TEXT_INPUT_DESCR = "icdgem.loader.text.input.descr";

    public static final String PROP_ICD9_TO_10_CM_OID = "icd9to10.cm.oid";
    public static final String PROP_ICD9_TO_10_CM_DESCR = "icd9to10.cm.descr";
    public static final String PROP_ICD9_TO_10_CM_LOCAL_NAME = "icd9to10.cm.local.name";
    public static final String PROP_ICD9_TO_10_CM_SOURCE = "icd9to10.cm.source";
    public static final String PROP_ICD9_TO_10_CM_COPYRIGHT = "icd9to10.cm.copyright";
        
    public static final String PROP_ICD9_TO_10_PCS_OID = "icd9to10.pcs.oid";
    public static final String PROP_ICD9_TO_10_PCS_DESCR = "icd9to10.pcs.descr";
    public static final String PROP_ICD9_TO_10_PCS_LOCAL_NAME = "icd9to10.pcs.local.name";
    public static final String PROP_ICD9_TO_10_PCS_SOURCE = "icd9to10.pcs.source";
    public static final String PROP_ICD9_TO_10_PCS_COPYRIGHT = "icd9to10.pcs.copyright";        
    
    public static final String PROP_ICD10_TO_9_CM_OID = "icd10to9.cm.oid";
    public static final String PROP_ICD10_TO_9_CM_DESCR = "icd10to9.cm.descr";
    public static final String PROP_ICD10_TO_9_CM_LOCAL_NAME = "icd10to9.cm.local.name";
    public static final String PROP_ICD10_TO_9_CM_SOURCE = "icd10to9.cm.source";
    public static final String PROP_ICD10_TO_9_CM_COPYRIGHT = "icd10to9.cm.copyright";
        
    public static final String PROP_ICD10_TO_9_PCS_OID = "icd10to9.pcs.oid";
    public static final String PROP_ICD10_TO_9_PCS_DESCR = "icd10to9.pcs.descr";
    public static final String PROP_ICD10_TO_9_PCS_LOCAL_NAME = "icd10to9.pcs.local.name";
    public static final String PROP_ICD10_TO_9_PCS_SOURCE = "icd10to9.pcs.source";
    public static final String PROP_ICD10_TO_9_PCS_COPYRIGHT = "icd10to9.pcs.copyright";    
    
    
    // textual presentation
    public static final String TEXTPRESENTATION_ID = "textualPresentation";
//    public static final String TEXTPRESENTATION_URI = ICD10_OID_ID_PREFIX + URI_DELIM + TEXTPRESENTATION_ID;
    
    //plain text
    public static final String PLAIN_FORMAT_ID = "text_plain";
//    public static final String PLAIN_FORMAT_URI = ICD10_OID_ID_PREFIX + URI_DELIM + PLAIN_FORMAT_ID;

    // english
//    public static final String LANG_URI = ICD10_OID_ID_PREFIX + DOT + "84";
    public static final String LANG_ENGLISH = "en";
//    public static final String LANG_ENGLISH_URI = LANG_URI + URI_DELIM + "en";
    
    /* artificial top node */
//    public static final String DEFAULT_TOP_NODE_CODE_SYSTEM_ID = ICD10_OID_ID_PREFIX;    
}