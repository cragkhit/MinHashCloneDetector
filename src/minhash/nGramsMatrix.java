package minhash;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class nGramsMatrix {
	private int n;
	private int width;
	private int height;
	protected int[][] matrix;
	private String[] possibleTerms;
	private HashMap<String, Integer> termIndexMap = new HashMap<String, Integer>();
	private int noOfRows;
	private int noOfCols;

	public nGramsMatrix(int n, String[] possibleTerms, int noOfDocs) {
		this.n = n;
		this.possibleTerms = possibleTerms;
		noOfCols = (int) Math.pow(possibleTerms.length, this.n);
		noOfRows = noOfDocs;
		initializeMatrix();
	}

	/***
	 * Create a matrix of terms and documents
	 * @param noOfCols number of total documents in the database (= no. of columns)
	 */
	private void initializeMatrix() {
		this.width = this.noOfCols;
		this.height = this.noOfRows;
		this.matrix = new int[this.height][this.width];
		this.generateTermIndexMap();
	}

	private void generateTermIndexMap() {
		for (int i = 0; i < this.noOfCols; i++) { // number of all possible n-gram combinations
			String ngram = "";
			int termIndex = -1;
			int leftOver = i; // start the left over equals to the number
			for (int j=this.n-1; j>=0; j--) { // go through all the terms in n-grams
				int pow = (int) Math.pow(26, j);
				termIndex = (leftOver / pow); // index of term is the result from div
				leftOver = i % pow; // the left over is the result from mod
				ngram = ngram + this.possibleTerms[termIndex]; // concatenate the terms together
			}
			// System.out.println(i + ": " + ngram);
			// ngram = this.possibleTerms[] + this.possibleTerms[y] + this.possibleTerms[z];
			// System.out.println(ngram + " " + i);
			this.termIndexMap.put(ngram, i);
		}
	}

	private int getTermIndex(String ngram) {
		return this.termIndexMap.get(ngram);
	}

	public void insert(String nGram, int docid) {
		this.matrix[docid][getTermIndex(nGram)] = 1;
	}
	
	public void printMatrix(String filepath) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filepath, "UTF-8");
			// print header of all n-grams
			for (int i = 0; i < this.width; i++) {

				String ngram = "";
				int termIndex = -1;
				int leftOver = i; // start the left over equals to the number
				for (int j = this.n - 1; j >= 0; j--) { // go through all the terms in n-grams
					int pow = (int) Math.pow(26, j);
					termIndex = (leftOver / pow); // index of term is the result from div
					leftOver = i % pow; // the left over is the result from mod
					ngram = ngram + this.possibleTerms[termIndex]; // concatenate the terms together
				}
				writer.print(ngram + ",");
			}
			writer.println();
			
			for (int i=0; i<this.height; i++) {
				for (int j=0; j<this.width; j++) {
					writer.print(matrix[i][j] + ",");
				}
				if (i != this.height-1) writer.println(); // print new line except the last line
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public int[] getDocumentAtIndex(int index) {
		return matrix[index];
	}
	
	public String printDocAtIndex(int index) {
		int[] doc = this.getDocumentAtIndex(index);
		int count = 0;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<doc.length; i++) {
			if (doc[i]!=0) 
				sb.append("1@" + i + " ");
		}
		
		return sb.toString();
	}
	
	public boolean[] getBooleanVectorAtIndex(int index) {
		int[] doc = this.getDocumentAtIndex(index);
		boolean[] boolDoc = new boolean[this.width];
		for (int i=0; i<this.width; i++) {
			if (doc[i]==0)
				boolDoc[i]=false;
			else
				boolDoc[i]=true;
		}
		return boolDoc;
	}

	public int getN() { return n; }
	public void setN(int n) { this.n = n; }
	public int getWidth() { return width; }
	public void setWidth(int width) { this.width = width; }
	public int getHeight() { return height; }
	public void setHeight(int height) { this.height = height; }
}
