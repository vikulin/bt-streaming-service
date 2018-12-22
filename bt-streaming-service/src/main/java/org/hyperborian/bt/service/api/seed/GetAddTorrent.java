package org.hyperborian.bt.service.api.seed;

import java.util.ArrayList;
import java.util.List;

public class GetAddTorrent extends RpcRequest<Object> {

	public GetAddTorrent(String id) {
		super(id);
		setMethod("aria2.addTorrent");
	}
	
	/**
	 * should be torrent content encoded base64
	 */
	private List<Object> params = new ArrayList<Object>();

	@Override
	public List<Object> getParams() {
		return params;
	}

	@Override
	public void setParams(List<Object> params) {
		this.params = params;
	}
	
	public void addParams(Object param) {
		this.params.add(param);
	}

}
