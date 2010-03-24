package cspom.dimacs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cspom.CSPOM;
import cspom.CSPParseException;
import cspom.DuplicateVariableException;
import cspom.compiler.PredicateParseException;
import cspom.variable.BooleanDomain;
import cspom.variable.CSPOMVariable;

public class CNFParser {
	private final CSPOM problem;

	private static final Pattern PARAMETER = Pattern
			.compile("^p cnf (\\d+) (\\d+)$");
	private static final Pattern VAR = Pattern.compile("(-?\\d+)");

	private final Map<Integer, CSPOMVariable> vars;

	public CNFParser(final CSPOM problem) {
		this.problem = problem;
		vars = new HashMap<Integer, CSPOMVariable>();
	}

	public void parse(InputStream is) throws IOException, CSPParseException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				is));

		List<Integer> currentClause = new ArrayList<Integer>();
		boolean parameter = false;
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			if (line.startsWith("c")) {
				continue;
			}
			final Matcher matcher = PARAMETER.matcher(line);
			if (matcher.matches()) {
				parameter = true;
				final int nbVars = Integer.valueOf(matcher.group(1));
				for (int i = 1; i <= nbVars; i++) {
					try {
						problem.addVariable(new CSPOMVariable("V" + i,
								BooleanDomain.DOMAIN));
					} catch (DuplicateVariableException e) {
						throw new IllegalStateException(e);
					}
				}
				continue;
			}
			if (!parameter) {
				throw new CSPParseException("Parameter line not found");
			}
			final Matcher varMatcher = VAR.matcher(line);
			while (varMatcher.find()) {
				final int var = Integer.valueOf(varMatcher.group(1));
				if (var == 0) {
					try {
						problem.ctr(clause(currentClause));
					} catch (PredicateParseException e) {
						throw new IllegalStateException(e);
					}
					currentClause.clear();
				} else {
					currentClause.add(var);
				}
			}
		}
	}

	private String clause(List<Integer> currentClause) {
		final StringBuilder clause = new StringBuilder();
		clause.append("or(");

		Iterator<Integer> i = currentClause.iterator();

		for (;;) {
			int v = i.next();
			if (v > 0) {
				clause.append('V').append(v);
			} else {
				clause.append("not(V").append(-v).append(')');
			}

			if (!i.hasNext()) {
				return clause.append(')').toString();
			}
			clause.append(", ");
		}

	}
}