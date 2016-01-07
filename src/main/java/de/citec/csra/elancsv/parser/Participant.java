/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.elancsv.parser;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class Participant {

	public final String id;
	public final String condition;
	public final String annotator;
	public final Map<String, Tier> tiers;

	public Participant(String id, String condition, String annotator) {
		this.id = id;
		this.condition = condition;
		this.annotator = annotator;
		this.tiers = new HashMap<>();
	}

}
