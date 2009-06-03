@echo off
REM Loads ICD General Equivalence Mapping (GEM) data.
REM
REM Options:
REM   -in,--input <uri> URI or path specifying location of the source file.
REM   -type, --type <type> Type of GEM data.  Valid values are 'i10to9cm', 'i9to10cm', 'i10to9pcs', or 'i9to10pcs'. Required.
REM   -ver --version <ver> Coding scheme version. Optional.  If it is not used a date/time stamp will be used for the coding scheme version.
REM   -t, --tag <tag> An optional tag ID (e.g. 'PRODUCTION' or 'TEST') to assign.
REM   -a, --activate <a> set this option if you want the coding scheme activated after it is loaded.
REM
REM Example: LoadICDGEM.bat -in "file:///C:/some/dir/test/icdgem/i9pcs2009.txt" -type i10to9cm -ver 1.0 -t MyTest
REM
java -Xmx1000m -cp "..\runtime\lbPatch.jar;..\runtime\lbRuntime.jar;..\runtime\lgICDGEMLoaderEx.jar" org.lexgrid.extension.loaders.icdgem.admin.LoadICDGEM %*
pause