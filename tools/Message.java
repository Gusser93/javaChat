package tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tools.IrcParser.Command;

import static tools.IrcParser.COLON;
import static tools.IrcParser.SPACE;
import static tools.IrcParser.CRLF;

public class Message {
	
	public static final String AT_ALL = "@all";
	
	private String prefix;
	private Command command;
	private List<String> params;

	public Message(String input) {
		String original = input;
		params = new ArrayList<String>();
		if(input.startsWith(COLON)) {
			int lastIndex = input.indexOf(SPACE);
			prefix = input.substring(1, lastIndex);
			input = input.substring(lastIndex+1);
		}
		
		int lastIndex = input.indexOf(SPACE);
		command = Command.valueOf(input.substring(0, lastIndex).toUpperCase());
		input = input.substring(lastIndex+1);
		
		while(!(input.startsWith(COLON) || input.equals(CRLF))) {
			String param = input.substring(0, lastIndex);
			input = input.substring(lastIndex+1);
			params.add(param);
		}
		
		if(input.startsWith(COLON)) {
			String param = input.substring(0, input.length()-2);
			params.add(param);
		}
		if(!input.equals(CRLF))
			throw new IllegalArgumentException("Input doesn't end with CR-LF. Input was\n" + original);
	}
	
	public Message(Command command, String... parameters) {
		this.command = command;
		Collections.addAll(this.params, parameters);
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public Command getCommand() {
		return command;
	}
	
	public String getCommandString() {
		return command.toString();
	}
	
	public List<String> getParams() {
		return params;
	}	
	
	public static Message sendPrivateMessage(String target, String text) {
		Message msg = new Message(Command.PRIVMSG, text);
		return msg;
	}
	
	public static Message sendBroadcastMessage(String text) {
		return sendPrivateMessage(AT_ALL, text);
	}
	
	@Override
	public String toString() {
		StringBuilder message = new StringBuilder();
		if(prefix != null) {
			message.append(COLON).append(prefix).append(SPACE);
		}
		message.append(command.toString()).append(SPACE);
		if(params.size() > 0) {
			for(int i = 0; i < params.size()-1; i++)
				message.append(params.get(i)).append(SPACE);
			message.append(COLON).append(params.get(params.size()-1));
		}
		message.append(CRLF);
		return message.toString();
	}
}