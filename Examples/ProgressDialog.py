# Bootstrap NX JAR for Jython
import os
import sys
import time
import datetime

script_directory = os.path.dirname(os.path.realpath('__file__'))
nx_jar_file = os.path.join(script_directory,"Nx.jar")
sys.path.append(nx_jar_file)

from com.nuix.nx import NuixConnection
from com.nuix.nx import LookAndFeelHelper
from com.nuix.nx.dialogs import ChoiceDialog
from com.nuix.nx.dialogs import TabbedCustomDialog
from com.nuix.nx.dialogs import CommonDialogs
from com.nuix.nx.dialogs import ProgressDialog
from com.nuix.nx.dialogs import ProcessingStatusDialog
from com.nuix.nx.digest import DigestHelper
from com.nuix.nx.controls.models import Choice

LookAndFeelHelper.setWindowsIfMetal()
NuixConnection.setUtilities(utilities)
NuixConnection.setCurrentNuixVersion(NUIX_VERSION)

# ===========================================================================
# Example of using ProgressDialog which allows your code to show a preogress
# dialog to the user with feedback while a long running process occurs.
# ===========================================================================

def progressDialogFunc(progress_dialog):
	# Set the title, and whether an abort button and log area are shown
	progress_dialog.setTitle("ProgressDialog Demo")
	progress_dialog.setAbortButtonVisible(False)
	progress_dialog.setLogVisible(False)

	# Demo progress bars and status areas
	progress_dialog.setMainProgress(1,3)
	progress_dialog.setMainStatus("Demo 1")
	progress_dialog.setSubStatus("Basic")
	progress_dialog.setSubProgress(0,100)

	# Simulate a process that take time and report progress updates while doing it
	for i in range(100):
		progress_dialog.setSubProgress(i+1)
		time.sleep(0.1)

	# Further demo with a loggin area
	progress_dialog.setLogVisible(True)
	progress_dialog.setMainProgress(2,3)
	progress_dialog.setMainStatus("Demo 2")
	progress_dialog.setSubStatus("With logging")
	progress_dialog.setSubProgress(0,5)

	# Simulate a process that take time and report progress updates while doing it
	for i in range(5):
		progress_dialog.setSubProgress(i+1)
		progress_dialog.logMessage("Stuff is happening: "+str(datetime.datetime.now()))
		time.sleep(1)

	# Demonstrate checking for a user's abort request
	# Abort occurs by either clicking abort and confirming or
	# Attempting to close the progress dialog while the block is
	# still running and confirming the abort
	# Aborting just sets a flag to True, tested using ProgressDialog.abortWasRequested
	# The script must check for this flag being set and handle the requested abort state
	progress_dialog.setAbortButtonVisible(True)
	progress_dialog.setMainProgress(3,3)
	progress_dialog.setMainStatus("Demo 2")
	progress_dialog.setSubStatus("With Abort")
	progress_dialog.setSubProgress(0,100)

	# Simulate a process that take time and report progress updates while doing it
	for i in range(100):
		progress_dialog.setSubProgress(i+1)
		progress_dialog.logMessage("Stuff is happening: "+str(datetime.datetime.now()))
		if progress_dialog.abortWasRequested():
			progress_dialog.logMessage("User has requested abort...")
			progress_dialog.setSubStatus("Aborting...")
			break
		time.sleep(0.25)

	progress_dialog.setSubStatus("")
	if progress_dialog.abortWasRequested():
		progress_dialog.setMainStatus("Completed: User Aborted")
	else:
		# Convenience method to set the progress dialog into a Completed state
		progress_dialog.setCompleted

# Show a dialog.  The dialog essentially exists just during
# the provided func.  When the func is exited, the progress
# dialog will enter a state which will allow the user to
# close the dialog.
ProgressDialog.forBlock(progressDialogFunc)