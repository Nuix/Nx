# Bootstrap the library
require_relative "NxBootstrap.rb"

# ===========================================================================
# Example of using ProgressDialog which allows your code to show a preogress
# dialog to the user with feedback while a long running process occurs.
# ===========================================================================

# Show a dialog.  The dialog essentially exists just during
# the provided block.  When the block is exited, the progress
# dialog will enter a state which will allow the user to
# close the dialog.
ProgressDialog.forBlock do |progress_dialog|
	# Set the title, and whether an abort button and log area are shown
	progress_dialog.setTitle("ProgressDialog Demo")
	progress_dialog.setAbortButtonVisible(false)
	progress_dialog.setLogVisible(false)

	# Demo progress bars and status areas
	progress_dialog.setMainProgress(1,3)
	progress_dialog.setMainStatus("Demo 1")
	progress_dialog.setSubStatus("Basic")
	progress_dialog.setSubProgress(0,100)

	# Simulate a process that take time and report progress updates while doing it
	100.times do |i|
		progress_dialog.setSubProgress(i+1)
		sleep(0.1)
	end

	# Further demo with a loggin area
	progress_dialog.setLogVisible(true)
	progress_dialog.setMainProgress(2,3)
	progress_dialog.setMainStatus("Demo 2")
	progress_dialog.setSubStatus("With logging")
	progress_dialog.setSubProgress(0,5)

	# Simulate a process that take time and report progress updates while doing it
	5.times do |i|
		progress_dialog.setSubProgress(i+1)
		progress_dialog.logMessage("Stuff is happening: #{Time.now}")
		sleep(1)
	end

	# Demonstrate checking for a user's abort request
	# Abort occurs by either clicking abort and confirming or
	# Attempting to close the progress dialog while the block is
	# still running and confirming the abort
	# Aborting just sets a flag to true, tested using ProgressDialog.abortWasRequested
	# The script must check for this flag being set and handle the requested abort state
	progress_dialog.setAbortButtonVisible(true)
	progress_dialog.setMainProgress(3,3)
	progress_dialog.setMainStatus("Demo 2")
	progress_dialog.setSubStatus("With Abort")
	progress_dialog.setSubProgress(0,100)

	# Simulate a process that take time and report progress updates while doing it
	100.times do |i|
		progress_dialog.setSubProgress(i+1)
		progress_dialog.logMessage("Stuff is happening: #{Time.now}")
		if progress_dialog.abortWasRequested
			progress_dialog.logMessage("User has requested abort...")
			progress_dialog.setSubStatus("Aborting...")
			break
		end
		sleep(0.25)
	end

	progress_dialog.setSubStatus("")
	if progress_dialog.abortWasRequested
		progress_dialog.setMainStatus("Completed: User Aborted")
	else
		# Convenience method to set the progress dialog into a Completed state
		progress_dialog.setCompleted
	end
end