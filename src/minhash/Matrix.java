package minhash;


public class Matrix {
	// size of the matrix = n x m
	protected int n;
	protected int m;
	protected int[][] matrix;

	public Matrix(int width, int height) {
		this.n = width;
		this.m = height;
		this.initializeMatrix();
	}
	
	private void initializeMatrix() {
		this.matrix = new int[n][m];
	}
}
