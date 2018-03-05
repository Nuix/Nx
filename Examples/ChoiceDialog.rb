# Bootstrap the library
require_relative "NxBootstrap.rb"

# ===================================
# Example usage of ChoiceDialog class
# ===================================

# This line keeps the Java style consistent with Nuix look and feel when running from console
LookAndFeelHelper.setWindowsIfMetal

# A choice contains a value, and optionally: a label, tooltip, initial state (checked/unchecked)
# Value can basically be anything.  When you query for what the user selected you get
# a list of values based on their selection.  The actual checkbox list displays the provided labels.

# Class for demonstration purposes
class SomeThing
	attr_accessor :name
	def initialize(name)
		@name = name
	end
end

choices = []
# Create a choice with label and value, value can be basic types (string, int, etc) or
# objects like a custom class or even Nuix objects.  The value is just data associated
# with the choice that is returned as the value of the selection, but what is displayed
# is provided independently
choices << Choice.new(SomeThing.new("Name 1"),"Choice 1")

# Create a choice with label, value and tooltip
choices << Choice.new(SomeThing.new("Name 2"),"Choice 2","This is choice number 2")

# Create a choice with label, value, tooltip and state checked (default is unchecked)
choices << Choice.new(SomeThing.new("Name 3"),"Choice 3","This is choice number 3",true)

# Create the choice dialog
type_name = "Some Thing" # Label for column
dialog_title = "Pick one or more 'SomeThing' choices"

# Selected choices will be nil if user canceled or closed dialog, otherwise
# it should be a list of SomeThing objects which correspond to the users selection
selected_choices = ChoiceDialog.forChoices(choices,type_name,dialog_title)

if selected_choices.nil?
	puts "User closed the dialog"
else
	selected_choices.each do |some_thing|
		puts "Selected SomeThing.name: #{some_thing.name}"
	end
end