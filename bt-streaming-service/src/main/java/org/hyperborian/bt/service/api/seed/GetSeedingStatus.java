package org.hyperborian.bt.service.api.seed;

import java.util.List;

public class GetSeedingStatus extends RpcRequest<String> {
	
	@Override
	public String getMethod() {
		return "aria2.tellStatus";
	}
	
	private List<String> params;

	@Override
	public List<String> getParams() {
		return params;
	}

	@Override
	public void setParams(List<String> params) {
		this.params = params;
	}

}
