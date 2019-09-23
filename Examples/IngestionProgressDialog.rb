# Bootstrap the library
require_relative "NxBootstrap.rb"

java_import "com.nuix.nx.dialogs.ProcessingStatusDialog"

test_case_location = "D:\\cases\\FakeData_8.0"
test_evidence_location = "D:\\natives\\FakeData"

$current_case = $utilities.getCaseFactory.open(test_case_location)

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
	"workerTemp" => "D:\\WorkerTemp",
})

processing_status_dialog = ProcessingStatusDialog.new
# This will begin processing and display the processing status dialog
processing_status_dialog.displayAndBeginProcessing(processor)

$current_case.close