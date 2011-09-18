package org.org.model;

import org.org.model.AbstractModelItem;

/**
 * @author pagregoire
 */
@SuppressWarnings("rawtypes")
public class PhonyModelItemLevel2 extends AbstractModelItem<PhonyModelItemLevel1, IModelItem> {
	private String uid;

	/**
	 * 
	 */
	public PhonyModelItemLevel2(String uid) {
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
