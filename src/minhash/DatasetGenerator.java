package minhash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DatasetGenerator {
	public static void main(String[] args) {
		try {

			File file = new File("dataset-1000000.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			Random randomGenerator = new Random();
			for (int i = 0; i < 1000000; i++) {
				for (int j=0; j<20; j++) {
					bw.write(Character.toString((char)(randomGenerator.nextInt(25)+65)));
				}
				bw.write("\n");
			}
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
