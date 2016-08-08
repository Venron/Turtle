package de.turtle.imp.models;

import java.io.Serializable;

public class Connection implements Serializable {
	private static final int DEFAULT_PORT = 55021;
	private String name;
	private String host;
	private int port;

	public Connection() {
		this.name = "localhost";
		this.host = "127.0.0.1";
		this.port = DEFAULT_PORT;
	}

	public Connection(String name, String host) {
		this.name = name;
		this.host = host;
		this.port = DEFAULT_PORT;
	}

	public Connection(String name, String host, int port) {
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static int getDefaultPort() {
		return DEFAULT_PORT;
	}

	@Override
	public String toString() {
		return "Connection [name=" + name + ", host=" + host + ", port=" + port + "]";
	}

}
