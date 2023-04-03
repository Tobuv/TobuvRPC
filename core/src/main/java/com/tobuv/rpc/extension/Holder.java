package com.tobuv.rpc.extension;

/**
 *用于持有目标对象
 * @param <T>
 */
public class Holder<T> {
    private volatile T value;//ExtensionLoad基于单例模式，为了保护线程安全使用volatile

    public Holder() {
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }
}