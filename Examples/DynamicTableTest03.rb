# Bootstrap the library
require_relative "NxBootstrap.rb"

# =========================================================
# Example of adding a DynamicaTable to a TabbedCustomDialog
# =========================================================

dialog = TabbedCustomDialog.new("Dynamic Table Test")
main_tab = dialog.addTab("main_tab","Main Tab")

# Define what the headers will be
headers = [
	"Key File",
	"Password",
]

# Define the records which will be displayed, this can essentially look
# like whatever you want as later the callback we define will be responsible
# for getting/setting values for individual records
records = []

# Now we add the dynamic table, configuring headers, records and callback which will get/set cell values
# Method signature
# public CustomTabPanel appendDynamicTable(String identifier, String controlLabel, List<String> headers,
# 	List<Object> records, DynamicTableValueCallback callback)
#
# Callback signature
# interact(Object record, int i, boolean setValue, Object aValue)
#
main_tab.appendDynamicTable("key_file_data","Key File Data",headers,records) do |record, column_index, setting_value, value|
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
			record[:key_file] = value.capitalize
		when 1
			record[:password] = value.capitalize
		end
	else
		# Logic for getting values
		case column_index
		when 0
			next record[:key_file]
		when 1
			next record[:password].gsub(/./,"*")
		end
	end
end

# Fetch the native DynamicTable control by its identifier
dynamic_table_control = main_tab.getControl("key_file_data")

# By default all added rows should be checked
dynamic_table_control.setDefaultCheckState(true)

# Enable adding records
dynamic_table_control.setUserCanAddRecords(true) do
	# Prompt user to select a file
	key_file = CommonDialogs.openFileDialog("C:\\","Key File")
	if !key_file.nil?
		# If user selected a file, addtionally prompt user to enter a password
		password = CommonDialogs.getInput("Password for #{key_file.getAbsolutePath}")
		if !password.nil?
			# If user provided both, yield a new record
			next {key_file: key_file.getAbsolutePath, password: password}
		end
	end

	# If we reached here, yield nil abort adding a new record
	next nil
end

dialog.display
if dialog.getDialogResult == true
	values = dialog.toMap
	puts "==== key_file_data ===="
	values["key_file_data"].each do |record|
		puts "Key => #{record[:key_file]}, Password => #{record[:password]}"
	end
end