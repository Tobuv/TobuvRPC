package com.tobuv.rpc.loadbalcancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 负载均衡策略的公共接口
 */
public interface LoadBalancer {

    Instance select(List<Instance> instances);

}