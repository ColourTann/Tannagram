import java.util.HashMap;

public class CharLink {
	char c;
	HashMap<Character, CharLink> links = new HashMap<>();
	boolean isWord;
	int maxWordFromHere=0;
	public CharLink(char c) {
		this.c=c;
	}
	public void makeWord(){
		this.isWord=true;
	}
	public CharLink link(char c){
		CharLink link = links.get(c);
		if(link==null){
			link = new CharLink(c);
			links.put(c, link);
		}
		return link;
	}
	public void setMaxWordFromHere(int length) {
		maxWordFromHere=Math.max(maxWordFromHere, length);
	}
}
