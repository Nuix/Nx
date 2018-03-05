# Bootstrap the library
require_relative "NxBootstrap.rb"

# ==========================================================================
# Example usage of SourceItemVisitor class which makes it a little easier to
# traverse source data using the SourceItem class in Nuix
# ==========================================================================

# SourceItemVisitor is a helper class that exposes the recursive nature of SourceItem in a more iterative manner
java_import "com.nuix.nx.sourceitem.SourceItemVisitor"
visitor = SourceItemVisitor.new
visitor.onVisit do |source_item|
	indent = "=" * source_item.getPath.size
	puts "#{indent}> #{source_item.getKind} - #{source_item.getLocalisedName}"
	next true
end
visitor.visit("C:\\@NUIX\\Natives",nil,nil)