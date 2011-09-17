package org.org.eclipse.core.utils.platform.filegen.dialogs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.filegen.dialogs.FieldDescriptorsBasedPromptDialog.FieldDescriptor;

public abstract class FieldDescriptorsValidator implements IFieldsValidator {

	private final List<FieldDescriptor> fieldDescriptors;

	public FieldDescriptorsValidator(List<FieldDescriptor> fieldDescriptors) {
		this.fieldDescriptors = Collections.unmodifiableList(fieldDescriptors);
	}

	@SuppressWarnings("unchecked")
	public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
		StringBuilderValidationResult result = new StringBuilderValidationResult();
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
			String value = (String) fieldValueHolders.get(new SimpleFieldIdentifier(fieldDescriptor.getFieldId())).getValue();
			if (fieldDescriptor.getMandatory() && (value == null || value.trim().equals(""))) {
				result.append(fieldDescriptor.getFieldLabel() + " is mandatory\n");
			}
		}

		return additionalValidate(result, fieldValueHolders, fieldDescriptors);
	}

	@SuppressWarnings("unchecked")
	public abstract IValidationResult additionalValidate(IValidationResult result, Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders, List<FieldDescriptor> fieldDescriptors);
}
