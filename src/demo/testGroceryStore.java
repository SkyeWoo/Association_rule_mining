package demo;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import algorithm.*;

public class testGroceryStore {
	public static void main(String[] args) {
		Preprocessing dp = new Preprocessing("dataset//GroceryStore//Groceries.csv");
		// TODO! min_sup和min_conf改为输入值
		float min_sup = 0.01f * dp.tCount;
		float min_conf = 0.5f;
		
		MemoryMXBean mmb = ManagementFactory.getMemoryMXBean();
		
		System.out.println("**********Apriori**********");
		long startTime = System.currentTimeMillis();
		long startMem = mmb.getHeapMemoryUsage().getUsed();
		Apriori ap = new Apriori(dp.dataSet, min_sup, min_conf);
		long endTime = System.currentTimeMillis();
		long endMem = mmb.getHeapMemoryUsage().getUsed();
		System.out.printf("Generated frequent itemsets: %d\n", ap.freISCount);
		System.out.printf("Computational cost(s): %f\n", (endTime - startTime) / 1000f);
		System.out.printf("Storage used(MB): %f\n", (endMem - startMem) / 1024f / 1024f);
		ap.genRules();
		
		System.out.println("**********FPGrowth*********");
		mmb = ManagementFactory.getMemoryMXBean();
		startTime = System.currentTimeMillis();
		startMem = mmb.getHeapMemoryUsage().getUsed();
		FPGrowth fp = new FPGrowth(dp, min_sup, min_conf);
		endTime = System.currentTimeMillis();
		endMem = mmb.getHeapMemoryUsage().getUsed();
		System.out.printf("Generated frequent itemsets: %d\n", fp.freISCount);
		System.out.printf("Computational cost(s): %f\n", (endTime - startTime) / 1000f);
		System.out.printf("Storage used(MB): %f\n", (endMem - startMem) / 1024f / 1024f);
		fp.genRules();
		
		System.out.println("**********Baseline*********");
		mmb = ManagementFactory.getMemoryMXBean();
		startTime = System.currentTimeMillis();
		startMem = mmb.getHeapMemoryUsage().getUsed();
		Baseline bl = new Baseline(dp.dataSet, min_sup, min_conf);
		endTime = System.currentTimeMillis();
		endMem = mmb.getHeapMemoryUsage().getUsed();
		System.out.printf("Generated frequent itemsets: %d\n", bl.freISCount);
		System.out.printf("Computational cost(s): %f\n", (endTime - startTime) / 1000f);
		System.out.printf("Storage used(MB): %f\n", (endMem - startMem) / 1024f / 1024f);
		bl.genRules();
	}
}
