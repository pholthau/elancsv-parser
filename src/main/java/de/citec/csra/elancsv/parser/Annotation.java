/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.elancsv.parser;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class Annotation {
	
	public final long start;
	public final long stop;
	public final long duration;
	public final String value;

	public Annotation(long start, long stop, long duration, String value) {
		this.start = start;
		this.stop = stop;
		this.duration = duration;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[start='"+ start + "', stop='" + stop + "', duration='" + duration + "', value='" + value + "']";
	}
}
