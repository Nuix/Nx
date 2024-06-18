# Bootstrap the library
require_relative "NxBootstrap.rb"

# ==================================================
# Example of CSVTable control in TabbedCustom Dialog
# ==================================================

# Create tabbed dialog and tab
dialog = TabbedCustomDialog.new("CSV Table")
main_tab = dialog.addTab("main_tab","Main")

# Add CSV table (table which can import CSV)
main_tab.appendCsvTable("csv_data",["English","Spanish","Chinese","Japanese"])

# Display dialog
dialog.display

# Display what was in the table
dialog.toMap["csv_data"].each_with_index do |record,record_index|
	puts "Record #{record_index}"
	record.each do |k,v|
		puts "#{k} => #{v}"
	end
end