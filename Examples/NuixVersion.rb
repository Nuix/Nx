# Bootstrap the library
require_relative "NxBootstrap.rb"

# ===========================================================================
# Example of using NuixVersion class which allows your code to make decisions
# based on version of Nuix running
# ===========================================================================

current_version = NuixConnection.getCurrentNuixVersion

if current_version.isAtLeast("6")
	puts "You are safe to use Nuix 6 features"
end

if current_version.isAtLeast("6.2")
	puts "You are safe to use Nuix 6.2 features"
end

if current_version.isAtLeast("6.2.5")
	puts "You are safe to use Nuix 6.2.5 features"
end

if current_version.isLessThan("6.2.5")
	puts "Sorry this script requires Nuix 6.2.5 or higher"
end

if current_version.isLessThan("6.2")
	puts "Sorry 6.2 specific features will be disabled"
end