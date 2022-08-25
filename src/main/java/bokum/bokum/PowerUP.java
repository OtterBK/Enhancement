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
import java.util.List;

public class PowerUP extends JavaPlugin {

    public Inventory powerUpUi;
    private final String powerUpUiTitle = "§0§l강화";
    private final int powerUpBtnIndex = 40;
    private List<Material> powerUpItemTypeList = new ArrayList<Material>();
    private final String levelString = "§f강화 §7+ §c";
    private final String msgPrefix = "§f[ §b강화 §f] ";

    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(new PowerUpEventHandler(), this);

        //시작될때
        powerUpUi = Bukkit.createInventory(null, 54, powerUpUiTitle);

        ItemStack blackGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
        for(int i = 0; i < powerUpUi.getSize(); i++){
            powerUpUi.setItem(i, blackGlassPane);
        }

        ItemStack air = new ItemStack(Material.AIR, 1);
        powerUpUi.setItem(20, air);

        powerUpUi.setItem(24, air);

        ItemStack powerUpBtn = new ItemStack(Material.ANVIL, 1);
        powerUpBtn.addUnsafeEnchantment(Enchantment.LUCK, 1);
        powerUpUi.setItem(powerUpBtnIndex, powerUpBtn);

        powerUpItemTypeList.add(Material.DIAMOND_SWORD);
        powerUpItemTypeList.add(Material.GOLD_SWORD);
        powerUpItemTypeList.add(Material.IRON_SWORD);
        powerUpItemTypeList.add(Material.STONE_SWORD);
        powerUpItemTypeList.add(Material.WOOD_SWORD);

        powerUpItemTypeList.add(Material.DIAMOND_HOE);
        powerUpItemTypeList.add(Material.GOLD_HOE);
        powerUpItemTypeList.add(Material.IRON_HOE);
        powerUpItemTypeList.add(Material.STONE_HOE);
        powerUpItemTypeList.add(Material.WOOD_HOE);

        powerUpItemTypeList.add(Material.DIAMOND_PICKAXE);
        powerUpItemTypeList.add(Material.GOLD_PICKAXE);
        powerUpItemTypeList.add(Material.IRON_PICKAXE);
        powerUpItemTypeList.add(Material.STONE_PICKAXE);
        powerUpItemTypeList.add(Material.WOOD_PICKAXE);

        powerUpItemTypeList.add(Material.DIAMOND_AXE);
        powerUpItemTypeList.add(Material.GOLD_AXE);
        powerUpItemTypeList.add(Material.IRON_AXE);
        powerUpItemTypeList.add(Material.STONE_AXE);
        powerUpItemTypeList.add(Material.WOOD_AXE);

        powerUpItemTypeList.add(Material.DIAMOND_SPADE);
        powerUpItemTypeList.add(Material.GOLD_SPADE);
        powerUpItemTypeList.add(Material.IRON_SPADE);
        powerUpItemTypeList.add(Material.STONE_SPADE);
        powerUpItemTypeList.add(Material.WOOD_SPADE);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    //게임내에서 명령어를 친 플레이어
    //콘솔창일 수도 있음

    @Override
    public boolean onCommand(CommandSender sender, Command command, String message, String[] args){
        if(message.equalsIgnoreCase("강화")){
            if(sender instanceof Player && sender.isOp()){
                Player player = (Player) sender;
                openUI(player);
            } else {
                sender.sendMessage(msgPrefix + "권한이 부족합니다.");
            }
        }
        return true;
    }

    public void openUI(Player player){
        player.openInventory(powerUpUi);
    }

    public void doPowerUp(Player doPlayer, ItemStack targetItem){
        //지금은 그냥 무조건 성공
        ItemMeta itemMeta = targetItem.getItemMeta();
        List<String> loreList = itemMeta.getLore(); //아이템 설명 줄 가져옴

        if(loreList == null) loreList = new ArrayList<String>();

        String powerLevelString = getPowerLevelString(loreList); //강화 관련 문자1줄 가져옴
        int powerLevel = getPowerLevel(powerLevelString); //몇강인지 가져옴

        if(powerLevel < 7){
            int rdNum = getRandom(1, 10);
            if(rdNum > powerLevel){
                powerLevel += 1;
                int stringIndex = loreList.indexOf(powerLevelString);

                if(powerLevel == 1){
                    loreList.add(levelString + 1);
                } else {
                    loreList.set(stringIndex, levelString + powerLevel);
                }

                doPlayer.sendMessage(msgPrefix + "§b강화에 성공했습니다!");
                doPlayer.getWorld().playSound(doPlayer.getLocation(), Sound.BLOCK_ANVIL_USE, 1.5f, 1.5f);
            } else {
                doPlayer.sendMessage(msgPrefix + "§c강화에 실패했습니다...");
                doPlayer.getWorld().playSound(doPlayer.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.5f, 1.5f);
            }
        } else {
            doPlayer.sendMessage(msgPrefix + "§c더 이상 강화가 불가능합니다.");
        }

        itemMeta.setLore(loreList);
        targetItem.setItemMeta(itemMeta);
    }

    //문자열에서 몇 강인지 반환 (예: "강화 + 1" -> 반환: 1
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

                if(!(evt.getSlot() == 20 || evt.getSlot() == 24)){
                    evt.setCancelled(true);
                }

                if(evt.getSlot() == powerUpBtnIndex){
                    ItemStack powerUpMaterial = inventory.getItem(24);
                    ItemStack targetItem = inventory.getItem(20);

                    if(targetItem == null || !powerUpItemTypeList.contains(targetItem.getType())){
                        clickedPlayer.sendMessage(msgPrefix + "§c해당 아이템은 강화가 불가능합니다.");
                    } else {
                        String powerLevelString = getPowerLevelString(targetItem.getItemMeta().getLore());
                        int powerLevel = getPowerLevel(powerLevelString);
                        int needAmount = powerLevel + 1;

                        if(powerUpMaterial == null || powerUpMaterial.getType() != Material.NETHER_STAR
                                || powerUpMaterial.getAmount() < needAmount){
                            clickedPlayer.sendMessage(msgPrefix + "§c네더의 별이 부족합니다. §f(§e"+needAmount+"개 필요§f)");
                        } else {
                            if(powerLevel < 7){ //강화 가능하면 네더의 별 감소
                                powerUpMaterial.setAmount(powerUpMaterial.getAmount() - needAmount);
                                inventory.setItem(24, powerUpMaterial);
                            }
                            doPowerUp(clickedPlayer, targetItem);
                        }
                    }
                }
            }
        }

        @EventHandler
        public void EntityDamagedByEntity(EntityDamageByEntityEvent evt){
            //플레이어가 엔티티를 때렸을 때
            //플레이어가 검을 들고있었다면
            //데미지를 1로 해라

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
            if(targetEntity.getCustomName().contains("강화")){
                openUI(player);
            }
        }

    }
}
