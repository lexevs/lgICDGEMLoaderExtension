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
package org.lexgrid.extension.loaders.icdgem.convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.LexGrid.messaging.LgMessageDirectorIF;
import org.LexGrid.util.sql.DBUtility;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.LexGrid.util.sql.lgTables.SQLTableUtilities;

import org.lexgrid.extension.loaders.icdgem.file.FileProcessor;
import org.lexgrid.extension.loaders.icdgem.utils.Association;
import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;
import org.lexgrid.extension.loaders.icdgem.utils.CodingScheme;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;
import org.lexgrid.extension.loaders.icdgem.utils.RootConcept;

/**
 * Conversion tool for loading ICD GEM files into SQL.
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 * @version subversion $Revision: 8756 $ checked in on $Date: 2007-08-30
 *          17:13:22 +0000 (Thu, 30 Aug 2007) $
 */
public class ICDGEMToLex {
	private ICDGEMProperties _props;
    private LgMessageDirectorIF _messages;
    private Connection _sqlConnection;
    private SQLTableUtilities _tableUtility;
    private SQLTableConstants _tableConstants;
    private CodingScheme _codingScheme;

    /**
     * @return the codingSchemeName
     */
    public CodingScheme getCodingScheme() {
        return _codingScheme;
    }
    
    public ICDGEMToLex(String fileLocation, String sqlServer, String sqlDriver,
            String sqlUsername, String sqlPassword, String tablePrefix, ICDGEMProperties props) throws Exception {
        _messages = props.getMessageDirector();
        _props = props;

        _codingScheme = FileProcessor.process(fileLocation, props);

        // set up the sql tables
        prepareDatabase(_codingScheme.getCsName(), sqlServer, sqlDriver, sqlUsername, sqlPassword,
                tablePrefix);

        _tableConstants = _tableUtility.getSQLTableConstants();

        loadConcepts(_codingScheme);
        loadHasSubtypeRelations(_codingScheme);
        loadMapsToRelations(_codingScheme);
        loadContainsRelations(_codingScheme);
        _sqlConnection.close();

    }

    private void prepareDatabase(String codingScheme, String sqlServer, String sqlDriver, String sqlUsername,
            String sqlPassword, String tablePrefix) throws Exception {
        try {
            _messages.info("ICDGEMToLex: prepareDatabase: Connecting to database");
            _sqlConnection = DBUtility.connectToDatabase(sqlServer, sqlDriver, sqlUsername, sqlPassword);
            // gsm_ = new GenericSQLModifier(_sqlConnection);
        } catch (ClassNotFoundException e) {
            _messages
                    .fatalAndThrowException("ICDGEMToLex: prepareDatabase: FATAL ERROR - The class you specified for your sql driver could not be found on the path.");
        }

        _tableUtility = new SQLTableUtilities(_sqlConnection, tablePrefix);

        _messages.info("ICDGEMToLex: prepareDatabase: Creating tables");
        _tableUtility.createDefaultTables();

        _messages.info("ICDGEMToLex: prepareDatabase: Creating constraints");
        _tableUtility.createDefaultTableConstraints();

        _messages.info("ICDGEMToLex: prepareDatabase: Cleaning tables");
        _tableUtility.cleanTables(codingScheme);
    }

    private void loadConcepts(CodingScheme codingScheme) throws Exception {
        PreparedStatement insert = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME));

        _messages.info("ICDGEMToLex: loadConcepts: Loading coding scheme");
        int ii = 1;
        /*
        codingSchemeName, 
        codingSchemeURI, 
        representsVersion, 
        formalName, 
        defaultLanguage, 
        approxNumConcepts, 
        isActive, 
        entryStateId, 
        releaseURI, 
        entityDescription, 
        copyright
        */

        insert.setString(ii++, codingScheme.getCsName()); // codingSchemeName
        insert.setString(ii++, codingScheme.getCsId()); // codingSchemeURI
        insert.setString(ii++, codingScheme.getRepresentsVersion()); // representsVersion
        insert.setString(ii++, codingScheme.getFormalName()); // formalName
        insert.setString(ii++, codingScheme.getDefaultLanguage()); // defaultLanguage
        insert.setInt(ii++, codingScheme.getConcepts().size()); // approxNumConcepts
        DBUtility.setBooleanOnPreparedStatment(insert, ii++, new Boolean(false)); // isActive
        insert.setInt(ii++, 0); // entryStateId
        insert.setString(ii++, SQLTableConstants.TBLCOLVAL_MISSING); // releaseURI
        insert.setString(ii++, codingScheme.getEntityDescription()); // entityDescription
        insert.setString(ii++, codingScheme.getCopyright()); // entityDescription

        try {
            insert.executeUpdate();
        } catch (SQLException e) {
            _messages.fatalAndThrowException(
                    "ICDGEMToLex: loadConcepts: FATAL ERROR - It is likely that your coding scheme name or CodingSchemeId is not unique.", e);
        }

        insert.close();

        try {
            _messages.info("ICDGEMToLex: loadConcepts: Loading coding scheme supported attributes");
            insert = _sqlConnection.prepareStatement(_tableConstants
                    .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME_SUPPORTED_ATTRIBUTES));
            /*
            codingSchemeName, 
            supportedAttributeTag, 
            id, 
            uri, 
            idValue, 
            val1, 
            val2
            */
            insert.setString(1, codingScheme.getCsName()); // codingSchemeName
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_LANGUAGE); // supportedAttributeTag
            insert.setString(3, codingScheme.getDefaultLanguage()); // id
            insert.setString(4, ""); // uri
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, codingScheme.getDefaultLanguage()); // idValue
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING); // val1
                insert.setString(7, ""); // val2
            }
            insert.executeUpdate();
            
            // isA
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, SQLTableConstants.TBLCOLVAL_ISA_ASSOCIATION);
            insert.setString(4, "");
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }            
            insert.executeUpdate();
            
            // hasSubType
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
            insert.setString(4, "");
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();
            
            // mapsTo
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, ICDGEMConstants.ASSOCIATION_MAPS_TO);
            insert.setString(4, "");
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, ICDGEMConstants.ASSOCIATION_MAPS_TO);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();
            
            // contains
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, ICDGEMConstants.ASSOCIATION_CONTAINS);
            insert.setString(4, "");
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, ICDGEMConstants.ASSOCIATION_CONTAINS);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();
            

            // supported coding scheme
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_CODINGSCHEME);
            insert.setString(3, codingScheme.getCsName());
            insert.setString(4, codingScheme.getCsId());
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, codingScheme.getCsName());
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();
            
            // supported coding scheme - ICD9
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_CODINGSCHEME);
            insert.setString(3, _props.getIcd9CmLocalName());
            insert.setString(4, _props.getIcd9CmUri());
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, _props.getIcd9CmLocalName());
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();

            // supported coding scheme - ICD10 CM
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_CODINGSCHEME);
            insert.setString(3, _props.getIcd10CmLocalName());
            insert.setString(4, _props.getIcd10CmUri());
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, _props.getIcd10CmLocalName());
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();
            
            // supported coding scheme - ICD10 CM
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_CODINGSCHEME);
            insert.setString(3, _props.getIcd10PcsLocalName());
            insert.setString(4, _props.getIcd10PcsUri());
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, _props.getIcd10PcsLocalName());
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();

            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_PROPERTY);
            insert.setString(3, SQLTableConstants.TBLCOLVAL_DEFINITION);
            insert.setString(4, "");
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, SQLTableConstants.TBLCOLVAL_DEFINITION);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }

            insert.executeUpdate();
            insert.close();

            insert = _sqlConnection.prepareStatement(_tableConstants
                    .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME_MULTI_ATTRIBUTES));

            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_LOCALNAME);
            insert.setString(3, codingScheme.getCsName());
            if (_tableConstants.supports2009Model()) {
                insert.setString(4, "");
                insert.setString(5, "");
            }

            insert.executeUpdate();

            String codingSchemeIdTemp = codingScheme.getCsId();
            int temp = codingSchemeIdTemp.lastIndexOf(':');
            if (temp > 0 && ((temp + 1) <= codingSchemeIdTemp.length())) {
                codingSchemeIdTemp = codingSchemeIdTemp.substring(temp + 1);
            }

            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_LOCALNAME);
            insert.setString(3, codingSchemeIdTemp);
            if (_tableConstants.supports2009Model()) {
                insert.setString(4, "");
                insert.setString(5, "");
            }

            insert.executeUpdate();

            if (codingScheme.getSource() != null && codingScheme.getSource().length() > 0) {
                insert.setString(1, codingScheme.getCsName());
                insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE);
                insert.setString(3, codingScheme.getSource());
                if (_tableConstants.supports2009Model()) {
                    insert.setString(4, "");
                    insert.setString(5, "");
                }

                insert.executeUpdate();
            }
            insert.close();
        } catch (SQLException e) {
            _messages.fatalAndThrowException("ICDGEMToLex: loadConcepts: FATAL ERROR - Problem loading the coding scheme supported attributes", e);
        }

        _messages.info("Loading relation definition");
        try {
            insert = _sqlConnection.prepareStatement(_tableConstants.getInsertStatementSQL(SQLTableConstants.RELATION));
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
            DBUtility.setBooleanOnPreparedStatment(insert, 3, new Boolean("true"), false);
            insert.setString(4, "");

            insert.executeUpdate();
            insert.close();
        } catch (SQLException e) {
            _messages.fatalAndThrowException("ICDGEMToLex: loadConcepts: FATAL ERROR - Problem loading the relation definition", e);
        }

        _messages.info("ICDGEMToLex: loadConcepts: Loading isa association definition");
        try {
            insert = _sqlConnection.prepareStatement(_tableConstants
                    .getInsertStatementSQL(SQLTableConstants.ASSOCIATION));
            /*
            codingSchemeName, 
            containerName, 
            entityCodeNamespace, 
            entityCode, 
            associationName, 
            forwardName, 
            reverseName, 
            inverseId, 
            isNavigable, 
            isTransitive, 
            isAntiTransitive, 
            isSymmetric, 
            isAntiSymmetric, 
            isReflexive, 
            isAntiReflexive, 
            isFunctional, 
            isReverseFunctional, 
            entityDescription
            */
            int k = 1;
            
            // hasSubType/Isa
            insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.getCsId()); // entityCodeNamespace
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_ISA_ASSOCIATION); // entityCode
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_ISA_ASSOCIATION); // associationName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_ISA_ASSOCIATION); // forwardName
            insert.setString(k++, ""); // reverseName
            insert.setString(k++, null); // inverseId
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isNavigable
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("false"), false); // isSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isAntiSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isFunctional
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReverseFunctional
            insert.setString(k++, "The parent child relationships."); // entityDescription
            insert.executeUpdate();
            
            // mapsTo
            k = 1;
            insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.getCsId()); // entityCodeNamespace
            insert.setString(k++, ICDGEMConstants.ASSOCIATION_MAPS_TO); // entityCode
            insert.setString(k++, ICDGEMConstants.ASSOCIATION_MAPS_TO); // associationName
            insert.setString(k++, ICDGEMConstants.ASSOCIATION_MAPS_TO); // forwardName
            insert.setString(k++, ""); // reverseName
            insert.setString(k++, null); // inverseId
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isNavigable
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("false"), false); // isSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isAntiSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isFunctional
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReverseFunctional
            insert.setString(k++, "Mapping relationship."); // entityDescription
            insert.executeUpdate();

            // contains
            k = 1;
            insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
            insert.setString(k++, ICDGEMConstants.ASSOCIATION_CONTAINS); // entityCode
            insert.setString(k++, ICDGEMConstants.ASSOCIATION_CONTAINS); // associationName
            insert.setString(k++, ICDGEMConstants.ASSOCIATION_CONTAINS); // forwardName
            insert.setString(k++, ""); // reverseName
            insert.setString(k++, null); // inverseId
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isNavigable
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("false"), false); // isSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isAntiSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isFunctional
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReverseFunctional
            insert.setString(k++, "Relationship between compound concepts and their parts."); // entityDescription
            insert.executeUpdate();
            
            insert.close();
        } catch (SQLException e) {
            _messages.fatalAndThrowException("ICDGEMToLex: loadConcepts: FATAL ERROR - Problem loading the association definition", e);
        }
        
        _messages.info("ICDGEMToLex: loadConcepts: Loading hassubtype association definition");
        try {
            insert = _sqlConnection.prepareStatement(_tableConstants
                    .getInsertStatementSQL(SQLTableConstants.ASSOCIATION));
            /*
            codingSchemeName, 
            containerName, 
            entityCodeNamespace, 
            entityCode, 
            associationName, 
            forwardName, 
            reverseName, 
            inverseId, 
            isNavigable, 
            isTransitive, 
            isAntiTransitive, 
            isSymmetric, 
            isAntiSymmetric, 
            isReflexive, 
            isAntiReflexive, 
            isFunctional, 
            isReverseFunctional, 
            entityDescription
            */
            int k = 1;
            insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION); // entityCode
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION); // associationName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION); // forwardName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_ISA_ASSOCIATION); // reverseName
            insert.setString(k++, null); // inverseId
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isNavigable
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiTransitive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("false"), false); // isSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isAntiSymmetric
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isAntiReflexive
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isFunctional
            DBUtility.setBooleanOnPreparedStatment(insert, k++, null, false); // isReverseFunctional
            insert.setString(k++, "The parent child relationships."); // entityDescription
            
            insert.executeUpdate();
            insert.close();
        } catch (SQLException e) {
            _messages.fatalAndThrowException("ICDGEMToLex: loadConcepts: FATAL ERROR - Problem loading the hassubtype association definition", e);
        }

        
        

        insert = _sqlConnection.prepareStatement(_tableConstants.getInsertStatementSQL(SQLTableConstants.ENTITY));

        PreparedStatement insertIntoConceptProperty = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ENTITY_PROPERTY));

        PreparedStatement checkForCode = _sqlConnection.prepareStatement("SELECT count(*) as found from "
                + _tableConstants.getTableName(SQLTableConstants.ENTITY) + " WHERE "
                + _tableConstants.codingSchemeNameOrId + " = ? AND " + _tableConstants.entityCodeOrId + " = ?");

        PreparedStatement checkForDefinition = _sqlConnection.prepareStatement("SELECT count(*) as found from "
                + _tableConstants.getTableName(SQLTableConstants.ENTITY_PROPERTY) + " WHERE "
                + _tableConstants.codingSchemeNameOrId + " = ? AND " + _tableConstants.entityCodeOrEntityId
                + " = ? AND " + _tableConstants.propertyOrPropertyName + " = ?");
        
        // mct
        PreparedStatement insertIntoEntityType = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ENTITY_TYPE));


        _messages.info("ICDGEMToLex: loadConcepts: Loading coded entry and concept property");

        ArrayList<BaseConcept> concepts = codingScheme.getConcepts();
        BaseConcept concept = null;

        for (int i = 0; i < concepts.size(); i++) {
            try {
            	concept = concepts.get(i);
                checkForCode.setString(1, codingScheme.getCsName());
                checkForCode.setString(2, concepts.get(i).getCode());
                ResultSet results = checkForCode.executeQuery();
                // only one result
                results.next();
                if (results.getInt("found") == 0) {
                    if (concept.getCode() == null || concept.getCode().length() == 0) {
                        _messages.fatalAndThrowException("ICDGEMToLex: loadConcepts: FATAL ERROR - The concept '" + concept.getCode()
                                + "' is missing the name.  Name is required.");
                    }
                    // only add it to the codedEntry table if it is not already
                    // there.
                    /*
                    codingSchemeName, 
                    entityCodeNamespace, 
                    entityCode, 
                    isDefined, 
                    isAnonymous, 
                    isActive, 
                    entryStateId, 
                    entityDescription
                    */
                    int k = 1;
                    insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
                    insert.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
                    insert.setString(k++, concept.getCode()); // entityCode
                    DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isDefined
                    DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("false"), false); // isAnonymous
                    DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isActive
                    insert.setLong(k++, i); // entryStateId
                    insert.setString(k++, concept.getCode()); // entityDescription
                    insert.executeUpdate();
                    
                    
                    // mct: add concept to entitytype table
                    // insertStatements.put(ENTITY_TYPE, "INSERT INTO " + getTableName(ENTITY_TYPE) + " ("
                    //        + TBLCOL_CODINGSCHEMENAME + ", " + TBLCOL_ENTITYCODENAMESPACE + ", " + TBLCOL_ENTITYCODE + ", "
                    //        + TBLCOL_ENTITYTYPE + ") VALUES (?,?,?,?)");
                    k = 1;
                    insertIntoEntityType.setString(k++, codingScheme.getCsName()); // codingSchemeName
                    insertIntoEntityType.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
                    insertIntoEntityType.setString(k++, concept.getCode()); // entityCode
                    insertIntoEntityType.setString(k++, SQLTableConstants.ENTITYTYPE_CONCEPT); // entityType
                    insertIntoEntityType.executeUpdate();
                    

                    // also add the textualPresentation to the conceptProperty
                    // table
                    /*
                    codingSchemeName, 
                    entityCodeNamespace, 
                    entityCode, 
                    propertyId, 
                    propertyType, 
                    propertyName, 
                    language, 
                    format, 
                    isPreferred, 
                    degreeOfFidelity, 
                    matchIfNoContext, 
                    representationalForm, 
                    isActive, 
                    entryStateId, 
                    propertyValue
                    */

                    k = 1;
                    insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // codingSchemeName
                    insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
                    insertIntoConceptProperty.setString(k++, concept.getCode()); // entityCode
                    insertIntoConceptProperty.setString(k++, "p-1"); // propertyId
                    insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_PRESENTATION); // propertyType
                    insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_TEXTUALPRESENTATION); // propertyName
                    insertIntoConceptProperty.setString(k++, codingScheme.getDefaultLanguage()); // language
                    insertIntoConceptProperty.setString(k++, ""); // format
                    DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isPreferred
                    insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
                    DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("false"), false); // matchIfNoContext
                    insertIntoConceptProperty.setString(k++, ""); // representationalForm
                    DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
                    insertIntoConceptProperty.setLong(k++, i);  // entryStateId
                    insertIntoConceptProperty.setString(k++, concept.getCode()); // propertyValue
                    insertIntoConceptProperty.executeUpdate();
                }

                if (concept.getCode() != null && concept.getCode().length() > 0) {
                    // description to definition
                    // check for match first
                    checkForDefinition.setString(1, codingScheme.getCsName());
                    checkForDefinition.setString(2, concept.getCode());
                    checkForDefinition.setString(3, SQLTableConstants.TBLCOLVAL_DEFINITION);

                    results = checkForDefinition.executeQuery();
                    // always one result
                    results.next();
                    if (results.getInt("found") > 0) {
                        _messages.info("ICDGEMToLex: loadConcepts: WARNING - The concept code: '" + concept.getCode() + "' name: '"
                                + concept.getCode() + "' has multiple descriptions.  Skipping later descriptions.");
                    } else {
                        /*
                        codingSchemeName, 
                        entityCodeNamespace, 
                        entityCode, 
                        propertyId, 
                        propertyType, 
                        propertyName, 
                        language, 
                        format, 
                        isPreferred, 
                        degreeOfFidelity, 
                        matchIfNoContext, 
                        representationalForm, 
                        isActive, 
                        entryStateId, 
                        propertyValue
                        */

                        int k = 1;
                        insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // codingSchemeName
                        insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
                        insertIntoConceptProperty.setString(k++, concept.getCode()); // entityCode
                        insertIntoConceptProperty.setString(k++, "d-1"); // propertyId
                        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyType
                        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyName
                        insertIntoConceptProperty.setString(k++, codingScheme.getDefaultLanguage()); // language
                        insertIntoConceptProperty.setString(k++, ""); // format
                        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, null, false); // isPreferred
                        insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
                        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, null, false); // matchIfNoContext
                        insertIntoConceptProperty.setString(k++, ""); // representationalForm
                        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
                        insertIntoConceptProperty.setLong(k++, i); // entryStateId
                        insertIntoConceptProperty.setString(k++, concept.getCode()); // propertyValue
                        insertIntoConceptProperty.executeUpdate();
                    }
                }
                if (i % 10 == 0) {
                    _messages.busy();
                }
                results.close();
            } catch (Exception e) {
                _messages.fatalAndThrowException("ICDGEMToLex: loadConcepts: Problem loading concept code " + concept, e);
            }
        }

        // Add the special code
        /*
        codingSchemeName, 
        entityCodeNamespace, 
        entityCode, 
        isDefined, 
        isAnonymous, 
        isActive, 
        entryStateId, 
        entityDescription
        */
        RootConcept specialConcept = new RootConcept(_props);

        int k = 1;
        insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
        insert.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
        insert.setString(k++, specialConcept.getCode()); // entityCode
        DBUtility.setBooleanOnPreparedStatment(insert, k++, null); // isDefined
        DBUtility.setBooleanOnPreparedStatment(insert, k++, null); // isAnonymous
        DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isActive
        insert.setLong(k++, 0); // entryStateId
        insert.setString(k++, specialConcept.getCode()); // entityDescription
        insert.executeUpdate();
        
        /*
        codingSchemeName, 
        entityCodeNamespace, 
        entityCode, 
        propertyId, 
        propertyType, 
        propertyName, 
        language, 
        format, 
        isPreferred, 
        degreeOfFidelity, 
        matchIfNoContext, 
        representationalForm, 
        isActive, 
        entryStateId, 
        propertyValue
        */
        k = 1;
        insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // codingSchemeName
        insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
        insertIntoConceptProperty.setString(k++, specialConcept.getCode()); // entityCode
        insertIntoConceptProperty.setString(k++, "p-1"); // propertyId
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_PRESENTATION); // propertyType
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_TEXTUALPRESENTATION); // propertyName
        insertIntoConceptProperty.setString(k++, codingScheme.getDefaultLanguage()); // language
        insertIntoConceptProperty.setString(k++, ""); // format
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isPreferred
        insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, null, false); // matchIfNoContext
        insertIntoConceptProperty.setString(k++, ""); // representationalForm
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
        insertIntoConceptProperty.setLong(k++, 0); // entryStateId
        insertIntoConceptProperty.setString(k++, specialConcept.getCode()); // propertyValue

        insertIntoConceptProperty.executeUpdate();
        
        // mct: add concpet to entitytype table
        // insertStatements.put(ENTITY_TYPE, "INSERT INTO " + getTableName(ENTITY_TYPE) + " ("
        //        + TBLCOL_CODINGSCHEMENAME + ", " + TBLCOL_ENTITYCODENAMESPACE + ", " + TBLCOL_ENTITYCODE + ", "
        //        + TBLCOL_ENTITYTYPE + ") VALUES (?,?,?,?)");
        k = 1;
        insertIntoEntityType.setString(k++, codingScheme.getCsName()); // codingSchemeName
        insertIntoEntityType.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
        insertIntoEntityType.setString(k++, specialConcept.getCode()); // entityCode
        insertIntoEntityType.setString(k++, SQLTableConstants.ENTITYTYPE_CONCEPT); // entityType
        insertIntoEntityType.executeUpdate();
        

        /*
        codingSchemeName, 
        entityCodeNamespace, 
        entityCode, 
        propertyId, 
        propertyType, 
        propertyName, 
        language, 
        format, 
        isPreferred, 
        degreeOfFidelity, 
        matchIfNoContext, 
        representationalForm, 
        isActive, 
        entryStateId, 
        propertyValue
        */
        k = 1;
        insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // codingSchemeName
        insertIntoConceptProperty.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
        insertIntoConceptProperty.setString(k++, specialConcept.getCode()); // entityCode
        insertIntoConceptProperty.setString(k++, "d-1"); // propertyId
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyType
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyName
        insertIntoConceptProperty.setString(k++, codingScheme.getDefaultLanguage()); // language
        insertIntoConceptProperty.setString(k++, ""); // format
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("false"), false); // isPreferred
        insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("false"), false); // matchIfNoContext
        insertIntoConceptProperty.setString(k++, ""); // representationalForm
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
        insertIntoConceptProperty.setLong(k++, 0); // entryStateId        
        insertIntoConceptProperty.setString(k++, specialConcept.getCode()); // propertyValue
        
        insertIntoConceptProperty.executeUpdate();

        insert.close();
        insertIntoConceptProperty.close();
        insertIntoEntityType.close(); // mct
        checkForCode.close();
        checkForDefinition.close();
    }

    private void loadHasSubtypeRelations(CodingScheme codingScheme) throws Exception {
        _messages.info("ICDGEMToLex: loadHasSubtypeRelations: Loading relationships");
        PreparedStatement insertIntoConceptAssociations = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY));

        PreparedStatement checkForAssociation = _sqlConnection.prepareStatement("SELECT count(*) as found from "
                + _tableConstants.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) + " WHERE "
                + _tableConstants.codingSchemeNameOrId + " = ? AND " + _tableConstants.containerNameOrContainerDC
                + " = ? AND " + _tableConstants.entityCodeOrAssociationId + " = ? AND "
                + _tableConstants.sourceCSIdOrEntityCodeNS + " = ? AND " + _tableConstants.sourceEntityCodeOrId
                + " = ? AND " + _tableConstants.targetCSIdOrEntityCodeNS + " = ? AND "
                + _tableConstants.targetEntityCodeOrId + " = ?");

        ArrayList<Association> hasSubTypeAsso = codingScheme.getHasSubTypeAssociations();
        Association asso = null;
        for (int i = 0; i < hasSubTypeAsso.size(); i++) {
            try {
            	asso = hasSubTypeAsso.get(i);
                checkForAssociation.setString(1, codingScheme.getCsName());
                checkForAssociation.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
                checkForAssociation.setString(3, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
                checkForAssociation.setString(4, codingScheme.getCsName());
                checkForAssociation.setString(5, asso.getSourceCode());
                checkForAssociation.setString(6, codingScheme.getCsName());
                checkForAssociation.setString(7, asso.getTargetCode());
                ResultSet results = checkForAssociation.executeQuery();
                // always one result
                results.next();
                if (results.getInt("found") > 0) {
                    _messages.info("ICDGEMToLex: loadHasSubtypeRelations: WARNING - Relationship (" + asso.getSourceCode() + ") -- (" + asso.getTargetCode() + ") already exists.  Skipping.");
                    continue;
                }
                /*
                codingSchemeName, 
                containerName, 
                entityCodeNamespace, 
                entityCode, 
                sourceEntityCodeNamespace, 
                sourceEntityCode, 
                targetEntityCodeNamespace, 
                targetEntityCode, 
                multiAttributesKey, 
                associationInstanceId, 
                isDefining, 
                isInferred, 
                isActive, 
                entryStateId
                */
                insertIntoConceptAssociations.setString(1, codingScheme.getCsName()); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.getCsName()); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION); // entityCode
                insertIntoConceptAssociations.setString(5, codingScheme.getCsName()); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, asso.getSourceCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, codingScheme.getCsName()); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, asso.getTargetCode()); // targetEntityCode
                insertIntoConceptAssociations.setString(9, null); // multiAttributesKey
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId 
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, false, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, false, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, true, false); // isActive
                insertIntoConceptAssociations.setLong(14, i); // entryStateId                
                
                insertIntoConceptAssociations.executeUpdate();
                if (i % 10 == 0) {
                    _messages.busy();
                }
                results.close();
            } catch (SQLException e) {
                _messages.fatalAndThrowException("ICDGEMToLex: loadHasSubtypeRelations: Problem loading relationships for " + asso.toString(), e);
            }
        }

        insertIntoConceptAssociations.close();
        checkForAssociation.close();

    }
    
    private void loadMapsToRelations(CodingScheme codingScheme) throws Exception {
        _messages.info("ICDGEMToLex: loadMapsToRelations: Loading relationships");
        PreparedStatement insertIntoConceptAssociations = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY));

        PreparedStatement checkForAssociation = _sqlConnection.prepareStatement("SELECT count(*) as found from "
                + _tableConstants.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) + " WHERE "
                + _tableConstants.codingSchemeNameOrId + " = ? AND " + _tableConstants.containerNameOrContainerDC
                + " = ? AND " + _tableConstants.entityCodeOrAssociationId + " = ? AND "
                + _tableConstants.sourceCSIdOrEntityCodeNS + " = ? AND " + _tableConstants.sourceEntityCodeOrId
                + " = ? AND " + _tableConstants.targetCSIdOrEntityCodeNS + " = ? AND "
                + _tableConstants.targetEntityCodeOrId + " = ?");

        ArrayList<Association> mapsToAssociations = codingScheme.getMapsToAssociations();
        Association asso = null;
        for (int i = 0; i < mapsToAssociations.size(); i++) {
            try {
            	asso = mapsToAssociations.get(i);
                checkForAssociation.setString(1, codingScheme.getCsName());
                checkForAssociation.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
                checkForAssociation.setString(3, ICDGEMConstants.ASSOCIATION_HAS_SUBTYPE);
                checkForAssociation.setString(4, codingScheme.getCsName());
                checkForAssociation.setString(5, asso.getSourceCode());
                checkForAssociation.setString(6, codingScheme.getCsName());
                checkForAssociation.setString(7, asso.getTargetCode());
                ResultSet results = checkForAssociation.executeQuery();
                // always one result
                results.next();
                if (results.getInt("found") > 0) {
                    _messages.info("ICDGEMToLex: loadMapsToRelations: WARNING - Relationship (" + asso.getSourceCode() + ") -- (" + asso.getTargetCode() + ") already exists.  Skipping.");
                    continue;
                }
                /*
                codingSchemeName, 
                containerName, 
                entityCodeNamespace, 
                entityCode, 
                sourceEntityCodeNamespace, 
                sourceEntityCode, 
                targetEntityCodeNamespace, 
                targetEntityCode, 
                multiAttributesKey, 
                associationInstanceId, 
                isDefining, 
                isInferred, 
                isActive, 
                entryStateId
                */
                insertIntoConceptAssociations.setString(1, codingScheme.getCsName()); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.getCsName()); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, ICDGEMConstants.ASSOCIATION_HAS_SUBTYPE); // entityCode
                insertIntoConceptAssociations.setString(5, codingScheme.getCsName()); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, asso.getSourceCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, codingScheme.getCsName()); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, asso.getTargetCode()); // targetEntityCode
                insertIntoConceptAssociations.setString(9, null); // multiAttributesKey
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId 
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, false, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, false, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, true, false); // isActive
                insertIntoConceptAssociations.setLong(14, i); // entryStateId                
                
                insertIntoConceptAssociations.executeUpdate();
                if (i % 10 == 0) {
                    _messages.busy();
                }
                results.close();
            } catch (SQLException e) {
                _messages.fatalAndThrowException("ICDGEMToLex: loadMapsToRelations: Problem loading relationships for " + asso.toString(), e);
            }
        }

        insertIntoConceptAssociations.close();
        checkForAssociation.close();

    }
    
    private void loadContainsRelations(CodingScheme codingScheme) throws Exception {
        _messages.info("ICDGEMToLex: loadContainsRelations: Loading relationships");
        PreparedStatement insertIntoConceptAssociations = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY));

        PreparedStatement checkForAssociation = _sqlConnection.prepareStatement("SELECT count(*) as found from "
                + _tableConstants.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) + " WHERE "
                + _tableConstants.codingSchemeNameOrId + " = ? AND " + _tableConstants.containerNameOrContainerDC
                + " = ? AND " + _tableConstants.entityCodeOrAssociationId + " = ? AND "
                + _tableConstants.sourceCSIdOrEntityCodeNS + " = ? AND " + _tableConstants.sourceEntityCodeOrId
                + " = ? AND " + _tableConstants.targetCSIdOrEntityCodeNS + " = ? AND "
                + _tableConstants.targetEntityCodeOrId + " = ?");

        ArrayList<Association> containsAsso = codingScheme.getHasSubTypeAssociations();
        Association asso = null;
        for (int i = 0; i < containsAsso.size(); i++) {
            try {
            	asso = containsAsso.get(i);
                checkForAssociation.setString(1, codingScheme.getCsName());
                checkForAssociation.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
                checkForAssociation.setString(3, ICDGEMConstants.ASSOCIATION_CONTAINS);
                checkForAssociation.setString(4, codingScheme.getCsName());
                checkForAssociation.setString(5, asso.getSourceCode());
                checkForAssociation.setString(6, codingScheme.getCsName());
                checkForAssociation.setString(7, asso.getTargetCode());
                ResultSet results = checkForAssociation.executeQuery();
                // always one result
                results.next();
                if (results.getInt("found") > 0) {
                    _messages.info("ICDGEMToLex: loadContainsRelations: WARNING - Relationship (" + asso.getSourceCode() + ") -- (" + asso.getTargetCode() + ") already exists.  Skipping.");
                    continue;
                }
                /*
                codingSchemeName, 
                containerName, 
                entityCodeNamespace, 
                entityCode, 
                sourceEntityCodeNamespace, 
                sourceEntityCode, 
                targetEntityCodeNamespace, 
                targetEntityCode, 
                multiAttributesKey, 
                associationInstanceId, 
                isDefining, 
                isInferred, 
                isActive, 
                entryStateId
                */
                insertIntoConceptAssociations.setString(1, codingScheme.getCsName()); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.getCsName()); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, ICDGEMConstants.ASSOCIATION_CONTAINS); // entityCode
                insertIntoConceptAssociations.setString(5, codingScheme.getCsName()); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, asso.getSourceCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, codingScheme.getCsName()); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, asso.getTargetCode()); // targetEntityCode
                insertIntoConceptAssociations.setString(9, null); // multiAttributesKey
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId 
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, false, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, false, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, true, false); // isActive
                insertIntoConceptAssociations.setLong(14, i); // entryStateId                
                
                insertIntoConceptAssociations.executeUpdate();
                if (i % 10 == 0) {
                    _messages.busy();
                }
                results.close();
            } catch (SQLException e) {
                _messages.fatalAndThrowException("ICDGEMToLex: loadContainsRelations: Problem loading relationships for " + asso.toString(), e);
            }
        }

        insertIntoConceptAssociations.close();
        checkForAssociation.close();

    }
    


    private void loadRelations(CodingScheme codingScheme, ArrayList<Association> associations) throws Exception {
        TreeSet<String> relationNameSet = new TreeSet<String>();
        _messages.info("ICDGEMToLex: loadRelations: Loading relationships");
        PreparedStatement insertIntoConceptAssociations = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY));

        PreparedStatement insert = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ASSOCIATION));
        Association association = null;

        for (int i = 0; i < associations.size(); i++) {
            try {
            	association = associations.get(i);
                relationNameSet.add(association.getRelationName());
                /*
                 * 1  codingSchemeName
                 * 2  containerName
                 * 3  entityCodeNamespace
                 * 4  entityCode
                 * 5  sourceEntityCodeNamespace
                 * 6  sourceEntityCode
                 * 7  targetEntityCodeNamespace
                 * 8  targetEntityCode
                 * 9  multiAttributesKey
                 * 10 associationInstanceId
                 * 11 isDefining
                 * 12 isInferred
                 * 13 isActive
                 * 14 entryStateId
                 */
                insertIntoConceptAssociations.setString(1, codingScheme.getCsName()); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.getCsId()); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, association.getRelationName()); // entityCode
                insertIntoConceptAssociations.setString(5, association.getSourceCodingScheme()); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, association.getSourceCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, association.getTargetCodingScheme()); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, association.getTargetCode()); // targetEntityCode
                insertIntoConceptAssociations.setString(9, null); // multiAttributesKey
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, null, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, null, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, null, false); // isActive
                insertIntoConceptAssociations.setLong(14, i); // entryStateId

                insertIntoConceptAssociations.executeUpdate();
                if (i % 10 == 0) {
                    _messages.busy();
                }

            } catch (SQLException e) {
                _messages.fatalAndThrowException("ICDGEMToLex: loadRelations: Problem loading relationships for " + association, e);
            }
        }

        for (Iterator<String> i = relationNameSet.iterator(); i.hasNext();) {
            _messages.info("ICDGEMToLex: loadRelations: Loading coding scheme supported attributes");
            insert = _sqlConnection.prepareStatement(_tableConstants
                    .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME_SUPPORTED_ATTRIBUTES));

            String rel_name = i.next().toString();
            
            /*
             * INSERT INTO codingSchemeSupportedAttrib (codingSchemeName, supportedAttributeTag, id, uri, idValue, val1, val2) VALUES ('ICD-10-to-9-CM-GEM','Association','mapsTo','','','','')"
             * codingSchemeName
             * supportedAttributeTag
             * id
             * uri
             * idValue
             * val1
             * val2
             */
            
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, rel_name);
            insert.setString(4, "");
            insert.setString(5, "");
            insert.setString(6, "");
            insert.setString(7, "");

            try {
                insert.executeUpdate();
            } catch (SQLException ex) {
                _messages.fatalAndThrowException("ICDGEMToLex: loadRelations: Problem loading supportedAssociation for " + rel_name, ex);
            }

            /*
             * codingSchemeName
             * containerName
             * entityCodeNamespace
             * entityCode
             * associationName
             * forwardName
             * reverseName
             * inverseId
             * isNavigable
             * isTransitive
             * isAntiTransitive
             * isSymmetric
             * isAntiSymmetric
             * isReflexive
             * isAntiReflexive
             * isFunctional
             * isReverseFunctional
             * entityDescription
             */
            insert = _sqlConnection.prepareStatement(_tableConstants
                    .getInsertStatementSQL(SQLTableConstants.ASSOCIATION));
            insert.setString(1, codingScheme.getCsName()); // codingSchemeName
            insert.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            // entityCodeNamespace
            // entityCode
            // sourceEntityCodeNamespace
            // sourceEntityCode
            // targetEntityCodeNamespace
            // targetEntityCode
            // multiAttributesKey
            // associationInstanceId
            // isDefining
            // isInferred
            // isActive
            // isActive
            // entryStateId
            insert.setString(3, rel_name);  
            insert.setString(4, rel_name);
            insert.setString(5, "");
            insert.setString(6, "");

            // TODO deal with isNavigable, inverse
            // properly - what should they be?
            DBUtility.setBooleanOnPreparedStatment(insert, 7, new Boolean("true"), false);
            DBUtility.setBooleanOnPreparedStatment(insert, 8, new Boolean("true"), false);
            DBUtility.setBooleanOnPreparedStatment(insert, 9, new Boolean("true"), false);
            DBUtility.setBooleanOnPreparedStatment(insert, 10, null, false);
            DBUtility.setBooleanOnPreparedStatment(insert, 11, null, false);
            DBUtility.setBooleanOnPreparedStatment(insert, 12, null, false);
            DBUtility.setBooleanOnPreparedStatment(insert, 13, null, false);
            DBUtility.setBooleanOnPreparedStatment(insert, 14, null, false);
            DBUtility.setBooleanOnPreparedStatment(insert, 15, null, false);
            DBUtility.setBooleanOnPreparedStatment(insert, 16, null, false);
            insert.setString(17, "");
            insert.setString(18, "");
            try {
                insert.executeUpdate();
            } catch (SQLException ex) {
                _messages.fatalAndThrowException("ICDGEMToLex: loadRelations: Problem loading Association for " + rel_name, ex);
            }

        }
        insert.close();
        insertIntoConceptAssociations.close();

    }
}