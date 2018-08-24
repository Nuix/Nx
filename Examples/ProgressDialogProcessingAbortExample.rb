=begin
This script will demonstrate showing a simple progress dialog while processing some data into a case.
The script will allow the user to abort processing while processing is happening.  To accomplish this
processing is started asynchrounously using the method Processor.processAsync vs the more common approach
Processor.process.  The reason for this is that while processing is occurring we need to poll the progress
dialog to see if the user has requested an abort.  If we were to call Processor.process, we would not be
able to do this because that is a blocking call, meaning that execution does not return to our script until
processing has completed and returned control to the script.  When we instead call Processor.processAsync
Nuix will begin processing asynchronously, provide a ProcessingJob object and immediately return control to
our script, allowing use to check for and act on the user requesting we abort.
=end

# Bootstrap the library
require_relative "NxBootstrap.rb"

# Directory of an existing Nuix case which will be opened by the script
case_directory = "C:\\@Nuix\\Cases\\Ziggy"

# Source data directory or file path
source_data_path = "C:\\@NUIX\\Natives\\Fake Invoices\\10 Images"

# Name of the evidence container we will process data under
evidence_name = "Data #{Time.now.to_i}" # Just use name based on current time

#Define and configure processing settings
processing_settings = {
	"addBccToEmailDigests" => false,
	"addCommunicationDateToEmailDigests" => false,
	"analysisLanguage" => "en",
	"calculateAuditedSize" => true,
	"calculateSSDeepFuzzyHash" => false,
	"carveFileSystemUnallocatedSpace" => false,
	"createThumbnails" => false,
	"detectFaces" => false,
	"digests" => [
		"MD5"
	],
	"enableExactQueries" => false,
	"extractEndOfFileSlackSpace" => false,
	"extractFromSlackSpace" => false,
	"extractNamedEntitiesFromProperties" => false,
	"extractNamedEntitiesFromText" => false,
	"extractNamedEntitiesFromTextStripped" => false,
	"extractShingles" => false,
	"hideEmbeddedImmaterialData" => false,
	"identifyPhysicalFiles" => true,
	"maxDigestSize" => 250000000,
	"maxStoredBinarySize" => 1000000000,
	"processFamilyFields" => false,
	"processText" => true,
	"processTextSummaries" => false,
	"recoverDeletedFiles" => false,
	"reportProcessingStatus" => "none",
	"reuseEvidenceStores" => true,
	"skinToneAnalysis" => false,
	"smartProcessRegistry" => false,
	"stemming" => false,
	"stopWords" => false,
	"storeBinary" => false,
	"traversalScope" => "full_traversal",
}

#Define and configure parallel processing settings (for workers)
parallel_processing_settings = {
	"workerCount" => 4,
	"workerMemory" => 1024,
	"workerTemp" => "C:\\WorkerTemp",
}

ProgressDialog.forBlock do |pd|
	# We will start with the abort button visible and make it visible while processing is occurring
	pd.setAbortButtonVisible(false)

	# Open the case we will be working with
	$current_case = $utilities.getCaseFactory.open(case_directory)

	# Create our processor
	processor = $current_case.createProcessor

	# Build our evidence container
	evidence_container = processor.newEvidenceContainer(evidence_name)
	evidence_container.addFile(source_data_path)
	evidence_container.save

	# Configure processing settings
	processor.setProcessingSettings(processing_settings)
	processor.setParallelProcessingSettings(parallel_processing_settings)

	# We will track when we started so we can report how long
	# the process took
	start_time = Time.now

	# This begins asynchronous (non-blocking) processing which means processing begins
	# and execution of the script continues in parallel.  We need to poll the processing job
	# until it completes or the user aborts
	pd.logMessage("Starting processing asynchronously...")
	processing_job = processor.processAsync

	# Now that processing has begun, show the abort button to the user
	pd.setAbortButtonVisible(true)

	# Track whether we have ask processing job to abort, so that once we have we don't
	# spam it will additional abort requests
	processing_asked_to_abort = false

	# Used to periodically show progress
	last_progress = Time.now

	# We enter a while loop that will keep iterating until the processing job signals it has finished
	# either because it has completed processing or because it has stopped due to use asking it to abort
	while processing_job.hasFinished != true
		# Periodically update main status with time elapsed
		if (Time.now - last_progress) > 1
			elapsed = Time.now - start_time
			pd.setMainStatus("Processing elapsed: #{elapsed.to_i} seconds")
			last_progress = Time.now
		end

		# While we loop waiting for processing to complete, we check the progress dialog
		# to determine if the user has requested an abort and whether we have already
		# asked the processing job to abort
		if pd.abortWasRequested == true && processing_asked_to_abort == false
			# User has requested abort, signal to processing job that we want it to abort
			processing_job.abort
			# Record that we have sent the processing job a request to abort
			processing_asked_to_abort = true
		end
	end
	
	# Record when processing has completed then report how long the whole process took
	finish_time = Time.now
	pd.logMessage("Processing finished in #{finish_time - start_time} seconds")


	# If the user aborted, show a message regarding this, else we will put the
	# progress dialog into a completed state
	if pd.abortWasRequested
		pd.setMainStatusAndLogIt("User Aborted Processing")
	else
		pd.setCompleted
	end

	# Be a good citizen and close the case once we're done with it
	$current_case.close
end