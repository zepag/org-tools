package org.org.model;

import org.org.model.AbstractModelItem;

/**
 * @author pagregoire
 */
@SuppressWarnings("unchecked")
public class PhonyModelItemLevel1 extends AbstractModelItem<RootModelItem, PhonyModelItemLevel2> {
	private String uid;

	/**
	 * 
	 */
	public PhonyModelItemLevel1(String uid) {
		this.uid = uid;
	}

	/**
	 * @see org.org.model.IModelItem#getUID()
	 */
	public String getUID() {
		return this.uid;
	}

	/**
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder("UID=" + this.uid + ";");
	}
}
