import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import com.sun.net.httpserver.*;
public class Main {

	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("words.txt"));
			String word=null;
			while((word=br.readLine())!=null){
				addWord(word);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(34197), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.createContext("/", new MyHandler());
		server.setExecutor(null); // creates a default executor
		System.out.println("server active");
		server.start();
	}


	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			InputStream body = t.getRequestBody();
			BufferedReader br = new BufferedReader(new InputStreamReader(body));
			String request ="";
			String s =br.readLine();
			if(s!=null&&s.contains("=")){
				request=s.substring(s.indexOf("=")+1);
			}
			request = URLDecoder.decode(request);
			request=request.toLowerCase();
			request=request.replaceAll("[^a-z]", "");
			if(request.length()>MAX_LENGTH){
				System.out.println("someone tried to break it");
			}
			else{
				System.out.println("request: "+request);
			}
			
			String response = createHTML(request);
			t.sendResponseHeaders(200, response.length());
			t.getResponseHeaders().add("Content-Type", "text/html");
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}


	static StringBuilder HTMLBuilder= new StringBuilder();
	static final int MAX_LENGTH = 50;
	static String createHTML(String word){

		HTMLBuilder.setLength(0);
		HTMLBuilder.append("<html>\n");
		HTMLBuilder.append("<body>\n");
		HTMLBuilder.append("<h1>Tannagram Finder</h1>\n");
		if(word!=null && word.length()>0){

			if(word.length()<MAX_LENGTH){

				long time = System.currentTimeMillis();
				ArrayList<String> anagrams = anagramise(word, false);
				long timeTaken=System.currentTimeMillis()-time;
				if(anagrams.size()==0){
					HTMLBuilder.append("<p>no anagrams found for "+word+" :(</p>\n");
				}
				else{
					if(anagrams.size()==1 && anagrams.get(0).equals(word)){
						HTMLBuilder.append("<p>"+word+" is a word but there are no anagrams</p>\n");
					}
					else{
					if(anagrams.size()>1 && anagrams.get(0).equals(word)){ 
						HTMLBuilder.append("<p>"+word+" is a word</p>\n");
					}
					HTMLBuilder.append(
							"<h3>Anagrams for "+word+":</h3>\n"+
							"<ul type=\"disc\">\n");
					for(String s:anagrams){
						if(s.equals(word))continue;
						HTMLBuilder.append("<li>"+s+"</li>\n");
					}
					HTMLBuilder.append("</ul>\n");
					}
				}
				HTMLBuilder.append("<p>took "+timeTaken/1000f+" seconds</p>");
			}
			else{
				HTMLBuilder.append("Stop trying to break my server!\n");
			}
		}

		HTMLBuilder.append("<form method=\"post\">\n"+
				"Enter a word: <input type=\"text\" name=\"query\" value=\"\">\n"+
				"<input type=\"submit\" value=\"Submit\">\n"+
				"</form>");
		HTMLBuilder.append("</body>\n");
		HTMLBuilder.append("</html>\n");
		return HTMLBuilder.toString();	
	}
	
	static ArrayList<String> anagramise(String source, boolean print){
		ArrayList<String> results = new ArrayList<>();
		boolean foundSelf=false;
		TraversableString s = new TraversableString(source);
		boolean ok = true;
		while(ok){
			int dist = couldBeWord(s.toString(), source.length());
			if(dist == -1){
				if(s.toString().equals(source))foundSelf=true;
				if(!results.contains(s.toString()) && !s.toString().equals(source)) results.add(s.toString());
				dist = s.toString().length()-1;
			}
			ok =s.addAt(s.toString().length()-1-dist);

		}

		if(print){
			if(results.size()==0){
				System.out.print("no anagrams found for "+source);
			}
			else{
				System.out.print("anagrams for "+source+": "+results.get(0));
				for(int i=1;i<results.size();i++){
					System.out.print(", "+results.get(i));
				}
			}
			System.out.println();
		}
		if(foundSelf)results.add(0, source);
		return results;
	}
	
	static ArrayList<ArrayList<String>> answers = new ArrayList<>();
	

	
//	static void thing (ArrayList<String> partial, String leftover){
//		if(leftover.length()==0){
//			answers.add(partial);
//			return;
//		}
//		TraversableString s = new TraversableString(leftover);
//		boolean ok = true;
//		while(ok){
//			int dist = couldBeWord(s.toString(), leftover.length());
//			if(dist == -1){
//				if(!results.contains(s.toString()) && !s.toString().equals(source)) results.add(s.toString());
//				dist = s.toString().length()-1;
//			}
//			ok =s.addAt(s.toString().length()-1-dist);
//		}
//		
//	}

	static HashMap<Character, CharLink> linkEntry = new HashMap<>();

	static CharLink entry = new CharLink('0');

	static ArrayList<CharLink> links = new ArrayList<>();

	static void addWord(String s){
		links.clear();
		CharLink prevLink=entry;
		CharLink newLink=null;
		for(int i=0;i<s.length();i++){
			char thisChar = s.charAt(i);
			newLink = prevLink.link(thisChar);
			links.add(newLink);
			if(i==s.length()-1){
				for(CharLink cl:links){
					cl.setMaxWordFromHere(s.length());
				}
				newLink.makeWord();
			}
			prevLink=newLink;
		}
	}

	static int couldBeWord(String word, int wordSize){
		int distance =0;
		CharLink current=entry;

		while(current.links.get(word.charAt(distance))!=null){
			current = current.links.get(word.charAt(distance));
			if(current.maxWordFromHere<wordSize) return distance;
			distance ++;
			if(distance == word.length()){
				if(current.isWord) return -1;
				else return distance;
			}
		}

		return distance;
	}

	static boolean isWord(String word){
		CharLink current=entry;
		for(int i=0;i<word.length();i++){
			char c = word.charAt(i);
			current = current.links.get(c);
			if(current==null) return false;
			if(i==word.length()-1){
				return current.isWord;
			}
		}
		return false;
	}
}
