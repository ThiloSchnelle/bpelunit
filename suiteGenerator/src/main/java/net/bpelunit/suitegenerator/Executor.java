package net.bpelunit.suitegenerator;

import java.io.File;

import net.bpelunit.suitegenerator.config.Config;

public class Executor {

	private static boolean createRecommendations = true;
	private static boolean createNewTestCases = true;

	public static void main(String[] args) {
		long start = System.nanoTime();
		File folder = new File("files/test/");
		Generator g = new Generator(new File(folder, Config.get().getClassificationTableName()));
		System.out.println((System.nanoTime() - start) / 1e6);
		start = System.nanoTime();
		g.generate(folder, createRecommendations, createNewTestCases);
		System.out.println((System.nanoTime() - start) / 1e6);
	}

}
