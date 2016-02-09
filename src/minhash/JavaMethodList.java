package minhash;

import java.util.ArrayList;

public class JavaMethodList {
	private String signature;
	private ArrayList<JavaMethod> jmList;
	
	public JavaMethodList() {
		signature = "";
		jmList = new ArrayList<JavaMethod>();
	}
	
	public void addJavaMethod(JavaMethod jm) {
		jmList.add(jm);
		signature = signature + jm.getFileName() + ":" + jm.getStartLine() + ":" + jm.getEndLine();
	}
	
	public int size() {
		return jmList.size();
	}
	
	public ArrayList<JavaMethod> getAllMethods() {
		return jmList;
	}
	
	public boolean contains(JavaMethod jm) {
		if (signature.contains(jm.getFileName()+":"+jm.getStartLine()+":"+jm.getEndLine()))
			return true;
		else 
			return false;
	}
}
