package de.turtle.imp.models;

import java.io.Serializable;

public class TransferPacket implements Serializable {
	public static final int AUTH = 0, MESSAGE = 1, DISCONNECT = 2, SHOWALL = 3, TEST = 4;
	private int type;
	private String message;
	private User sender;

	public TransferPacket() {
		this(1, "SEND_MESSAGE", new User());
	}

	public TransferPacket(int type, String message, User sender) {
		this.type = type;
		this.message = message;
		this.sender = sender;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	@Override
	public String toString() {
		return "TransferPacket [type=" + type + ", message=" + message + ", sender=" + sender + "]";
	}

}
