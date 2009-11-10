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
import java.util.UUID;

import org.LexGrid.managedobj.InsertException;
import org.LexGrid.managedobj.ObjectAlreadyExistsException;
import org.LexGrid.messaging.LgMessageDirectorIF;
import org.LexGrid.util.sql.DBUtility;
import org.LexGrid.util.sql.GenericSQLModifier;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.LexGrid.util.sql.lgTables.SQLTableUtilities;

import org.apache.commons.lang.StringUtils;
import org.lexgrid.extension.loaders.icdgem.utils.LexEntryStateHelper;
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
    private PreparedStatement _insertIntoEntryState;
    private GenericSQLModifier _sqlModifier;
    private boolean _lexGridPost50 = false;

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
        _messages.info("ICDGEMToLex: Loader Version: " + _props.getLoaderVersion());        
        _lexGridPost50 = props.lexGridPost50();
        _codingScheme = FileProcessor.process(fileLocation, props);

        prepareDatabase(_codingScheme.getCsName(), sqlServer, sqlDriver, sqlUsername, sqlPassword,
                tablePrefix);
        
        prepCodingScheme(_codingScheme);
        loadConcepts(_codingScheme);
        loadHasSubtypeRelations(_codingScheme);
        loadMapsToRelations(_codingScheme);
        loadContainsRelations(_codingScheme);
        _insertIntoEntryState.close();
        _sqlConnection.close();
    }

    private void prepareDatabase(String codingScheme, String sqlServer, String sqlDriver, String sqlUsername,
            String sqlPassword, String tablePrefix) throws Exception {
        try {
            _messages.info("ICDGEMToLex: prepareDatabase: Connecting to database");
            _sqlConnection = DBUtility.connectToDatabase(sqlServer, sqlDriver, sqlUsername, sqlPassword);
            _sqlModifier = new GenericSQLModifier(_sqlConnection);
            // gsm_ = new GenericSQLModifier(_sqlConnection);
        } catch (ClassNotFoundException e) {
            _messages
                    .fatalAndThrowException("ICDGEMToLex: prepareDatabase: FATAL ERROR - The class you specified for your sql driver could not be found on the path.");
        }

        _tableUtility = new SQLTableUtilities(_sqlConnection, tablePrefix);
        _tableConstants = _tableUtility.getSQLTableConstants();
        _insertIntoEntryState = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.ENTRY_STATE));                        
        
        

        _messages.info("ICDGEMToLex: prepareDatabase: Creating tables");
        _tableUtility.createDefaultTables();

        _messages.info("ICDGEMToLex: prepareDatabase: Creating constraints");
        _tableUtility.createDefaultTableConstraints();

        _messages.info("ICDGEMToLex: prepareDatabase: Cleaning tables");
        _tableUtility.cleanTables(codingScheme);
    }
    
    private void prepCodingScheme(CodingScheme codingScheme) throws Exception {
        PreparedStatement insert = _sqlConnection.prepareStatement(_tableConstants
                .getInsertStatementSQL(SQLTableConstants.CODING_SCHEME));

        _messages.info("ICDGEMToLex: prepCodingScheme: Loading coding scheme");
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
        LexEntryStateHelper codingSchemeLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_CODINGSCHEME);
        addEntryState(codingSchemeLesh.getEntryStateId(), 
        		codingSchemeLesh.getEntryType(), 
        		codingSchemeLesh.getOwner(), 
        		SQLTableConstants.TBLCOL_ISACTIVE, 
        		codingSchemeLesh.getEffectiveDate().toString(),
        		codingSchemeLesh.getExpirationDate().toString(),
        		codingSchemeLesh.getRevisionId(), 
        		codingSchemeLesh.getPrevisionId(),
        		codingSchemeLesh.getChangeType(),
        		codingSchemeLesh.getRelativeOrder()
        		);        
        
        insert.setString(ii++, codingScheme.getCsName()); // codingSchemeName
        insert.setString(ii++, codingScheme.getCsUri()); // codingSchemeURI
        insert.setString(ii++, codingScheme.getRepresentsVersion()); // representsVersion
        insert.setString(ii++, codingScheme.getCsName()); // formalName
        insert.setString(ii++, codingScheme.getDefaultLanguage()); // defaultLanguage
        insert.setInt(ii++, codingScheme.getConcepts().size()); // approxNumConcepts
        DBUtility.setBooleanOnPreparedStatment(insert, ii++, new Boolean(false)); // isActive
        insert.setInt(ii++, codingSchemeLesh.getEntryStateId()); // entryStateId
        insert.setString(ii++, SQLTableConstants.TBLCOLVAL_MISSING); // releaseURI
        insert.setString(ii++, codingScheme.getEntityDescription()); // entityDescription
        insert.setString(ii++, codingScheme.getCopyright()); // entityDescription

        try {
            insert.executeUpdate();
        } catch (SQLException e) {
            _messages.fatalAndThrowException(
                    "ICDGEMToLex: prepCodingScheme: FATAL ERROR - It is likely that your coding scheme name or CodingSchemeId is not unique.", e);
        }

        insert.close();

        try {
            _messages.info("ICDGEMToLex: prepCodingScheme: Loading coding scheme supported attributes");
//-------------------------------------------            
// CODING_SCHEME_SUPPORTED_ATTRIBUTES  start-
//-------------------------------------------            
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
            insert.setString(4, codingScheme.getCsUri());
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
            
            // supported attribute 'namespace' ICD-10-PCS
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_NAMESPACE);
            insert.setString(3, _props.getIcd10PcsLocalName());
            insert.setString(4, _props.getIcd10PcsUri());
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, _props.getIcd10PcsLocalName());
                insert.setString(6, _props.getIcd10PcsLocalName());
                insert.setString(7, "");
            }
            insert.executeUpdate();
            
            // supported attribute 'namespace' ICD-10-CM
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_NAMESPACE);
            insert.setString(3, _props.getIcd10CmLocalName());
            insert.setString(4, _props.getIcd10CmUri());
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, _props.getIcd10CmLocalName());
                insert.setString(6, _props.getIcd10CmLocalName());
                insert.setString(7, "");
            }
            insert.executeUpdate();
            
            // supported attribute 'namespace' ICD-9-CM
            insert.setString(1, codingScheme.getCsName());
            insert.setString(2, SQLTableConstants.TBLCOLVAL_SUPPTAG_NAMESPACE);
            insert.setString(3, _props.getIcd9CmLocalName());
            insert.setString(4, _props.getIcd9CmUri());
            if (_tableConstants.supports2009Model()) {
                insert.setString(5, _props.getIcd9CmLocalName());
                insert.setString(6, _props.getIcd9CmLocalName());
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
//-------------------------------------------            
// CODING_SCHEME_SUPPORTED_ATTRIBUTES  end  -
//-------------------------------------------            

            
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

            String codingSchemeIdTemp = codingScheme.getCsUri();
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
            _messages.fatalAndThrowException("ICDGEMToLex: prepCodingScheme: FATAL ERROR - Problem loading the coding scheme supported attributes", e);
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
            _messages.fatalAndThrowException("ICDGEMToLex: prepCodingScheme: FATAL ERROR - Problem loading the relation definition", e);
        }

        _messages.info("ICDGEMToLex: prepCodingScheme: Loading isa association definition");
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
			LexEntryStateHelper isaLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ASSOCIATION);
		    addEntryState(isaLesh.getEntryStateId(), 
		    		isaLesh.getEntryType(), 
		    		isaLesh.getOwner(), 
		      		SQLTableConstants.TBLCOL_ISACTIVE, 
		      		isaLesh.getEffectiveDate().toString(),
		      		isaLesh.getExpirationDate().toString(),
		      		isaLesh.getRevisionId(), 
		      		isaLesh.getPrevisionId(),
		      		isaLesh.getChangeType(),
		      		isaLesh.getRelativeOrder()
		      		);            
            
            // hasSubType/Isa
            insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
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
            if(this._lexGridPost50 == true) {
    			insert.setInt(k++, isaLesh.getEntryStateId()); // entryStateId
            }            
            insert.setString(k++, "The parent child relationships."); // entityDescription
            insert.executeUpdate();
            
            // mapsTo
			LexEntryStateHelper mapsToLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ASSOCIATION);
		    addEntryState(mapsToLesh.getEntryStateId(), 
		    		mapsToLesh.getEntryType(), 
		    		mapsToLesh.getOwner(), 
		      		SQLTableConstants.TBLCOL_ISACTIVE, 
		      		mapsToLesh.getEffectiveDate().toString(),
		      		mapsToLesh.getExpirationDate().toString(),
		      		mapsToLesh.getRevisionId(), 
		      		mapsToLesh.getPrevisionId(),
		      		mapsToLesh.getChangeType(),
		      		mapsToLesh.getRelativeOrder()
		      		);            
            
            k = 1;
            insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
            insert.setString(k++, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
            insert.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
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
            if(this._lexGridPost50 == true) {
    			insert.setInt(k++, mapsToLesh.getEntryStateId()); // entryStateId
            }                        
            insert.setString(k++, "Mapping relationship."); // entityDescription
            insert.executeUpdate();

            // contains
            k = 1;
			LexEntryStateHelper containsLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ASSOCIATION);
		    addEntryState(containsLesh.getEntryStateId(), 
		    		containsLesh.getEntryType(), 
		    		containsLesh.getOwner(), 
		      		SQLTableConstants.TBLCOL_ISACTIVE, 
		      		containsLesh.getEffectiveDate().toString(),
		      		containsLesh.getExpirationDate().toString(),
		      		containsLesh.getRevisionId(), 
		      		containsLesh.getPrevisionId(),
		      		containsLesh.getChangeType(),
		      		containsLesh.getRelativeOrder()
		      		);                        
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
            if(this._lexGridPost50 == true) {
    			insert.setInt(k++, containsLesh.getEntryStateId()); // entryStateId
            }                                    
            insert.setString(k++, "Relationship between compound concepts and their parts."); // entityDescription
            insert.executeUpdate();
            
            insert.close();
        } catch (SQLException e) {
            _messages.fatalAndThrowException("ICDGEMToLex: prepCodingScheme: FATAL ERROR - Problem loading the association definition", e);
        }
        
        _messages.info("ICDGEMToLex: prepCodingScheme: Loading hassubtype association definition");
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
			LexEntryStateHelper hasSubTypeLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ASSOCIATION);
		    addEntryState(hasSubTypeLesh.getEntryStateId(), 
		    		hasSubTypeLesh.getEntryType(), 
		    		hasSubTypeLesh.getOwner(), 
		      		SQLTableConstants.TBLCOL_ISACTIVE, 
		      		hasSubTypeLesh.getEffectiveDate().toString(),
		      		hasSubTypeLesh.getExpirationDate().toString(),
		      		hasSubTypeLesh.getRevisionId(), 
		      		hasSubTypeLesh.getPrevisionId(),
		      		hasSubTypeLesh.getChangeType(),
		      		hasSubTypeLesh.getRelativeOrder()
		      		);                                    
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
            if(this._lexGridPost50 == true) {
    			insert.setInt(k++, hasSubTypeLesh.getEntryStateId()); // entryStateId
            }                                                
            insert.setString(k++, "The parent child relationships."); // entityDescription
            
            insert.executeUpdate();
            insert.close();
        } catch (SQLException e) {
            _messages.fatalAndThrowException("ICDGEMToLex: prepCodingScheme: FATAL ERROR - Problem loading the hassubtype association definition", e);
        }
    }

    private void loadConcepts(CodingScheme codingScheme) throws Exception {

        PreparedStatement insert = _sqlConnection.prepareStatement(_tableConstants.getInsertStatementSQL(SQLTableConstants.ENTITY));

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

        ArrayList<BaseConcept> concepts = codingScheme.getUniqueConcepts();
        BaseConcept concept = null;

        for (int i = 0; i < concepts.size(); i++) {
            try {
            	concept = concepts.get(i);
                checkForCode.setString(1, codingScheme.getCsName());
                checkForCode.setString(2, concepts.get(i).getCode());
                ResultSet results = checkForCode.executeQuery();
                // only one result
                results.next();
                LexEntryStateHelper conceptLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITY);
                addEntryState(conceptLesh.getEntryStateId(), 
                  		conceptLesh.getEntryType(), 
                  		conceptLesh.getOwner(), 
                  		SQLTableConstants.TBLCOL_ISACTIVE, 
                  		conceptLesh.getEffectiveDate().toString(),
                  		conceptLesh.getExpirationDate().toString(),
                  		conceptLesh.getRevisionId(), 
                  		conceptLesh.getPrevisionId(),
                  		conceptLesh.getChangeType(),
                  		conceptLesh.getRelativeOrder()
                  		);
                
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
                    insert.setLong(k++, conceptLesh.getEntryStateId()); // entryStateId
                    insert.setString(k++, concept.getDescription()); // entityDescription
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
        			LexEntryStateHelper propLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITYPROPERTY);
        		    addEntryState(propLesh.getEntryStateId(), 
        		    		propLesh.getEntryType(), 
        		    		propLesh.getOwner(), 
        		      		SQLTableConstants.TBLCOL_ISACTIVE, 
        		      		propLesh.getEffectiveDate().toString(),
        		      		propLesh.getExpirationDate().toString(),
        		      		propLesh.getRevisionId(), 
        		      		propLesh.getPrevisionId(),
        		      		propLesh.getChangeType(),
        		      		propLesh.getRelativeOrder()
        		      		);                                    

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
                    insertIntoConceptProperty.setLong(k++, propLesh.getEntryStateId());  // entryStateId
                    insertIntoConceptProperty.setString(k++, concept.getDescription()); // propertyValue
                    insertIntoConceptProperty.executeUpdate();
                }

                if (concept.getCode() != null && concept.getCode().length() > 0) {
                    // description to definition
                    // check for match first
                    checkForDefinition.setString(1, codingScheme.getCsName());
                    checkForDefinition.setString(2, concept.getDescription());
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
            			LexEntryStateHelper prop2Lesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITYPROPERTY);
            		    addEntryState(prop2Lesh.getEntryStateId(), 
            		    		prop2Lesh.getEntryType(), 
            		    		prop2Lesh.getOwner(), 
            		      		SQLTableConstants.TBLCOL_ISACTIVE, 
            		      		prop2Lesh.getEffectiveDate().toString(),
            		      		prop2Lesh.getExpirationDate().toString(),
            		      		prop2Lesh.getRevisionId(), 
            		      		prop2Lesh.getPrevisionId(),
            		      		prop2Lesh.getChangeType(),
            		      		prop2Lesh.getRelativeOrder()
            		      		);                                    
                        
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
                        insertIntoConceptProperty.setLong(k++, prop2Lesh.getEntryStateId()); // entryStateId
                        insertIntoConceptProperty.setString(k++, concept.getDescription()); // propertyValue
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
        
        LexEntryStateHelper specialConceptLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITY);
        addEntryState(specialConceptLesh.getEntryStateId(), 
        		specialConceptLesh.getEntryType(), 
        		specialConceptLesh.getOwner(), 
          		SQLTableConstants.TBLCOL_ISACTIVE, 
          		specialConceptLesh.getEffectiveDate().toString(),
          		specialConceptLesh.getExpirationDate().toString(),
          		specialConceptLesh.getRevisionId(), 
          		specialConceptLesh.getPrevisionId(),
          		specialConceptLesh.getChangeType(),
          		specialConceptLesh.getRelativeOrder()
          		);

        int k = 1;
        insert.setString(k++, codingScheme.getCsName()); // codingSchemeName
        insert.setString(k++, codingScheme.getCsName()); // entityCodeNamespace
        insert.setString(k++, specialConcept.getCode()); // entityCode
        DBUtility.setBooleanOnPreparedStatment(insert, k++, null); // isDefined
        DBUtility.setBooleanOnPreparedStatment(insert, k++, null); // isAnonymous
        DBUtility.setBooleanOnPreparedStatment(insert, k++, new Boolean("true"), false); // isActive
        insert.setLong(k++, specialConceptLesh.getEntryStateId()); // entryStateId
        insert.setString(k++, specialConcept.getDescription()); // entityDescription
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
		LexEntryStateHelper prop3Lesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITYPROPERTY);
	    addEntryState(prop3Lesh.getEntryStateId(), 
	    		prop3Lesh.getEntryType(), 
	    		prop3Lesh.getOwner(), 
	      		SQLTableConstants.TBLCOL_ISACTIVE, 
	      		prop3Lesh.getEffectiveDate().toString(),
	      		prop3Lesh.getExpirationDate().toString(),
	      		prop3Lesh.getRevisionId(), 
	      		prop3Lesh.getPrevisionId(),
	      		prop3Lesh.getChangeType(),
	      		prop3Lesh.getRelativeOrder()
	      		);                                            
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
        insertIntoConceptProperty.setLong(k++, prop3Lesh.getEntryStateId()); // entryStateId
        insertIntoConceptProperty.setString(k++, specialConcept.getDescription()); // propertyValue

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
		LexEntryStateHelper prop4Lesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITYPROPERTY);
	    addEntryState(prop4Lesh.getEntryStateId(), 
	    		prop4Lesh.getEntryType(), 
	    		prop4Lesh.getOwner(), 
	      		SQLTableConstants.TBLCOL_ISACTIVE, 
	      		prop4Lesh.getEffectiveDate().toString(),
	      		prop4Lesh.getExpirationDate().toString(),
	      		prop4Lesh.getRevisionId(), 
	      		prop4Lesh.getPrevisionId(),
	      		prop4Lesh.getChangeType(),
	      		prop4Lesh.getRelativeOrder()
	      		);                                                    
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
        insertIntoConceptProperty.setLong(k++, prop4Lesh.getEntryStateId()); // entryStateId        
        insertIntoConceptProperty.setString(k++, specialConcept.getDescription()); // propertyValue
        
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

        ArrayList<Association> hasSubTypeAsso = codingScheme.getUniqueHasSubTypeAssociations();
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
        		LexEntryStateHelper conAssocLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITYASSNSTOENTITY);
        	    addEntryState(conAssocLesh.getEntryStateId(), 
        	    		conAssocLesh.getEntryType(), 
        	    		conAssocLesh.getOwner(), 
        	      		SQLTableConstants.TBLCOL_ISACTIVE, 
        	      		conAssocLesh.getEffectiveDate().toString(),
        	      		conAssocLesh.getExpirationDate().toString(),
        	      		conAssocLesh.getRevisionId(), 
        	      		conAssocLesh.getPrevisionId(),
        	      		conAssocLesh.getChangeType(),
        	      		conAssocLesh.getRelativeOrder()
        	      		);                                                    
                
                insertIntoConceptAssociations.setString(1, codingScheme.getCsName()); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.getCsName()); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, SQLTableConstants.TBLCOLVAL_HASSUBTYPE_ASSOCIATION); // entityCode
                insertIntoConceptAssociations.setString(5, asso.getSourceCodingScheme()); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, asso.getSourceCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, asso.getTargetCodingScheme()); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, asso.getTargetCode()); // targetEntityCode
                if(this._lexGridPost50 == true){
                    //always populate the multiattributeskey -- in this case a random UUID
                	insertIntoConceptAssociations.setString(9, UUID.randomUUID().toString());             
                	
                } else {
                    insertIntoConceptAssociations.setString(9, null); // multiAttributesKey                	
                }
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId 
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, false, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, false, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, true, false); // isActive
                insertIntoConceptAssociations.setLong(14, conAssocLesh.getEntryStateId()); // entryStateId                
                
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
    
    // mapsTo
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

        ArrayList<Association> mapsToAssociations = codingScheme.getUniqueMapsToAssociations();
        Association asso = null;
        for (int i = 0; i < mapsToAssociations.size(); i++) {
            try {
            	asso = mapsToAssociations.get(i);
                checkForAssociation.setString(1, codingScheme.getCsName());
                checkForAssociation.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS);
                checkForAssociation.setString(3, ICDGEMConstants.ASSOCIATION_MAPS_TO);
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
        		LexEntryStateHelper conAssocLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITYASSNSTOENTITY);
        	    addEntryState(conAssocLesh.getEntryStateId(), 
        	    		conAssocLesh.getEntryType(), 
        	    		conAssocLesh.getOwner(), 
        	      		SQLTableConstants.TBLCOL_ISACTIVE, 
        	      		conAssocLesh.getEffectiveDate().toString(),
        	      		conAssocLesh.getExpirationDate().toString(),
        	      		conAssocLesh.getRevisionId(), 
        	      		conAssocLesh.getPrevisionId(),
        	      		conAssocLesh.getChangeType(),
        	      		conAssocLesh.getRelativeOrder()
        	      		);                                                    
                
                insertIntoConceptAssociations.setString(1, codingScheme.getCsName()); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.getCsName()); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, ICDGEMConstants.ASSOCIATION_MAPS_TO); // entityCode
                insertIntoConceptAssociations.setString(5, asso.getSourceCodingScheme()); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, asso.getSourceCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, asso.getTargetCodingScheme()); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, asso.getTargetCode()); // targetEntityCode
                if(this._lexGridPost50 == true){
                    //always populate the multiattributeskey -- in this case a random UUID
                	insertIntoConceptAssociations.setString(9, UUID.randomUUID().toString());             
                	
                } else {
                    insertIntoConceptAssociations.setString(9, null); // multiAttributesKey                	
                }
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId 
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, false, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, false, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, true, false); // isActive
                insertIntoConceptAssociations.setLong(14, conAssocLesh.getEntryStateId()); // entryStateId                
                
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

        ArrayList<Association> containsAsso = codingScheme.getUniqueContainsAssociations();
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
        		LexEntryStateHelper conAssocLesh = new LexEntryStateHelper(SQLTableConstants.ENTRY_STATE_TYPE_ENTITYASSNSTOENTITY);
        	    addEntryState(conAssocLesh.getEntryStateId(), 
        	    		conAssocLesh.getEntryType(), 
        	    		conAssocLesh.getOwner(), 
        	      		SQLTableConstants.TBLCOL_ISACTIVE, 
        	      		conAssocLesh.getEffectiveDate().toString(),
        	      		conAssocLesh.getExpirationDate().toString(),
        	      		conAssocLesh.getRevisionId(), 
        	      		conAssocLesh.getPrevisionId(),
        	      		conAssocLesh.getChangeType(),
        	      		conAssocLesh.getRelativeOrder()
        	      		);                                                    
                
                insertIntoConceptAssociations.setString(1, codingScheme.getCsName()); // codingSchemeName
                insertIntoConceptAssociations.setString(2, SQLTableConstants.TBLCOLVAL_DC_RELATIONS); // containerName
                insertIntoConceptAssociations.setString(3, codingScheme.getCsName()); // entityCodeNamespace
                insertIntoConceptAssociations.setString(4, ICDGEMConstants.ASSOCIATION_CONTAINS); // entityCode
                insertIntoConceptAssociations.setString(5, codingScheme.getCsName()); // sourceEntityCodeNamespace
                insertIntoConceptAssociations.setString(6, asso.getSourceCode()); // sourceEntityCode
                insertIntoConceptAssociations.setString(7, asso.getTargetCodingScheme()); // targetEntityCodeNamespace
                insertIntoConceptAssociations.setString(8, asso.getTargetCode()); // targetEntityCode
                if(this._lexGridPost50 == true){
                    //always populate the multiattributeskey -- in this case a random UUID
                	insertIntoConceptAssociations.setString(9, UUID.randomUUID().toString());             
                	
                } else {
                    insertIntoConceptAssociations.setString(9, null); // multiAttributesKey                	
                }
                insertIntoConceptAssociations.setString(10, null); // associationInstanceId 
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 11, false, false); // isDefining
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 12, false, false); // isInferred
                DBUtility.setBooleanOnPreparedStatment(insertIntoConceptAssociations, 13, true, false); // isActive
                insertIntoConceptAssociations.setLong(14, conAssocLesh.getEntryStateId()); // entryStateId                
                
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
    
    protected void addEntryState(int entryStateId, String entryType, String owner, String status, String effectiveDate,
            String expirationDate, String revisionId, String prevRevisionId, String changeType, int relativeOrder)
            throws InsertException, ObjectAlreadyExistsException, SQLException {
        // Insert only if there is any data.
        if (!StringUtils.isBlank(owner) || !StringUtils.isBlank(status) 
                || effectiveDate != null || expirationDate != null
                || !StringUtils.isBlank(revisionId) || !StringUtils.isBlank(prevRevisionId)
                || !StringUtils.isBlank(changeType))
        {
            int k = 1;
            _insertIntoEntryState.setInt(k++, entryStateId);
            _insertIntoEntryState.setString(k++, entryType);
            _insertIntoEntryState.setString(k++, owner);
            _insertIntoEntryState.setString(k++, status);
            _insertIntoEntryState.setTimestamp(k++, null);
            _insertIntoEntryState.setTimestamp(k++, null);
            _insertIntoEntryState.setString(k++, revisionId);
            _insertIntoEntryState.setString(k++, prevRevisionId);
            _insertIntoEntryState.setString(k++, (changeType != null ? changeType : " "));
            _insertIntoEntryState.setInt(k++, relativeOrder);
            
            if(_sqlModifier.getDatabaseType().equals("ACCESS")){
            	_insertIntoEntryState.setString(k++, null);
            } else {
            	_insertIntoEntryState.setObject(k++, null, java.sql.Types.BIGINT);
            }
            
            _insertIntoEntryState.execute();            
        }
    }

}