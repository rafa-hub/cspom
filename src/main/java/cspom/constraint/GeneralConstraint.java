package cspom.constraint;

import cspom.variable.CSPOMVariable;

public final class GeneralConstraint extends AbstractConstraint {

	public GeneralConstraint(String name, String description,
			CSPOMVariable... scope) {
		super(name, description, scope);
	}

	public GeneralConstraint(String description, CSPOMVariable... scope) {
		super(description, scope);
	}

}
