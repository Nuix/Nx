# Bootstrap the library
require_relative "NxBootstrap.rb"

java_import "com.nuix.nx.controls.models.ReportDataModel"

# In this example we display a progress dialog and add a small
# report to it.  The report can be continually updated via
# the ReportDataModel.

rdm = ReportDataModel.new
section1 = {"Average" => 0.0, "Count" => 0, "Minimum" => 0.0, "Maximum" => 0.0}
section2 = {"Height" => 12.3, "Width" => 3, "Fill" => false}
rdm.addSection "Summary Data", section1
rdm.addSection "Size Information", section2

ProgressDialog.forBlock do | pd |
	pd.set_title "Progress Report..."
	pd.set_abort_button_visible false
  pd.set_main_progress_visible false
	pd.set_sub_progress_visible false
  pd.set_log_visible false

  pd.set_main_status_and_log_it "Starting to Count"
	pd.add_report rdm

	for counter in 1..30 do
    # Update the values in the report as needed...
	  rdm.updateData "Summary Data", "Count", counter


    # Also update progress when required.
    if 15 == counter
      pd.set_sub_status_and_log_it "Half way there."
    end
	  pd.set_main_progress counter, 30

	  sleep 1
	end

	pd.set_main_status_and_log_it "Finished"
  pd.set_sub_status ""

end
