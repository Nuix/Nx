# Bootstrap the library
require_relative "NxBootstrap.rb"

# =========================================================
# Example of adding a DynamicaTable to a TabbedCustomDialog
# =========================================================

dialog = TabbedCustomDialog.new("Dynamic Table Test")
main_tab = dialog.addTab("main_tab","Main Tab")

# Define what the headers will be
headers = [
	"First",
	"Last",
	"Location",
	"Occupation",
]

# Define the records which will be displayed, this can essentially look
# like whatever you want as later the callback we define will be responsible
# for getting/setting values for individual records
records = [
	{first: "Luke", last: "Skywalker", location: "Tatooine", occupation: "Moisture Farmer", allegiance: "Rebels"},
	{first: "Beru", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer", allegiance: "Neutral"},
	{first: "Owen", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer", allegiance: "Neutral"},
	{first: "Obi-wan", last: "Kenobi", location: "Tatooine", occupation: "Hermit", allegiance: "Rebels"},
	{first: "Darth", last: "Vader", location: "Space?", occupation: "Guy you don't mess with", allegiance: "Empire"},
]

# Now we add the dynamic table, configuring headers, records and callback which will get/set cell values
# Method signature
# public CustomTabPanel appendDynamicTable(String identifier, String controlLabel, List<String> headers,
# 	List<Object> records, DynamicTableValueCallback callback)
#
# Callback signature
# interact(Object record, int i, boolean setValue, Object aValue)
#
main_tab.appendDynamicTable("characters_table","Characters",headers,records) do |record, column_index, setting_value, value|
	# record: The current record the table wants to interact with from the records array
	# column_index: The column index the table wants to interact with
	# setting_value: True if the table wishes to set a new value for this record/column index, false if reading the current value
	# value: If setting_value is true, the value the table wishes to store back on the item

	# Debugging messages
	show_debug = false
	if show_debug
		if setting_value
			puts "Setting column #{column_index} with value '#{value}' in object:\n#{record.inspect}"
		else
			puts "Getting column #{column_index} in object:\n#{record.inspect}"
		end
	end

	if setting_value
		# Logic for setting values
		case column_index
		when 0
			# Example of modifying value before storing it
			record[:first] = value.capitalize
		when 1
			record[:last] = value.capitalize
		when 2
			record[:location] = value
		when 3
			record[:occupation] = value
		end
	else
		# Logic for getting values
		case column_index
		when 0
			next record[:first]
		when 1
			next record[:last]
		when 2
			next record[:location]
		when 3
			next record[:occupation]
		end
	end
end

# This allows the user to add records
# The method must supply blank record objects for when the table adds a new record
#
# Method Signature
# setUserCanAddRecords(boolean value,Supplier<Object> callback)
#

# Fetch the native DynamicTable control by its identifier
dynamic_table_control = main_tab.getControl("characters_table")
dynamic_table_model = dynamic_table_control.getModel

# Enable adding records
dynamic_table_control.setUserCanAddRecords(true) do
	next {first: "", last: "", location: "", occupation: "", allegiance: ""}
end

# We probably also want to enable some columns for editing, I know this is not especially
# well designed at the moment, this control was made in a hurry to fit a need I had at some point
(0..3).each do |column_index|
	dynamic_table_control.getModel.setColumnEditable(column_index)
end

# Were going to add a combo box which changes what is available in the dynamic table
main_tab.appendComboBox("allegiance","Allegiance",["All","Empire","Rebels","Neutral"])
main_tab.getControl("allegiance").addItemListener do |event_stuff|
	new_value = main_tab.getControl("allegiance").getSelectedItem
	case new_value
	when "All"
		dynamic_table_model.setRecords(records)
	else
		dynamic_table_model.setRecords(records.select{|r|r[:allegiance] == new_value})
	end
end

dialog.display
if dialog.getDialogResult == true
	values = dialog.toMap
	puts "Selected (checked) Characters"
	values["characters_table"].each do |record|
		puts "-"*10
		puts "Name: #{record[:last]}, #{record[:first]}"
		puts "Location: #{record[:location]}"
		puts "Occupation: #{record[:occupation]}"
	end
end