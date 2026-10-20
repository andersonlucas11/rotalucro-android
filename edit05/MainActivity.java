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
    private DailyStore daily;
    private LinearLayout content;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        store = new RideStore(this);
        daily = new DailyStore(this);
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
        add(content, button(daily.monitoring() ? "■ DESLIGAR CONTADOR DE HORAS" : "▶ LIGAR CONTADOR DE HORAS", daily.monitoring() ? WARNING : ACCENT, v -> {
            if (daily.monitoring()) daily.stop(); else daily.start();
            render();
        }), 15);
        add(content, label("Controle de horas. O monitoramento AUTOMÁTICO da Uber/99 ainda não foi habilitado: o botão não lê outros aplicativos.", 11, MUTED, false), 6);

        LinearLayout status = vertical(PANEL, 14);
        add(status, label("● MODO SEGURO: SEM ACESSO ÀS TELAS", 15, ACCENT, true), 0);
        add(status, label("Sem acessibilidade, sem internet e sem leitura automática. " +
                "Você confirma cada corrida concluída no histórico da Uber ou 99, " +
                "informa valor, km e tempo e só então ela entra no faturamento.",
                12, MUTED, false), 7);
        add(content, status, 20);

        List<Ride> rides = store.completed();
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_YEAR), year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        double total = 0, uber = 0, ninetyNine = 0, km = 0, pickup = 0, trip = 0, cash = 0, appPaid = 0;
        int count = 0, min = 0;
        for (Ride r : rides) {
            Calendar c = Calendar.getInstance(); c.setTimeInMillis(r.timestamp);
            if (c.get(Calendar.YEAR) == year && c.get(Calendar.DAY_OF_YEAR) == day) {
                total += r.value; km += r.km; pickup += r.pickupKm; trip += r.tripKm; min += r.minutes; count++;
                if (r.payment.equals("DINHEIRO")) cash += r.value; else appPaid += r.value;
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
        add(content, label("Busca: " + number(pickup) + " km  •  Viagem: " + number(trip) + " km  •  Total: " + number(km) + " km", 13, ACCENT, true), 10);
        add(content, label("Km e tempo informados manualmente; confirme os valores no histórico.", 11, MUTED, false), 6);

        LinearLayout platforms = vertical(PANEL, 16);
        add(platforms, label("GANHOS POR PLATAFORMA  •  HOJE", 12, MUTED, true), 0);
        add(platforms, label("Uber     " + money(uber), 17, TEXT, true), 12);
        add(platforms, label("99          " + money(ninetyNine), 17, TEXT, true), 9);
        add(platforms, label("Dinheiro: " + money(cash) + "  |  App/cartão: " + money(appPaid), 12, ACCENT, false), 12);
        add(content, platforms, 18);
        String todayKey = daily.day(System.currentTimeMillis());
        double goal = daily.get(todayKey, "goal");
        double fuel = daily.get(todayKey, "fuel");
        double food = daily.get(todayKey, "food");
        double parking = daily.get(todayKey, "parking");
        double other = daily.get(todayKey, "other");
        double expenses = fuel + food + parking + other;
        final double todayRevenue = total;
        LinearLayout close = vertical(PANEL, 15);
        add(close, label("META E FECHAMENTO DO DIA", 16, TEXT, true), 0);
        add(close, label("Data: " + new SimpleDateFormat("dd/MM/yyyy", pt).format(new Date()), 12, MUTED, false), 7);
        add(close, label("Meta de hoje: " + (goal > 0 ? money(goal) : "não definida"), 16, TEXT, true), 10);
        add(close, label("Diferença para a meta: " + (goal > 0 ? money(total - goal) : "—"), 14, ACCENT, true), 5);
        add(close, label("Despesas: " + money(expenses) + "  •  Resultado após despesas: " + money(total - expenses), 13, TEXT, true), 10);
        long workMin = daily.duration(todayKey);
        if (daily.monitoring() && daily.startedAt() > 0) workMin += (System.currentTimeMillis() - daily.startedAt()) / 60000;
        add(close, label("Horas do contador: " + time((int) workMin), 13, MUTED, false), 7);
        add(close, label(daily.closed(todayKey) ? "Dia fechado ✓" : "Dia em aberto", 12, daily.closed(todayKey) ? ACCENT : WARNING, true), 7);
        add(close, button("Definir meta de hoje", CARD, v -> editDaily(todayKey, "goal", "Meta de faturamento (R$)", goal)), 12);
        add(close, button("Informar despesas e fechar dia", ACCENT, v -> closeDay(todayKey, todayRevenue)), 9);
        add(content, close, 14);

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
            add(box, label("Busca " + number(r.pickupKm) + " km • Viagem " + number(r.tripKm) + " km • Total " + number(r.km) + " km" + (r.estimatedKmTime ? " (estim.)" : "")
                    + "  •  " + time(r.minutes), 12, MUTED, false), 5);
            if (!r.note.isEmpty()) add(box, label(r.note, 11, MUTED, false), 5);
            add(box, button("✎ Editar corrida", CARD, v -> editSavedRide(r)), 8);
            add(content, box, 9);
        }
        add(content, button("+ Registrar corrida concluída", CARD, v -> reviewPending(null)), 20);
        add(content, button("Atualizar painel", PANEL, v -> render()), 10);
        add(content, label("Versão de teste sem leitura automática. Não registre corridas canceladas. " + "Confira o histórico real na Uber/99.",
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
        final EditText pickup = field("Km para buscar", ride.pickupKm > 0 ? number(ride.pickupKm) : "0");
        final EditText km = field("Km com passageiro", ride.tripKm > 0 ? number(ride.tripKm) : (ride.km > 0 ? number(ride.km) : ""));
        final EditText minutes = field("Tempo em minutos", ride.minutes > 0 ? Integer.toString(ride.minutes) : "");
        add(form, label("Confira com a viagem concluída no histórico da plataforma.", 13, TEXT, false), 0);
        add(form, value, 8);
        add(form, pickup, 6);
        add(form, km, 6);
        android.widget.Spinner payment = new android.widget.Spinner(this);
        payment.setAdapter(new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Pelo aplicativo/cartão/voucher", "Dinheiro"}));
        payment.setSelection("DINHEIRO".equals(ride.payment) ? 1 : 0);
        add(form, payment, 6);
        add(form, minutes, 6);
        if (manual) add(form, label("Selecione Uber ou 99 no botão de confirmação.", 12, MUTED, false), 8);
        android.widget.CheckBox confirmed = new android.widget.CheckBox(this);
        confirmed.setText("Confirmo que esta corrida está CONCLUÍDA no histórico da plataforma (não foi cancelada).");
        confirmed.setTextColor(TEXT);
        confirmed.setTextSize(13);
        add(form, confirmed, 12);

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
                if (!confirmed.isChecked()) {
                    Toast.makeText(this, "Confirme primeiro que a viagem foi concluída.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (manual) ride.platform = "Uber";
                if (confirmRide(ride, value, pickup, km, minutes, payment, manual)) dialog.dismiss();
            });
            if (manual) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(btn -> {
                    if (!confirmed.isChecked()) {
                        Toast.makeText(this, "Confirme primeiro que a viagem foi concluída.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ride.platform = "99";
                    if (confirmRide(ride, value, pickup, km, minutes, payment, true)) dialog.dismiss();
                });
            }
        });
        dialog.show();
    }

    private void editDaily(String day, String key, String title, double old) {
        final EditText entry = field(title, old > 0 ? String.format(pt, "%.2f", old) : "");
        LinearLayout form = vertical(PANEL, 14);
        add(form, label("Data: " + day + "  •  A meta não altera o faturamento.", 12, MUTED, false), 0);
        add(form, entry, 8);
        new AlertDialog.Builder(this).setTitle(title).setView(form)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", (d,w) -> { daily.put(day, key, readDouble(entry)); render(); }).show();
    }

    private void closeDay(String day, double total) {
        LinearLayout form = vertical(PANEL, 12);
        add(form, label("Data " + day + "  •  As despesas são lançadas uma vez, sem descontar a meta como gasto.", 12, MUTED, false), 0);
        String[] keys = {"fuel", "food", "parking", "other"};
        String[] names = {"Combustível R$", "Alimentação R$", "Pedágios/estacionamento R$", "Outras despesas R$"};
        EditText[] entries = new EditText[keys.length];
        for (int i=0; i<keys.length; i++) {
            entries[i] = field(names[i], String.format(pt, "%.2f", daily.get(day, keys[i])));
            add(form, label(names[i], 12, TEXT, false), 8);
            add(form, entries[i], 2);
        }
        add(form, label("Faturamento: " + money(total) + ". A meta é apenas comparação; não reduz o lucro.", 13, ACCENT, true), 12);
        new AlertDialog.Builder(this).setTitle("Fechar dia").setView(form)
            .setNegativeButton("Voltar", null)
            .setPositiveButton("Salvar fechamento", (d,w) -> {
                for (int i=0; i<keys.length; i++) daily.put(day, keys[i], readDouble(entries[i]));
                daily.close(day);
                if (daily.monitoring()) daily.stop();
                render();
                Toast.makeText(this, "Fechamento salvo!", Toast.LENGTH_SHORT).show();
            }).show();
    }

    private boolean confirmRide(Ride ride, EditText value, EditText pickup, EditText km, EditText minutes, android.widget.Spinner payment, boolean manual) {
        ride.value = readDouble(value); ride.pickupKm = readDouble(pickup); ride.tripKm = readDouble(km);
        ride.km = ride.pickupKm + ride.tripKm;
        ride.payment = payment.getSelectedItemPosition() == 1 ? "DINHEIRO" : "APP";
        ride.minutes = (int) readDouble(minutes);
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

    private void editSavedRide(Ride original) {
        Ride ride=original.copy();
        LinearLayout form=vertical(PANEL,12);
        EditText value=field("Valor final (R$)",number(ride.value));
        EditText pickup=field("Km de busca",number(ride.pickupKm));
        EditText trip=field("Km da viagem",number(ride.tripKm));
        EditText minutes=field("Duração em minutos",Integer.toString(ride.minutes));
        android.widget.Spinner payment=new android.widget.Spinner(this);
        payment.setAdapter(new android.widget.ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
             new String[]{"Aplicativo/cartão/voucher","Dinheiro"}));
        payment.setSelection("DINHEIRO".equals(ride.payment)?1:0);
        add(form,label("Corrigindo a corrida existente. Não será criada outra viagem.",13,TEXT,false),0);
        add(form,value,6);add(form,pickup,6);add(form,trip,6);add(form,minutes,6);add(form,payment,6);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Editar corrida • "+ride.platform)
          .setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Salvar correção",null).create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            Ride changed=ride.copy();
            changed.value=readDouble(value);changed.pickupKm=readDouble(pickup);
            changed.tripKm=readDouble(trip);changed.km=changed.pickupKm+changed.tripKm;
            changed.minutes=(int)readDouble(minutes);
            changed.payment=payment.getSelectedItemPosition()==1?"DINHEIRO":"APP";
            if(changed.value<=0||changed.km<=0||changed.minutes<=0||changed.pickupKm<0||changed.tripKm<0){
                Toast.makeText(this,"Confira valor, quilômetros e tempo.",Toast.LENGTH_LONG).show();return;
            }
            changed.estimatedKmTime=false;
            changed.note="Corrida corrigida manualmente.";
            if(!store.updateCompleted(changed)){
                Toast.makeText(this,"Não foi possível localizar essa corrida.",Toast.LENGTH_LONG).show();return;
            }
            dialog.dismiss();render();
            Toast.makeText(this,"Corrida atualizada sem duplicar!",Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }
}
