/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.elancsv.parser;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class Tier {

	public final String name;
	public final Set<Annotation> annotations;
//	public final boolean numeric;

	public Tier(String name) {
		this.name = name;
		this.annotations = new HashSet<>();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name='" + name + "', values=" + annotations + "]";
	}

}
