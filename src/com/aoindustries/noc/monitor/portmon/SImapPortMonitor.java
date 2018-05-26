/*
 * Copyright 2018 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.noc.monitor.portmon;

import com.aoindustries.net.HttpParameters;
import com.aoindustries.net.InetAddress;
import com.aoindustries.net.Port;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Monitors with IMAP-specific protocol support over SSL.
 *
 * @author  AO Industries, Inc.
 */
public class SImapPortMonitor extends ImapPortMonitor {

	private final boolean ssl;

	public SImapPortMonitor(InetAddress ipAddress, Port port, HttpParameters monitoringParameters) {
		super(ipAddress, port, monitoringParameters);
		// Use SSL unless explicitely disabled with ssl=false
		ssl = !"false".equalsIgnoreCase(monitoringParameters.getParameter("ssl"));
	}

	/**
	 * This port is SSL.
	 */
	@Override
	protected boolean isSsl() {
		return true;
	}

	@Override
	protected Socket connect() throws Exception {
		boolean successful = false;
		Socket s = super.connect();
		try {
			if(ssl) {
				SSLSocketFactory sslFact = (SSLSocketFactory)SSLSocketFactory.getDefault();
				s = sslFact.createSocket(s, ipAddress.toString(), port.getPort(), true);
			}
			successful = true;
			return s;
		} finally {
			if(!successful) s.close();
		}
	}

	@Override
	public String checkPort(Socket socket, InputStream socketIn, OutputStream socketOut) throws Exception {
		if(ssl) {
			return super.checkPort(socket, socketIn, socketOut);
		} else {
			return CONNECTED_SUCCESSFULLY + " (SSL disabled)";
		}
	}
}
