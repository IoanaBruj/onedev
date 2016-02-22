package com.pmease.gitplex.core.util.branchmatcher;

import java.io.Serializable;

import com.pmease.commons.wicket.editable.annotation.Editable;

@Editable
public interface LocalBranchMatcher extends Serializable {
	boolean matches(String branch);
}