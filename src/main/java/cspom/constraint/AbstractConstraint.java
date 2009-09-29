package cspom.constraint;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cspom.variable.Variable;

public abstract class AbstractConstraint implements Constraint {

	private final String name;

	private final Variable[] scope;

	private final int arity;

	private final Map<Variable, Integer> positions;

	private String description;

	public AbstractConstraint(final String description, final Variable... scope) {
		this(null, description, scope);
	}

	public AbstractConstraint(final String name, final String description,
			final Variable... scope) {
		this.scope = scope;
		this.name = name;
		this.description = description;
		arity = scope.length;
		positions = new HashMap<Variable, Integer>(arity);
		for (int i = arity; --i >= 0;) {
			positions.put(scope[i], i);
		}
	}

	public Variable[] getScope() {
		return scope;
	}

	public String toString() {
		return "(" + Arrays.toString(scope) + ")";
	}

	public int getArity() {
		return arity;
	}

	public int getPosition(final Variable variable) {
		return positions.get(variable);
	}

	public int hashCode() {
		return Arrays.hashCode(scope) * getDescription().hashCode();
	}

	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof AbstractConstraint)) {
			return false;
		}
		final AbstractConstraint constraint = (AbstractConstraint) object;
		return Arrays.equals(getScope(), constraint.getScope())
				&& description.equals(constraint.description);
	}
}
