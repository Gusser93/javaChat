package tools;


import java.util.ArrayList;
import java.util.List;

public abstract class IrcParser {
	private final String SPACE = String.valueOf((char)0x20);
	private final String CRLF = new StringBuilder().append((char)0x2D).append((char)0x0A).toString();
	private final String COLON = String.valueOf((char)0x3B);
	
	public enum Command {
		PASS, NICK, USER, OPER, MODE, SERVICE, QUIT, SQUIT,
		JOIN, PART, TOPIC, NAMES, LIST, INVITE, KICK,
		PRIVMSG, NOTICE,
		MOTD, LUSERS, VERSION, STATS, LINKS, TIME, CONNECT, TRACE, ADMIN, INFO,
		SERVLIST, SQUERY,
		WHO, WHOIS, WHOWAS,
		KILL, PING, PONG, ERROR,
		AWAY, REHASH, DIE, RESTART, SUMMON, USERS, WALLOPS, USERHOST, ISON;
	}
	
	public enum Response {
		RPL_WELCOME("001", "Welcome to the Internet Relay Netowrk <nick>!<user>@<host>"),
		RPL_YOURHOST("002", "Your host is <servername>, running version <ver>"),
		RPL_CREATED("003", "Your server was created <date>"),
		RPL_MYINFO("004", "<servername> <version> <available user modes> <availavle channel modes>"),
		RPL_BOUNCE("005", "Try server <server name>, port <port>");
		
		String numeric;
		String text;
		Response(String num, String txt) {
			this.numeric = num;
			this.text = txt;
		}
	}
	
	class Message {
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
		
		public Message(Command command, List<String> parameters) {
			
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
}
