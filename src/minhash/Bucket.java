package minhash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Bucket {
	private ArrayList<Integer> jmList;
	private boolean removed;
	
	public Bucket() {
		jmList = new ArrayList<Integer>();
		removed = false;
	}
	
	public void setRemoved(boolean removed) {
		this.removed = removed;
	}
	
	public boolean isRemoved() {
		return this.removed;
	}
	
	public void add(JavaMethod jm) {
		jmList.add(jm.getId());
	}
	
	public ArrayList<Integer> getAllItems() {
		return jmList;
	}
	
	public boolean contains(JavaMethod jm) {
		for (int i=0; i<jmList.size(); i++) {
			if (jmList.get(i)==jm.getId())
				return true;
		}
		return false;
	}
	
	public Integer get(int index) {
		return jmList.get(index);
	}
	
	public void addSet(Set<Integer> jSet) {
		TreeSet<Integer> jmSet = new TreeSet<Integer>(jmList);
		// find union
		jmSet.addAll(jSet);
		jmList = new ArrayList<Integer>(jmSet);
	}
	
	public Set<Integer> getAllItemSet() {
		Set<Integer> items = new TreeSet<Integer>();
		for (int i=0; i<jmList.size(); i++)
			items.add(jmList.get(i));
		return items;
	}
	
	public int size() {
		return jmList.size();
	}
}
