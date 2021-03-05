package program;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import include.Http;

public class SRR {
	private static boolean ver_err = false;
	
	private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0";
	private String cookies = "";
	
	private String userId = null;
	private String isCaptain = "";
	private String gameDataVersion = "";
	private String clientVersion = "";
	private String clientPlatform = "WebGL";
	
	public String getUserId() {
		return userId;
	}
	
	public SRR(String cookies, String clientVersion) {
		this.cookies = cookies;
		this.clientVersion = clientVersion;
		
		JsonObject raw = json(getUser());
		String ver = raw.getAsJsonObject("info").getAsJsonPrimitive("version").getAsString();
		if(!ver.equals(clientVersion)) {
			if(!ver_err) {
				ver_err = true;
				System.err.println("Client version is outdated " + clientVersion + " -> " + ver);
				System.err.println("not critical but can cause issues");
			}
			this.clientVersion = ver;
			raw = json(getUser());
			constructor(raw);
		} else {
			constructor(raw);
		}
	}
	
	private void constructor(JsonObject getUser) {
		this.gameDataVersion = getUser.getAsJsonObject("info").getAsJsonPrimitive("dataVersion").getAsString();
		getUser = getUser.getAsJsonObject("data");
		this.userId = getUser.getAsJsonPrimitive("userId").getAsString();
		this.isCaptain = getUser.getAsJsonPrimitive("isCaptain").getAsString();
	}
	
	private static JsonObject json(String json) {
		return new Gson().fromJson(json, JsonObject.class);
	}
	

	public Http getPost(String cn) {
		Http post = new Http();
		
		post.addHeader("User-Agent", userAgent);
		post.addHeader("Cookie", cookies);
		
		post.setUrl("https://www.streamraiders.com/api/game/");
		post.addUrlArg("cn", cn);
		
		if(userId != null) {
			post.addEncArg("userId", userId);
			post.addEncArg("isCaptain", isCaptain);
		}
		post.addEncArg("gameDataVersion", gameDataVersion);
		post.addEncArg("command", cn);
		post.addEncArg("clientVersion", clientVersion);
		post.addEncArg("clientPlatform", clientPlatform);
		
		return post;
	}
	
	
	private String getUser() {
		Http post = getPost("getUser");
		post.addEncArg("skipDateCheck", "true");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getAvailableCurrencies() {
		Http post = getPost("getAvailableCurrencies");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String collectQuestReward(String slotId) {
		Http post = getPost("collectQuestReward");
		post.addEncArg("slotId", slotId);
		post.addEncArg("autoComplete", "False");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getUserQuests() {
		Http post = getPost("getUserQuests");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	
	public String getCurrentStoreItems() {
		Http post = getPost("getCurrentStoreItems");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getUserEventProgression() {
		Http post = getPost("getUserEventProgression");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String updateFavoriteCaptains(String captainId, boolean fav) {
		
		Http post = getPost("updateFavoriteCaptains");
		post.addEncArg("isFavorited", (fav ? "True" : "False"));
		post.addEncArg("captainId", captainId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String addPlayerToRaid(String captainId, String userSortIndex) {
		
		Http post = getPost("addPlayerToRaid");
		post.addEncArg("userSortIndex", userSortIndex);
		post.addEncArg("captainId", captainId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String leaveCaptain(String captainId) {
		
		Http post = getPost("leaveCaptain");
		post.addEncArg("captainId", captainId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getCaptainsForSearch(int page, int resultsPerPage, boolean fav, boolean live, boolean searchForCaptain, String name) {
		
		JsonObject filter = new JsonObject();
		filter.addProperty("favorite", (fav ? "true" : "false"));
		if(name != null) filter.addProperty((searchForCaptain ? "twitchUserName" : "mainGame"), name);
		if(live) filter.addProperty("isLive", "1");
		filter.addProperty("mode", "pve");
		
		Http post = getPost("getCaptainsForSearch");
		post.addEncArg("page", ""+page);
		post.addEncArg("resultsPerPage", ""+resultsPerPage);
		post.addEncArg("filters", filter.toString());
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	
	public String getRaidPlan(String raidId) {
		
		Http post = getPost("getRaidPlan");
		post.addEncArg("raidId", raidId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getCurrentTime() {
		Http post = getPost("getCurrentTime");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	
	public String getRaid(String raidId) {
		
		Http post = getPost("getRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("maybeSendNotifs", "False");
		post.addEncArg("placementStartIndex", "0");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getActiveRaidsByUser() {
		
		Http post = getPost("getActiveRaidsByUser");
		post.addEncArg("placementStartIndices", "{}");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getMapData(String map) {
		
		Http get = new Http();
		get.setUrl("https://d2k2g0zg1te1mr.cloudfront.net/maps/" + map + ".txt");
		get.addHeader("User-Agent", userAgent);
		
		String ret = null;
		try {
			ret = get.sendGet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
	public String getRaidStatsByUser(String raidId) {

		Http post = getPost("getRaidStatsByUser");
		post.addEncArg("raidId", raidId);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String addToRaid(String raidId, String placementData) {

		Http post = getPost("addToRaid");
		post.addEncArg("raidId", raidId);
		post.addEncArg("placementData", placementData);
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
	
	
	public String getUserUnits() {

		Http post = getPost("getUserUnits");
		
		String text = null;
		try {
			text = post.sendUrlEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}
}
