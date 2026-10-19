package br.com.rotalucro.app;

import org.json.JSONException;
import org.json.JSONObject;

/** Dados de uma viagem; números ausentes ficam com zero. */
public class Ride {
    public String id = "";
    public String platform = "";
    public String status = "PENDENTE";
    public String note = "";
    public long timestamp = System.currentTimeMillis();
    public long triggerAt = 0L;
    public double value = 0.0;
    public double km = 0.0;
    public double pickupKm = 0.0;
    public double tripKm = 0.0;
    public String payment = "APP";
    public int minutes = 0;
    public boolean estimatedKmTime = true;

    public Ride copy() {
        Ride r = new Ride();
        r.id = id; r.platform = platform; r.status = status;
        r.note = note; r.timestamp = timestamp; r.triggerAt = triggerAt;
        r.value = value; r.km = km; r.pickupKm = pickupKm; r.tripKm = tripKm; r.payment = payment; r.minutes = minutes;
        r.estimatedKmTime = estimatedKmTime;
        return r;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id); o.put("platform", platform); o.put("status", status);
            o.put("note", note); o.put("timestamp", timestamp); o.put("triggerAt", triggerAt);
            o.put("value", value); o.put("km", km); o.put("pickupKm", pickupKm); o.put("tripKm", tripKm); o.put("payment", payment); o.put("minutes", minutes);
            o.put("estimatedKmTime", estimatedKmTime);
        } catch (JSONException ignored) { }
        return o;
    }

    public static Ride fromJson(JSONObject o) {
        Ride r = new Ride();
        if (o == null) return r;
        r.id = o.optString("id", "");
        r.platform = o.optString("platform", "");
        r.status = o.optString("status", "PENDENTE");
        r.note = o.optString("note", "");
        r.timestamp = o.optLong("timestamp", System.currentTimeMillis());
        r.triggerAt = o.optLong("triggerAt", 0);
        r.value = o.optDouble("value", 0);
        r.km = o.optDouble("km", 0);
        r.pickupKm = o.optDouble("pickupKm", 0); r.tripKm = o.optDouble("tripKm", r.km);
        r.payment = o.optString("payment", "APP");
        r.minutes = o.optInt("minutes", 0);
        r.estimatedKmTime = o.optBoolean("estimatedKmTime", true);
        return r;
    }
}
