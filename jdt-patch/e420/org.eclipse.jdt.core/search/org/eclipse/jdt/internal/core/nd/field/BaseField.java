package org.eclipse.jdt.internal.core.nd.field;

abstract class BaseField implements IField {
	protected int offset;
	private String fieldName;

	protected final void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public final void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public final int getOffset() {
		return this.offset;
	}

	@Override
	public final String getFieldName() {
		return this.fieldName;
	}
}
