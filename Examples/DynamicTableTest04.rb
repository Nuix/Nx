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
	"Score",
	"Has Force"
]

# Define the records which will be displayed, this can essentially look
# like whatever you want as later the callback we define will be responsible
# for getting/setting values for individual records
records = [
	{first: "Luke", last: "Skywalker", location: "Tatooine", occupation: "Moisture Farmer", score: 100, hasForce: true},
	{first: "Beru", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer", score: -25, hasForce: false},
	{first: "Owen", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer", score: 75, hasForce: false},
	{first: "Obi-wan", last: "Kenobi", location: "Tatooine", occupation: "Hermit", score: -50, hasForce: true},
]

# Note: scores are randomly assigned and do not reflect judgement of any character :-D

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
		when 4
			record[:score] = value
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
		when 4
			next record[:score]
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

# Customize highlighting such that negative values are highlighted RED when the character IS NOT a force user
dynamic_table_control.addCellHighlighter(Java::JavaAwt::Color::RED) do |cell_info|
	# cell_info is a DynamicTableControl.CellInfo instance, which contains various info we can make use of:
	cell_value = cell_info.value
	row = cell_info.row
	col = cell_info.column
	record = cell_info.rowRecord # In case you want to inspect other values in that row

	highlight_it = cell_value.is_a?(Integer) && cell_value <= 0
	next highlight_it
end

# Customize highlighting such that negative values are highlighted YELLOW when the character IS a force user
dynamic_table_control.addCellHighlighter(Java::JavaAwt::Color::YELLOW) do |cell_info|
	# cell_info is a DynamicTableControl.CellInfo instance, which contains various info we can make use of:
	cell_value = cell_info.value
	row = cell_info.row
	col = cell_info.column
	record = cell_info.rowRecord # In case you want to inspect other values in that row

	highlight_it = record[:hasForce] && cell_value.is_a?(Integer) && cell_value <= 0
	next highlight_it
end

# Enable adding records
dynamic_table_control.setUserCanAddRecords(true) do
	next {first: "", last: "", location: "", occupation: ""}
end
# We probably also want to enable some columns for editing, I know this is not especially
# well designed at the moment, this control was made in a hurry to fit a need I had at some point
(0..3).each do |column_index|
	dynamic_table_control.getModel.setColumnEditable(column_index)
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
		puts "Score: #{record[:score]}"
		puts "Has Force: #{record[:hasForce]}"
	end
end