package tools;


import java.util.ArrayList;
import java.util.List;

public abstract class IrcParser {
	private final String SPACE = String.valueOf((char)0x20);
	private final String CRLF = new StringBuilder().append((char)0x2D).append((char)0x0A).toString();
	
	class Message {
		private String prefix;
		private String command;
		private List<String> params;
	
		public Message(String input) {
			String original = input;
			params = new ArrayList<String>();
			if(input.startsWith(":")) {
				int lastIndex = input.indexOf(SPACE);
				prefix = input.substring(0, lastIndex);
				input = input.substring(lastIndex);
			}
			
			int lastIndex = input.indexOf(SPACE);
			command = input.substring(0, lastIndex);
			input = input.substring(lastIndex);
			
			while(!(input.startsWith(":") || input.equals(CRLF))) {
				String param = input.substring(0, lastIndex);
				input = input.substring(lastIndex);
				params.add(param);
			}
			
			if(input.startsWith(":")) {
				String param = input.substring(0, input.length()-2);
			}
			if(!input.equals(CRLF))
				throw new IllegalArgumentException("Input doesn't end with CR-LF. Input was\n" + original);
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public String getCommand() {
			return command;
		}
		
		public List<String> getParams() {
			return params;
		}
	}
}
