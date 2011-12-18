/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.geoff;

/**
 * Provides context for items to be added to a database and retained by
 * name so that they may be referred to from within the same context.
 *
 * @author Nigel Small
 */
public interface Namespace {

	/**
	 * Apply a single rule to this namespace.
	 *
	 * @param rule the rule to apply
	 * @throws DependencyException if the rule contains an unsatisfied dependency
	 * @throws IllegalRuleException if the rule content is deemed illegal
	 */
	public void apply(Rule rule) throws DependencyException, IllegalRuleException;

	/**
	 * Apply a set of rules to this namespace. The order in which these rules are
	 * applied should attempt to satisfy dependencies if possible.
	 *
	 * @param rules the set of rules to apply
	 * @throws DependencyException if dependencies cannot be resolved within the rule set
	 * @throws IllegalRuleException if the content of any rule is deemed illegal
	 */
	public void apply(RuleSet rules) throws DependencyException, IllegalRuleException;

}
