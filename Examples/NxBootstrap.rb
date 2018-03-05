# This essentially "bootstraps" the library from a Ruby script
# The following code can easily be copied to the top of your
# new script to get you started.
#
# This code loads the JAR from the same directory the script file
# is located, then loads up a few commonly used classes from the
# JAR and then performs a few initialization tasks:
# - Make sure the Java look and feel is Windows in case were running via nuix_console.exe
#   this step makes sure the look is consistent regardless of where the script is ran
# - Pass an instance of the Utilities object to the library for its use
# - Pass the current Nuix version string to the library

script_directory = File.dirname(__FILE__)
require File.join(script_directory,"Nx.jar")
java_import "com.nuix.nx.NuixConnection"
java_import "com.nuix.nx.LookAndFeelHelper"
java_import "com.nuix.nx.dialogs.ChoiceDialog"
java_import "com.nuix.nx.dialogs.TabbedCustomDialog"
java_import "com.nuix.nx.dialogs.CommonDialogs"
java_import "com.nuix.nx.dialogs.ProgressDialog"
java_import "com.nuix.nx.dialogs.ProcessingStatusDialog"
java_import "com.nuix.nx.digest.DigestHelper"
java_import "com.nuix.nx.controls.models.Choice"

LookAndFeelHelper.setWindowsIfMetal
NuixConnection.setUtilities($utilities)
NuixConnection.setCurrentNuixVersion(NUIX_VERSION)