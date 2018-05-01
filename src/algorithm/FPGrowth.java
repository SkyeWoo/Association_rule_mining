package algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import demo.Preprocessing;

public class FPGrowth {
	private float min_sup, min_conf;
	private FPTree fpTree;
	private Map<Set<String>, Integer> frequentItemsets;
	public int freISCount = 0;
	public String fileName;
	
	private Map<String, Float> rules = new HashMap<>();
	
	public FPGrowth(Preprocessing dp, float min_sup, float min_conf) {
		this.min_sup = min_sup;
		this.min_conf = min_conf;
		
		frequentItemsets = new HashMap<>();
		
		fpTree = new FPTree(dp.dataSet, min_sup);
		
		fpGrowth();
		
		fileName = "fpgrowth-groceries-rules-" + "min_sup=" + min_sup + "-min_con=" + min_conf + ".csv";
	}
	
	private void fpGrowth() {
		// ��ʼʱģʽΪ�� ��Ӧα�����е���(fpTree, null)
		List<String> pattern = new ArrayList<>();
		fpGrowth(fpTree, pattern);
		freISCount = frequentItemsets.size();
	}

	/**
	 * �ݹ���ھ�ÿ������FPTree, �ۼӺ�׺Ƶ����ֱ��FPTreeֻ��һ��·����FPTreeΪ��
	 * @param fpTree
	 * @param pattern
	 */
	private void fpGrowth(FPTree fpTree, List<String> pattern) {
		// ֻ��һ��·�������, ����·���ϵ����item����Ƶ����
		// ��·���н���ÿ����Ϧ²���ģʽpattern�Ȧ·���Ƶ����
		// ģʽ��support count = ���н����С֧�ּ���
		if (hasSinglePath(fpTree)) {
			Map<Set<String>, Integer> path = new HashMap<>();
			int count = 0; // ����ǰ׺������, �����������combine
			
			// ׷�ݵ����ڵ�·��
			if (fpTree.root.child != null) {
				FPTreeNode node = fpTree.root.child.get(0);
				while (node != null) {
					count++;
					Set<String> items = new HashSet<>();
					items.add(node.item);
					path.put(items, node.count);
					
					if (node.child == null) break;
					
					node = node.child.get(0);
				}
			}
			
			combinePrepathCombination(path, pattern, count);
		}
		
		else {
			for (int i = fpTree.header.size() - 1; i >= 0; i--) {
				FPTreeNode head = fpTree.header.get(i);
				pattern.add(head.item);
				
				// �ڵ�ǰǰ׺���в���head.item��������
				FPTree newTree = genFPTree(fpTree, head.item);
				
				Set<String> key = new HashSet<>();
				key.addAll(pattern);
				
				// ��Ƶ������supֵ����ͷ��ڵ��supֵ������
				frequentItemsets.put(key, head.count);
				
				if (newTree.root != null)
					fpGrowth(newTree, pattern);
				
				// ��֤ÿ��pattern = �¡�ai, ����aiΪ���������ͷ���нڵ�
				pattern.remove(head.item);
			}
		}
	}
	
	/**
	 * ����pattern����������pattern�� supportֵ��prePath��Ԫ�ص�sup����
	 * @param prePath pattern��ǰ׺������
	 * @param pattern
	 */
	private void combine(Map<Set<String>, Integer> prePath, List<String> pattern) {
		for (Map.Entry<Set<String>, Integer> entry : prePath.entrySet()) {
			Set<String> itemSet = new HashSet<>();
			itemSet.addAll(entry.getKey());
			itemSet.addAll(pattern);
			
			frequentItemsets.put(itemSet, entry.getValue());
		}
	}
	
	/**
	 * �ڵ�·�������������Ԫ����pattern������Ƶ����
	 * @param path ��·��������
	 * @param pattern ����ģʽ
	 * @param count ·���ڵ����, Ҳ��combine�Ĳ㼶��, ����2^count-1�����
	 */
	private void combinePrepathCombination(Map<Set<String>, Integer> path, List<String> pattern, int count) {
		for (int i = 2; i <= count; i++) {
			combine(path, pattern);
			Map<Set<String>, Integer> newPath = new HashMap<>();
			for (Map.Entry<Set<String>, Integer> elem1 : path.entrySet())
				for (Map.Entry<Set<String>, Integer> elem2 : path.entrySet())
					if (elem1.equals(elem2) == false) {
						Set<String> combination = new HashSet<>();
						combination.addAll(elem1.getKey());
						combination.addAll(elem2.getKey());
						
						// �²�������ϵ�supֵ�ɹ�����ϵ�Ԫ��supֵ��С����һ������
						if (combination.size() == i && newPath.containsKey(combination) == false) {
							int sup = (elem1.getValue() > elem2.getValue()) ? elem2.getValue() : elem1.getValue();
							newPath.put(combination, sup);
						}
					}
			
			path = newPath;
		}
		
		combine(path, pattern);
	}

	/**
	 * @param fpTree �ڵ�ai��ǰ׺��
	 * @param item �ڵ�ai��id
	 * @return �ڵ�ai��������
	 */
	public FPTree genFPTree(FPTree fpTree, String item) {
		List<List<String>> newItemsets = new ArrayList<>();
		for (FPTreeNode node : fpTree.header) {
			// ��ͷ������ʼѰ�����нڵ�ai, ����¼�����ڵ�ai��ǰ׺·��
			if (node.item.equals(item)) {
				FPTreeNode list = node.next;
				while (list != null) {
					List<String> record = new ArrayList<>();
					getPath(list.parent, record);
					
					// �ж���supֵ�ͼӶ�����ͬ���ļ�¼
					for (int i = 0; i < list.count; i++)
						newItemsets.add(record);
					
					list = list.next;
				}
				
				break;
			}
		}
		
		// �����µ����ݼ�, ���㹹���µ�FPTree
		Map<String, List<Integer>> dataSet = new TreeMap<>();
		int count = 1;
		for (List<String> items : newItemsets) {
			for (int i = 0; i < items.size(); i++)
				if (dataSet.containsKey(items.get(i)))
					dataSet.get(items.get(i)).add(count);
				else {
					ArrayList<Integer> taskList = new ArrayList<Integer>();
					taskList.add(count);
					dataSet.put(items.get(i), taskList);
				}
			count++;
		}
		
		// �����µ�item��������
		return new FPTree(dataSet, min_sup);
	}
	
	/**
	 * �ж�FPTree���Ƿ�ֻ��һ��·��
	 */
	private boolean hasSinglePath(FPTree fpt) {
		FPTreeNode node = fpt.root;
		while (true) {
			if (node.child == null) return true;
			if (node.child.size() == 1)
				node = node.child.get(0);
			else return false;
		}
	}
	
	/**
	 * �ݹ�ػ��ݵ����ڵ�, record��¼·��
	 */
	private void getPath(FPTreeNode node, List<String> record) {
		if (node.item == null) return;
		record.add(node.item);
		getPath(node.parent, record);
	}
	
	/**
	 * ���ɹ���
	 */
	public void genRules() {
		for (Map.Entry<Set<String>, Integer> e1 : frequentItemsets.entrySet())
			for (Map.Entry<Set<String>, Integer> e2 : frequentItemsets.entrySet()) {
				Set<String> items1 = e1.getKey();
				Set<String> items2 = e2.getKey();
				if (items1 != items2 && items1.containsAll(items2)) {
					float conf = (float) e1.getValue() / (float) e2.getValue();
					if (conf >= min_conf) 
						genRule(items1, items2, conf);
				}
			}
//		if (rules.isEmpty() == false)
//			writeIntoFile();
	}
	
	private void genRule(Set<String> l, Set<String> t, float conf) {
		// t -> (l-t)
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