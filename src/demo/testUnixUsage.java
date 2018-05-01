package demo;

import java.util.*;

import algorithm.*;

public class testUnixUsage {
	public static Map<String, List<Integer>> dataSet = new HashMap<>();
	public static void main(String[] args) {
		
		for (int i = 0; i < 9; i++) {
			String path = "dataset//UNIX_usage//USER" + i + "//sanitized_all.981115184025";
			Preprocessing subdp = new Preprocessing(path, "UNIX");
			System.out.println("UNIX_usage data" + i + " loaded!");
			float min_sup = 50f, min_conf = 0.5f;
			Apriori subap = new Apriori(subdp.dataSet, min_sup, min_conf);
			subap.fileName = "user" + i + "apriori-min_sup=" + min_sup + "-min_conf=" + min_conf + ".csv";
			subap.genRules();
			
			genDataSet(subap, i);
		}
		
		System.out.println("***************************");

		float min_sup = 2f, min_conf = 0.1f;
		Apriori ap = new Apriori(dataSet, min_sup, min_conf);
		ap.fileName = "apriori-UNIX-rules-" + "min_sup=" + min_sup + "-min_con=" + min_conf + ".csv";
		ap.genRules();
	}
	
	private static void genDataSet(Apriori subap, int no) {
		List<String> itemSetSet = new ArrayList<>();
		
		for (int h = subap.frequentItemsets.size() - 1; h >= 0; h--) {
			Map<Set<String>, Integer> high = subap.frequentItemsets.get(h);
			for (Map.Entry<Set<String>, Integer> eh : high.entrySet()) {
				Set<String> sh = eh.getKey();
				if (eh.getValue() != -1) {
					for (int l = h - 1; l >= 0; l--) {
						Map<Set<String>, Integer> low = subap.frequentItemsets.get(l);
						for (Map.Entry<Set<String>, Integer> el : low.entrySet()) {
							Set<String> sl = el.getKey();
							if (el.getValue() != -1 && sh.containsAll(sl))
								low.put(sl, -1);
						}
					}
					StringBuilder sb = new StringBuilder();
					for (String str : sh) {
						sb.append(str); sb.append(",");
					}
					sb.deleteCharAt(sb.length() - 1);
					
					itemSetSet.add(sb.toString());
				}
			}
		}
		
		for (String itemSet : itemSetSet) {
			if (dataSet.containsKey(itemSet))
				dataSet.get(itemSet).add(no);
			else {
				List<Integer> tmp = new ArrayList<>();
				tmp.add(no);
				dataSet.put(itemSet, tmp);
			}
		}
	}
}
