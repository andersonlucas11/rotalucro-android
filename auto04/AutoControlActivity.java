package br.com.rotalucro.app;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
public class AutoControlActivity extends Activity {
  @Override protected void onCreate(Bundle b){super.onCreate(b);draw();}
  private void draw(){
    LinearLayout l=new LinearLayout(this);l.setPadding(28,45,28,20);l.setOrientation(LinearLayout.VERTICAL);
    TextView info=new TextView(this);info.setTextSize(20);
    boolean on=getSharedPreferences("auto_control",MODE_PRIVATE).getBoolean("enabled",false);
    info.setText("RotaLucro — leitura automática\nEstado: "+(on?"LIGADO":"DESLIGADO")+"\n\nLeitura limitada à Uber Motorista e à 99 Motorista. Não aceita corridas nem realiza pagamentos. Dados finais não confirmados ficam pendentes. Não há conexão com a internet.");
    l.addView(info);
    Button toggle=new Button(this);toggle.setText(on?"DESLIGAR LEITURA":"LIGAR LEITURA");
    toggle.setOnClickListener(v->{getSharedPreferences("auto_control",MODE_PRIVATE).edit().putBoolean("enabled",!on).apply();draw();});l.addView(toggle);
    Button access=new Button(this);access.setText("CONFIGURAR ACESSIBILIDADE NO ANDROID");
    access.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));l.addView(access);
    Button back=new Button(this);back.setText("VOLTAR AO PAINEL");
    back.setOnClickListener(v->finish());l.addView(back);
    setContentView(l);
  }
}