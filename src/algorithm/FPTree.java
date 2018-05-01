package algorithm;

import java.util.*;
import java.util.Map.Entry;

import demo.Preprocessing;

class FPTreeNode {
	public String item;
	public int count;
	public List<FPTreeNode> child;
	public FPTreeNode parent;
	public FPTreeNode next;
	
	public FPTreeNode(String item, int count, FPTreeNode parent) {
		this.item = item;
		this.count = count;
		this.parent = parent;
		next = null;
		child = null;
	}

	public FPTreeNode(String item) {
		this.item = item;
		count = 1;
		parent = null;
		next = null;
		child = null;
	}

	public void addChild(FPTreeNode child) {
		if (this.child == null) this.child = new ArrayList<>();
		this.child.add(child);
	}
	
	public FPTreeNode searchChild(String item) {
		if (this.child != null)
			for (FPTreeNode c : this.child)
				if (c.item.equals(item))
					return c;
			
		return null;
	}
}

public class FPTree {
	public List<FPTreeNode> header;
	public FPTreeNode root;
	private Map<String, List<Integer>> dataSet;
	private Collection<List<String>> itemSet;
	private float min_sup;
	
	public FPTree(Map<String, List<Integer>> dataSet, float min_sup) {
		this.min_sup = min_sup;
		this.dataSet = dataSet;
		
		header = new ArrayList<>();
		root = new FPTreeNode(null);
		
		buildHeader();
		
		buildTaskList();
		
		constructFPTree();
	}
	
	/**
	 * ����FPTreeͷ��
	 */
	private void buildHeader() {
		// �޳�supֵС��min_sup���
		for (Iterator<Map.Entry<String, List<Integer>>> it = dataSet.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, List<Integer>> item = it.next();
			if (item.getValue().size() < min_sup)
				it.remove();
		}

		// ����������������������
		dataSet = sortMapByValue(dataSet);
		
		for (Map.Entry<String, List<Integer>> entry : dataSet.entrySet()) {
//			System.out.printf("%s %d\n", entry.getKey(), entry.getValue().size());
			if (entry.getValue().size() >= min_sup)
				header.add(new FPTreeNode(entry.getKey(), entry.getValue().size(), null));
		}
	}
	
	/**
	 * ����FPTree
	 */
	private void constructFPTree() {
		for (List<String> items : itemSet)
			insertTree(items, root);
	}
	
	/**
	 * �ݹ�ؼ���ڵ�, ������ڵ��Ѿ������ýڵ�, �ڵ�count����, �����½��ӽڵ�
	 * @param items ����, ÿ�������а������item
	 * @param r �����ڵ�ĸ��ڵ�
	 */
	private void insertTree(List<String> items, FPTreeNode r) {
		if (items.size() <= 0) return;

		String item = items.remove(0);
		
		FPTreeNode node = r.searchChild(item);
		if (node == null) {
			node = new FPTreeNode(item, 1, r);
			r.addChild(node);
			
			// ���½ڵ�����ͷ����
			for (FPTreeNode head : header)
				if (head.item.equals(item)) {
					while (head.next != null)
						head = head.next;
					head.next = node;
					break;
				}
		}
		
		else
			node.count++;
		
		insertTree(items, node);
	}
	
	/**
	 * ��ÿ������, ��supֵ����min_sup��item����������
	 */
	private void buildTaskList() {
		Map<Integer, List<String>> taskList = new HashMap<>();
		for (Map.Entry<String, List<Integer>> entry : dataSet.entrySet()) {
			String item = entry.getKey();
			List<Integer> list = entry.getValue();
			
			for (int task : list) {
				if (taskList.containsKey(task) == false) {
					List<String> tasklist = new ArrayList<>();
					tasklist.add(item);
					taskList.put(task, tasklist);
				}
				else
					taskList.get(task).add(item);
			}
		}
		itemSet = taskList.values();
	}
	
	public static Map<String, List<Integer>> sortMapByValue(Map<String, List<Integer>> oriMap) {
		Map<String, List<Integer>> sortedMap = new LinkedHashMap<>();
		List<Map.Entry<String, List<Integer>>> entryList = new ArrayList<>(oriMap.entrySet());
		Collections.sort(entryList, new MapValueComparator());
		
		Iterator<Map.Entry<String, List<Integer>>> iter = entryList.iterator();
		Map.Entry<String, List<Integer>> tmpEntry = null;
		while (iter.hasNext()) {
			tmpEntry = iter.next();
			sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
		}
		return sortedMap;
	}
}

class MapValueComparator implements Comparator<Map.Entry<String, List<Integer>>> {
	@Override
	public int compare(Entry<String, List<Integer>> o1, Entry<String, List<Integer>> o2) {
		return o2.getValue().size() - o1.getValue().size();
	}
}
