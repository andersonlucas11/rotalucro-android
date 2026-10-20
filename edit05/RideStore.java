package br.com.rotalucro.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Persistência totalmente local; não usa Internet. */
public class RideStore {
    private static final String PREFS = "rides_v1";
    private final SharedPreferences prefs;

    public RideStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private String key(String platform) { return platform.equals("Uber") ? "uber" : "99"; }

    public synchronized void saveCandidate(Ride ride) {
        prefs.edit().putString("offer_" + key(ride.platform), ride.toJson().toString()).apply();
    }

    public synchronized Ride candidate(String platform) {
        String str = prefs.getString("offer_" + key(platform), "");
        try { return str.isEmpty() ? null : Ride.fromJson(new JSONObject(str)); }
        catch (Exception e) { return null; }
    }

    public synchronized void clearCandidate(String platform) {
        prefs.edit().remove("offer_" + key(platform)).apply();
    }

    public synchronized List<Ride> allPending() {
        List<Ride> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString("pending_list", "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) list.add(Ride.fromJson(item));
            }
        } catch (Exception ignored) { }
        return list;
    }

    public synchronized Ride pending(String platform) {
        Ride found = null;
        for (Ride ride : allPending()) {
            if (!ride.platform.equals(platform)) continue;
            if (found == null || found.triggerAt < ride.triggerAt) found = ride;
        }
        return found;
    }

    private JSONArray asArray(List<Ride> list) {
        JSONArray array = new JSONArray();
        for (Ride ride : list) array.put(ride.toJson());
        return array;
    }

    public synchronized void setPending(Ride ride) {
        List<Ride> list = allPending();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(ride.id)) {
                list.set(i, ride.copy()); found = true; break;
            }
        }
        if (!found) list.add(0, ride.copy());
        prefs.edit().putString("pending_list", asArray(list).toString()).commit();
    }

    public synchronized void discardPending(String id) {
        List<Ride> list = allPending();
        list.removeIf(r -> r.id.equals(id));
        prefs.edit().putString("pending_list", asArray(list).toString()).commit();
    }

    public synchronized List<Ride> completed() {
        List<Ride> list = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs.getString("completed", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o != null) list.add(Ride.fromJson(o));
            }
        } catch (Exception ignored) { }
        return list;
    }

    /** Transação única: antes de somar, garante que este ID ainda não foi adicionado. */
    public synchronized boolean complete(Ride ride) {
        if (ride.value <= 0 || ride.km <= 0 || ride.minutes <= 0) return false;
        List<Ride> list = completed();
        for (Ride saved : list) {
            if (saved.id.equals(ride.id)) return false;
        }
        Ride done = ride.copy();
        done.status = "CONCLUIDA";
        list.add(0, done);
        JSONArray array = new JSONArray();
        for (Ride r : list) array.put(r.toJson());
        // commit síncrono para evitar duplicatas se o serviço reiniciar.
        List<Ride> pendingList = allPending();
        pendingList.removeIf(r -> r.id.equals(ride.id));
        prefs.edit()
                .putString("completed", array.toString())
                .putString("pending_list", asArray(pendingList).toString())
                .remove("offer_" + key(ride.platform))
                .commit();
        return true;
    }

    /** Atualiza somente a corrida com este ID; preserva a data e não cria duplicatas. */
    public synchronized boolean updateCompleted(Ride edited) {
        List<Ride> list=completed();
        for(int i=0;i<list.size();i++){
            if(list.get(i).id.equals(edited.id)){
                Ride safe=edited.copy();
                safe.status="CONCLUIDA";
                safe.timestamp=list.get(i).timestamp;
                list.set(i,safe);
                return prefs.edit().putString("completed",asArray(list).toString()).commit();
            }
        }
        return false;
    }
}
