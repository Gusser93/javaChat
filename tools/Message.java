package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tools.IrcParser.Command;
import tools.IrcParser.Mode;
import tools.IrcParser.Response;

import static tools.IrcParser.COLON;
import static tools.IrcParser.SPACE;
import static tools.IrcParser.CRLF;

public class Message {

	public static final String AT_ALL = "#*";

	private String prefix;
	private Command command;
	private Response response;
	public List<String> params;

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
			lastIndex = input.indexOf(SPACE);
			String param = input.substring(0, lastIndex);
			input = input.substring(lastIndex+1);
			params.add(param);
		}

		if(input.startsWith(COLON)) {
			String param = input.substring(1, input.length());
			//input = input.substring(input.length()-2);
			params.add(param.trim());
		}
		/*if(!input.equals(CRLF))
			throw new IllegalArgumentException("Input doesn't end with CR-LF. Input was\n" + original + "\n is actual " + input);*/
	}

	public Message(Command command, String... parameters) {
		this.params = new ArrayList<String>();
		this.command = command;
		Collections.addAll(this.params, parameters);
	}

	public Message(String server, List<String> parameters, Response res) {
		StringBuilder toBePrefix = new StringBuilder();
		toBePrefix.append(COLON).append(server);
		this.prefix = toBePrefix.toString();
		this.response = res;
		this.params = parameters;
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
		return new Message(Command.PRIVMSG, target, text);
	}

	public static Message sendBroadcastMessage(String text) {
		return sendPrivateMessage(AT_ALL, text);
	}

	public static Message sendNickname(String nickname) {
		return new Message(Command.NICK, nickname);
	}

	public static Message sendUser(String uname, String realname, Mode mode) {
		return new Message(Command.USER, uname, mode.toString(), "*", realname);
	}

	public static Message sendPassword(String password) {
		return new Message(Command.PASS, password);
	}

	public static Message sendWelcome(String nickname, String user, String host, String server){
		String prefix = new StringBuilder().append(nickname).append("!").append(user).append("@").append(host).toString();
		String text = Response.RPL_WELCOME.text + " " + prefix;
		List<String> parameters = new ArrayList<String>();
		Collections.addAll(parameters, nickname, text);
		return new Message(server, parameters, Response.RPL_WELCOME);
	}

	public String getTarget() {
		if(Command.PRIVMSG.equals(this.command)) {
			return this.params.get(0);
		} else
			throw new IllegalArgumentException("This message is not a private message with target");
	}

	public String getBody() {
		if(Command.PRIVMSG.equals(this.command)) {
			System.out.println(Arrays.toString(params.toArray()));
			return this.params.get(1);
		} else
			throw new IllegalArgumentException("This message is not a private message with body");
	}

	@Override
	public String toString() {
		StringBuilder message = new StringBuilder();
		if(prefix != null) {
			message.append(COLON).append(prefix).append(SPACE);
		}
		if(command != null)
			message.append(command.toString()).append(SPACE);
		else {
			message.append(response.numeric).append(SPACE);
		}
		if(params.size() > 0) {
			for(int i = 0; i < params.size()-1; i++)
				message.append(params.get(i)).append(SPACE);
			message.append(COLON).append(params.get(params.size()-1));
		}
		message.append(CRLF);
		return message.toString();
	}
}
