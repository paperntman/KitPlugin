package kitplugin.kitplugin;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileManager {
    private static File dataFolderFile;
    private static Map<String, ItemStack[]> itemMap = new HashMap<>();

    public static void setup(String path) throws IOException, ClassNotFoundException {
        new File(path).mkdir();
        dataFolderFile = new File(path + File.separator+"data");
       itemMap = loadMapFromFile(dataFolderFile);
    }

    public static void addKit(Player player, String name){
        itemMap.put(player.getName()+"-"+name, player.getInventory().getContents());
    }

    public static void delKit(Player player, String name){
        itemMap.remove(player+"-"+name);
    }

    public static ItemStack[] loadKit(Player player, String name){
        return itemMap.get(player.getName()+"-"+name);
    }

    public static String[] kitList(Player player){
        return itemMap.keySet().stream().filter(s -> s.startsWith(player.getName())).toList().toArray(String[]::new);
    }

    public static void save() throws IOException {
        saveMapToFile(itemMap, dataFolderFile);
    }
    private static void saveMapToFile(Map<String, ItemStack[]> a, File file) throws IOException {
        Map<String, Map<String, Object>[]> convertedMap = new HashMap<>();
        for (Map.Entry<String, ItemStack[]> entry : a.entrySet()) {
            ItemStack[] itemStacks = entry.getValue();
            Map<String, Object>[] itemMaps = new Map[itemStacks.length];
            for (int i = 0; i < itemStacks.length; i++) {
                if(itemStacks[i] == null) itemStacks[i] = new ItemStack(Material.AIR);
                itemMaps[i] = serialize(itemStacks[i]);
            }
            convertedMap.put(entry.getKey(), itemMaps);
        }
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outputStream.writeObject(convertedMap);
        }
    }

    private static Map<String, ItemStack[]> loadMapFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Map<String, Object>[]> convertedMap = (Map<String, Map<String, Object>[]>) inputStream.readObject();
            Map<String, ItemStack[]> originalMap = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>[]> entry : convertedMap.entrySet()) {
                Map<String, Object>[] itemMaps = entry.getValue();
                ItemStack[] itemStacks = new ItemStack[itemMaps.length];
                for (int i = 0; i < itemMaps.length; i++) {
                    itemStacks[i] = deserialize(itemMaps[i]);
                }
                originalMap.put(entry.getKey(), itemStacks);
            }
            return originalMap;
        }
    }

    public static Map<String, Object> serialize(ItemStack item) {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("type", item.getType().name());
        serialized.put("amount", item.getAmount());
        ItemMeta itemMeta = item.getItemMeta();
        serialized.put("enchantments", serializeEnchantments(item.getEnchantments()));
        if (itemMeta != null){
            serialized.put("name", itemMeta.getDisplayName());
            if(itemMeta instanceof Damageable) {
                serialized.put("damage", ((Damageable) itemMeta).getDamage());
            }
            serialized.put("unbreakable", itemMeta.isUnbreakable());
            item.setItemMeta(itemMeta);
        }
        return serialized;
    }

    // 역직렬화 함수
    public static ItemStack deserialize(Map<String, Object> serialized) {
        ItemStack item = new ItemStack(org.bukkit.Material.getMaterial((String) serialized.get("type")));
        item.setAmount((int) serialized.get("amount"));
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName((String) serialized.get("name"));
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage((int) serialized.get("damage"));
            }
            meta.setUnbreakable(((boolean) serialized.get("unbreakable")));
            item.setItemMeta(meta);
        }
        Map<Enchantment, Integer> enchantments = deserializeEnchantments((Map<String, Integer>) serialized.get("enchantments"));
        item.addUnsafeEnchantments(enchantments);
        return item;
    }

    // Enchantment 맵을 String-Integer 맵으로 직렬화
    private static Map<String, Integer> serializeEnchantments(Map<Enchantment, Integer> enchantments) {
        Map<String, Integer> serialized = new HashMap<>();
        for (Enchantment enchantment : enchantments.keySet()) {
            serialized.put(enchantment.getName(), enchantments.get(enchantment));
        }
        return serialized;
    }

    // String-Integer 맵을 Enchantment 맵으로 역직렬화
    private static Map<Enchantment, Integer> deserializeEnchantments(Map<String, Integer> serialized) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (String key : serialized.keySet()) {
            Enchantment enchantment = Enchantment.getByName(key);
            int level = serialized.get(key);
            enchantments.put(enchantment, level);
        }
        return enchantments;
    }
}
