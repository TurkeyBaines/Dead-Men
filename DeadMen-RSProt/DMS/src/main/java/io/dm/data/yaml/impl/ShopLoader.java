package io.dm.data.yaml.impl;

import io.dm.data.yaml.YamlFile;
import io.dm.model.shop.Shop;
import io.dm.model.shop.ShopManager;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
public class ShopLoader extends YamlFile<Shop> {

    public ShopLoader(){

    }
    @Override
    public String path() {
        return "shops/";
    }

    @Override
    public void forEachFile(Object object) {
        ShopManager.registerShop((Shop) dataClass().cast( object));
    }

    @Override
    public Class dataClass() {
        return Shop.class;
    }

}
