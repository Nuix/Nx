# Bootstrap the library
require_relative "NxBootstrap.rb"

# ============================================================================================
# Example of adding a DynamicaTable to a TabbedCustomDialog to allow user to select mime types
# See also DynamicTableTest.rb
# ============================================================================================

dialog = TabbedCustomDialog.new("Mime Type Selection Dynamic Table Test")
main_tab = dialog.addTab("main_tab","Main Tab")

# Define what the headers will be
headers = [
	"Kind",
	"Mime Type",
	"Type Name",
	"Preferred Extension",
]

records = $utilities.getItemTypeUtility.getAllTypes.sort_by{|t| [t.getKind.getName,t.getName]}

main_tab.appendDynamicTable("mime_types_table","Mime Types",headers,records) do |record, column_index, setting_value, value|
	if !setting_value
		case column_index
		when 0
			next record.getKind.getName
		when 1
			next record.getName
		when 2
			next record.getLocalisedName
		when 3
			next record.getPreferredExtension || ""
		end
	end
end

dialog.display
if dialog.getDialogResult == true
	values = dialog.toMap
	puts "Selected Mime Types"
	values["mime_types_table"].each do |record|
		puts "==== #{record.getName} ===="
		puts "Kind: #{record.getKind.getName}"
		puts "Type Name: #{record.getLocalisedName}"
		puts "Preferred Extension: #{record.getPreferredExtension}"
	end
end