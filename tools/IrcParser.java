package tools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class IrcParser {
	public static final String SPACE = String.valueOf((char)0x20);
	public static final String CRLF = new StringBuilder().append((char)0x0D).append((char)0x0A).toString();
	public static final String COLON = String.valueOf((char)0x3a);
	
	public static enum Command {
		PASS, NICK, USER, OPER, MODE, SERVICE, QUIT, SQUIT,
		JOIN, PART, TOPIC, NAMES, LIST, INVITE, KICK,
		PRIVMSG, NOTICE,
		MOTD, LUSERS, VERSION, STATS, LINKS, TIME, CONNECT, TRACE, ADMIN, INFO,
		SERVLIST, SQUERY,
		WHO, WHOIS, WHOWAS,
		KILL, PING, PONG, ERROR,
		AWAY, REHASH, DIE, RESTART, SUMMON, USERS, WALLOPS, USERHOST, ISON;
	}
	
	public static enum Response {
		RPL_WELCOME("001", "Welcome to the Internet Relay Netowrk"),// <nick>!<user>@<host>"),
		RPL_YOURHOST("002", "Your host is <servername>, running version <ver>"),
		RPL_CREATED("003", "Your server was created <date>"),
		RPL_MYINFO("004", "<servername> <version> <available user modes> <availavle channel modes>"),
		RPL_BOUNCE("005", "Try server <server name>, port <port>"),
		ERR_ALREADYREGISTRED("462", "Unauthorized command (already registered)");
		
		String numeric;
		String text;
		Method genText;
		Response(String num, String txt) {
			this.numeric = num;
			this.text = txt;
		}
		
		
	}
	
	public static enum Mode {
		NONE;
	}
}
