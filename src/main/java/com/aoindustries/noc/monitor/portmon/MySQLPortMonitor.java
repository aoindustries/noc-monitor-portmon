/*
 * Copyright 2009-2013, 2016, 2017, 2018, 2019, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserve parameterd.
 */
package com.aoindustries.noc.monitor.portmon;

import com.aoindustries.aoserv.client.mysql.Database;
import com.aoindustries.aoserv.client.mysql.Server;
import com.aoindustries.aoserv.client.mysql.User;
import com.aoindustries.net.InetAddress;
import com.aoindustries.net.Port;
import com.aoindustries.net.URIEncoder;
import com.aoindustries.net.URIParameters;

/**
 * Monitors a MySQL database.
 *
 * @author  AO Industries, Inc.
 */
public class MySQLPortMonitor extends JdbcPortMonitor {

	private final boolean ssl;

	public MySQLPortMonitor(InetAddress ipAddress, Port port, URIParameters monitoringParameters) {
		super(ipAddress, port, monitoringParameters);
		if(ipAddress.isLoopback()) {
			// Do not use SSL unless explicitely enabled with ssl=true
			ssl = "true".equalsIgnoreCase(monitoringParameters.getParameter("ssl"));
		} else {
			// Use SSL unless explicitely disabled with ssl=false
			ssl = !"false".equalsIgnoreCase(monitoringParameters.getParameter("ssl"));
		}
	}

	@Override
	protected String getDriver() {
		// TODO: com.mysql.cj.jdbc.Driver once using JDBC driver 8.0+
		return "com.mysql.jdbc.Driver";
	}

	/**
	 * See <a href="https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-configuration-properties.html">https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-configuration-properties.html</a>,
	 *     <a href="https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-using-ssl.html">https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-using-ssl.html</a>
	 */
	@Override
	protected String getJdbcUrl(InetAddress ipAddress, int port, String database) {
		StringBuilder jdbcUrl = new StringBuilder();
		jdbcUrl
			.append("jdbc:mysql://")
			.append(ipAddress.toBracketedString());
		if(port != Server.DEFAULT_PORT.getPort()) {
			jdbcUrl.append(':').append(port);
		}
		jdbcUrl.append('/');
		URIEncoder.encodeURIComponent(database, jdbcUrl);
		jdbcUrl.append("?connectTimeout=");
		URIEncoder.encodeURIComponent(Integer.toString(TIMEOUT), jdbcUrl);
		jdbcUrl.append("&socketTimeout=");
		URIEncoder.encodeURIComponent(Integer.toString(TIMEOUT), jdbcUrl);
		jdbcUrl.append("&tcpKeepAlive=true");
		jdbcUrl.append("&useSSL=");
		URIEncoder.encodeURIComponent(Boolean.toString(ssl), jdbcUrl);
		if(ssl) {
			jdbcUrl.append("&requireSSL=true");
		}
		jdbcUrl.append("&netTimeoutForStreamingResults=");
		URIEncoder.encodeURIComponent(Integer.toString(TIMEOUT), jdbcUrl);
		return jdbcUrl.toString();
	}

	@Override
	protected String getDefaultUsername() {
		return User.MYSQLMON.toString();
	}

	@Override
	protected String getDefaultDatabase() {
		return Database.MYSQLMON.toString();
	}
}
