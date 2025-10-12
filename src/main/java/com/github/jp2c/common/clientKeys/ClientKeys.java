package com.github.jp2c.common.clientKeys;

public final class ClientKeys {
    private ClientKeys() {}

    public static final ClientKey<Integer> POINT = new ClientKey<>("point", Integer.class);
    public static final ClientKey<String> NICKNAME = new ClientKey<>("nickname", String.class);
    public static final ClientKey<Boolean> IS_READY = new ClientKey<>("isReady", Boolean.class);
}