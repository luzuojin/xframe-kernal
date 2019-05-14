package dev.xframe.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * .ct(code template) file parser
 * @author luzj
 */
public class CtParser {

	public static Map<String, String> parse(String file) {
	    Map<String, String> cts = new HashMap<>(8);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(file)))) {
			LinkedList<CT> queue = new LinkedList<>();
			CT cur = null;
			boolean comment = false;
			while(br.ready()) {
			    String l = br.readLine().trim();
				if(l.startsWith("/*")) {
					comment = true;
				}
				if(l.endsWith("*/")) {
					comment = false;
					continue;
				}
				if(comment) {
					continue;
				}
				
				if(l.startsWith("///")) {
					if(l.length() > 3) { //start
						if(cur != null) queue.addLast(cur);
						cur = new CT(l.substring(3));
					} else { //ends
						cts.put(cur.key, cur.val.toString());
						cur = queue.size() > 0 ? queue.pollLast() : null;
					}
					continue;
				}
				if(cur == null) {
					continue;
				}
				cur.add(l);
			}
		} catch (IOException e) {
			//ignore
		}
		return cts;
	}

	static class CT {
		final String key;
		final StringBuilder val;
		public CT(String key) {
			this.key = key;
			this.val = new StringBuilder();
		}
		public void add(String l) {
			if(val.length() > 0)
				val.append("\n");
			val.append(l);
		}
	}
	
}
