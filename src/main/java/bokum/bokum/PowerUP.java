package bokum.bokum;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PowerUP extends JavaPlugin {

    private final int powerUpTargetIndex = 20;
    private final int powerUpMaterialIndex = 24;
    private final int powerUpBtnIndex = 40;
    private final int maxPowerLevel = 7;
    private final double baseAttackSpeed = 1.0d;

    private final String powerUpUiTitle = "§0§l강화";
    private final String levelString = "§f강화 §7+ §c";
    private final String msgPrefix = "§f[ §b강화 §f] ";

    private Inventory powerUpUi;
    private List<Material> powerUpItemTypeList = new ArrayList<Material>();
    private HashMap<String, Inventory> uiMap = new HashMap<String, Inventory>();

    ///////// Utility
    public static int getRandom(int min, int max) {
        return (int)(Math.random() * (max - min + 1) + min);
    }

    //아이템 공격 값 수정
    public ItemStack setItemDamage(ItemStack targetItem, double damageValue){
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(targetItem);

        NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
        NBTTagList modifiers = compound.getList("AttributeModifiers", 10);
        if(modifiers == null) modifiers = new NBTTagList();
        NBTTagCompound damage = new NBTTagCompound();

        damage.set("AttributeName", new NBTTagString("generic.attackDamage"));
        damage.set("Name", new NBTTagString("generic.attackDamage"));
        damage.set("Amount", new NBTTagDouble(damageValue));
        damage.set("Operation", new NBTTagInt(0));
        damage.set("UUIDLeast", new NBTTagInt(getRandom(1,99999)));
        damage.set("UUIDMost", new NBTTagInt(getRandom(1,99999)));
        damage.set("Slot", new NBTTagString("mainhand"));

        removeTagCompoundByAttributeName(modifiers, "generic.attackDamage");

        modifiers.add(damage);
        compound.set("AttributeModifiers", modifiers);
        nmsStack.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    //아이템 속도 값 설정
    public ItemStack setItemAttackSpeed(ItemStack targetItem, double speedValue){
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(targetItem);

        NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
        NBTTagList modifiers = compound.getList("AttributeModifiers", 10);
        if(modifiers == null) modifiers = new NBTTagList();
        NBTTagCompound attackSpeed = new NBTTagCompound();

        attackSpeed.set("AttributeName", new NBTTagString("generic.attackSpeed"));
        attackSpeed.set("Name", new NBTTagString("generic.attackSpeed"));
        attackSpeed.set("Amount", new NBTTagDouble(speedValue));
        attackSpeed.set("Operation", new NBTTagInt(0));
        attackSpeed.set("UUIDLeast", new NBTTagInt(getRandom(1,99999)));
        attackSpeed.set("UUIDMost", new NBTTagInt(getRandom(1,99999)));
        attackSpeed.set("Slot", new NBTTagString("mainhand"));

        removeTagCompoundByAttributeName(modifiers, "generic.attackSpeed");

        modifiers.add(attackSpeed);
        compound.set("AttrMyUtilityibuteModifiers", modifiers);
        nmsStack.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static void removeTagCompoundByAttributeName(NBTTagList nbtTagList, String tagString){
        for(int i = 0; i < nbtTagList.size(); i++){
            NBTTagCompound tagCompound = nbtTagList.get(i);
            if(tagCompound.hasKey("AttributeName") && tagCompound.getString("AttributeName").equalsIgnoreCase(tagString)){
                nbtTagList.remove(i);
            }
        }
    }

    @Override
    public void onEnable() {
        // 원본 강화 UI 생성
        powerUpUi = Bukkit.createInventory(null, 54, powerUpUiTitle);

        // 배경 전부다 검정 유리판으로
        ItemStack blackGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
        for(int i = 0; i < powerUpUi.getSize(); i++){
            powerUpUi.setItem(i, blackGlassPane);
        }

        // 강화 아이템, 소재 올릴 빈 칸 지정
        ItemStack air = new ItemStack(Material.AIR, 1);
        powerUpUi.setItem(powerUpTargetIndex, air);
        powerUpUi.setItem(24, air);

        // 강화 버튼
        ItemStack powerUpBtn = new ItemStack(Material.ANVIL, 1);
        powerUpBtn.addUnsafeEnchantment(Enchantment.LUCK, 1);
        powerUpUi.setItem(powerUpBtnIndex, powerUpBtn);

        // 강화 대상 아이템 종류 지정
        // 검
        powerUpItemTypeList.add(Material.DIAMOND_SWORD);
        powerUpItemTypeList.add(Material.GOLD_SWORD);
        powerUpItemTypeList.add(Material.IRON_SWORD);
        powerUpItemTypeList.add(Material.STONE_SWORD);
        powerUpItemTypeList.add(Material.WOOD_SWORD);

        //괭이
        powerUpItemTypeList.add(Material.DIAMOND_HOE);
        powerUpItemTypeList.add(Material.GOLD_HOE);
        powerUpItemTypeList.add(Material.IRON_HOE);
        powerUpItemTypeList.add(Material.STONE_HOE);
        powerUpItemTypeList.add(Material.WOOD_HOE);

        //곡괭이
        powerUpItemTypeList.add(Material.DIAMOND_PICKAXE);
        powerUpItemTypeList.add(Material.GOLD_PICKAXE);
        powerUpItemTypeList.add(Material.IRON_PICKAXE);
        powerUpItemTypeList.add(Material.STONE_PICKAXE);
        powerUpItemTypeList.add(Material.WOOD_PICKAXE);

        //도끼
        powerUpItemTypeList.add(Material.DIAMOND_AXE);
        powerUpItemTypeList.add(Material.GOLD_AXE);
        powerUpItemTypeList.add(Material.IRON_AXE);
        powerUpItemTypeList.add(Material.STONE_AXE);
        powerUpItemTypeList.add(Material.WOOD_AXE);

        //삽
        powerUpItemTypeList.add(Material.DIAMOND_SPADE);
        powerUpItemTypeList.add(Material.GOLD_SPADE);
        powerUpItemTypeList.add(Material.IRON_SPADE);
        powerUpItemTypeList.add(Material.STONE_SPADE);
        powerUpItemTypeList.add(Material.WOOD_SPADE);

        Bukkit.getPluginManager().registerEvents(new PowerUpEventHandler(), this); //이벤트 등록ㄴ

        Bukkit.getLogger().info(msgPrefix+"강화 플러그인 로드됨");

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(msgPrefix+"강화 플러그인 언로드됨");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String message, String[] args){
        Player player = (Player) sender;
        if(message.equalsIgnoreCase("강화")){
            if(sender instanceof Player && sender.isOp()){
                openPowerUI(player);
            } else {
                sender.sendMessage(msgPrefix + "권한이 부족합니다.");
            }
        }
        return true;
    }

    //강화 UI 표시
    public void openPowerUI(Player player){
        Inventory tempUI = null; //플레이어에게 표시할 UI
        if(uiMap.containsKey(player.getUniqueId().toString())){
            tempUI = uiMap.get(player.getUniqueId().toString()); //이미 고유 UI 가 있으면 그걸로
        } else {
            tempUI = Bukkit.createInventory(null, powerUpUi.getSize(), powerUpUi.getTitle());
            tempUI.setContents(powerUpUi.getContents()); //없으면 원분 UI 복사 후 put
            uiMap.put(player.getUniqueId().toString(), tempUI);
        }
        player.openInventory(tempUI);
    }

    //초기 강화 상태
    public ItemStack initPowerUpItem(ItemStack targetItem){
        ItemMeta itemMeta = targetItem.getItemMeta();
        itemMeta.setUnbreakable(true); //내구도 무한

        List<String> loreList = itemMeta.getLore(); //기본 강화 0
        if(loreList == null) loreList = new ArrayList<String>();
        loreList.add(levelString + 0);
        itemMeta.setLore(loreList);

        targetItem.setItemMeta(itemMeta);

        targetItem = setItemDamage(targetItem, 1.0d); //초기 데미지는 1
        targetItem = setItemAttackSpeed(targetItem, baseAttackSpeed);

        return targetItem;
    }

    //강화하기
    public ItemStack doPowerUp(Player doPlayer, ItemStack targetItem){
        ItemMeta itemMeta = targetItem.getItemMeta();
        List<String> loreList = itemMeta.getLore(); //아이템 설명 줄 가져옴

        if(loreList == null) loreList = new ArrayList<String>();

        String powerLevelString = getPowerLevelString(loreList); //강화 관련 문자1줄 가져옴
        int powerLevel = getPowerLevel(powerLevelString); //현재 몇강인지 가져옴

        int rdNum = getRandom(1, 10);
        if(rdNum > powerLevel+1){ //성공 시

            powerLevel += 1;
            targetItem = setItemDamage(targetItem, powerLevel+1); //실제 데미지 설정, 1강일 땐 2여야해서 +1
            targetItem = setItemAttackSpeed(targetItem, baseAttackSpeed);

            itemMeta = targetItem.getItemMeta(); //ItemMeta 새로 복사

            int stringIndex = loreList.indexOf(powerLevelString);

            if(powerLevelString == null){
                loreList.add(levelString + 1);
            } else {
                loreList.set(stringIndex, levelString + powerLevel);
            }

            doPlayer.sendMessage(msgPrefix + "§a강화에 성공했습니다!");
            doPlayer.getWorld().playSound(doPlayer.getLocation(), Sound.BLOCK_ANVIL_USE, 1.5f, 1.5f);

        } else { //실패 시

            doPlayer.sendMessage(msgPrefix + "§c강화에 실패했습니다...");
            doPlayer.getWorld().playSound(doPlayer.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.5f, 1.5f);

        }

        itemMeta.setLore(loreList);
        itemMeta.setUnbreakable(true); //내구도 무한
        targetItem.setItemMeta(itemMeta);

        return targetItem;
    }

    //문자열에서 몇 강인지 반환 (예: "강화 + 1" -> 반환: 1)
    public int getPowerLevel(String powerLevelString){

        int powerLevel = 0;

        if(powerLevelString != null){
            String baseLevelString = powerLevelString.replace(levelString, "");
            powerLevel = Integer.parseInt(baseLevelString);
        }

        return powerLevel;

    }

    //여러 문자열에서 강화 관련 문자열 반환 (예: "안녕, ㅎㅇㅎㅇ, 강화 + 1, ㅂㅇㅂㅇ" -> 반환: '강화 + 1')
    public String getPowerLevelString(List<String> loreList){

        if(loreList == null) return null;

        for(String lore : loreList){
            if(lore.contains(levelString)){
                return lore;
            }
        }
        return null;
    }

    //강화된 아이템인지 확인
    public boolean isPowerUpItem(ItemStack item){
        return getPowerLevelString(item.getItemMeta().getLore()) != null;
    }

    /////////이벤트
    class PowerUpEventHandler implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent evt){
            if(!(evt.getWhoClicked() instanceof Player)){
                return;
            }
            Player clickedPlayer = (Player) evt.getWhoClicked();
            Inventory inventory = evt.getClickedInventory();

            if(inventory == null || inventory.getTitle() == null) return;

            if(inventory.getTitle().equalsIgnoreCase(powerUpUiTitle)){

                if(!(evt.getSlot() == powerUpTargetIndex || evt.getSlot() == powerUpMaterialIndex)){ //강화 아이템, 소재 칸 말고는 클릭 불가
                    evt.setCancelled(true);
                }

                if(evt.getSlot() == powerUpBtnIndex){ //강화 버튼 클릭 시
                    ItemStack powerUpMaterial = inventory.getItem(powerUpMaterialIndex);
                    ItemStack targetItem = inventory.getItem(powerUpTargetIndex);

                    if(targetItem == null || !powerUpItemTypeList.contains(targetItem.getType())){
                        clickedPlayer.sendMessage(msgPrefix + "§c해당 아이템은 강화가 불가능합니다.");
                        return;
                    }

                    String powerLevelString = getPowerLevelString(targetItem.getItemMeta().getLore());
                    int powerLevel = getPowerLevel(powerLevelString);
                    int needAmount = powerLevel+1;

                    if(powerUpMaterial == null || powerUpMaterial.getType() != Material.NETHER_STAR || powerUpMaterial.getAmount() < needAmount){
                        clickedPlayer.sendMessage(msgPrefix + "§c네더의 별이 부족합니다. §f(§e"+needAmount+"개 필요§f)");
                        return;
                    }

                    if(powerLevel < maxPowerLevel){ //강화 가능하면 네더의 별 감소 후 강화
                        powerUpMaterial.setAmount(powerUpMaterial.getAmount() - needAmount);
                        inventory.setItem(powerUpMaterialIndex, powerUpMaterial);

                        ItemStack powerUpItem = doPowerUp(clickedPlayer, targetItem);
                        inventory.setItem(powerUpTargetIndex, powerUpItem);
                    } else {
                        clickedPlayer.sendMessage(msgPrefix+"§c더 이상 강화가 불가능합니다.");
                    }

                }
            }
        }

//          아이템 자체 데미지 설정 가능해서 이거 안씀씀
//        EventHandler
//        public void EntityDamagedByEntity(EntityDamageByEntityEvent evt){
//            Entity victimEntity = evt.getEntity();
//            Entity damagerEntity = evt.getDamager();
//
//            if(damagerEntity instanceof Player){
//                Player damagerPlayer = (Player)damagerEntity;
//                ItemStack rightHandItem = damagerPlayer.getInventory().getItemInMainHand();
//                Material rightHandItemType = rightHandItem.getType();
//                if(powerUpItemTypeList.contains(rightHandItemType)){
//
//                    List<String> loreList = rightHandItem.getItemMeta().getLore();
//
//                    String powerLevelString = getPowerLevelString(loreList);
//                    int powerLevel = getPowerLevel(powerLevelString);
//
//                    evt.setDamage(1 + powerLevel);
//
//                    rightHandItem.setDurability((short)0);
//                }
//            }
//        }

        @EventHandler
        public void onPlayerItemPickup(EntityPickupItemEvent evt){
            Entity entity = evt.getEntity();
            if(entity instanceof Player){
                Item item = evt.getItem();
                ItemStack itemStack = evt.getItem().getItemStack();

                if(powerUpItemTypeList.contains(itemStack.getType()) && !isPowerUpItem(itemStack)){ //강화 대상인데 강화되지 않은 경우
                    ItemStack editedStack = initPowerUpItem(itemStack); //초기 강화 상태로 설정
                    item.setItemStack(editedStack);
                }
            }
        }

        @EventHandler
        public void PrepareItemCraftEvent(PrepareItemCraftEvent evt){
            if(evt.getRecipe() == null) return;

            ItemStack resultItem = evt.getRecipe().getResult();
            if(resultItem == null) return;

            if(powerUpItemTypeList.contains(resultItem.getType()) && !isPowerUpItem(resultItem)){ //강화 대상인데 강화되지 않은 경우
                ItemStack newResultItem = initPowerUpItem(resultItem); //초기 강화 상태로 설정
                evt.getInventory().setResult(newResultItem);
            }
        }

        @EventHandler
        public void onPlayerInteractEntity(PlayerInteractEntityEvent evt){
            Player player = evt.getPlayer();
            Entity targetEntity = evt.getRightClicked();
            if(targetEntity.getCustomName() != null && targetEntity.getCustomName().contains("강화")){ //강화 NPC 클릭 시
                openPowerUI(player);
            }
        }

    }
}
