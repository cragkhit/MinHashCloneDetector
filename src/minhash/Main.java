package minhash;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import de.jungblut.nlp.MinHash;

// import info.debatty.java.lsh.MinHash;

public class Main {
	private static nGramGenerator ngen;
	private static nGramsMatrix matrix;
	private static final int n = 3;
	// private static final int DIC_SIZE = (int) Math.pow(26, n);
	private static final int NORMAL_MODE = 0;
	private static final int PARTITION_MODE = 1;
	private static final int SLIDING_MODE = 2;
	private static final int MINHASH_SIG_SIZE = 20;
	private static final int PARTITION_SIZE = 10;
	private static final int PARTITION_SIG_SIZE = 4;
	private static final String outputfile = "clones.xml";
	private static Dictionary dictionary = new Dictionary();
	private static MinHash minhash;
	private static LSH lsh = new LSH(PARTITION_SIG_SIZE);
	private static ArrayList<JavaMethod> methodList;
	public static HashMap<Integer, JavaMethod> methodMap;
	private static int MINLINE = 6;
	
	public static void main(String[] args) {
		ngen = new nGramGenerator(n);
		
		ArrayList<String> tokens = new ArrayList<String>();
		TreeSet<String> allTokens = new TreeSet<String>();
		File folder = new File("/Users/Chaiyong/Downloads/BellonBenchmark/eclipse-jdtcore/src");
		// ArrayList<String> fileList = listFilesForFolder(folder);
		LinkedList<File> fileList = (LinkedList<File>) listFilesForFolderRecursive(folder);
		ArrayList<JavaMethod> ngramsFileList = new  ArrayList<JavaMethod>();
		JavaTokenizer tokenizer = new JavaTokenizer();
		methodMap = new HashMap<Integer, JavaMethod>();

		// default is partition mode
		int mode = NORMAL_MODE;
		// the mode has been set
		if (args.length > 0) {
			if (args[0].equals("s")) {
				mode = SLIDING_MODE;
				System.out.println("Mode: Sliding");
			} else {
				System.out.println("Mode: Partitioning");
			}
		}
		else {
			System.out.println("Mode: Normal MinHashing");
		}
		System.out.println("Generating n-gram dictionary ...");
		methodList = new ArrayList<JavaMethod>();
		
		int idCount = 0;
		int nGramFileListCount = 0;
		for (int i=0; i<fileList.size(); i++) {
			System.out.println(i + ": " + fileList.get(i).getAbsolutePath());
			try {
				/* Parse and extract method body */
				FileInputStream in = new FileInputStream(fileList.get(i).getAbsolutePath());
				CompilationUnit cu;
				try {
					// parse the file
					cu = JavaParser.parse(in);
					List<TypeDeclaration> typeDeclarations = cu.getTypes();
		            for (TypeDeclaration typeDec : typeDeclarations) {
		                List<BodyDeclaration> members = typeDec.getMembers();
		                if(members != null) {
							for (BodyDeclaration member : members) {
								// extract the constructors
								if (member instanceof ConstructorDeclaration) {
									ConstructorDeclaration constructor = (ConstructorDeclaration) member;
									if ((constructor.getEndLine() - constructor.getBeginLine() + 1) >= MINLINE) {
										JavaMethod jm = new JavaMethod(idCount, fileList.get(i).getAbsolutePath(), constructor.getName(),
												constructor.getDeclarationAsString() + constructor.getBlock(),
												constructor.getBeginLine(), constructor.getEndLine(), "");
										methodList.add(jm);
										// add the id <---> JavaMethod to the
										// map
										methodMap.put(idCount, jm);
										idCount++;
									}
									// extract all the methods
								} else if (member instanceof MethodDeclaration) {
									MethodDeclaration method = (MethodDeclaration) member;
									if ((method.getEndLine() - method.getBeginLine() + 1) >= MINLINE) {
										JavaMethod jm = new JavaMethod(idCount, fileList.get(i).getAbsolutePath(), method.getName(),
												method.getDeclarationAsString() + method.getBody().toString(),
												method.getBeginLine(), method.getEndLine(), "");
										methodList.add(jm);
										// add the id <---> JavaMethod to the map
										methodMap.put(idCount, jm);
										idCount++;
									}
								}
							}
		                }
		            }
				} finally {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (JavaMethod s : methodList) {
			try {
				tokens = tokenizer.getTokensFromString(s.getMethod());
				//System.out.println("Method = \n" + s.getMethod());
				/** TODO: remove the method's body since we're done with it here */
				// this is to save memory
				s.setMethod("");
				
				allTokens.addAll(tokens);
				// generating n-grams from the given normalized java tokens
				ArrayList<String> ngrams = ngen.generateNGramsFromJavaTokens(tokens);
				s.setNgrams(ngrams);
				// add all extracted java tokens to the set to create dictionary
				dictionary.addAll(ngrams);
				// add a list of n-grams extracted from the code to the final array
				ngramsFileList.add(s);
				nGramFileListCount++;
				// System.out.println(s.getFileName() + "," + s.getMethodName() + "," 
				// + s.getStartLine() + ", " + s.getEndLine());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println("total count = " + idCount);
		System.out.println("total methods = " + methodList.size());
		// System.out.println("total ngram list = " + ngramsFileList.size());
		
		// System.out.println("\nToken size = " + allTokens.size());
		// System.out.println("Dic size = " + dictionary.size());
		
		// initialize minhash after knowing the dic size
		/** OLD MINHASH **/
		// minhash = new MinHash(PARTITION_SIG_SIZE, dictionary.size());
		
		/** NEW MINHASH **/
		minhash = MinHash.create(MINHASH_SIG_SIZE, 0);
		
		// iterate through all the doc again to create minhash signature
		for (int i=0; i<ngramsFileList.size(); i++) {
			JavaMethod jm = ngramsFileList.get(i);
			ArrayList<String> docNgrams = jm.getNgrams(); 
			//System.out.println("Size = " + docNgrams.size());
			int[] sig;
			if (mode==PARTITION_MODE)
				sig = generatePartitionMinHash(docNgrams);
			else if (mode==SLIDING_MODE)
				sig = generateSlidingMinHash(docNgrams);
			else // NORMAL MODE
				sig = generateMinHash(docNgrams);
			
			//System.out.println(Arrays.toString(sig));
			lsh.hash(sig, jm);
			
			// System.out.print(".");
		}
		
		System.out.println("\nHashing with r = " + PARTITION_SIG_SIZE);
		lsh.printBuckets(false);
		
		// lsh.printBuckets(false);
		System.out.println("\nCreating clone clusters ... ");
		try {
			writeResults(outputfile, lsh.getRefinedCloneClusters());
			System.out.println("\nDone writing to output file: " + outputfile);
		} catch (IOException e) {
			System.out.println("ERROR: cannot write the output file.");
			e.printStackTrace();
		}
		
	}
	
	public static void writeResults(String outputfile, ArrayList<Bucket> jmList) throws IOException {
		File file = new File(outputfile);

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		int idCount = 1;
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		// Instantiate a Date object
		Date date = new Date();

		bw.write("<CloneClasses timestamp=\"" + date.toString() +  "\">\n");
		for (Bucket j : jmList) {
			bw.write("<CloneClass>\n");
			bw.write("<ID>" + idCount + "</ID>\n");
			for (int i = 0; i < j.size(); i++) {
				JavaMethod jm = methodMap.get(j.get(i));
				bw.write("<Clone>\n"); 
				bw.write("<Fragment>\n");
				bw.write("<File>"+jm.getFileName()+"</File>\n");
				bw.write("<Start>"+ jm.getStartLine() + "</Start>\n");
				bw.write("<End>" + jm.getEndLine() + "</End>\n");
				bw.write("</Fragment>\n");
				bw.write("</Clone>\n");
			}				
			bw.write("</CloneClass>\n");
			idCount++;
		}
		bw.write("</CloneClasses>\n");
		bw.close();

	}
	
	public static void generateNGramsFromFile(String filepath) {
		File file = new File(filepath);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int count = 0;
			while ((line = br.readLine()) != null) {
				String[] ngrams = ngen.generateNGrams(line);
				for (int i=0; i<ngrams.length; i++) {
					matrix.insert(ngrams[i], count);
				}
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static int[] generateMinHash(ArrayList<String> ngrams) {
		int[] minHashSig = new int[MINHASH_SIG_SIZE];
		int index = 0;
		NGramArray narray = new NGramArray(dictionary, n);
		while (index < ngrams.size()) {
			narray.insert(ngrams.get(index));
			narray.print();
			index++;
		}
		/** TODO: OLD MINHASH **/
		// minHashSig = minhash.signature(narray.getValue());
		/** TODO: NEW MINHASH **/
		minHashSig = minhash.minHashVector(narray.getVector());
		return minHashSig;
	}
	
	public static int[] generatePartitionMinHash(ArrayList<String> ngrams) {
		// size of minhash signature is equal to <no. of partition> * <no. of signature per partition>.
		int[] minHashSig = new int[((int) Math.ceil(ngrams.size()/(double)PARTITION_SIZE))*PARTITION_SIG_SIZE];
		int index = 0;
		int pCount=0;
		while (index < ngrams.size()) {
			NGramArray narray = new NGramArray(dictionary, n);
			// get x n-grams at a time (one partition)
			for (int i = index; (i < index + PARTITION_SIZE && i < ngrams.size()); i++) {
				narray.insert(ngrams.get(i));
			}
			narray.print();
			/** TODO: OLD MINHASH **/
			// int[] sig = minhash.signature(narray.getValue());
			/** TODO: NEW MINHASH **/
			int[] sig = minhash.minHashVector(narray.getVector());
	        // for (int x: sig) { System.out.print(x + " "); } System.out.println();
			System.arraycopy(sig, 0, minHashSig, (index / PARTITION_SIZE) * PARTITION_SIG_SIZE, PARTITION_SIG_SIZE);
			index += PARTITION_SIZE;
			pCount++;
		}
		// System.out.print(pCount + ",");
		return minHashSig;
	}
	
	public static int[] generateSlidingMinHash(ArrayList<String> ngrams) {
		// total number of sliding windows = |ngrams| - window_size + 1
		int[] minHashSig = new int[(ngrams.size() - PARTITION_SIZE + 1) * PARTITION_SIG_SIZE];
		int index = 0;
		while (index < ngrams.size() - PARTITION_SIZE + 1) {
			NGramArray narray = new NGramArray(dictionary, n);
			// get 4 n-grams at a time (one partition)
			for (int i = index; (i < index + PARTITION_SIZE && i < ngrams.size()); i++) {
				narray.insert(ngrams.get(i));
			}
			// narray.print();
			// System.out.println();
			int[] sig = minhash.minHashVector(narray.getVector());
			// System.out.println(sig[0]);
			System.arraycopy(sig, 0, minHashSig, index * PARTITION_SIG_SIZE, PARTITION_SIG_SIZE);
			// move the window one step ahead
			index += 1;
		}
		return minHashSig;
	}
	
	// read number of lines
	// copied from http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	// copied from http://stackoverflow.com/questions/1844688/read-all-files-in-a-folder
	public static ArrayList<String> listFilesForFolder(File folder) {
		ArrayList<String> fileList = new ArrayList<String>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	fileList.add(fileEntry.getAbsolutePath());
	        }
	    }
	    return fileList;
	}
	
	public static Collection<File> listFilesForFolderRecursive(File folder) {
		// get only java files
		Collection files = FileUtils.listFiles(
				  folder, 
				  new RegexFileFilter(".*.java"), 
				  DirectoryFileFilter.DIRECTORY
				);
		System.out.println("Found: " + files.size() + " files.");
		return files;
	}
}
