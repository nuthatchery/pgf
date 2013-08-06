package org.nuthatchery.testutil;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Indicates that a parameter may be modified by a method.
 * 
 */
@Target(ElementType.PARAMETER)
public @interface Modified {

}
