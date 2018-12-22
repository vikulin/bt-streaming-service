package org.hyperborian.bt.service.api.seed;

public class RpcResponseError {
	
	private ReponseError error;

	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ReponseError getError() {
		return error;
	}

	public void setError(ReponseError error) {
		this.error = error;
	}


}
