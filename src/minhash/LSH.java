package minhash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class LSH {
	private int band;
	private int row;
	private HashMap<String, Bucket> bucketMap;
	private HashMap<Integer, ArrayList<String>> jmMap;
	private ArrayList<String> collidedHashList = new ArrayList<String>();
	
	public LSH(int band, int row) {
		this.band = band;
		this.row = row;
		bucketMap = new HashMap<String, Bucket>();
		jmMap = new HashMap<Integer, ArrayList<String>>();
	}
	
	public LSH(int row) {
		this.row = row;
		bucketMap = new HashMap<String, Bucket>();
		jmMap = new HashMap<Integer, ArrayList<String>>();
	}
	
	public String hash(int[] signature, JavaMethod jm) {
		//System.out.println("Signature: " + Arrays.toString(signature));
		int index = 0;
		String hash = null;
		// go through each band
		while (index < signature.length) {
			// has all the rows in the same band
			String concatSigRow = "";
			for (int j = index; j < (index + row); j++)
				concatSigRow += signature[j];
			// System.out.println("concatenated signature = " + concatSigRow);
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-256");
				// Change this to "UTF-16" if needed
				md.update(concatSigRow.getBytes("UTF-8"));
				byte[] digest = md.digest();
				String hashString = String.format("%064x", new java.math.BigInteger(1, digest));
				hash = hashString;
				
				//System.out.println(hashString);
				// there's already some items in the same bucket
				Bucket b=bucketMap.get(hashString);
				if (b == null) { 
					b = new Bucket(); 
				}
				if (!b.contains(jm)) {
					b.add(jm);
					// add the method to the bucket using its signature's hash value
					bucketMap.put(hashString, b);
					// add the method to another hash map using its id
					// if none of the this method signature is found before, create it
					if (jmMap.get(jm.getId())==null)
					{
						ArrayList<String> sigList = new ArrayList<String>();
						sigList.add(hashString);
						jmMap.put(jm.getId(), sigList);
					} else {
						// add the signature's hash to the list
						ArrayList<String> temp = jmMap.get(jm.getId());
						temp.add(hashString);
						jmMap.put(jm.getId(), temp);
					}
					
					// found a hash with collisions
					if (b.size()>1 && !collidedHashList.contains(hashString)) {
						collidedHashList.add(hashString);
						//System.out.println(hashString);
					}
				}
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			index += row;
		}
		
		return hash;
	}
	
	public void printBuckets(boolean isAll) {
		if (isAll) {
			Iterator<Entry<String, Bucket>> it = bucketMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				String hashString = (String) pair.getKey();
				Bucket b = (Bucket) pair.getValue();
				// System.out.println(hashString + " = " + b.getAllItemSet());
			}
		} else {
			for (String s : collidedHashList) {
				Bucket b = bucketMap.get(s);
				ArrayList<Integer> jmList = b.getAllItems();
//				System.out.print("Cluster: ");
//				for (int i=0; i<jmList.size(); i++) {
//					System.out.print(jmList.get(i) + " ");
//				}
//				System.out.println();
			}
		}
	}
	
	public String createMethodSignature(JavaMethod jm) {
		return jm.getFileName()+":"+jm.getStartLine()+":"+jm.getEndLine();
	}
	
	public ArrayList<Bucket> getRefinedCloneClusters() {
		System.out.println("Cleaning clusters... removing single buckets");
		cleanCluster();
		ArrayList<Bucket> cloneClusters = getAllCloneClusters();
		ArrayList<Bucket> results = new ArrayList<Bucket>();
		TreeSet<Integer> removedSet = new TreeSet<Integer>();
		
		for (int n = 0; n < cloneClusters.size(); n++) {
//			System.out.println("=============== NEW ROUND n = " + n + " ================");
			Bucket b = cloneClusters.get(n);
			// skip the removed ones
			if (!removedSet.contains(n)) {
				Set<Integer> jmList = b.getAllItemSet();
//				System.out.print("current bucket = ");
//				System.out.println(printSet(jmList));
				for (int j = 0; j < cloneClusters.size(); j++) {
					Bucket b2 = cloneClusters.get(j);
					if (b!=b2 && !removedSet.contains(j)) {
						Set<Integer> jmList2 = b2.getAllItemSet();
//						System.out.print("comparing bucket = ");
//						System.out.println(printSet(jmList2));
						Set<Integer> temp = new TreeSet<Integer>(jmList);
						// find intersection
						temp.retainAll(jmList2);
						// there is an intersection
						if (temp.size()!=0) {
							removedSet.add(j);
							b.addSet(jmList2);

//							System.out.print("after merge bucket = ");
//							System.out.println(printSet(b.getAllItemSet()));
							
//							System.out.print("removed set = ");
//							System.out.println(printSet(removedSet));
						}
					}
				}
			}
		}
		
//		System.out.print("removed set = ");
//		System.out.println(printSet(removedSet));
		
		for (int n = 0; n < cloneClusters.size(); n++) {
			if (!removedSet.contains(n)) {
				results.add(cloneClusters.get(n));
			}
		}
		
		return results;
	}
	
	public String printSet(Set<Integer> s) {
		String result = "";
		for (Integer i: s) {
			result = result + i + " ";
		}
		
		return result;
	}
	
	public void cleanCluster() {
		Iterator it = bucketMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Bucket> pair = (Map.Entry<String, Bucket>)it.next();
	        // clean single bucket
	        if (pair.getValue().size()==1)
	        		it.remove(); 
	    }
	}
	
	public ArrayList<Bucket> getAllCloneClusters() {
		ArrayList<Bucket> cloneClusters = new ArrayList<Bucket>();
		for (String s : collidedHashList) {
			Bucket b = bucketMap.get(s);
			cloneClusters.add(b);
		}
		
		return cloneClusters;
	}
	
	public int getSize() {
		return bucketMap.size();
	}
	
	public Set<Integer> query(int[] signature) {
		int index = 0;
		Set<Integer> result = new TreeSet<Integer>();
		// System.out.println(bucketMap.size());
		// go through each band
		while (index < signature.length) {
			// has all the rows in the same band
			String concatSigRow = "";
			for (int j = index; j < (index + row); j++)
				concatSigRow += signature[j];
			// System.out.println("concatenated signature = " + concatSigRow);
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-256");
				// Change this to "UTF-16" if needed
				md.update(concatSigRow.getBytes("UTF-8"));
				byte[] digest = md.digest();
				String hashString = String.format("%064x", new java.math.BigInteger(1, digest));
				// System.out.println(hashString);
				Bucket b=bucketMap.get(hashString);
				if (b != null) { 
					 result.addAll(b.getAllItemSet());
				}
				
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			index += row;
		}
		
		return result;
	}
}
