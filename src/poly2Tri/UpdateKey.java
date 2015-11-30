package poly2Tri;

import poly2Tri.splayTree.BTreeNode;
import poly2Tri.splayTree.SplayTreeAction;

public class UpdateKey implements SplayTreeAction {

	public void action(BTreeNode node, double y) {
		((Linebase)node.data()).setKeyValue(y);
	}

}
