package minhash;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class Dictionary {
	private final Set<String> ngramTermSet;
	
	public Dictionary() {
		ngramTermSet = new TreeSet<String>();
	}
	
	public void addAll(Collection<? extends String> ngrams) {
		ngramTermSet.addAll(ngrams);
	}
	
	public int size() {
		return ngramTermSet.size();
	}
	
	public Set<String> getSet() {
		return ngramTermSet;
	}
	
	public void print() {
		for (String s: ngramTermSet) {
			System.out.print(s + "\t");
		}
		System.out.println();
	}
}
