package algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import demo.Preprocessing;

public class Apriori {
	public List<Map<Set<String>, Integer>> frequentItemsets;
	private Map<String, List<Integer>> dataSet;
	private float min_sup, min_conf;
	public int freISCount = 0;
	public String fileName;
	
	public Map<String, Float> rules = new HashMap<>();
	
	public Apriori(Map<String, List<Integer>> dataSet, float min_sup, float min_conf) {
		frequentItemsets = new ArrayList<Map<Set<String>, Integer>>();
		this.dataSet = dataSet;
		this.min_sup = min_sup;
		this.min_conf = min_conf;
		
		genFrequentItemsets();
		
		fileName = "apriori-groceries-rules-" + "min_sup=" + min_sup + "-min_con=" + min_conf + ".csv";
	}
	
	/**
	 * 生成第一个频集L1
	 */
	private void genFirst() {
		Map<Set<String>, Integer> L1 = new HashMap<>();
		
		for (Map.Entry<String, List<Integer>> entry : dataSet.entrySet()) {
			String item = entry.getKey();
			int sup = entry.getValue().size();
			
			if (sup >= min_sup) {
				Set<String> itemSet = new HashSet<>(); // 类型受限, 必须转换为集合
				itemSet.add(item);
				L1.put(itemSet, sup); // 例如：{I1}, 4
			}
		}
		
		freISCount += L1.size();
		
//		for (Map.Entry<Set<String>, Integer> entry : L1.entrySet()) {
//			System.out.println(entry.getKey().toString() + ":" + entry.getValue());
//		}
		frequentItemsets.add(L1);
	}
	
	/**
	 * 由Lk生成Lk+1
	 */
	private void genFrequentItemsets() {
		genFirst();
		
		for (int k = 1; ; k++) {
			Map<Set<String>, Integer> Lk1 = new HashMap<>();
			
			// 生成候选集
			Set<Set<String>> Ck1 = genCandidates(k);
			
			// 对候选集中的项集判断, 如果项集大于min_sup则连同sup值放入Lk+1中
			for (Set<String> itemSet : Ck1) {
				Set<Integer> supList = new HashSet<>();
				for (String item : itemSet) {
					if (supList.isEmpty())
						supList.addAll(dataSet.get(item));
					else
						supList.retainAll(dataSet.get(item));
				}
				
				if (supList.size() >= min_sup)
					Lk1.put(itemSet, supList.size());
			}
			
			if (Lk1.isEmpty())
				break;
			
			freISCount += Lk1.size();
			frequentItemsets.add(Lk1);
		}
	}
	
	/**
	 * 由Lk生成候选集Ck+1
	 */
	public Set<Set<String>> genCandidates(int k) {
		Set<Set<String>> Ck1 = new HashSet<>();
		Set<Set<String>> Ck = frequentItemsets.get(k - 1).keySet();
		
		for (Set<String> itemSet1 : Ck)
			for (Set<String> itemSet2 : Ck) {
				Set<String> itemSet = new HashSet<>();
				itemSet.addAll(itemSet1);
				itemSet.addAll(itemSet2);
				
				if (itemSet.size() == k + 1 && Ck1.contains(itemSet) == false) {
					boolean flag = true;
					for (String item : itemSet) {
						
						// 对{I1, I2, I4} 检查 {I1, I2} {I1, I4} {I2, I4} 是否都在L2中
						// 如果有一个不存在 那么{I1, I2, I4}不在C3中
						Set<String> tmp = new HashSet<>();
						tmp.addAll(itemSet);
						tmp.remove(item);
						
						if (Ck.contains(tmp) == false) {
							flag = false; break;
						}
					}
					
					if (flag == true)
						Ck1.add(itemSet);
				}
			}
		
		
		return Ck1;
	}
	
	/**
	 * 生成规则
	 */
	public void genRules() {
		int itemsetsSize = frequentItemsets.size();
		for (int l = 1; l < itemsetsSize; l++) {
			Map<Set<String>, Integer> lItemSetSet = frequentItemsets.get(l);
			for (Map.Entry<Set<String>, Integer> lItemSet : lItemSetSet.entrySet()) {
				Set<String> lkey = lItemSet.getKey();
				float lvalue = (float)lItemSet.getValue();
				
				for (int t = 0; t < l; t++) {
					Map<Set<String>, Integer> tItemSetSet = frequentItemsets.get(t);
					for (Map.Entry<Set<String>, Integer> tItemSet : tItemSetSet.entrySet()) {
						Set<String> tkey = tItemSet.getKey();
						float tvalue = (float)tItemSet.getValue();
						
						float conf = lvalue / tvalue;
						if (lkey.containsAll(tkey) && conf >= min_conf)
							genRule(tkey, lkey, conf);
					}
				}
			}
		}
		
//		if (rules.isEmpty() == false)
//			writeIntoFile();
//		for (String rule : rules) {
//			System.out.println(rule);
//		}
	}
	
	private void genRule(Set<String> t, Set<String> l, float conf) {
		// t -> (l - t)
		
		Set<String> tmp = new HashSet<>();
		tmp.addAll(l);
		tmp.removeAll(t);
		
		StringBuilder sb = new StringBuilder();
		sb.append("\"{");
		for (String str : t) {
			sb.append(str); sb.append(", ");
		}
		sb.setLength(sb.length() - 2);
		sb.append("} -> {");
		for (String str : tmp) {
			sb.append(str); sb.append(", ");
		}
		sb.setLength(sb.length() - 2);
		sb.append("}\"");
		
		rules.put(sb.toString(), conf);
	}
	
	private void writeIntoFile() {
		File output = new File(fileName);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(output));
			for (Map.Entry<String, Float> rule : rules.entrySet()) {
//				System.out.println(rule.getKey());
				bw.write(rule.getKey()); bw.write(","); bw.write(rule.getValue().toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
