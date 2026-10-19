package br.com.rotalucro.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Painel de ganhos sem coleta em servidor e sem biblioteca externa. */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(7, 16, 31);
    private static final int PANEL = Color.rgb(20, 34, 54);
    private static final int CARD = Color.rgb(29, 46, 68);
    private static final int ACCENT = Color.rgb(17, 212, 164);
    private static final int TEXT = Color.WHITE;
    private static final int MUTED = Color.rgb(165, 184, 200);
    private static final int WARNING = Color.rgb(255, 187, 89);
    private final Locale pt = new Locale("pt", "BR");
    private RideStore store;
    private LinearLayout content;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        store = new RideStore(this);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if (store != null) render();
    }

    private int dp(float n) { return (int) (getResources().getDisplayMetrics().density * n + 0.5f); }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView label(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextColor(color); t.setTextSize(sp);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void add(LinearLayout host, View view, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(top);
        host.addView(view, p);
    }

    private LinearLayout vertical(int background, int padding) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(padding), dp(padding), dp(padding), dp(padding));
        if (background != 0) col.setBackground(shape(background, 16));
        return col;
    }

    private Button button(String s, int color, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(s); b.setTextColor(Color.WHITE);
        b.setTextSize(14); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(color, 13));
        b.setPadding(dp(10), dp(10), dp(10), dp(10));
        b.setOnClickListener(listener);
        return b;
    }

    private String money(double n) { return NumberFormat.getCurrencyInstance(pt).format(n); }
    private String number(double n) { return String.format(pt, "%.1f", n); }
    private String time(int n) { return String.format(pt, "%dh %02dmin", n / 60, n % 60); }

    private boolean accessibilityEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabled == null) return false;
        TextUtils.SimpleStringSplitter split = new TextUtils.SimpleStringSplitter(':');
        split.setString(enabled);
        ComponentName expected = new ComponentName(this, RideAccessibilityService.class);
        while (split.hasNext()) {
            ComponentName active = ComponentName.unflattenFromString(split.next());
            if (expected.equals(active)) return true;
        }
        return false;
    }

    private void render() {
        ScrollView root = new ScrollView(this);
        root.setFillViewport(true);
        root.setBackgroundColor(BG);
        content = vertical(0, 20);
        content.setPadding(dp(18), dp(18), dp(18), dp(32));
        root.addView(content);
        setContentView(root);

        TextView brand = label("ROTA  LUCRO", 25, TEXT, true);
        add(content, brand, 2);
        add(content, label("Seu painel Uber + 99  •  dados no celular", 13, MUTED, false), 4);

        LinearLayout status = vertical(PANEL, 14);
        boolean active = accessibilityEnabled();
        add(status, label(active ? "● Monitoramento autorizado" : "● Leitura automática desativada",
                15, active ? ACCENT : WARNING, true), 0);
        add(status, label("O app apenas lê informações visíveis da Uber e da 99. Não clica, aceita ou cancela por você.",
                12, MUTED, false), 7);
        add(status, button(active ? "Ver permissões" : "Ativar leitura no Android", CARD,
                v -> new AlertDialog.Builder(this)
                        .setTitle("Permissão de acessibilidade")
                        .setMessage("Ative 'RotaLucro - leitura de corridas' nas configurações de acessibilidade. " +
                                "O serviço acompanha apenas Uber Motorista e 99 Motorista, por sua autorização. " +
                                "O acesso pode ler dados que esses aplicativos exibirem, por isso use apenas versões em que confia.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Abrir ajustes", (d, w) -> {
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        }).show()), 12);
        add(content, status, 20);

        List<Ride> rides = store.completed();
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_YEAR), year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        double total = 0, uber = 0, ninetyNine = 0, km = 0;
        int count = 0, min = 0;
        for (Ride r : rides) {
            Calendar c = Calendar.getInstance(); c.setTimeInMillis(r.timestamp);
            if (c.get(Calendar.YEAR) == year && c.get(Calendar.DAY_OF_YEAR) == day) {
                total += r.value; km += r.km; min += r.minutes; count++;
                if (r.platform.equals("Uber")) uber += r.value; else ninetyNine += r.value;
            }
        }

        LinearLayout main = vertical(ACCENT, 20);
        add(main, label("FATURAMENTO DE HOJE", 13, BG, true), 0);
        add(main, label(money(total), 37, BG, true), 4);
        add(main, label(count + " corridas concluídas", 14, BG, true), 4);
        add(content, main, 18);

        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(LinearLayout.HORIZONTAL);
        cols.addView(kpi("DISTÂNCIA", number(km) + " km"), flex(1, 0, 6));
        cols.addView(kpi("TEMPO", time(min)), flex(1, 6, 0));
        add(content, cols, 12);

        LinearLayout rates = new LinearLayout(this);
        rates.setOrientation(LinearLayout.HORIZONTAL);
        rates.addView(kpi("R$/KM", km > 0 ? money(total / km) : "—"), flex(1, 0, 6));
        rates.addView(kpi("R$/HORA", min > 0 ? money(total / (min / 60.0)) : "—"), flex(1, 6, 0));
        add(content, rates, 10);
        add(content, label("Km e tempo podem ser estimativas da oferta; confira os detalhes das viagens.", 11, MUTED, false), 10);

        LinearLayout platforms = vertical(PANEL, 16);
        add(platforms, label("GANHOS POR PLATAFORMA  •  HOJE", 12, MUTED, true), 0);
        add(platforms, label("Uber     " + money(uber), 17, TEXT, true), 12);
        add(platforms, label("99          " + money(ninetyNine), 17, TEXT, true), 9);
        add(content, platforms, 18);

        add(content, label("CORRIDAS PENDENTES", 18, TEXT, true), 24);
        int pendingCount = 0;
        for (Ride pending : store.allPending()) {
            String platform = pending.platform;
            pendingCount++;
            LinearLayout box = vertical(PANEL, 14);
            add(box, label(platform + "  •  aguardando confirmação", 15, WARNING, true), 0);
            add(box, label(pending.note, 12, MUTED, false), 8);
            add(box, label("Capturado: " + money(pending.value) + "  ·  " + number(pending.km)
                    + " km  ·  " + pending.minutes + " min", 12, TEXT, false), 8);
            add(box, button("Revisar e confirmar", CARD, v -> reviewPending(pending)), 12);
            add(content, box, 12);
        }
        if (pendingCount == 0) add(content, label("Nenhuma corrida aguardando conferência.", 13, MUTED, false), 10);

        add(content, label("HISTÓRICO DE CORRIDAS", 18, TEXT, true), 26);
        if (rides.isEmpty()) add(content, label("Ainda não existem corridas concluídas registradas.", 13, MUTED, false), 10);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm", pt);
        for (int i = 0; i < Math.min(20, rides.size()); i++) {
            Ride r = rides.get(i);
            LinearLayout box = vertical(PANEL, 13);
            add(box, label(r.platform + "  ·  " + fmt.format(new Date(r.timestamp)) + "       " + money(r.value), 15, TEXT, true), 0);
            add(box, label(number(r.km) + " km" + (r.estimatedKmTime ? " (estim.)" : "")
                    + "  •  " + time(r.minutes), 12, MUTED, false), 5);
            if (!r.note.isEmpty()) add(box, label(r.note, 11, MUTED, false), 5);
            add(content, box, 9);
        }
        add(content, button("+ Registrar manualmente (emergência)", CARD, v -> reviewPending(null)), 20);
        add(content, button("Atualizar painel", PANEL, v -> render()), 10);
        add(content, label("Protótipo beta. Verifique o histórico real na Uber/99 antes de usar para fins financeiros.",
                12, MUTED, false), 18);
    }

    private LinearLayout.LayoutParams flex(float weight, int left, int right) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        p.leftMargin = dp(left); p.rightMargin = dp(right);
        return p;
    }

    private LinearLayout kpi(String title, String value) {
        LinearLayout box = vertical(PANEL, 15);
        add(box, label(title, 11, MUTED, true), 0);
        add(box, label(value, 20, TEXT, true), 7);
        return box;
    }

    private EditText field(String title, String value) {
        EditText e = new EditText(this);
        e.setText(value);
        e.setSingleLine(true);
        e.setHint(title);
        e.setTextColor(TEXT);
        e.setHintTextColor(MUTED);
        e.setTextSize(16);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return e;
    }

    private double readDouble(EditText e) {
        try { return Double.parseDouble(e.getText().toString().replace(",", ".").trim()); }
        catch (Exception ignored) { return 0; }
    }

    private void reviewPending(Ride existing) {
        final boolean manual = existing == null;
        final Ride ride = manual ? new Ride() : existing.copy();
        final LinearLayout form = vertical(PANEL, 12);
        final EditText value = field("Valor final R$", ride.value > 0 ? number(ride.value) : "");
        final EditText km = field("Km percorridos", ride.km > 0 ? number(ride.km) : "");
        final EditText minutes = field("Tempo em minutos", ride.minutes > 0 ? Integer.toString(ride.minutes) : "");
        add(form, label("Confira com a viagem concluída no histórico da plataforma.", 13, TEXT, false), 0);
        add(form, value, 8);
        add(form, km, 6);
        add(form, minutes, 6);
        if (manual) add(form, label("Selecione Uber ou 99 no botão de confirmação.", 12, MUTED, false), 8);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(manual ? "Nova corrida manual" : "Conferir " + ride.platform)
                .setView(form)
                .setNegativeButton("Cancelar", (d, w) -> { });
        if (!manual) builder.setNeutralButton("Descartar pendente", (d, w) -> {
            store.discardPending(ride.id); render();
        });
        else builder.setNeutralButton("Salvar na 99", null);
        builder.setPositiveButton(manual ? "Salvar na Uber" : "Confirmar corrida", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(v -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                if (manual) ride.platform = "Uber";
                if (confirmRide(ride, value, km, minutes, manual)) dialog.dismiss();
            });
            if (manual) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(btn -> {
                    ride.platform = "99";
                    if (confirmRide(ride, value, km, minutes, true)) dialog.dismiss();
                });
            }
        });
        dialog.show();
    }

    private boolean confirmRide(Ride ride, EditText value, EditText km, EditText minutes, boolean manual) {
        ride.value = readDouble(value); ride.km = readDouble(km); ride.minutes = (int) readDouble(minutes);
        if (ride.value <= 0 || ride.km <= 0 || ride.minutes <= 0) {
            Toast.makeText(this, "Informe valor, quilômetros e tempo maiores que zero.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (manual) {
            ride.id = "manual-" + System.currentTimeMillis();
            ride.timestamp = System.currentTimeMillis();
        }
        ride.estimatedKmTime = false;
        ride.note = manual ? "Registro manual." : "Dados conferidos manualmente no histórico.";
        if (!store.complete(ride)) {
            Toast.makeText(this, "Registro duplicado ou incompleto.", Toast.LENGTH_SHORT).show();
            return false;
        }
        render();
        Toast.makeText(this, "Corrida adicionada ao faturamento!", Toast.LENGTH_SHORT).show();
        return true;
    }
}
