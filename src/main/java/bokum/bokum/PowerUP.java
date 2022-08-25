package bokum.bokum;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
    private final String powerUpUiTitle = "§0§l강화";
    private final String levelString = "§f강화 §7+ §c";
    private final String msgPrefix = "§f[ §b강화 §f] ";

    private Inventory powerUpUi;
    private List<Material> powerUpItemTypeList = new ArrayList<Material>();
    private HashMap<String, Inventory> uiMap = new HashMap<String, Inventory>();

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
        if(message.equalsIgnoreCase("강화")){
            if(sender instanceof Player && sender.isOp()){
                Player player = (Player) sender;
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

    //강화하기
    public void doPowerUp(Player doPlayer, ItemStack targetItem){
        ItemMeta itemMeta = targetItem.getItemMeta();
        List<String> loreList = itemMeta.getLore(); //아이템 설명 줄 가져옴

        if(loreList == null) loreList = new ArrayList<String>();

        String powerLevelString = getPowerLevelString(loreList); //강화 관련 문자1줄 가져옴
        int powerLevel = getPowerLevel(powerLevelString); //현 재몇강인지 가져옴

        int rdNum = getRandom(1, 10);
        if(rdNum > powerLevel){ //성공 시

            powerLevel += 1;
            int stringIndex = loreList.indexOf(powerLevelString);

            if(powerLevel == 1){
                loreList.add(levelString + 1);
            } else {
                loreList.set(stringIndex, levelString + powerLevel);
            }

            doPlayer.sendMessage(msgPrefix + "§b강화에 성공했습니다!");
            doPlayer.getWorld().playSound(doPlayer.getLocation(), Sound.BLOCK_ANVIL_USE, 1.5f, 1.5f);

        } else { //실패 시

            doPlayer.sendMessage(msgPrefix + "§c강화에 실패했습니다...");
            doPlayer.getWorld().playSound(doPlayer.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.5f, 1.5f);

        }

        itemMeta.setLore(loreList);
        targetItem.setItemMeta(itemMeta);
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

    public static int getRandom(int min, int max) {
        return (int)(Math.random() * (max - min + 1) + min);
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
                    int needAmount = powerLevel + 1;

                    if(powerUpMaterial == null || powerUpMaterial.getType() != Material.NETHER_STAR || powerUpMaterial.getAmount() < needAmount){
                        clickedPlayer.sendMessage(msgPrefix + "§c네더의 별이 부족합니다. §f(§e"+needAmount+"개 필요§f)");
                        return;
                    }

                    if(powerLevel < maxPowerLevel){ //강화 가능하면 네더의 별 감소 후 강화
                        powerUpMaterial.setAmount(powerUpMaterial.getAmount() - needAmount);
                        inventory.setItem(powerUpMaterialIndex, powerUpMaterial);

                        doPowerUp(clickedPlayer, targetItem);
                    } else {
                        clickedPlayer.sendMessage(msgPrefix+"§c더 이상 강화가 불가능합니다.");
                    }

                }
            }
        }

        @EventHandler
        public void EntityDamagedByEntity(EntityDamageByEntityEvent evt){
            Entity victimEntity = evt.getEntity();
            Entity damagerEntity = evt.getDamager();

            if(damagerEntity instanceof Player){
                Player damagerPlayer = (Player)damagerEntity;
                ItemStack rightHandItem = damagerPlayer.getInventory().getItemInMainHand();
                Material rightHandItemType = rightHandItem.getType();
                if(powerUpItemTypeList.contains(rightHandItemType)){

                    List<String> loreList = rightHandItem.getItemMeta().getLore();

                    String powerLevelString = getPowerLevelString(loreList);
                    int powerLevel = getPowerLevel(powerLevelString);

                    evt.setDamage(1 + powerLevel);

                    rightHandItem.setDurability((short)0);
                }
            }
        }

        @EventHandler
        public void onPlayerInteractEntity(PlayerInteractEntityEvent evt){
            Player player = evt.getPlayer();
            Entity targetEntity = evt.getRightClicked();
            if(targetEntity.getCustomName().contains("강화")){ //강화 NPC 클릭 시
                openPowerUI(player);
            }
        }

    }
}
