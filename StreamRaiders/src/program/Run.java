package program;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import include.Time;
import include.GUI;
import include.Heatmap;
import include.Pathfinding;
import program.QuestEventRewards.Quest;
import program.SRR.NoInternetException;
import program.SRRHelper.PvPException;

public class Run {

	
	private String cookies = "";
	private static String clientVersion = StreamRaiders.get("clientVersion");
	
	private String name = "";
	private LocalDateTime started = null;
	
	public LocalDateTime getDateTime() {
		return started;
	}
	
	public void resetDateTime() {
		started = LocalDateTime.now();
		rews = new JsonObject();
	}
	
	
	public Run(String name, String cookies) {
		this.cookies = cookies;
		this.name = name;
	}
	
	public SRRHelper srrh = null;
	
	
	public void showMap(int index) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if(srrh == null) return;
				try {
					srrh.loadMap(srrh.getRaids()[index]);
					Map map = srrh.getMap();
					MapConv.asGui(map);
				} catch (PvPException e) {}
			}
		});
		t.start();
	}
	
	
	private boolean running = false;
	
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
		if(running) {
			t= new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						srrh = new SRRHelper(cookies, clientVersion);
						isReloading = false;
						started = LocalDateTime.now();
						runs();
					} catch (NoInternetException e) {
						StreamRaiders.log(name + ": Run -> Maybe your internet connection failed", e, true);
						GUI.setBackground(name+"::start", Color.red);
						setRunning(false);
					} catch (Exception e) {
						StreamRaiders.log(name + ": Run -> setRunning", e);
						GUI.setBackground(name+"::start", Color.red);
						setRunning(false);
					}
				}
			});
			t.start();
		}
	}

	private Thread t = null;
	
	public void interrupt() {
		time = 0;
	}

	
	public void runs() {
		String part = "null";
		try {
			if(!isRunning()) return;
			
			part = "chests";
			if(chests()) {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {}
			}
			
			part = "captains";
			captains();

			part = "raids";
			if(raids()) raids();
			
			part = "collectEvent";
			collectEvent();
			
			part = "claimQuests";
			claimQuests();
			
			part = "reload store";
			srrh.reloadStore();

			part = "buy store";
			store();

			part = "unlock units";
			unlock();
			
			part = "upgrade units";
			upgradeUnits();
			
			sleep((int) Math.round(Math.random()*620) + 100);
		} catch (Exception e) {
			reload(20, part, e);
		}
	}
	
	private boolean isReloading = false;
	
	private void reload(int sec, String part, Exception e) {

		LocalTime lt = LocalTime.now();
		System.out.println("reload srrh in 20 sec for " + name + " at " + lt.getHour() + ":" + lt.getMinute());
		GUI.setBackground(name+"::start", Color.yellow);
		
		time = sec;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				int min = (int) Math.floor(time / 60);
				int sec = time % 60;
				
				String smin = "";
				String ssec = ""+sec;
				if(min != 0) {
					smin = min+":";
					if(sec < 10) {
						ssec = "0"+sec;
					}
				}
				
				GUI.setText(name + "::counter", "Reload in " + smin + ssec);
				
				if(time <= 0) {
					t.cancel();
					System.out.println("started reload for " + name);
					
					try {
						String ver = srrh.reload();
						
						System.out.println("completed reloading srrh for " + name);
						if(ver != null) {
							System.out.println("client outdated: " + ver);
						} else {
							StreamRaiders.log("critical error happened for " + name + " at \"" + part + "\" -> skipped this round", e);
						}
						
						GUI.setBackground(name+"::start", Color.green);
						sleep(10);
					} catch (NoInternetException e2) {
						StreamRaiders.log(name + ": Run -> Maybe your internet connection failed", e2, true);
						GUI.setBackground(name+"::start", Color.red);
						setRunning(false);
					} catch (Exception e1) {
						StreamRaiders.log("failed to reload srrh for " + name, e1);
						
						if(isReloading) {
							GUI.setBackground(name+"::start", Color.red);
							setRunning(false);
						} else {
							StreamRaiders.log("critical error happened for " + name + " at \"" + part + "\" -> try to reload again", e);
							isReloading = true;
							reload(15*60, part, e);
						}
					}
				}
				
				if(!isRunning()) t.cancel();
				
				time--;
			}
		}, 0, 1000);
	}
	

	private int time = 0;
	
	private void sleep(int sec) {
		time = sec;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				int min = (int) Math.floor(time / 60);
				int sec = time % 60;
				
				String smin = "";
				String ssec = ""+sec;
				if(min != 0) {
					smin = min+":";
					if(sec < 10) {
						ssec = "0"+sec;
					}
				}
				
				GUI.setText(name + "::counter", smin + ssec);
				
				if(time <= 0) {
					t.cancel();
					runs();
				}
				
				if(!isRunning()) t.cancel();
				
				time--;
			}
		}, 0, 1000);
	}
	
	private String[] neededUnits = new String[0];

	private void claimQuests() {
		srrh.updateQuests();
		
		neededUnits = srrh.getNeededUnitTypesForQuests();
		
		Quest[] quests = srrh.getClaimableQuests();
		
		for(int i=0; i<quests.length; i++) {
			String err = srrh.claimQuest(quests[i]);
			if(err != null) StreamRaiders.log(name + ": Run -> claimQuests: err=" + err, null);
		}
	}
	
	private List<String> potionsTiers = Arrays.asList("5,11,14,22,29".split(","));
	
	private void collectEvent() {
		srrh.updateEvent();
		
		boolean bp = srrh.hasBattlePass();
		int tier = srrh.getEventTier();
		for(int i=1; i<tier; i++) {
			if(potionsTiers.contains(""+i)) continue;
			String err = srrh.collectEvent(i, false);
			if(err != null && !err.equals("cant collect")) {
				StreamRaiders.log(name + ": Run -> collectEvent -> basic: err=" + err, null);
			}
			if(!bp) continue;
			
			err = srrh.collectEvent(i, true);
			if(err != null && !err.equals("cant collect")) {
				StreamRaiders.log(name + ": Run -> collectEvent -> pass: err=" + err, null);
			}
		}
	}
	
	private void unlock() {
		Unit[] unlockable = srrh.getUnits(SRC.Helper.canUnlockUnit);
		
		for(int i=0; i<unlockable.length; i++) {
			String err = srrh.unlockUnit(unlockable[i]);
			if(err != null && !err.equals("not enough gold"))
				StreamRaiders.log(name + ": Run -> unlock: type=" + unlockable[i].get(SRC.Unit.unitType) + ", err=" + err, null);
		}
	}
	
	
	
	private void store() {
		JsonArray items = srrh.getStoreItems(SRC.Store.notPurchased);
		for(int i=0; i<items.size(); i++) {
			String err = srrh.buyItem(items.get(i).getAsJsonObject());
			if(err != null && !err.equals("not enough gold"))
				StreamRaiders.log(name + ": Run -> store: item=" + items.get(i) + ", err=" + err, null);
		}
	}
	
	
	private void upgradeUnits() {
		
		JsonObject sCon = MainFrame.getConfig(name).getAsJsonObject("specs");
		
		Unit[] us = srrh.getUnits(SRC.Helper.canUpgradeUnit);
		for(int i=0; i<us.length; i++) {
			String err = srrh.upgradeUnit(us[i], sCon.getAsJsonPrimitive(us[i].get(SRC.Unit.unitType)).getAsString());
			if(err != null) {
				if(!(err.equals("no specUID") || err.equals("cant upgrade unit"))) {
					StreamRaiders.log(name + ": Run -> upgradeUnits: type=" + us[i].get(SRC.Unit.unitType) + " err=" + err, null);
					break;
				}
			}
		}
	}
	
	private String[] pveloy = "? bronze silver gold".split(" ");
	
	private boolean raids() {
		boolean ret = false;
		
		JsonObject uCon = MainFrame.getConfig(name).getAsJsonObject("units");
		
		Unit[] units = srrh.getUnits(SRC.Helper.canPlaceUnit);

		Raid[] plra = srrh.getRaids(SRC.Helper.canPlaceUnit);

		Raid[] all = srrh.getRaids();

		for(int i=0; i<4; i++) {
			if(i<all.length) {
				int wins = Integer.parseInt(all[i].get(SRC.Raid.pveWins));
				int lvl = Integer.parseInt(all[i].get(SRC.Raid.pveLoyaltyLevel));
				if(lvl == 0) lvl = 3;
				GUI.setText(name+"::name::"+i, all[i].get(SRC.Raid.twitchDisplayName) + " - " + wins + "|" + pveloy[lvl]);
				GUI.setText(name+"::chest::"+i, all[i].getFromNode(SRC.MapNode.chestType).replace("chest", ""));
			} else {
				GUI.setText(name+"::name::"+i, "");
				GUI.setText(name+"::chest::"+i, "");
			}
		}
		
		if(plra.length != 0) {
			for(int i=0; i<plra.length; i++) {
				try {
					if(units.length == 0) {
						break;
					}
					
					srrh.loadMap(plra[i]);
					Map map = srrh.getMap();
					
					JsonArray ppt = map.getPresentPlanTypes();
					
					boolean apt = !(ppt.size() == 0);
					
					int[] maxheat = Heatmap.getMaxHeat(map, 5);
					int[][] banned = new int[0][0];
					
					loop:
					while(true) {
						fpt = null;
						Unit unit = findUnit(units, apt, ppt, uCon);
						if(unit == null) unit = findUnit(units, false, ppt, uCon);
						if(unit == null) break;
						JsonArray allowedPlanTypes = new JsonArray();
						allowedPlanTypes.add(fpt);
						
						while(true) {
							int[] pos = Pathfinding.search(MapConv.asField(map, unit.canFly(), allowedPlanTypes, maxheat, banned));
							
							if(pos == null) {
								if(fpt == null) break loop;
								ppt.remove(new JsonPrimitive(fpt));
								continue loop;
							}
							
							String err = srrh.placeUnit(plra[i], unit, false, pos, fpt != null);
							
							if(err == null) {
								units = remove(units, unit);
								break loop;
							}
							
							if(err.equals("PERIOD_ENDED") || err.equals("AT_POPULATION_LIMIT")) break loop;
							
							if(banned.length >= 5) {
								StreamRaiders.log(name + ": Run -> raids -> unitPlacement: tdn=" + plra[i].get(SRC.Raid.twitchDisplayName) + ", err=" + err, null);
								break loop;
							}
							
							banned = add(banned, pos);
						}
					}
					
				} catch (PvPException e) {
					switchRaid(plra[i].get(SRC.Raid.userSortIndex));
					ret = true;
				}
			}
		}
		return ret;
	}
	
	private String fpt = null;
	
	private Unit findUnit(Unit[] units, boolean apt, JsonArray ppt, JsonObject uCon) {
		Unit unit = null;
		for(int j=0; j<units.length; j++) {
			String tfpt = null;
			if(apt) {
				JsonArray pts = units[j].getPlanTypes();
				for(int k=0; k<pts.size(); k++) {
					String pt = pts.get(k).getAsString();
					if(ppt.contains(new JsonPrimitive(pt))) tfpt = pt;
				}
				if(tfpt == null) continue;
			}
			
			if(Arrays.asList(neededUnits).contains(units[j].get(SRC.Unit.unitType))) {
				unit = units[j];
				break;
			}
			if(!uCon.getAsJsonPrimitive(units[j].get(SRC.Unit.unitType)).getAsBoolean()) continue;
			if(unit == null) {
				unit = units[j];
				fpt = tfpt;
			} else {
				int rank = Integer.parseInt(units[j].get(SRC.Unit.rank));
				if(rank > Integer.parseInt(unit.get(SRC.Unit.rank))) {
					unit = units[j];
					fpt = tfpt;
				}
			}
		}
		return unit;
	}
	
	private JsonObject rews = new JsonObject();
	
	public JsonObject getRews() {
		return rews;
	}
	
	
	private boolean chests() {
		Raid[] rera = srrh.getRaids(SRC.Helper.isReward);
		if(rera.length != 0) {
			
			for(int i=0; i<rera.length; i++) {
				
				JsonObject jo = rera[i].getChest(srrh.getSRR());
				
				Set<String> keys = jo.keySet();
				
				for(String key: keys) {
					try {
						rews.addProperty(key, rews.getAsJsonPrimitive(key).getAsInt() + jo.getAsJsonPrimitive(key).getAsInt());
					} catch (Exception e) {
						rews.addProperty(key, jo.getAsJsonPrimitive(key).getAsInt());
					}
				}
			}
			return true;
		}
		return false;
	}
	
	private JsonObject bannedCaps = new JsonObject();
	
	private boolean captains() {
		Raid[] raids = srrh.getRaids(SRC.Helper.all);
		Set<String> set = bannedCaps.deepCopy().keySet();
		for(String cap : set) {
			if(Time.isAfter(bannedCaps.getAsJsonPrimitive(cap).getAsString(), srrh.getServerTime()))
				bannedCaps.remove(cap);
		}
		boolean changed = false;
		JsonObject cCon = MainFrame.getConfig(name).getAsJsonObject("chests");
		while(true) {
			boolean breakout = true;
			for(int i=0; i<raids.length; i++) {
				if(!raids[i].isSwitchable(srrh.getServerTime(), true, 10)) continue;
				if(raids[i].isOffline(srrh.getServerTime(), true, 10)) {
					switchRaid(raids[i].get(SRC.Raid.userSortIndex));
					changed = true;
					breakout = false;
				} else {
					String ct = raids[i].getFromNode(SRC.MapNode.chestType);
					if(ct.contains("bone") || !cCon.getAsJsonPrimitive(ct).getAsBoolean()) {
						bannedCaps.addProperty(raids[i].get(SRC.Raid.captainId), Time.plusMinutes(srrh.getServerTime(), 30));
						switchRaid(raids[i].get(SRC.Raid.userSortIndex));
						changed = true;
						breakout = false;
					}
					
				}
			}
			if(breakout) {
				break;
			} else {
				raids = srrh.getRaids(SRC.Helper.all);
			}
		}
		return changed;
	}
	
	private String switchRaid(String sortIndex) {
		JsonObject cap = null;
		int site = 1;

		JsonArray caps = srrh.search(site, 6, true, true, false, null);
		while(caps.size() != 0) {
			for(int i=0; i<caps.size(); i++) {
				JsonObject icap = caps.get(i).getAsJsonObject();
				if(bannedCaps.has(icap.getAsJsonPrimitive(SRC.Raid.captainId).getAsString())) continue;
				
				int loyalty = Integer.parseInt(icap.getAsJsonPrimitive(SRC.Raid.pveLoyaltyLevel).getAsString());
				try {
					int oldLoy = Integer.parseInt(cap.getAsJsonPrimitive(SRC.Raid.pveLoyaltyLevel).getAsString());
					if(loyalty > oldLoy) {
						cap = icap;
					}
					if(loyalty == 0) {
						cap = icap;
						break;
					}
				} catch(Exception e) {
					cap = icap;
				}
			}
			caps = srrh.search(site++, 6, true, true, false, null);
		}
		
		if(cap == null) {
			site = 1;
			while(true) {
				caps = srrh.search(site++, 6, false, true, false, "stream raiders");
				if(caps.size() == 0) break;
				for(int i=0; i<caps.size(); i++) {
					JsonObject icap = caps.get(i).getAsJsonObject();
					if(bannedCaps.has(icap.getAsJsonPrimitive(SRC.Raid.captainId).getAsString())) continue;
					cap = icap;
					break;
				}
				if(cap != null) {
					String err = srrh.setFavorite(cap, true);
					if(err != null) StreamRaiders.log(name + ": Run -> switchRaid -> setFavorite: err=" + err, null);
					break;
				}
			}
		}
		
		if(cap == null) {
			site = 1;
			while(true) {
				caps = srrh.search(site++, 6, false, true, false, null);
				if(caps.size() == 0) break;
				for(int i=0; i<caps.size(); i++) {
					JsonObject icap = caps.get(i).getAsJsonObject();
					if(bannedCaps.has(icap.getAsJsonPrimitive(SRC.Raid.captainId).getAsString())) continue;
					cap = icap;
					break;
				}
				if(cap != null) {
					String err = srrh.setFavorite(cap, true);
					if(err != null) StreamRaiders.log(name + ": Run -> switchRaid -> setFavorite: err=" + err, null);
					break;
				}
			}
		}
		
		srrh.switchRaid(cap, sortIndex);
		return cap.getAsJsonPrimitive(SRC.Raid.twitchDisplayName).getAsString();
	}
	
	
	private static Unit[] remove(Unit[] arr, Unit item) {
		int index = -1;
		for(int i=0; i<arr.length; i++) {
			if(arr[i].equals(item)) {
				index = i;
			}
		}
		if(index == -1) return arr;
		
		Unit[] arr2 = new Unit[arr.length - 1];
		System.arraycopy(arr, 0, arr2, 0, index);
		System.arraycopy(arr, index + 1, arr2, index, arr.length-index-1);
		
		return arr2;
	}
	
	private static int[][] add(int[][] arr, int[] item) {
		int[][] arr2 = new int[arr.length + 1][];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
	
}
