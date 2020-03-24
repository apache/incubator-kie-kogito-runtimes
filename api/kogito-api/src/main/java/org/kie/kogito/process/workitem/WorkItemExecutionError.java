package org.kie.kogito.process.workitem;

public class WorkItemExecutionError extends RuntimeException {

	private static final long serialVersionUID = 4739415822214766299L;

	private final String errorCode;
	
	public WorkItemExecutionError(String errorCode) {
		super("WorkItem execution failed with error code " + errorCode);
		this.errorCode = errorCode;
	}

	public WorkItemExecutionError(String errorCode, Throwable e) {
		super("WorkItem execution failed with error code " + errorCode, e);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public String toString() {
		return "WorkItemExecutionError [errorCode=" + errorCode + "]";
	}
	
}
