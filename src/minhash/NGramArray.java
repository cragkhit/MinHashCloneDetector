package minhash;

import java.util.HashMap;
import java.util.Set;

import de.jungblut.math.sparse.SparseBitVector;
import de.jungblut.math.sparse.SparseDoubleVector;

public class NGramArray {
	private boolean[] value;
	private SparseBitVector vector;
	private SparseDoubleVector dvector;
	private Dictionary dictionary;
	private HashMap<String, Integer> termIndexMap = new HashMap<String, Integer>();
	
	public NGramArray(Dictionary dictionary, int n) {
		this.dictionary = dictionary;
		value = new boolean[this.dictionary.size()];
		vector = new SparseBitVector(this.dictionary.size());
		dvector = new SparseDoubleVector(this.dictionary.size());
		this.generateTermIndexMap();
	}
	
	private void generateTermIndexMap() {
		Set<String> dicSet = dictionary.getSet();
		int count = 0;
		for (String s: dicSet) {
			this.termIndexMap.put(s, count);
			count++;
		}
	}

	private int getTermIndex(String ngram) {
		try {
			return this.termIndexMap.get(ngram);
		} catch (Exception e) {
			System.out.println(ngram + ": " + this.termIndexMap.get(ngram));
			return -1;
		}
	}

	public void insert(String nGram) {
		this.value[getTermIndex(nGram)] = true;
		// using bit vector with occurence (0,1)
		this.vector.set(getTermIndex(nGram), 1);
		// using double vector with n-gram frequency
		double d = this.dvector.get(getTermIndex(nGram));
		this.dvector.set(getTermIndex(nGram), ++d);
	}

	/* TODO: fix */
	public void print() {
		for (int i = 0; i < value.length; i++) {
	
		}
	}
	
	/* TODO: fix */
	public void printToFile(String filepath) {
		/*
		PrintWriter writer;
		try {
			writer = new PrintWriter(filepath, "UTF-8");
			for (int i = 0; i < dicsize; i++) {

				String ngram = "";
				int termIndex = -1;
				// start the left over equals to the number
				int leftOver = i; 
				// go through all the terms in n-grams
				for (int j = this.n - 1; j >= 0; j--) { 
					int pow = (int) Math.pow(26, j);
					// index of term is the result from div
					termIndex = (leftOver / pow); 
					// the left over is the result from mod
					leftOver = i % pow; 
					// concatenate the terms togeth
					ngram = ngram + this.possibleTerms[termIndex];
				}
				
				writer.print(ngram + " ");
			
				if (value[i])
					writer.print("1\n");
				else
					writer.print("0\n");
			}
			writer.println();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public boolean[] getValue() {
		return value;
	}
	
	public SparseBitVector getVector() {
		return vector;
	}
	
	public SparseDoubleVector getDoubleVector() {
		return dvector;
	}
}
