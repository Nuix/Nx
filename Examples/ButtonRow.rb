# Bootstrap the library
require_relative "NxBootstrap.rb"

# ==========================================================================
# Example of TabbedCustomDialog class which allows you to include a settings
# dialog in your script
# ==========================================================================

dialog = TabbedCustomDialog.new("Button Row Testing")
main_tab = dialog.addTab("main_tab","Main Tab")

# This appendX method actually returns a new control unlike most
# the other which returns the CustomTabPanel
# We want to capture this new control because it is to it that
# we are going to add buttons!
button_row_alpha = main_tab.appendButtonRow("button_row_alpha")

button_row_alpha.appendButton("button_red","Red") do
	CommonDialogs.showInformation("You clicked 'Red'")
end

button_row_alpha.appendButton("button_green","Green") do
	CommonDialogs.showInformation("You clicked 'Green'")
end

button_row_alpha.appendButton("button_blue","Blue") do
	CommonDialogs.showInformation("You clicked 'Blue'")
end

# We can also use method chaining to add buttons, although we
# need to change how we provide the action listener block.  Each
# call to appendButton on the ButtonRow returns the ButtonRow.
button_row_beta = main_tab.appendButtonRow("button_row_beta")

button_row_beta
	.appendButton("button_triangle","Triangle"){ CommonDialogs.showInformation("You clicked 'Triangle'") }
	.appendButton("button_square","Square"){ CommonDialogs.showInformation("You clicked 'Square'") }
	.appendButton("button_pentagon","Pentagon"){ CommonDialogs.showInformation("You clicked 'Pentagon'") }

# Like other controls we can grab the native control if we wish to modify it.
button_red = main_tab.getControl("button_red")
button_red.setFont(button_red.getFont.deriveFont(java.awt.Font::BOLD))

dialog.display
dialog.toMap.each do |key,value|
	puts "#{key}: #{value}"
end