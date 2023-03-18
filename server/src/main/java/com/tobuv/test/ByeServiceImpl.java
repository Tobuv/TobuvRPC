package com.tobuv.test;

import com.tobuv.rpc.annotation.Service;
import com.tobuv.rpc.api.ByeService;

@Service
public class ByeServiceImpl implements ByeService {

    @Override
    public String bye(String name) {
        return "bye, " + name;
    }
}