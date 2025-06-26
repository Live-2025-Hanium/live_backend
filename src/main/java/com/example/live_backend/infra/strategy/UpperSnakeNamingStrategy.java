package com.example.live_backend.infra.strategy;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class UpperSnakeNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return toUpperCase(super.toPhysicalTableName(name, jdbcEnvironment));
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return toUpperCase(super.toPhysicalColumnName(name, jdbcEnvironment));
	}

	private Identifier toUpperCase(Identifier identifier) {
		if (identifier == null) {
			return null;
		}
		return Identifier.toIdentifier(identifier.getText().toUpperCase(), identifier.isQuoted());
	}
}
