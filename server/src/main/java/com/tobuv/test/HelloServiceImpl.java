package com.tobuv.test;

import com.tobuv.rpc.annotation.Service;
import com.tobuv.rpc.api.HelloObject;
import com.tobuv.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到消息：{}", object.getMessage());
        return "这是Impl1方法";
    }

}