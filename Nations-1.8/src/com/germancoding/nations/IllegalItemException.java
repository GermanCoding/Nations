package com.germancoding.nations;

/**
 * This exception is thrown when the plugin found a invalid NationItemStack or if a NationItemStack failed to load.
 * @author Max
 *
 */
public class IllegalItemException extends Exception {

	private static final long serialVersionUID = -4008896039022392945L;

	public IllegalItemException() {
		super();
	}

	public IllegalItemException(String s) {
		super(s);
	}

}
