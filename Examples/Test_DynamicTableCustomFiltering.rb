# Bootstrap the library
require_relative "NxBootstrap.rb"

java_import com.nuix.nx.controls.filters.DynamicTableFilterProvider

# A custom filter provider extends the class com.nuix.nx.controls.filters.DynamicTableFilterProvider
# and needs to at least provide implementation of to methods:
#
# >> handlesExpression(filter_expression)
# This method will be given the string expression in the filter text box.  This is where your implementation
# gets to decide whether it want to try to interpret this filter expresion or not.  Return true if you can
# handle this, false if not.
#
# >> keepRecord(source_index,is_checked,filter_expression,record,row_values)
# This method will be invoked for each record if handlesExpression returned true.  The method is expected to
# return true for records you wish to keep and false for record you wish to filter out.  The method is provided
# some information to make that decision with:
# source_index: The 0 based index into the full collection of records
# is_checked: Whether the given record has been checked in the GUI
# filter_expression: The string expression that was entered into the filter text box
# record: The raw record object
# row_values: A map of values for the row/record where key is the header and value is whatever value is provided by the callback
#             you defined when you initially added the dynamic table control.
#
# >> (Optional) 

# ======================================
# Define some custom filtering providers
# ======================================

class OddRowFilter < DynamicTableFilterProvider

	def handlesExpression(filter_expression)
		if filter_expression =~ /:odd:/i
			return true
		else
			return false
		end
	end

	def keepRecord(source_index,is_checked,filter_expression,record,row_values)
		is_even = ((source_index + 1) % 2 == 0)
		return !is_even
	end

end

class EvenRowFilter < DynamicTableFilterProvider

	def handlesExpression(filter_expression)
		if filter_expression =~ /:even:/i
			return true
		else
			return false
		end
	end

	def keepRecord(source_index,is_checked,filter_expression,record,row_values)
		is_even = (source_index + 1) % 2 == 0
		return is_even
	end
end

class MagicNumberFilter < DynamicTableFilterProvider

	def initialize
		@parser = /magicnumber ([=<>]{1,2}) ([0-9]+)/i
	end

	def handlesExpression(filter_expression)
		if filter_expression =~ @parser
			return true
		else
			return false
		end
	end

	def keepRecord(source_index,is_checked,filter_expression,record,row_values)
		magicnumber = row_values["Magic Number"]
		match_data = @parser.match(filter_expression)
		op = match_data[1]
		value = match_data[2].to_i
		case op
		when "<"
			return magicnumber < value
		when ">"
			return magicnumber > value
		when "=", "=="
			return magicnumber == value
		when ">="
			return magicnumber >= value
		when "<="
			return magicnumber <= value
		end

		# If we didn't know how to handle it, then don't match
		return false
	end
end

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
	"Magic Number",
]

# Define the records which will be displayed, this can essentially look
# like whatever you want as later the callback we define will be responsible
# for getting/setting values for individual records
records = [
	{first: "Luke", last: "Skywalker", location: "Tatooine", occupation: "Moisture Farmer", allegiance: "Rebels", magicnumber: 100000},
	{first: "Beru", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer", allegiance: "Neutral", magicnumber: 200000},
	{first: "Owen", last: "Lars", location: "Tatooine", occupation: "Moisture Farmer", allegiance: "Neutral", magicnumber: 500500},
	{first: "Obi-wan", last: "Kenobi", location: "Tatooine", occupation: "Hermit", allegiance: "Rebels", magicnumber: 700000},
	{first: "Darth", last: "Vader", location: "Space?", occupation: "Guy you don't mess with", allegiance: "Empire", magicnumber: 800000},
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
		when 4
			record[:magicnumber] = value
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
			next record[:magicnumber]
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

# Add our custom filters, to try them out, use filters like:
# :odd:
# :even:
# magicnumber > 500000
# magicnumber < 500000
# magicnumber = 500500
dynamic_table_model.getCustomFilterProviders.add(OddRowFilter.new)
dynamic_table_model.getCustomFilterProviders.add(EvenRowFilter.new)
dynamic_table_model.getCustomFilterProviders.add(MagicNumberFilter.new)

# Enable adding records
dynamic_table_control.setUserCanAddRecords(true) do
	next {first: "", last: "", location: "", occupation: "", allegiance: "", magicnumber: rand(100000..999999)}
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
		puts "Allegiance: #{record[:allegiance]}"
		puts "Magic Number: #{record[:magicnumber]}"
	end
end