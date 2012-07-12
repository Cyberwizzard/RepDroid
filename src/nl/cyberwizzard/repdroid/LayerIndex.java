package nl.cyberwizzard.repdroid;

public class LayerIndex {
	public int index = 0;				// Layer number
	public int offset = 0;				// Offset in bytes to layer start
	public LayerIndex next = null;		// Linked list style reference to next
	public LayerIndex prev = null;
}
