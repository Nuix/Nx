Nx
==========

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) ![This script was last tested in Nuix 9.10](https://img.shields.io/badge/Script%20Tested%20in%20Nuix-9.10-green.svg)

View the GitHub project [here](https://github.com/Nuix/Nx) or download the latest release [here](https://github.com/Nuix/Nx/releases).

View the Java Docs [here](https://nuix.github.io/Nx/).

# Overview

**Written By:** Jason Wells

Nx is a library of classes designed to make it easier to create a settings dialog from a script and display a progress dialog while a script performs some work.  The classes in the library, most notably [TabbedCustomDialog](https://github.com/Nuix/Nx/blob/master/Java/src/main/java/com/nuix/nx/dialogs/TabbedCustomDialog.java), attempt to simplify the GUI construction process for a script by abstracting away the numerous Java calls that might normally be required to build a GUI, down to simpler calls like:

```ruby
# Add a checkbox
main_tab.appendCheckBox("apply_tags","Apply Tags",false)
```

Additionally [TabbedCustomDialog](https://github.com/Nuix/Nx/blob/master/Java/src/main/java/com/nuix/nx/dialogs/TabbedCustomDialog.java) provides the ability to save and/or load control settings to/from JSON files.

![image](https://user-images.githubusercontent.com/11775738/43547829-491de700-9591-11e8-8c01-0c9e60d3cfb8.png)

# Getting Started

## Setup

If you wish to compile/view/alter the code, open the `Nx` sub-directory in [IntelliJ IDEA](https://www.jetbrains.com/idea/download/other.html) (note that community edition should be fine).  You will need accomplish a few additional steps to build/test the project.

First you will need to download a Nuix Engine Release from [here](https://download.nuix.com/releases/engine/).  Then extract the downloaded zip file's contents somewhere such as `C:\EngineRelease`.

![image](https://github.com/Nuix/Nx/assets/11775738/81d9c057-532a-4622-ae7f-b857fbef1578)

Then head over to your environment variables and configure the following:

| Variable                 | Optional     | Description |
|--------------------------|--------------|-------------|
| `NUIX_ENGINE_DIR`        | **REQUIRED** | Gradle build file uses this to resolve some Nuix run-time dependencies Nx builds against as well as necessary pieces to run tests. |
| `NUIX_USERNAME`          | **OPTIONAL** | Used by tests which need a running Nuix Engine instance to obtain a CLS (Cloud License Server) license.                            |
| `NUIX_PASSWORD`          | **OPTIONAL** | Used by tests which need a running Nuix Engine instance to obtain a CLS (Cloud License Server) license.                            |

![image](https://github.com/Nuix/Nx/assets/11775738/54b47ad3-7292-4daa-b2d4-da4ae5971c95)

If you just wish to build the JAR then run the task **nxOnlyJar** which will produce a jar in the `JAR` directory at the repository root.

![image](https://github.com/Nuix/Nx/assets/11775738/d44e1b76-b48c-4d91-8cec-f2b58753541d)

If you wish to execute some of the Ruby examples, locate the `RubyExamples` tests

![image](https://github.com/Nuix/Nx/assets/11775738/87be071c-7c8d-4b3b-affb-15d7a50c1ce2)

Then from within that file, you can execute any individual example test

![RunTest](https://github.com/Nuix/Nx/assets/11775738/0ac840c2-ada5-4f8f-98f3-4fb64f8d2f2f)

## Using Ruby

Once you have downloaded or built an Nx JAR file, copy it to the same directory as your script.  From your script you can then [bootstrap](https://github.com/Nuix/Nx/blob/master/Examples/NxBootstrap.rb) the library like so:

```ruby
# This essentially "bootstraps" the library from a Ruby script
# The following code can easily be copied to the top of your
# new script to get you started.

# Determine the directory in which this script resides
script_directory = File.dirname(__FILE__)

# Load up the JAR
require File.join(script_directory,"Nx.jar")

# Import classes from the JAR so we don't need to reference them by fully qualified name mostly
# as a convenience step
java_import "com.nuix.nx.NuixConnection"
java_import "com.nuix.nx.LookAndFeelHelper"
java_import "com.nuix.nx.dialogs.ChoiceDialog"
java_import "com.nuix.nx.dialogs.TabbedCustomDialog"
java_import "com.nuix.nx.dialogs.CommonDialogs"
java_import "com.nuix.nx.dialogs.ProgressDialog"
java_import "com.nuix.nx.dialogs.ProcessingStatusDialog"
java_import "com.nuix.nx.digest.DigestHelper"
java_import "com.nuix.nx.controls.models.Choice"

# Sets the look and feel to "windows" if it is currently "metal".  This is for consistency
# if a script is being ran via nuix_console.exe
LookAndFeelHelper.setWindowsIfMetal

# Hand off copy of the Utilities object to the library
NuixConnection.setUtilities($utilities)

# Inform the library of the current version of Nuix.  This allows the library (and your code) to
# branch logic based on version in use via the NuixVersion class in the library
NuixConnection.setCurrentNuixVersion(NUIX_VERSION)
```

## Using Python

Nuix uses Jython to run Python scripts (JRuby for Ruby scripts).  This allows you to use this library from Python as well by adapting the paradigms to their Jython equivalent.  While this repository has a Ruby focus, see [TabbedCustomDialog.py](https://github.com/Nuix/Nx/blob/master/Examples/TabbedCustomDialog.py) which demonstrates the porting of [TabbedCustomDialog.rb](https://github.com/Nuix/Nx/blob/master/Examples/TabbedCustomDialog.rb) from Ruby to Python.

## Example Settings Dialog

![image](https://user-images.githubusercontent.com/11775738/43551318-78e11ae8-959b-11e8-8872-7b87e44ae2b2.png)

```ruby
# Make sure bootstrap code from above has already been executed at some point before this!!

# Create an instance of the tabbed dialog
dialog = TabbedCustomDialog.new

# Optionally set dialog width and height
dialog.setSize(400,300)

# Create our first tab with identifier 'main_tab' and label 'Main'
main_tab = dialog.addTab("main_tab","Main")

# Add a checkbox
main_tab.appendCheckBox("apply_tags","Apply Tags",true)

# Add a text field
main_tab.appendTextField("tag_name","Tag Name","MyTag")

# Lets make it so tag name is only enabled when `apply_tags` is checked
main_tab.enabledOnlyWhenChecked("tag_name","apply_tags")

# Add another checkbox
main_tab.appendCheckBox("save_report","Save CSV Report",false)

# Add save file path control for CSV file
main_tab.appendSaveFileChooser("csv_report_file","Report CSV File","Comma Separated Values","csv")

# Lets make it so report file path chooser is only enabled when `save_report` is checked
main_tab.enabledOnlyWhenChecked("csv_report_file","save_report")

# Define validations.  This will be ran when user clicks "OK" allowing the following code to
# validate each setting.  We yield false if anything is not valid or ultimately yield true
# if everything looks good.
dialog.validateBeforeClosing do |values|
	# Make sure that if user is applying tags they provided a tag name
	if values["apply_tags"] && values["tag_name"].strip.empty?
		# Use dialog helper to show a user what was wrong
		CommonDialogs.showWarning("Please provide a non-empy tag name.","No Tag Name")
		# Make sure we yield false to signify validation did not pass
		next false
	end

	# Make sure that if user wants to save a report that they provided a file path
	if values["save_report"] && values["csv_report_file"].strip.empty?
		# Use dialog helper to show a user what was wrong
		CommonDialogs.showWarning("Please provide a report file path.","No Report File Path")
		# Make sure we yield false to signify validation did not pass
		next false
	end

	# If nothing failed validation make sure we yield True
	next true
end

# Show the dialog to the user.  This is a blocking call until
# the dialog closes either from clicking "OK" button or "Cancel"
# button or dialog is closed
dialog.display

# Determine result of showing dialog.
# True if user clicked "OK" and settings were valid
# False if user clicked "Cancel" or closed the dialog
if dialog.getDialogResult == true
	# Get a Ruby hash / Java Map of settings user provided in dialog
	values = dialog.toMap

	# Each setting's value is associated to the identifier provided
	puts "Apply Tags: #{values["apply_tags"]}"
	puts "Tag Name: #{values["tag_name"]}"
	puts "Save CSV Report: #{values["save_report"]}"
	puts "Report CSV File: #{values["csv_report_file"]}"

	# ...
	# Here we would do something interesting based upon the settings provided by the user
	# ...
end
```

A tab may have controls such as:
- Buttons
- Check Boxes
- Radio Buttons
- Combo Boxes
- Date Pickers
- Text Fields
- Password Fields
- Multi-line Text Areas
- Headers
- Numeric Spinners
- Tables
- More complex controls for things like worker settings and OCR settings

Refer to the [documentation for the CustomTabPanel](https://nuix.github.io/Nx/com/nuix/nx/dialogs/CustomTabPanel.html) class for a list of methods which add controls to a tab.  Further examples can be found in the [Examples directory](https://github.com/Nuix/Nx/tree/master/Examples).

# License

```
Copyright 2023 Nuix

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```