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
import java.util.Iterator;
import java.util.TreeSet;

import org.LexGrid.LexBIG.Preferences.loader.LoadPreferences.LoaderPreferences;
import org.LexGrid.messaging.LgMessageDirectorIF;
import org.LexGrid.util.sql.DBUtility;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.LexGrid.util.sql.lgTables.SQLTableUtilities;

import org.lexgrid.extension.loaders.icdgem.utils.Association;
import org.lexgrid.extension.loaders.icdgem.utils.BaseConcept;
import org.lexgrid.extension.loaders.icdgem.utils.CodingScheme;
import org.lexgrid.extension.loaders.icdgem.utils.ICDConceptFactory;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;
import org.lexgrid.extension.loaders.icdgem.utils.TextUtility;

/**
 * Conversion tool for loading ICD GEM files into SQL.
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 * @version subversion $Revision: 8756 $ checked in on $Date: 2007-08-30
 *          17:13:22 +0000 (Thu, 30 Aug 2007) $
 */
public class ICDGEMToLex {
    private String token_ = "\t";
    private LgMessageDirectorIF messages_;
    private Connection sqlConnection_;
    private SQLTableUtilities tableUtility_;
    private SQLTableConstants tableConstants_;
    private BaseConcept specialConcept = null;
    private CodingScheme codingScheme_;

    /**
     * @return the codingSchemeName
     */
    public CodingScheme getCodingScheme() {
        return this.codingScheme_;
    }
    
    public ICDGEMToLex(String fileLocation, String token, String sqlServer, String sqlDriver,
            String sqlUsername, String sqlPassword, String tablePrefix, LoaderPreferences loaderPrefs,
            LgMessageDirectorIF messageDirector, ICDGEMProperties props) throws Exception {
        messages_ = messageDirector;
        if (token != null && token.length() > 0) {
            token_ = token;
        }

        // this verifies all of the rules except the description rules - and
        // determines A or B.
        codingScheme_ = TextUtility.readAndVerifyConcepts(fileLocation, messages_, token_, props);

        // set up the sql tables
        prepareDatabase(codingScheme_.codingSchemeName, sqlServer, sqlDriver, sqlUsername, sqlPassword,
                tablePrefix);

        tableConstants_ = tableUtility_.getSQLTableConstants();

        // load the concepts, verify the description status.
        loadConcepts(codingScheme_);

        loadHasSubtypeRelations(codingScheme_);
        loadRelations(codingScheme_);

        sqlConnection_.close();

    }

    private void prepareDatabase(String codingScheme, String sqlServer, String sqlDriver, String sqlUsername,
            String sqlPassword, String tablePrefix) throws Exception {
        try {
            messages_.info("ICD10ToLex: prepareDatabase: Connecting to database");
            sqlConnection_ = DBUtility.connectToDatabase(sqlServer, sqlDriver, sqlUsername, sqlPassword);
            // gsm_ = new GenericSQLModifier(sqlConnection_);
        } catch (ClassNotFoundException e) {
            messages_
                    .fatalAndThrowException("ICD10ToLex: prepareDatabase: FATAL ERROR - The class you specified for your sql driver could not be found on the path.");
        }

        tableUtility_ = new SQLTableUtilities(sqlConnection_, tablePrefix);

        messages_.info("ICD10ToLex: prepareDatabase: Creating tables");
        tableUtility_.createDefaultTables();

        messages_.info("ICD10ToLex: prepareDatabase: Creating constraints");
        tableUtility_.createDefaultTableConstraints();

        messages_.info("ICD10ToLex: prepareDatabase: Cleaning tables");
        tableUtility_.cleanTables(codingScheme);
    }

    private void loadConcepts(CodingScheme codingScheme) throws Exception {
        PreparedStatement insert = sqlConnection_.prepareStatement(tableConstants_
                .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME));

        messages_.info("ICD10ToLex: loadConcepts: Loading coding scheme");
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

        insert.setString(ii++, codingScheme.codingSchemeName); // codingSchemeName
        insert.setString(ii++, codingScheme.codingSchemeId); // codingSchemeURI
        insert.setString(ii++, codingScheme.representsVersion); // representsVersion
        insert.setString(ii++, codingScheme.formalName); // formalName
        insert.setString(ii++, codingScheme.defaultLanguage); // defaultLanguage
        insert.setInt(ii++, codingScheme.concepts.length); // approxNumConcepts
        DBUtility.setBooleanOnPreparedStatment(insert, ii++, new Boolean(false)); // isActive
        insert.setInt(ii++, 0); // entryStateId
        insert.setString(ii++, SQLTableConstants.TBLCOLVAL_MISSING); // releaseURI
        insert.setString(ii++, codingScheme.entityDescription); // entityDescription
        insert.setString(ii++, codingScheme.copyright); // entityDescription

        try {
            insert.executeUpdate();
        } catch (SQLException e) {
            messages_.fatalAndThrowException(
                    "ICD10ToLex: loadConcepts: FATAL ERROR - It is likely that your coding scheme name or CodingSchemeId is not unique.", e);
        }

        insert.close();

        try {
            messages_.info("ICD10ToLex: loadConcepts: Loading coding scheme supported attributes");
            insert = sqlConnection_.prepareStatement(tableConstants_
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
            insert.setString(1, codingScheme.codingSchemeName); // codingSchemeName
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_LANGUAGE); // supportedAttributeTag
            insert.setString(3, codingScheme.defaultLanguage); // id
            insert.setString(4, ""); // uri
            if (tableConstants_.supports2009Model()) {
                insert.setString(5, codingScheme.defaultLanguage); // idValue
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING); // val1
                insert.setString(7, ""); // val2
            }
            insert.executeUpdate();
            
            // mct test
            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, SQLTableConstants.TBLCOLVAL_ISA_ASSOCIATION);
            insert.setString(4, "");
            if (tableConstants_.supports2009Model()) {
                insert.setString(5, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }            
            insert.executeUpdate();
            
            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
            insert.setString(4, "");
            if (tableConstants_.supports2009Model()) {
                insert.setString(5, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }
            insert.executeUpdate();

            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_CODINGSCHEME);
            insert.setString(3, codingScheme.codingSchemeName);
            insert.setString(4, codingScheme.codingSchemeId);
            if (tableConstants_.supports2009Model()) {
                insert.setString(5, codingScheme.codingSchemeName);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }

            insert.executeUpdate();

            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_PROPERTY);
            insert.setString(3, SQLTableConstants.TBLCOLVAL_DEFINITION);
            insert.setString(4, "");
            if (tableConstants_.supports2009Model()) {
                insert.setString(5, SQLTableConstants.TBLCOLVAL_DEFINITION);
                insert.setString(6, SQLTableConstants.TBLCOLVAL_MISSING);
                insert.setString(7, "");
            }

            insert.executeUpdate();
            insert.close();

            insert = sqlConnection_.prepareStatement(tableConstants_
                    .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME_MULTI_ATTRIBUTES));

            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_LOCALNAME);
            insert.setString(3, codingScheme.codingSchemeName);
            if (tableConstants_.supports2009Model()) {
                insert.setString(4, "");
                insert.setString(5, "");
            }

            insert.executeUpdate();

            String codingSchemeIdTemp = codingScheme.codingSchemeId;
            int temp = codingSchemeIdTemp.lastIndexOf(':');
            if (temp > 0 && ((temp + 1) <= codingSchemeIdTemp.length())) {
                codingSchemeIdTemp = codingSchemeIdTemp.substring(temp + 1);
            }

            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_LOCALNAME);
            insert.setString(3, codingSchemeIdTemp);
            if (tableConstants_.supports2009Model()) {
                insert.setString(4, "");
                insert.setString(5, "");
            }

            insert.executeUpdate();

            if (codingScheme.source != null && codingScheme.source.length() > 0) {
                insert.setString(1, codingScheme.codingSchemeName);
                insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_SOURCE);
                insert.setString(3, codingScheme.source);
                if (tableConstants_.supports2009Model()) {
                    insert.setString(4, "");
                    insert.setString(5, "");
                }

                insert.executeUpdate();
            }
            insert.close();
        } catch (SQLException e) {
            messages_.fatalAndThrowException("ICD10ToLex: loadConcepts: FATAL ERROR - Problem loading the coding scheme supported attributes", e);
        }

        messages_.info("Loading relation definition");
        try {
            insert = sqlConnection_.prepareStatement(tableConstants_.getInsertStatementSQL(SQLTableConstants.RELATION));
            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
            DBUtility.setBooleanOnPreparedStatment(insert, 3, new Boolean("true"), false);
            insert.setString(4, "");

            insert.executeUpdate();
            insert.close();
        } catch (SQLException e) {
            messages_.fatalAndThrowException("ICD10ToLex: loadConcepts: FATAL ERROR - Problem loading the relation definition", e);
        }

        messages_.info("ICD10ToLex: loadConcepts: Loading isa association definition");
        try {
            insert = sqlConnection_.prepareStatement(tableConstants_
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
            insert.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
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
            insert.close();
        } catch (SQLException e) {
            messages_.fatalAndThrowException("ICD10ToLex: loadConcepts: FATAL ERROR - Problem loading the association definition", e);
        }
        
        messages_.info("ICD10ToLex: loadConcepts: Loading hassubtype association definition");
        try {
            insert = sqlConnection_.prepareStatement(tableConstants_
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
            insert.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
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
            messages_.fatalAndThrowException("ICD10ToLex: loadConcepts: FATAL ERROR - Problem loading the hassubtype association definition", e);
        }

        
        

        insert = sqlConnection_.prepareStatement(tableConstants_.getInsertStatementSQL(SQLTableConstants.ENTITY));

        PreparedStatement insertIntoConceptProperty = sqlConnection_.prepareStatement(tableConstants_
                .getInsertStatementSQL(SQLTableConstants.ENTITY_PROPERTY));

        PreparedStatement checkForCode = sqlConnection_.prepareStatement("SELECT count(*) as found from "
                + tableConstants_.getTableName(SQLTableConstants.ENTITY) + " WHERE "
                + tableConstants_.codingSchemeNameOrId + " = ? AND " + tableConstants_.entityCodeOrId + " = ?");

        PreparedStatement checkForDefinition = sqlConnection_.prepareStatement("SELECT count(*) as found from "
                + tableConstants_.getTableName(SQLTableConstants.ENTITY_PROPERTY) + " WHERE "
                + tableConstants_.codingSchemeNameOrId + " = ? AND " + tableConstants_.entityCodeOrEntityId
                + " = ? AND " + tableConstants_.propertyOrPropertyName + " = ?");
        
        // mct
        PreparedStatement insertIntoEntityType = sqlConnection_.prepareStatement(tableConstants_
                .getInsertStatementSQL(SQLTableConstants.ENTITY_TYPE));


        messages_.info("ICD10ToLex: loadConcepts: Loading coded entry and concept property");

        BaseConcept[] concepts = codingScheme.concepts;

        for (int i = 0; i < concepts.length; i++) {
            try {
                checkForCode.setString(1, codingScheme.codingSchemeName);
                checkForCode.setString(2, concepts[i].getCode());
                ResultSet results = checkForCode.executeQuery();
                // only one result
                results.next();
                if (results.getInt("found") == 0) {
                    if (concepts[i].getName() == null || concepts[i].getName().length() == 0) {
                        messages_.fatalAndThrowException("ICD10ToLex: loadConcepts: FATAL ERROR - The concept '" + concepts[i].getCode()
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
                    insert.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
                    insert.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
                    insert.setString(k++, concepts[i].getCode()); // entityCode
                    DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isDefined
                    DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("false"), false); // isAnonymous
                    DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isActive
                    insert.setLong(k++, i); // entryStateId
                    insert.setString(k++, concepts[i].getDescription()); // entityDescription
                    insert.executeUpdate();
                    
                    
                    // mct: add concept to entitytype table
                    // insertStatements.put(ENTITY_TYPE, "INSERT INTO " + getTableName(ENTITY_TYPE) + " ("
                    //        + TBLCOL_CODINGSCHEMENAME + ", " + TBLCOL_ENTITYCODENAMESPACE + ", " + TBLCOL_ENTITYCODE + ", "
                    //        + TBLCOL_ENTITYTYPE + ") VALUES (?,?,?,?)");
                    k = 1;
                    insertIntoEntityType.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
                    insertIntoEntityType.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
                    insertIntoEntityType.setString(k++, concepts[i].getCode()); // entityCode
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
                    insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
                    insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
                    insertIntoConceptProperty.setString(k++, concepts[i].getCode()); // entityCode
                    insertIntoConceptProperty.setString(k++, "p-1"); // propertyId
                    insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_PRESENTATION); // propertyType
                    insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_TEXTUALPRESENTATION); // propertyName
                    insertIntoConceptProperty.setString(k++, codingScheme.defaultLanguage); // language
                    insertIntoConceptProperty.setString(k++, ""); // format
                    DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isPreferred
                    insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
                    DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("false"), false); // matchIfNoContext
                    insertIntoConceptProperty.setString(k++, ""); // representationalForm
                    DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
                    insertIntoConceptProperty.setLong(k++, i);  // entryStateId
                    insertIntoConceptProperty.setString(k++, concepts[i].getDescription()); // propertyValue
                    insertIntoConceptProperty.executeUpdate();
                }

                if (concepts[i].getDescription() != null && concepts[i].getDescription().length() > 0) {
                    // description to definition
                    // check for match first
                    checkForDefinition.setString(1, codingScheme.codingSchemeName);
                    checkForDefinition.setString(2, concepts[i].getCode());
                    checkForDefinition.setString(3, SQLTableConstants.TBLCOLVAL_DEFINITION);

                    results = checkForDefinition.executeQuery();
                    // always one result
                    results.next();
                    if (results.getInt("found") > 0) {
                        messages_.info("ICD10ToLex: loadConcepts: WARNING - The concept code: '" + concepts[i].getCode() + "' name: '"
                                + concepts[i].getName() + "' has multiple descriptions.  Skipping later descriptions.");
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
                        insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
                        insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
                        insertIntoConceptProperty.setString(k++, concepts[i].getCode()); // entityCode
                        insertIntoConceptProperty.setString(k++, "d-1"); // propertyId
                        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyType
                        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyName
                        insertIntoConceptProperty.setString(k++, codingScheme.defaultLanguage); // language
                        insertIntoConceptProperty.setString(k++, ""); // format
                        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, null, false); // isPreferred
                        insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
                        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, null, false); // matchIfNoContext
                        insertIntoConceptProperty.setString(k++, ""); // representationalForm
                        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
                        insertIntoConceptProperty.setLong(k++, i); // entryStateId
                        insertIntoConceptProperty.setString(k++, concepts[i].getDescription()); // propertyValue
                        insertIntoConceptProperty.executeUpdate();
                    }
                }
                if (i % 10 == 0) {
                    messages_.busy();
                }
                results.close();
            } catch (Exception e) {
                messages_.fatalAndThrowException("ICD10ToLex: loadConcepts: Problem loading concept code " + concepts[i], e);
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

        int k = 1;
        insert.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
        insert.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
        insert.setString(k++, specialConcept.getCode()); // entityCode
        DBUtility.setBooleanOnPreparedStatment(insert, k++, null); // isDefined
        DBUtility.setBooleanOnPreparedStatment(insert, k++, null); // isAnonymous
        DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isActive
        insert.setLong(k++, 0); // entryStateId
        insert.setString(k++, specialConcept.getName()); // entityDescription
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
        insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
        insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
        insertIntoConceptProperty.setString(k++, specialConcept.getCode()); // entityCode
        insertIntoConceptProperty.setString(k++, "p-1"); // propertyId
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_PRESENTATION); // propertyType
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_TEXTUALPRESENTATION); // propertyName
        insertIntoConceptProperty.setString(k++, codingScheme.defaultLanguage); // language
        insertIntoConceptProperty.setString(k++, ""); // format
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isPreferred
        insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, null, false); // matchIfNoContext
        insertIntoConceptProperty.setString(k++, ""); // representationalForm
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
        insertIntoConceptProperty.setLong(k++, 0); // entryStateId
        insertIntoConceptProperty.setString(k++, specialConcept.getName()); // propertyValue

        insertIntoConceptProperty.executeUpdate();
        
        // mct: add concpet to entitytype table
        // insertStatements.put(ENTITY_TYPE, "INSERT INTO " + getTableName(ENTITY_TYPE) + " ("
        //        + TBLCOL_CODINGSCHEMENAME + ", " + TBLCOL_ENTITYCODENAMESPACE + ", " + TBLCOL_ENTITYCODE + ", "
        //        + TBLCOL_ENTITYTYPE + ") VALUES (?,?,?,?)");
        k = 1;
        insertIntoEntityType.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
        insertIntoEntityType.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
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
        insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // codingSchemeName
        insertIntoConceptProperty.setString(k++, codingScheme.codingSchemeName); // entityCodeNamespace
        insertIntoConceptProperty.setString(k++, specialConcept.getCode()); // entityCode
        insertIntoConceptProperty.setString(k++, "d-1"); // propertyId
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyType
        insertIntoConceptProperty.setString(k++, SQLTableConstants.TBLCOLVAL_DEFINITION); // propertyName
        insertIntoConceptProperty.setString(k++, codingScheme.defaultLanguage); // language
        insertIntoConceptProperty.setString(k++, ""); // format
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("false"), false); // isPreferred
        insertIntoConceptProperty.setString(k++, ""); // degreeOfFidelity
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("false"), false); // matchIfNoContext
        insertIntoConceptProperty.setString(k++, ""); // representationalForm
        DBUtility.setBooleanOnPreparedStatment(insertIntoConceptProperty, k++, new Boolean("true"), false); // isActive
        insertIntoConceptProperty.setLong(k++, 0); // entryStateId        
        insertIntoConceptProperty.setString(k++, specialConcept.getDescription()); // propertyValue
        
        insertIntoConceptProperty.executeUpdate();

        insert.close();
        insertIntoConceptProperty.close();
        insertIntoEntityType.close(); // mct
        checkForCode.close();
        checkForDefinition.close();
    }

    private void loadHasSubtypeRelations(CodingScheme codingScheme) throws Exception {
        messages_.info("ICD10ToLex: loadHasSubtypeRelations: Loading relationships");
        PreparedStatement insertIntoConceptAssociations = sqlConnection_.prepareStatement(tableConstants_
                .getInsertStatementSQL(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY));

        PreparedStatement checkForAssociation = sqlConnection_.prepareStatement("SELECT count(*) as found from "
                + tableConstants_.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) + " WHERE "
                + tableConstants_.codingSchemeNameOrId + " = ? AND " + tableConstants_.containerNameOrContainerDC
                + " = ? AND " + tableConstants_.entityCodeOrAssociationId + " = ? AND "
                + tableConstants_.sourceCSIdOrEntityCodeNS + " = ? AND " + tableConstants_.sourceEntityCodeOrId
                + " = ? AND " + tableConstants_.targetCSIdOrEntityCodeNS + " = ? AND "
                + tableConstants_.targetEntityCodeOrId + " = ?");

        Concept[] concepts = codingScheme.concepts;

        for (int i = 0; i < concepts.length; i++) {
            try {
                Concept parent = TextUtility.getParent(concepts, i);
                if (parent == null) {
                    parent = specialConcept;
                }
                checkForAssociation.setString(1, codingScheme.codingSchemeName);
                checkForAssociation.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
                checkForAssociation.setString(3, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION);
                checkForAssociation.setString(4, codingScheme.codingSchemeName);
                checkForAssociation.setString(5, parent.getCode());
                checkForAssociation.setString(6, codingScheme.codingSchemeName);
                checkForAssociation.setString(7, concepts[i].getCode());
                ResultSet results = checkForAssociation.executeQuery();
                // always one result
                results.next();
                if (results.getInt("found") > 0) {
                    messages_.info("ICD10ToLex: loadHasSubtypeRelations: WARNING - Relationship '" + parent.getCode() + "' (" + parent.getName() + ") to '"
                            + concepts[i].getCode() + "' (" + concepts[i].getName() + ") already exists.  Skipping.");
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
                insertIntoConceptAssociations.setString(1, codingScheme.codingSchemeName); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.codingSchemeName); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION); // entityCode
                insertIntoConceptAssociations.setString(5, codingScheme.codingSchemeName); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, parent.getCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, codingScheme.codingSchemeName); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, concepts[i].getCode()); // targetEntityCode
                insertIntoConceptAssociations.setString(9, null); // multiAttributesKey
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId 
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, false, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, false, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, true, false); // isActive
                insertIntoConceptAssociations.setLong(14, i); // entryStateId                
                
                insertIntoConceptAssociations.executeUpdate();
                if (i % 10 == 0) {
                    messages_.busy();
                }
                results.close();
            } catch (SQLException e) {
                messages_.fatalAndThrowException("ICD10ToLex: loadHasSubtypeRelations: Problem loading relationships for " + concepts[i], e);
            }
        }

        insertIntoConceptAssociations.close();
        checkForAssociation.close();

    }

    private void loadRelations(CodingScheme codingScheme) throws Exception {
        TreeSet relationNameSet = new TreeSet();
        messages_.info("ICD10ToLex: loadRelations: Loading relationships");
        PreparedStatement insertIntoConceptAssociations = sqlConnection_.prepareStatement(tableConstants_
                .getInsertStatementSQL(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY));

        PreparedStatement insert = sqlConnection_.prepareStatement(tableConstants_
                .getInsertStatementSQL(SQLTableConstants.ASSOCIATION));
        Association[] associations = codingScheme.associations;

        for (int i = 0; i < associations.length; i++) {
            try {
                relationNameSet.add(associations[i].getRelationName());
                insertIntoConceptAssociations.setString(1, codingScheme.codingSchemeName);
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
                insertIntoConceptAssociations.setString(3, associations[i].getRelationName());
                insertIntoConceptAssociations.setString(4, associations[i].getSourceCodingScheme());
                insertIntoConceptAssociations.setString(5, SQLTableConstants.ENTITYTYPE_CONCEPT);
                insertIntoConceptAssociations.setString(6, associations[i].getSourceCode());
                insertIntoConceptAssociations.setString(7, associations[i].getTargetCodingScheme());
                insertIntoConceptAssociations.setString(8, SQLTableConstants.ENTITYTYPE_CONCEPT);
                insertIntoConceptAssociations.setString(9, associations[i].getTargetCode());
                insertIntoConceptAssociations.setString(10, null);
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 9, null, false);
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 10, null, false);
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, null, false);

                insertIntoConceptAssociations.executeUpdate();
                if (i % 10 == 0) {
                    messages_.busy();
                }

            } catch (SQLException e) {
                messages_.fatalAndThrowException("ICD10ToLex: loadRelations: Problem loading relationships for " + associations[i], e);
            }
        }

        for (Iterator i = relationNameSet.iterator(); i.hasNext();) {
            messages_.info("ICD10ToLex: loadRelations: Loading coding scheme supported attributes");
            insert = sqlConnection_.prepareStatement(tableConstants_
                    .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME_SUPPORTED_ATTRIBUTES));

            String rel_name = i.next().toString();
            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_ASSOCIATION);
            insert.setString(3, rel_name);
            insert.setString(4, "");
            insert.setString(5, "");
            insert.setString(6, "");
            insert.setString(7, "");

            try {
                insert.executeUpdate();
            } catch (SQLException ex) {
                messages_.fatalAndThrowException("ICD10ToLex: loadRelations: Problem loading supportedAssociation for " + rel_name, ex);
            }

            insert = sqlConnection_.prepareStatement(tableConstants_
                    .getInsertStatementSQL(SQLTableConstants.ASSOCIATION));
            insert.setString(1, codingScheme.codingSchemeName);
            insert.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
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
                messages_.fatalAndThrowException("ICD10ToLex: loadRelations: Problem loading Association for " + rel_name, ex);
            }

        }
        insert.close();
        insertIntoConceptAssociations.close();

    }
}