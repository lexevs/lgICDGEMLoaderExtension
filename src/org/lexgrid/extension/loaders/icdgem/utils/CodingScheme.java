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

import java.util.ArrayList;

public class CodingScheme {
    private String _codingSchemeName;
    private String _codingSchemeId;
    private String _defaultLanguage;
    private String _representsVersion;
    private String _formalName;
    private String _source;
    private String _entityDescription;
    private String _copyright;
    private ArrayList<BaseConcept> _concepts;
    private ArrayList<Association> _hasSubTypeAssociations;
    private ArrayList<Association> _mapsToAssociations;
    private ArrayList<Association> _containsAssociations;
    
    public CodingScheme(String codingSchemeName,
    		String codingSchemeId, String defaultLanguage,
    		String representsVersion, String formalName,
    		String source, String entityDescription,
    		String copyright) {
    	_codingSchemeName = codingSchemeName;
    	_codingSchemeId = codingSchemeId;
    	_defaultLanguage = defaultLanguage;
    	_representsVersion = representsVersion;
    	_formalName = formalName;
    	_source = source;
    	_entityDescription = entityDescription;
    	_copyright = copyright;
    	
    	_concepts = new ArrayList<BaseConcept>();
    	_hasSubTypeAssociations = new ArrayList<Association>();
    	_mapsToAssociations = new ArrayList<Association>();
    	_containsAssociations = new ArrayList<Association>();
    }
    
    public CodingScheme() {
    	_concepts = new ArrayList<BaseConcept>();
    	_hasSubTypeAssociations = new ArrayList<Association>();
    	_mapsToAssociations = new ArrayList<Association>();
    	_containsAssociations = new ArrayList<Association>();    	
    }
    
    public void setCsName(String csName) {
    	_codingSchemeName = csName;
    }
    
    public String getCsName() {
    	return _codingSchemeName;
    }
    
    public void setCsId(String csId) {
    	_codingSchemeId = csId;
    }
    
    public String getCsId() {
    	return _codingSchemeId;
    }
    
    public void setDefaultLanguage(String defaultLanguage) {
    	_defaultLanguage = defaultLanguage;
    }
    
    public String getDefaultLanguage() {
    	return _defaultLanguage;
    }
    
    public void setRepresentsVersion(String representsVersion) {
    	_representsVersion = representsVersion;
    }
    
    public String getRepresentsVersion() {
    	return _representsVersion;
    }
    
    public void setFormalName(String formalName) {
    	_formalName = formalName;
    }
    
    public String getFormalName() {
    	return _formalName;
    }
    
    public void setSource(String source) {
    	_source = source;
    }
    
    public String getSource() {
    	return _source;
    }
    
    public void setEntityDescription(String entityDescription) {
    	_entityDescription = entityDescription;
    }
    
    public String getEntityDescription() {
    	return _entityDescription;
    }
    
    public void setCopyright(String copyright) {
    	_copyright = copyright;
    }
    
    public String getCopyright() {
    	return _copyright;
    }
    
    public ArrayList<BaseConcept> getConcepts() {
    	return _concepts;
    }
    
    public void addConcpet(BaseConcept concept) {
    	_concepts.add(concept);
    }
    
    public ArrayList<Association> getHasSubTypeAssociations() {
    	return _hasSubTypeAssociations;
    }
    
    public void addHasSubTypeAssociation(Association hasSubType) {
    	_hasSubTypeAssociations.add(hasSubType);
    }
    
    public ArrayList<Association> getMapsToAssociations() {
    	return _mapsToAssociations;
    }
    
    public void addMapsToAssociation(Association mapsTo) {
    	_mapsToAssociations.add(mapsTo);
    }
    
    public ArrayList<Association> getContainsAssociations() {
    	return _containsAssociations;
    }
    
    public void addContainsAssociation(Association contains) {
    	_containsAssociations.add(contains);
    }
    

}