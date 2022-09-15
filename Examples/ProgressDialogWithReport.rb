require_relative "NxBootstrap.rb"

java_import "com.nuix.nx.controls.models.ReportDataModel"

# In this example we display a progress dialog and add a small
# report to it.  The report can be continually updated via
# the ReportDataModel.

rdm = ReportDataModel.new
section1 = {"Average" => 0.0, "Count" => 0, "Minimum" => 100.0, "Maximum" => 0.0}
section2 = {"Height" => 12.3, "Width" => 3, "Fill" => false}
rdm.addSection "Summary Data", section1
rdm.addSection "Size Information", section2

ProgressDialog.forBlock do | pd |
  pd.set_title "Progress Report..."
  pd.set_abort_button_visible false
  pd.set_sub_progress_visible false

  pd.set_main_status_and_log_it "Starting to Count"
  pd.add_report rdm

  for counter in 1..30 do
    pd.set_main_progress counter, 30

    old_max = rdm.get_data_field_value("Summary Data", "Maximum").to_i
    old_min = rdm.get_data_field_value("Summary Data", "Minimum").to_i
    old_avg = rdm.get_data_field_value("Summary Data", "Average").to_f

    new_val = rand(100) + 1
    pd.set_sub_status "#{new_val}"
    new_avg = ((old_avg * (counter - 1)) + new_val) / counter
    new_max = new_val > old_max ? new_val : old_max
    new_min = new_val < old_min ? new_val : old_min

    rdm.update_data "Summary Data", "Count", counter
    rdm.update_data "Summary Data", "Average", '%.2f' % new_avg
    rdm.update_data "Summary Data", "Minimum", new_min
    rdm.update_data "Summary Data", "Maximum", new_max

    sleep 1
  end

	pd.set_main_status_and_log_it "Finished"
  pd.set_sub_status ""

end
