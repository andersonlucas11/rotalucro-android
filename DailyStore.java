package br.com.rotalucro.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Metas e despesas por dia: nunca altera os registros de viagens. */
public class DailyStore {
    private final SharedPreferences prefs;
    public DailyStore(Context context) { prefs = context.getSharedPreferences("daily_v3", Context.MODE_PRIVATE); }
    public String day(long time) { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(time)); }
    private JSONObject read(String day) {
        try { return new JSONObject(prefs.getString("day_" + day, "{}")); }
        catch (Exception e) { return new JSONObject(); }
    }
    public double get(String day, String field) { return read(day).optDouble(field, 0); }
    public boolean closed(String day) { return read(day).optBoolean("closed", false); }
    public void put(String day, String field, double value) {
        JSONObject obj = read(day);
        try { obj.put(field, value); obj.put("closed", false); } catch (Exception ignored) {}
        prefs.edit().putString("day_" + day, obj.toString()).commit();
    }
    public void close(String day) {
        JSONObject obj = read(day);
        try { obj.put("closed", true); } catch (Exception ignored) {}
        prefs.edit().putString("day_" + day, obj.toString()).commit();
    }
    public boolean monitoring() { return prefs.getBoolean("monitoring_requested", false); }
    public void setMonitoring(boolean value) { prefs.edit().putBoolean("monitoring_requested", value).commit(); }
    public long startedAt() { return prefs.getLong("session_start", 0); }
    public long duration(String day) { return prefs.getLong("minutes_" + day, 0); }
    public void setDuration(String day, long min) { prefs.edit().putLong("minutes_" + day, min).commit(); }
    public void start() { prefs.edit().putLong("session_start", System.currentTimeMillis()).putBoolean("monitoring_requested", true).commit(); }
    public void stop() {
        long from = startedAt(), end = System.currentTimeMillis();
        if (from > 0 && end > from) {
            // Time segments are credited by their calendar day, including sessions across midnight.
            long t = from;
            while (t < end) {
                String d = day(t);
                java.util.Calendar c = java.util.Calendar.getInstance(); c.setTimeInMillis(t);
                c.add(java.util.Calendar.DATE, 1); c.set(java.util.Calendar.HOUR_OF_DAY, 0);
                c.set(java.util.Calendar.MINUTE, 0); c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0);
                long next = Math.min(end, c.getTimeInMillis());
                setDuration(d, duration(d) + (next - t) / 60000);
                t = next;
            }
        }
        prefs.edit().putLong("session_start", 0).putBoolean("monitoring_requested", false).commit();
    }
}
