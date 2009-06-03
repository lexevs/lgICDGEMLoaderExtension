# Loads ICD General Equivalence Mapping (GEM) data.
#
# Options:
#   -in,   --input <uri> URI or path specifying location of the source file.
#   -type, --type <type> Type of GEM data.  Valid values are 'i10to9cm', 'i9to10cm', 'i10to9pcs', or 'i9to10pcs'. Required.
#   -ver   --version <ver> Coding scheme version. Optional.  If it is not used a date/time stamp will be used for the coding scheme version.
#   -t,    --tag <id> An optional tag ID (e.g. 'PRODUCTION' or 'TEST') to assign.
#   -a,    --activate <a> set this option if you want the coding scheme activated after it is loaded.
#
# Example: LoadICDGEM.bat -in "file:///some/dir/test/icdgem/i9pcs2009.txt" -type i10to9cm -ver 1.0 -t MyTest
#
java -Xmx1000m -cp "../runtime/lbPatch.jar:../runtime/lbRuntime.jar:../runtime/lgICDGEMLoaderEx.jar" org.lexgrid.extension.loaders.icdgem.admin.LoadICDGEM $@