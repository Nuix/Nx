# Bootstrap the library
require_relative "NxBootstrap.rb"

# In this example we display a progress dialog and loop until
# we have detected that the user requested an abort.
# ProgressDialog.abortWasRequested will return true once the user has
# requested the script abort either by closing the window and confirming abort
# or by clicking the abort button on confirming abort
ProgressDialog.forBlock do |pd|
	pd.setTitle("Looping...")
	pd.setAbortButtonVisible(true)

	pd.logMessage("This dialog will loop until you abort or it counts to 30")

	count = 0
	while pd.abortWasRequested == false
		# Exit our while loop if we hit 30
		break if count >= 30
		# Show the current count in the main status line
		pd.setMainStatus("Looped #{count} times")
		# Show current progress to 30
		pd.setMainProgress(count,30)
		# Incrememnt count
		count += 1
		# Wait 1 second
		sleep(1)
	end

	# If the user aborted, show a message regarding this, else we will put the
	# progress dialog into a completed state
	if pd.abortWasRequested
		pd.setMainStatusAndLogIt("User Aborted at #{count}")
	else
		pd.setCompleted
	end
end