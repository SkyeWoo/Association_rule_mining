package demo;

import java.io.*;
import java.util.*;

public class Preprocessing {
	public Map<String, List<Integer>> dataSet = new HashMap<String, List<Integer>>();
	public int tCount = 1; // 记事务编号
	
	public Preprocessing(String path) {
		File in = new File(path);
		FileReader fr;
		try {
			fr = new FileReader(in);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			line = br.readLine();
			
			while (line != null) {
				buildDataSet(line, tCount);
				line = br.readLine(); tCount++;
			}
			
			br.close(); fr.close();
			
			System.out.println("Groceries.csv loaded!");
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + in.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Preprocessing(String path, String flag) {
		File in = new File(path);
		FileReader fr;
		try {
			fr = new FileReader(in);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			List<String> record = new ArrayList<String>();
			tCount = 0;
			String last = null;
			
			while (line != null) {
				if (line.equals("**SOF**")) {
					tCount++; record.clear(); last = null;
				}
				else if (line.equals("**EOF**")) {
					if (last != null && record.contains(last) == false)
						record.add(last);
					
					for (String str : record) {
						if (dataSet.containsKey(str)) 
							dataSet.get(str).add(tCount);
						else {
							List<Integer> list = new ArrayList<>();
							list.add(tCount);
							dataSet.put(str, list);
						}
					}
				}
				else if (line.matches("^[a-z]*")) {
					if (last != null && record.contains(last) == false)
						record.add(last);
					
					last = line;
				}
				
				line = br.readLine();
			}
			
			br.close(); fr.close();
			
//			System.out.println("UNIX_usage data loaded!");
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + in.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void buildDataSet(String line, int no) {
		String[] items = line.split(",");
		items[1] = items[1].substring(2); // 去掉开头的"{
		items[items.length - 1] = items[items.length - 1].substring(0, items[items.length - 1].length() - 2);
										// 去掉结尾的}"
		
		for (int i = 1; i < items.length; i++)
			if (dataSet.containsKey(items[i]))
				dataSet.get(items[i]).add(no);
			else {
				ArrayList<Integer> noList = new ArrayList<Integer>();
				noList.add(no);
				dataSet.put(items[i], noList);
			}
	}
}
