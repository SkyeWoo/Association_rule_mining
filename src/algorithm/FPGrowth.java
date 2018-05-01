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
		// 初始时模式为空 对应伪代码中调用(fpTree, null)
		List<String> pattern = new ArrayList<>();
		fpGrowth(fpTree, pattern);
		freISCount = frequentItemsets.size();
	}

	/**
	 * 递归地挖掘每个条件FPTree, 累加后缀频繁集直到FPTree只有一条路径或FPTree为空
	 * @param fpTree
	 * @param pattern
	 */
	private void fpGrowth(FPTree fpTree, List<String> pattern) {
		// 只有一条路径情况下, 所有路径上的组合item都是频繁集
		// 对路径中结点的每个组合β产生模式pattern∪β放入频繁集
		// 模式的support count = β中结点最小支持计数
		if (hasSinglePath(fpTree)) {
			Map<Set<String>, Integer> path = new HashMap<>();
			int count = 0; // 计算前缀结点个数, 便利后续组合combine
			
			// 追溯到根节点路径
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
				
				// 在当前前缀树中产生head.item的条件树
				FPTree newTree = genFPTree(fpTree, head.item);
				
				Set<String> key = new HashSet<>();
				key.addAll(pattern);
				
				// 新频繁集的sup值是由头表节点的sup值决定的
				frequentItemsets.put(key, head.count);
				
				if (newTree.root != null)
					fpGrowth(newTree, pattern);
				
				// 保证每次pattern = β∪ai, 其中ai为逆序遍历的头表中节点
				pattern.remove(head.item);
			}
		}
	}
	
	/**
	 * 连接pattern的条件树与pattern， support值由prePath中元素的sup决定
	 * @param prePath pattern的前缀条件树
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
	 * 在单路径条件树中组合元素与pattern构成新频繁集
	 * @param path 单路径条件树
	 * @param pattern 现有模式
	 * @param count 路径节点个数, 也是combine的层级数, 共有2^count-1个组合
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
						
						// 新产生的组合的sup值由构成组合的元素sup值较小的那一个决定
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
	 * @param fpTree 节点ai的前缀树
	 * @param item 节点ai的id
	 * @return 节点ai的条件树
	 */
	public FPTree genFPTree(FPTree fpTree, String item) {
		List<List<String>> newItemsets = new ArrayList<>();
		for (FPTreeNode node : fpTree.header) {
			// 由头表链开始寻找树中节点ai, 并记录不含节点ai的前缀路径
			if (node.item.equals(item)) {
				FPTreeNode list = node.next;
				while (list != null) {
					List<String> record = new ArrayList<>();
					getPath(list.parent, record);
					
					// 有多少sup值就加多少条同样的记录
					for (int i = 0; i < list.count; i++)
						newItemsets.add(record);
					
					list = list.next;
				}
				
				break;
			}
		}
		
		// 生成新的数据集, 方便构造新的FPTree
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
		
		// 生成新的item的条件树
		return new FPTree(dataSet, min_sup);
	}
	
	/**
	 * 判断FPTree中是否只有一条路径
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
	 * 递归地回溯到根节点, record记录路径
	 */
	private void getPath(FPTreeNode node, List<String> record) {
		if (node.item == null) return;
		record.add(node.item);
		getPath(node.parent, record);
	}
	
	/**
	 * 生成规则
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