import java.util.Arrays;
import java.util.HashSet;

public class TraversableString {
	String source;
	int[] letterIndices;
	public TraversableString(String word) {
		this.source=word;
		letterIndices = new int[word.length()];
		for(int i=0;i<letterIndices.length;i++){
			letterIndices[i]=i;
		}
	}
	
	public boolean addAt(int _index){
		
		int charIndex = source.length()-_index-1;
		int bestIndex = charIndex;
		
		for(int checkIndex = charIndex; checkIndex<source.length(); checkIndex++){
			if(letterIndices[checkIndex]>letterIndices[charIndex] && // a bigger index in the list
					(letterIndices[checkIndex]<letterIndices[bestIndex] || bestIndex==charIndex) && // and either smaller than the best index or if it hasn't been set yet 
					source.charAt(letterIndices[checkIndex])!=source.charAt(letterIndices[bestIndex])){ // and different to what you want to swap with
				bestIndex=checkIndex;
			}
		}
		if(bestIndex==charIndex){
			if(_index<source.length()-1){
				return addAt(_index+1);
			}
			else{
				return false;
			}
		}
		
		// swap charIndex and bestIndex //
		
		int temp = letterIndices[charIndex];
		letterIndices[charIndex] = letterIndices[bestIndex];
		letterIndices[bestIndex] = temp;
		
		// now sort the indices after bestIndex
		if(charIndex+1<source.length()) Arrays.sort(letterIndices, charIndex+1, source.length());
		
		if(checkSet.contains(toString().hashCode())){
			addAt(_index);
		}
		
		checkSet.add(toString().hashCode());
		
		return true;
	}
	HashSet<Integer> checkSet = new HashSet<>();
	static StringBuilder resultBuilder = new StringBuilder();
	public String toString(){
		resultBuilder.setLength(0);
		for(int i:letterIndices){
			resultBuilder.append(source.charAt(i));
		}
		return resultBuilder.toString();
	}
	
}
