package tool;

import api.API;

import java.util.HashMap;

/**
 * Created by Eldath on 2017/1/29 0029.
 *
 * @author Eldath
 */
public class APISurvivePool {
    private HashMap<API, Boolean> survive;
    private static APISurvivePool instance = null;

    public static APISurvivePool getInstance() {
        if (instance == null) instance = new APISurvivePool();
        return instance;
    }

    private APISurvivePool() {
        survive = new HashMap<>();
    }

    public boolean containAPI(API input) {
        return survive.containsKey(input);
    }

    public boolean addAPI(API input) {
        return survive.put(input, true);
    }

    public void setAPISurvive(API input, boolean state) {
        survive.put(input, state);
    }

    public boolean isSurvive(API input) {
        return survive.get(input);
    }
}
