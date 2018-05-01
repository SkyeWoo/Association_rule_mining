package algorithm;

import java.util.*;

public class Baseline extends Apriori {
	public Baseline(Map<String, List<Integer>> dataSet, float min_sup, float min_conf) {
		super(dataSet, min_sup, min_conf);
		
		fileName = "baseline-groceries-rules-" + "min_sup=" + min_sup + "-min_con=" + min_conf + ".csv";
	}
	
	@Override
	public Set<Set<String>> genCandidates(int k) {
		Set<Set<String>> Ck1 = new HashSet<>();
		Set<Set<String>> Ck = frequentItemsets.get(k - 1).keySet();
		
		for (Set<String> itemSet1 : Ck)
			for (Set<String> itemSet2 : Ck) {
				Set<String> itemSet = new HashSet<>();
				itemSet.addAll(itemSet1);
				itemSet.addAll(itemSet2);
				
				if (itemSet.size() == k + 1 && Ck1.contains(itemSet) == false)
					Ck1.add(itemSet);
			}
		
		
		return Ck1;
	}
}
