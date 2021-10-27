# Bootstrap NX JAR for Jython
import os
import sys

script_directory = os.path.dirname(os.path.realpath('__file__'))
nx_jar_file = os.path.join(script_directory,"Nx.jar")
sys.path.append(nx_jar_file)

from com.nuix.nx import NuixConnection
from com.nuix.nx import LookAndFeelHelper
from com.nuix.nx.dialogs import ChoiceDialog
from com.nuix.nx.dialogs import TabbedCustomDialog
from com.nuix.nx.dialogs import CommonDialogs
from com.nuix.nx.dialogs import ProgressDialog
from com.nuix.nx.dialogs import ProcessingStatusDialog
from com.nuix.nx.digest import DigestHelper
from com.nuix.nx.controls.models import Choice

LookAndFeelHelper.setWindowsIfMetal()
NuixConnection.setUtilities(utilities)
NuixConnection.setCurrentNuixVersion(NUIX_VERSION)

# ==========================================================================
# Example of TabbedCustomDialog class which allows you to include a settings
# dialog in your script
# ==========================================================================

# Open a test case, change this to a case on your system
current_case = utilities.getCaseFactory().open('D:\\cases\\Case 1',{"migrate":True})

# Tell the library what the current case is
NuixConnection.setCurrentCase(current_case)

# Create an instance of the tabbed dialog
dialog = TabbedCustomDialog()

# Associate a help file to the dialog, this will create a help menu item which
# will call on the OS to open the specified file when clicked
dialog.setHelpFile(os.path.join(script_directory,"TestHelpFile.txt"))

# Demonstrate adding menu items
def func():
	CommonDialogs.showInformation("This is an example informative dialog!")
dialog.addMenu("Common Dialogs","Show Information Dialog",func)

def func():
	CommonDialogs.showWarning("This is an example warning dialog!",func)
dialog.addMenu("Common Dialogs","Show Warning Dialog",func)

def func():
	CommonDialogs.showError("This is an example error dialog!")
dialog.addMenu("Common Dialogs","Show Error Dialog",func)

# ================================
# Add a tab and test date controls
# ================================
date_tab = dialog.addTab("date_tab","Date Controls")
date_tab.appendDatePicker("date001","Date Picker 1")
date_tab.appendDatePicker("date002","Date Picker 2",None)
date_tab.appendDatePicker("date003","Date Picker 3","19820602")

# =====================================
# Add a tab and test checkable controls
# =====================================
checkable_tab = dialog.addTab("checkable_tab","Checkable Controls")
checkable_tab.appendCheckBox("check001","Checkbox 1",True)
checkable_tab.appendRadioButton("radio001","Radio 1 - Group 1", "group1", True)
checkable_tab.appendRadioButton("radio002","Radio 2 - Group 1", "group1", False)
checkable_tab.appendRadioButton("radio003","Radio 3 - Group 2", "group2", True)
checkable_tab.appendRadioButton("radio004","Radio 4 - Group 2", "group2", False)

food_choices = []
food_choices.append("Pizza")
food_choices.append("Pho")
food_choices.append("Broccoli Beef")
food_choices.append("Tea Leaf Salad")
food_choices.append("Spicy Beef")
food_choices.append("Potstickers")
food_choices.append("Gyro")
food_choices.append("Calzone")
food_choices.append("Pancackes")

checked_food_choices = []
checked_food_choices.append("Pizza")
checked_food_choices.append("Gyro")
checked_food_choices.append("Calzone")

# With defaults checked
checkable_tab.appendMultipleChoiceComboBox("multi_choice_combo001","Multiple Choice (defaults)",food_choices,checked_food_choices)

# Without defaults checked
checkable_tab.appendMultipleChoiceComboBox("multi_choice_combo002","Multiple Choice (no defaults)",food_choices)

# ================================
# Add a tab and test text controls
# ================================
text_tab = dialog.addTab("text_tab","Text Controls")
text_tab.appendTextField("text002","Text Field","cat")
text_tab.appendCheckableTextField("text003_check",True,"text003_text","This is the text value","Use Text for Stuff?")
text_tab.appendTextArea("textarea001","Text Area","You can edit this one!")
information = """This is an "information" text area.  Intended to allow you to provide notes to a user,
this text area's contents are read only."""
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
choice_table_tab.appendStringChoiceTable("choices001","Tags",current_case.getAllTags())

# =========
# CSV Table
# =========
csv_table_tab = dialog.addTab("csv_table_tab","CSV Table")
headers = []
headers.append("Search Term")
headers.append("Tag")
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

combo_choices = []
combo_choices.append("Cat")
combo_choices.append("Dog")
combo_choices.append("Cat & Dog")
combo_choices.append("Mouse")
combo_choices.append("Bird")
combo_choices.append("Monkey")
combo_choices.append("Alligator")

other_tab.appendComboBox("combo01","Combo Box",combo_choices)

def func():
	CommonDialogs.showInformation("You changed your choice!")
other_tab.appendComboBox("combo02","Combo Box w/ Callback",combo_choices,func)
other_tab.appendSearchableComboBox("combo03","Searchable Combo Box",combo_choices)

# ========================
# Radio button group tests
# ========================
radio_button_group_tab = dialog.addTab("radio_button_group_tab","Radio Button Group")

# First group
radio_buttons = {
	"I don't like animals": "dont_like_animals_radio",
	"Cats": "cat_radio",
	"Dog": "dog_radio"
}
radio_button_group_tab.appendRadioButtonGroup("Favorite Animal","animal_radio_group",radio_buttons)

# Second group
radio_buttons = {
	"I don't like food": "dont_like_food_radio",
	"Pizza": "pizza_radio",
	"Hot Dog": "hot_dog_radio",
	"Salad": "salad_radio"
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
headers = ["Name", "Color", "Age"]

# Define some data to display
values = [
	{"name": "Ziggy", "color": "Grey", "age": 10},
	{"name": "Ringo", "color": "Grey", "age": 10},
	{"name": "Scrappy", "color": "Black", "age": 5},
	{"name": "Dolly", "color": "Black", "age": 1},
]

# Need to supply a callback which can fetch field values from a given record in the data
# and potential save values back assuming you wish the table to be editable
def table_func(record,column,set_value,value):
	if set_value == False:
		if column == 0:
			return record["name"]
		elif column == 1:
			return record["color"]
		elif column == 2:
			return record["age"]
		else:
			return "Unknown"
	else:
		if column == 0:
			record["name"] = value
		elif column == 1:
			record["color"] = value
		elif column == 2:
			record["age"] = value

dynamic_table_tab.appendDynamicTable("dynamic_table","Dynamic Table",headers,values,table_func)

# ============================================================================
# Display the dialog and then dump a listing of the settings the user provided
# ============================================================================
dialog.display()
print("Dialog Result: {}".format(dialog.getDialogResult()))

current_case.close()