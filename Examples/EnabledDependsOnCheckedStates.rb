# Bootstrap the library
require_relative "NxBootstrap.rb"

# ==========================================================================
# Example of TabbedCustomDialog class which allows you to include a settings
# dialog in your script
# ==========================================================================

case_directory ||= "FALLBACK_VALUE"

# Open a test case, change this to a case on your system
$current_case = $utilities.getCaseFactory.create(case_directory,{})

# Tell the library what the current case is
NuixConnection.setCurrentCase($current_case)

# Create an instance of the tabbed dialog
dialog = TabbedCustomDialog.new
dialog.setSize(300,600)

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

# =====================================
# Add a tab and test checkable controls
# =====================================
checkable_tab = dialog.addTab("checkable_tab","Checkable Controls")

checkable_tab.appendHeader("enabledOnlyWhenAllChecked")
checkable_tab.appendCheckBoxes("chk_a","A",false,"chk_b","B",false)
checkable_tab.appendCheckBoxes("chk_c","C",false,"chk_d","D",false)
checkable_tab.appendCheckBox("chk_all_abcd","A and B and C and D",false)
dialog.enabledOnlyWhenAllChecked("chk_all_abcd","chk_a","chk_b","chk_c","chk_d")

checkable_tab.appendHeader("enabledOnlyWhenNoneChecked")
checkable_tab.appendCheckBoxes("chk_w","W",false,"chk_x","X",false)
checkable_tab.appendCheckBoxes("chk_y","Y",false,"chk_z","Z",false)
checkable_tab.appendCheckBox("chk_none_wxyz","not W and not X and not Y and not Z",false)
dialog.enabledOnlyWhenNoneChecked("chk_none_wxyz","chk_w","chk_x","chk_y","chk_z")

checkable_tab.appendHeader("enabledIfAnyChecked")
checkable_tab.appendCheckBoxes("chk_cat","W",false,"chk_dog","X",false)
checkable_tab.appendCheckBoxes("chk_mouse","Y",false,"chk_monkey","Z",false)
checkable_tab.appendCheckBox("chk_any_animal","cat or dog or mouse or monkey",false)
dialog.enabledIfAnyChecked("chk_any_animal","chk_cat","chk_dog","chk_mouse","chk_monkey")

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