package nl.cyberwizzard.repdroid;

/**
 * Thrown when querying one of the command classes for an argument which its opcode
 * does not support.
 * @author Berend Dekens
 */
public class ArgumentInvalidException extends Exception {
	private static final long serialVersionUID = -5926793426456387680L;
	
	public ArgumentInvalidException(String str) {
		super(str);
	}
	
	public ArgumentInvalidException(int opcode, char arg) {
		super("Opcode "+opcode+" does not have argument named '"+arg+"'");
	}
}
