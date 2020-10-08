# Bootstrap the library
require_relative "NxBootstrap.rb"

# ==========================================================================
# Example of TabbedCustomDialog class which allows you to include a settings
# dialog in your script
# ==========================================================================

# Create an instance of the tabbed dialog
dialog = TabbedCustomDialog.new
dialog.enableStickySettings("D:\\temp\\TestStickSettings.json")

require 'securerandom'

# Generate a large amount of choices
puts "Generating random choices..."
choices = 1_000_000.times.map do |n|
	l = n.to_s.rjust(10,"0")
	next Choice.new(l,l,l,rand(0..100) > 95)
end

main_tab = dialog.addTab("main_tab","Main")
main_tab.appendChoiceTable("many_choices","Many Choices",choices)

puts "Displaying settings dialog..."
dialog.display