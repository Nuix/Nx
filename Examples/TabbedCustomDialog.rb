# Bootstrap the library
require_relative "NxBootstrap.rb"

# ==========================================================================
# Example of TabbedCustomDialog class which allows you to include a settings
# dialog in your script
# ==========================================================================

case_directory = java.io.File.new(File.join(File.dirname(__FILE__),"TestCase_#{Time.now.to_i}"))
if case_directory.exists
	case_directory.delete
end

# Open a test case
$current_case = $utilities.getCaseFactory.create(case_directory,{})

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

# ===============================
# Add a tab and test choice table
# ===============================
string_list_tab = dialog.addTab("string_list_tab","String List Table")
string_list_tab.appendStringList("string_list_001")
string_list_tab.appendStringList("string_list_002", true)
string_list_tab.appendStringList("string_list_003",["Alpha","Beta","Gamma"])
string_list_tab.appendStringList("string_list_004",["Alpha","Beta","Gamma"],true)

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

other_tab.appendSlider("slider001","Slider Control 1",50_000.0,1.0,100_000.0)
other_tab.appendSlider("slider002","Slider Control 2",0.5)
other_tab.appendSlider("slider003","Slider Control 3",50_000,1,100_000)
other_tab.appendSlider("slider004","Slider Control 4",50)
other_tab.appendSlider("slider005","Slider Control 5")


combo_choices = [
	"Cat",
	"Dog",
	"Cat & Dog",
	"Mouse",
	"Bird",
	"Monkey",
	"Alligator",
]
other_tab.appendComboBox("combo01","Combo Box",combo_choices)
other_tab.appendComboBox("combo02","Combo Box w/ Callback",combo_choices) do
	CommonDialogs.showInformation("You changed your choice!")
end
other_tab.appendSearchableComboBox("combo03","Searchable Combo Box",combo_choices)

# Force a specific choice to be selected by default that isn't the first choice in the list of choices
other_tab.setText("combo01","Bird")

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

# Define custom settings to configure the OCR settings control with
regenerate_pdfs = true
update_pdf_text = true
update_item_text = true

# text modification choices: append, overwrite
text_modification = "append"

# quality choices: default, document_archiving_accuracy, document_archiving_speed, book_archiving_accuracy
#                  book_archiving_speed, document_conversion_accuracy, document_conversion_speed
#                  text_extraction_accuracy, text_extraction_speed, field_level_recognition
quality = "default" 

# rotation choices: auto, no_rotation, left, right, upside_down
rotation = "auto"

deskew = true
output_directory = "D:\\Temp\\MyOcrOutputDirectory"
update_duplicates = true
ocr_timeout_minutes = 73

# Languages choices: see https://github.com/Nuix/Nx/blob/master/Java/src/main/java/com/nuix/nx/controls/OcrSettings.java#L297-L482
languages = ["English","Catalan"]

ocr_control = ocr_settings_tab.getControl("ocr_settings")
ocr_control.getChckbxRegeneratePdfs.setSelected(regenerate_pdfs)
ocr_control.getChckbxUpdatePdfText.setSelected(update_pdf_text)
ocr_control.getChckbxUpdateItemText.setSelected(update_item_text)
ocr_control.getComboTextModification.setSelectedValue(text_modification)
ocr_control.getComboQuality.setSelectedValue(quality)
ocr_control.getComboRotation.setSelectedValue(rotation)
ocr_control.getChckbxDeskew.setSelected(deskew)
ocr_control.getOutputDirectory.setPath(output_directory)
ocr_control.setUpdateDuplicates(update_duplicates)
ocr_control.setTimeoutMinutes(ocr_timeout_minutes)

# Changing checked languages requires tweaking selected choices in the languages table
languages_table = ocr_control.getLanguageChoices
languages_table.getTableModel.uncheckAllChoices
checked_language_choices = []
languages.each do |language|
	language_choice = languages_table.getTableModel.getFirstChoiceByLabel(language)
	if !language_choice.nil?
		languages_table.getTableModel.setChoiceSelection(language_choice,true)
		checked_language_choices << language_choice
	else
		# Language wasn't able to be resolved to an available choice
	end
end
languages_table.getTableModel.sortChoicesToTop(checked_language_choices)

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