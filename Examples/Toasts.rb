# Bootstrap the library
require_relative "NxBootstrap.rb"

# Toasts are small, transient informational dialogs.  They generally appear in the bottom left corner of an application
# animate in, display for a short period, then animate out.

java_import "com.nuix.nx.dialogs.Toast"
java_import "java.awt.Rectangle"
java_import "java.awt.Point"
java_import "java.awt.Dimension"


toast = Toast.new

###
# Display a Toast in a fixed position on screen (x = 1400, y = 1000)
toast_size = Dimension.new 500, 80
toast_location = Point.new 1400, 1000
toast_bounds = Rectangle.new toast_location, toast_size

toast.show_toast "Example Toast\n" +
                   "This toast is in a fixed position on the screen,", toast_bounds

# Wait a while so the previous toast goes away
sleep 10

if !window.nil?
    ###
    # This is a trick to get the toast to display in the bottom right corner of the Nuix Window.  It causes a brief
    # flicker in the UI.  It creates a new tab, gets screen coordinates, then closes that new tab.
    java_import "javax.swing.JPanel"
    tempView = JPanel.new
    window.addTab "Temp", tempView
    screen_location = tempView.location_on_screen
    screen_size = tempView.size
    tempView.parent.parent.close_selected_tab

    bottom = screen_location.y + screen_size.height
    right = screen_location.x + screen_size.width

    toast_size = Dimension.new 500, 80
    toast_location = Point.new right - toast_size.width, bottom - toast_size.height
    toast_bounds = Rectangle.new toast_location, toast_size

    toast.show_toast "Example Toast\n" +
                       "This toast should be in the bottom right of th Workstation.", toast_bounds
end