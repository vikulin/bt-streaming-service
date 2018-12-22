package org.hyperborian.bt.service.api.seed;

import java.security.SecureRandom;
import java.util.List;

public abstract class RpcRequest<T> {
	
	public RpcRequest() {
		this.id = getRandomHexString(12);
	}
	
	public RpcRequest(String id) {
		this.id = id;
	}
	
	private String jsonrpc = "2.0";

	private String id;
	
	private String method;
	
	public String getJsonrpc() {
		return jsonrpc;
	}

	public void setJsonrpc(String jsonrpc) {
		this.jsonrpc = jsonrpc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getMethod() {
		return method;
	}

	public abstract List<T> getParams();

	public abstract void setParams(List<T> params);
	
	private String getRandomHexString(int numchars){
        SecureRandom r = new SecureRandom();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

}
