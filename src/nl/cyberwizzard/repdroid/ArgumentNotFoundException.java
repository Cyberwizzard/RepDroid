package nl.cyberwizzard.repdroid;

/**
 * Thrown when querying one of the Command classes for a missing argument.
 * @author Berend Dekens
 */

public class ArgumentNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ArgumentNotFoundException(String s) {
		super(s);
	}
	
	public ArgumentNotFoundException(char c) {
		super("Argument '"+c+"' not found");
	}
}
