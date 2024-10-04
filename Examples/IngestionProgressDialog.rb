# Bootstrap the library
require_relative "NxBootstrap.rb"

# Following variables are expected to be set elsewhere
test_case_location ||= "FALLBACK_VALUE"
test_evidence_location ||= "FALLBACK_VALUE"

java_import "com.nuix.nx.dialogs.ProcessingStatusDialog"

$current_case = $utilities.getCaseFactory.create(test_case_location,{})

processor = $current_case.createProcessor

# Create evidence
evidence = processor.newEvidenceContainer("Test #{Time.now.to_i}",{})
evidence.addFile(test_evidence_location)
evidence.save

# Set processing settings, this step is always a good idea (rather than relying on defaults)
# plus if you want the settings to show up in the processing status control properly, you
# should set them explicitly
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
	"maxStoredBinarySize" => 250000000,
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
processor.setProcessingSettings(processing_settings)

# Set worker settings, this step is always a good idea (rather than relying on defaults)
# plus if you want the settings to show up in the processing status control properly, you
# should set them explicitly
processor.setParallelProcessingSettings({
	"workerCount" => 4,
})

processing_status_dialog = ProcessingStatusDialog.new
processing_status_dialog.setAutoCloseDelaySeconds(42) # Set to non-default value
# This will begin processing and display the processing status dialog
puts "Handing off to processing status dialog..."
processing_status_dialog.displayAndBeginProcessing(processor)

puts "Job was aborted?: #{processing_status_dialog.getJobWasAborted}"
puts "Job was stopped?: #{processing_status_dialog.getJobWasStopped}"

$current_case.close