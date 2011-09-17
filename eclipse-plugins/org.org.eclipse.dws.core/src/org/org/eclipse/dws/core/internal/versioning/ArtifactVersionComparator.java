/*******************************************************************************
 * Copyright (c) 2008 Pierre-Antoine Grégoire.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Antoine Grégoire - initial API and implementation
 *******************************************************************************/
package org.org.eclipse.dws.core.internal.versioning;

import java.util.Comparator;

import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;


/**
 * The Class ArtifactVersionComparator.
 */
public class ArtifactVersionComparator implements Comparator<ArtifactVersion> {
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(ArtifactVersion o1, ArtifactVersion o2) {
		int result=0;
		Artifact artifact1=(o1.getParent()==null)?null:o1.getParent();
		Group group1=(artifact1.getParent()==null)?null:artifact1.getParent();
		Artifact artifact2=(o2.getParent()==null)?null:o2.getParent();
		Group group2=(artifact2.getParent()==null)?null:artifact2.getParent();
		int groupsComparison =group1==null?0:group1.compareTo(group2);
		if(groupsComparison==0){
			int artifactComparison =artifact1==null?0:artifact1.compareTo(artifact2);
			if(artifactComparison==0){
				int artifactVersionComparison =o1==null?0:o1.compareTo(o2);
				if(artifactVersionComparison==0){
					
				}else{
					result=-artifactVersionComparison;
				}
			}else{
				result=artifactComparison;
			}
		}else{
			result=groupsComparison;
		}
		return result;
	}

}
