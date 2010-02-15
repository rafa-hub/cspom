/**
 * CSPFJ Competitor - CSP solver using the CSPFJ API for Java
 * Copyright (C) 2006 Julien VION
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package cspom.extension;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public final class Extension {

	// private static final Logger logger = Logger
	// .getLogger("competitor.problem.relation");

	private final int arity;

	private final boolean init;

	private final Collection<Number[]> tuples;

	private final String name;

	public Extension(final String name, final int arity, final boolean init) {
		super();
		this.arity = arity;
		this.init = init;
		this.tuples = new ArrayList<Number[]>();
		this.name = name;
	}

	public void parse(final int nbTuples, final String string)
			throws ParseException {

		final String[] tupleList = string.split("\\|");

		if (tupleList.length != nbTuples) {
			throw new ParseException("Inconsistent number of Tuples ("
					+ tupleList.length + " /= " + nbTuples + ") in " + string,
					0);
		}

		for (String parsedTuple : tupleList) {
			final String[] valueList = parsedTuple.trim().split(" +");

			if (valueList.length != arity) {
				throw new ParseException("Incorrect arity (" + valueList.length
						+ " /= " + arity + ") in " + parsedTuple.trim(), 0);
			}

			final Number[] tuple = new Number[arity];
			for (int j = arity; --j >= 0;) {
				tuple[j] = Integer.parseInt(valueList[j]);
			}
			addTuple(tuple);

		}
	}

	public void addTuple(final Number[] tuple) {
		assert tuple.length == arity;
		tuples.add(tuple.clone());
	}

	public int getArity() {
		return arity;
	}

	public boolean init() {
		return init;
	}

	public Collection<Number[]> getTuples() {
		return tuples;
	}

	public String tupleString() {
		final StringBuilder stb = new StringBuilder();
		for (Number[] tuple : tuples) {
			for (Number value : tuple) {
				stb.append(value).append(' ');
			}
			stb.delete(stb.length() - 1, stb.length()).append('|');
		}
		return stb.delete(stb.length() - 1, stb.length()).toString();
	}

	public String toString() {

		return super.toString() + ": " + arity + "-ary, " + tuples.size()
				+ " tuples, " + (init ? "conflicts" : "supports");// + ": "
		// +
		// tupleString();

	}

	public int getNbTuples() {
		return tuples.size();
	}

	public boolean evaluate(final Number[] values) {
		for (Number[] tuple : tuples) {
			if (Arrays.equals(tuple, values)) {
				return !init;
			}
		}
		return init;
	}

	public Extension reverse(final int[] newOrder) {
		final Extension reversed = new Extension("rev-" + name, arity, init);

		for (Number[] tuple : tuples) {
			final Number[] reversedTuple = new Number[arity];
			for (int j = arity; --j >= 0;) {
				reversedTuple[j] = tuple[newOrder[j]];
			}
			reversed.addTuple(reversedTuple);
		}

		assert reversed.getNbTuples() == getNbTuples();
		return reversed;
	}

	public String getName() {
		return name;
	}
}
