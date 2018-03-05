# Bootstrap the library
require_relative "NxBootstrap.rb"

# ======================================
# Examples using the CommonDialogs class
# ======================================

# Show dialog to get user to confirm something
confirmed = CommonDialogs.getConfirmation("Are you sure you want to know the current time?","Get Current Time")
if confirmed
	puts "The current time is #{Time.now}"
else
	puts "I will not show you the time then..."
end

# Show a dialog to have the user select a directory
directory = CommonDialogs.getDirectory("C:\\","Select a Directory")
puts "You selected directory: #{directory}"

# Show a dialog to have the user type something in
input = CommonDialogs.getInput("What is your favorite color?")
puts "Your favorite color is: #{input}"

# Show a dialog to have the user select one of several choices
pet_choices = [
	"Cat",
	"Dog",
	"Mouse",
]
pet_choice = CommonDialogs.getSelection("Select your favorite pet type",pet_choices)
puts "Your favorite pet: #{pet_choice}"

# Prompt user to pick save file location
save_file = CommonDialogs.saveFileDialog("C:\\","Plain Text File","txt","Select where to save the log")
puts "Save File: #{save_file}"

# Prompt user to pick a file to open
open_file = CommonDialogs.openFileDialog("C:\\","Comma Separated Values","csv","Select CSV file to open")
puts "Open File: #{open_file}"

# Show information message dialog
CommonDialogs.showInformation("This is some information","Informative!")

# Show warning message dialog
CommonDialogs.showWarning("This is a warning!","You have been warned")

# Show error message dialog
CommonDialogs.showError("Uh oh, something had an error (not really).","Error Dialog")