# Bootstrap the library
require_relative "NxBootstrap.rb"

# ==========================================================================
# Example of TabbedCustomDialog class which allows you to include a settings
# dialog in your script
# ==========================================================================

# Open a test case, change this to a case on your system
$current_case = $utilities.getCaseFactory.open('D:\Cases\FakeData_8.0')

# Tell the library what the current case is
NuixConnection.setCurrentCase($current_case)

# Create an instance of the tabbed dialog
dialog = TabbedCustomDialog.new

# Associate a help file to the dialog, this will create a help menu item which
# will call on the OS to open the specified file when clicked
dialog.setHelpFile(File.join(File.dirname(__FILE__),"TestHelpFile.txt"))

# Demonstrate adding menu items
dialog.addMenu("Common Dialogs","Show Information Dialog") do
	CommonDialogs.showInformation("This is an example informative dialog!")
end
dialog.addMenu("Common Dialogs","Show Warning Dialog") do
	CommonDialogs.showWarning("This is an example warning dialog!")
end
dialog.addMenu("Common Dialogs","Show Error Dialog") do
	CommonDialogs.showError("This is an example error dialog!")
end

# ================================
# Add a tab and test date controls
# ================================
date_tab = dialog.addTab("date_tab","Date Controls")
date_tab.appendDatePicker("date001","Date Picker 1")
date_tab.appendDatePicker("date002","Date Picker 2",nil)
date_tab.appendDatePicker("date003","Date Picker 3","19820602")

# =====================================
# Add a tab and test checkable controls
# =====================================
checkable_tab = dialog.addTab("checkable_tab","Checkable Controls")
checkable_tab.appendCheckBox("check001","Checkbox 1",true)
checkable_tab.appendRadioButton("radio001","Radio 1 - Group 1", "group1", true)
checkable_tab.appendRadioButton("radio002","Radio 2 - Group 1", "group1", false)
checkable_tab.appendRadioButton("radio003","Radio 3 - Group 2", "group2", true)
checkable_tab.appendRadioButton("radio004","Radio 4 - Group 2", "group2", false)

food_choices = [
	"Pizza",
	"Pho",
	"Broccoli Beef",
	"Tea Leaf Salad",
	"Spicy Beef",
	"Potstickers",
	"Gyro",
	"Calzone",
	"Pancackes"
]

checked_food_choices = [
	"Pizza",
	"Gyro",
	"Calzone",
]

# With defaults checked
checkable_tab.appendMultipleChoiceComboBox("multi_choice_combo001","Multiple Choice (defaults)",food_choices,checked_food_choices)

# Without defaults checked
checkable_tab.appendMultipleChoiceComboBox("multi_choice_combo002","Multiple Choice (no defaults)",food_choices)

# ================================
# Add a tab and test text controls
# ================================
text_tab = dialog.addTab("text_tab","Text Controls")
text_tab.appendTextField("text002","Text Field","cat")
text_tab.appendCheckableTextField("text003_check",true,"text003_text","This is the text value","Use Text for Stuff?")
text_tab.appendTextArea("textarea001","Text Area","You can edit this one!")
information = <<INFO
This is an "information" text area.  Intended to allow you to provide notes to a user,
this text area's contents are read only.
INFO
text_tab.appendInformation("information001","Information Text Area",information)
text_tab.appendPasswordField("pass001","Password Field 1","")
text_tab.appendPasswordField("pass002","Password Field 2","cat")

# ================================
# Add a tab and test file controls
# ================================
file_tab = dialog.addTab("file_tab","File Controls")
file_tab.appendDirectoryChooser("path001","Directory Chooser")
file_tab.appendOpenFileChooser("path002","Open File Chooser","Text File","txt")
file_tab.appendSaveFileChooser("path003","Save File Chooser","Text File","txt")

# With initial dialog directory set
file_tab.appendDirectoryChooser("path004","Directory Chooser","C:\\Temp")
file_tab.appendOpenFileChooser("path005","Open File Chooser","Text File","txt","C:\\Temp")
file_tab.appendSaveFileChooser("path006","Save File Chooser","Text File","txt","C:\\Temp")

file_tab.appendHeader("File Path List")
file_tab.appendPathList("file_paths")

# ===============================
# Add a tab and test choice table
# ===============================
choice_table_tab = dialog.addTab("choice_table_tab","Choice Table")
choice_table_tab.appendStringChoiceTable("choices001","Tags",$current_case.getAllTags)

# =========
# CSV Table
# =========

csv_table_tab = dialog.addTab("csv_table_tab","CSV Table")
headers = [
	"Search Term",
	"Tag"
]
default_import_directory = "D:\\Temp"
csv_table_tab.appendCsvTable("csv_table",headers,default_import_directory)

# ===========================================
# Add a tab and test some other misc controls
# ===========================================
other_tab = dialog.addTab("other_tab","Other Controls")
other_tab.appendHeader("Header")
other_tab.appendSeparator("Separator")
other_tab.appendSpinner("spinner001","Spinner Control 1")
other_tab.appendSpinner("spinner002","Spinner Control 2",1337)
other_tab.appendSpinner("spinner003","Spinner Control 3",1337,100,2000)
other_tab.appendSpinner("spinner004","Spinner Control 4",1000,100,2000,100)

# ========================
# Radio button group tests
# ========================
radio_button_group_tab = dialog.addTab("radio_button_group_tab","Radio Button Group")

# First group
radio_buttons = {
	"I don't like animals" => "dont_like_animals_radio",
	"Cats" => "cat_radio",
	"Dog" => "dog_radio"
}
radio_button_group_tab.appendRadioButtonGroup("Favorite Animal","animal_radio_group",radio_buttons)

# Second group
radio_buttons = {
	"I don't like food" => "dont_like_food_radio",
	"Pizza" => "pizza_radio",
	"Hot Dog" => "hot_dog_radio",
	"Salad" => "salad_radio",
}
radio_button_group_tab.appendRadioButtonGroup("Favorite Food","food_radio_group",radio_buttons)

# ===============================================================================
# Test some of the controls designed to collect and feed settings to the Nuxi API
# ===============================================================================

# Worker settings.  Value returned by this control can be fed to setParallelProcessingSettings
workers_tab = dialog.addTab("workers_tab","Workers")
workers_tab.appendLocalWorkerSettings("worker_settings")

# Traversal settings.  Value returned by this control can be provided to 
# BatchExporter.setTraversalOptions
traversal_settings_tab = dialog.addTab("traversal_settings_tab","Traversal Settings")
traversal_settings_tab.appendBatchExporterTraversalSettings("traversal_settings")

# Some native export settings.  Value returned by this control can be provided to
# BatchExporter.addProduct as the second argument when the first argument is "native"
native_settings_tab = dialog.addTab("native_settings_tab","Native Settings")
native_settings_tab.appendBatchExporterNativeSettings("native_settings")

# Some text export settings.  Value returned by this control can be provided to
# BatchExporter.addProduct as the second argument when the first argument is "text"
text_settings_tab = dialog.addTab("text_settings_tab","Text Settings")
text_settings_tab.appendBatchExporterTextSettings("text_settings")

# Some OCR settings.  Value returned by this control can be provided to
# OcrProcessor.process or processAsync as the second argument
ocr_settings_tab = dialog.addTab("ocr_settings_tab","OCR Settings")
ocr_settings_tab.appendOcrSettings("ocr_settings")

# ==========================
# Test dynamic table control
# ==========================
dynamic_table_tab = dialog.addTab("dynamic_table_tab","Dynamic Table")

# Define the headers
headers = [
	"Name", "Color", "Age"
]

# Define some data to display
values = [
	{:name => "Ziggy", :color => "Grey", :age => 10},
	{:name => "Ringo", :color => "Grey", :age => 10},
	{:name => "Scrappy", :color => "Black", :age => 5},
	{:name => "Dolly", :color => "Black", :age => 1},
]

# Need to supply a callback which can fetch field values from a given record in the data
# and potential save values back assuming you wish the table to be editable
dynamic_table_tab.appendDynamicTable("dynamic_table","Dynamic Table",headers,values) do |record,column|
	case column
	when 0
		next record[:name]
	when 1
		next record[:color]
	when 2
		next record[:age].to_s
	else
		next "Unknown"
	end
end

# ============================================================================
# Display the dialog and then dump a listing of the settings the user provided
# ============================================================================
dialog.display
puts "Dialog Result: #{dialog.getDialogResult}"

# Display all the settings the user provided
puts "Dialog Data:"
dialog.toMap.each do |key,value|
	if key == "file_paths"
		puts "\t#{key}"
		value.each do |path|
			puts "\t\t#{path}"
		end
	elsif value.is_a?(java.util.Date)
		puts "\t#{key} = #{value.toString}"
	elsif value.is_a?(java.util.ArrayList)
		puts "\t#{key} = #{value.to_a.inspect}"
	else
		puts "\t#{key} = #{value.inspect}"
	end
end

# Close the case
$current_case.close