package io.dm.model.item.attributes;

import com.google.common.collect.Maps;
import io.dm.model.item.Item;
import io.dm.utility.AttributePair;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.dm.model.item.attributes.AttributeTypes.CHARGES;

/**
 * Helper methods for item attributes.
 * @author ReverendDread on 4/10/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project Kronos
 */
public final class AttributeExtensions {

    public static final int deincrementCharges(Item item, int amount) {
        int charges = getCharges(item);
        setCharges(item, charges - amount);
        return getCharges(item);
    }

    public static int getCharges(Item item){
        return item.getAttributeInt(CHARGES.name(), 0);
    }

    public static void setCharges(Item item, int chargesTotal) {
        if (chargesTotal > 0)
            item.putAttribute(CHARGES.name(), chargesTotal);
        else
            clearCharges(item);
        item.update();
    }

    public static boolean clearCharges(Item item){
        item.clearAttribute(CHARGES.name());
        item.update();
        return true;
    }

    public static void addCharges(Item item, int toAdd) {
        int charges = getCharges(item);
        setCharges(item, charges + toAdd);
    }

    public static void putAttribute(Item item, String attribute, String value) {
        if (value.equalsIgnoreCase("0"))
            item.clearAttribute(attribute);
        else
            item.putAttribute(attribute, value);
        item.update();
    }

    public static void putAttribute(Item item, AttributeTypes attribute, Object value) {
        putAttribute(item, attribute.name(), String.valueOf(value));
    }

    public static int hashAttributes(Map<String, String> attributes) {
        return attributes == null || attributes.isEmpty() ? 0 : Objects.hash(attributes);
    }

    public static Map<String, String> attributeMapTypes(AttributePair<AttributeTypes, Object>... attributes) {
        Map<String, String> attributeMap = Maps.newHashMap();
        for (AttributePair<AttributeTypes, Object> pair: attributes) {
            attributeMap.put(pair.getKey().name(), String.valueOf(pair.getValue()));
        }
        return attributeMap;
    }

    public static Map<String, String> attributeMapObjects(AttributePair<Object, Object>... attributes) {
        Map<String, String> attributeMap = Maps.newHashMap();
        for (AttributePair<Object, Object> pair: attributes) {
            attributeMap.put(String.valueOf(pair.getKey()), String.valueOf(pair.getValue()));
        }
        return attributeMap;
    }

    public static Map<String, String> attributeMapStrings(AttributePair<String, String>... attributes) {
        Map<String, String> attributeMap = Maps.newHashMap();
        for (AttributePair<String, String> pair: attributes) {
            attributeMap.put(pair.getKey(), String.valueOf(pair.getValue()));
        }
        return attributeMap;
    }

    public static void putAttributes(Item item ,Map<String, String> attributes) {
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            item.putAttribute(attribute.getKey(), attribute.getValue());
        }
        item.update();
    }


    public static boolean hasAttribute(Item item, AttributeTypes charges) {
        return item.copyOfAttributes().containsKey(charges.name());
    }

    public static void printAttributes(Item item) {
        System.out.println(item.copyOfAttributes().toString());
    }

}
