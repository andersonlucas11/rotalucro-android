package br.com.rotalucro.app;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.SharedPreferences;
import java.util.UUID;

/** Leitor local: apenas Uber Motorista e 99; nunca executa cliques. */
public class AutoRideService extends AccessibilityService {
  private RideStore store;
  private SharedPreferences settings;
  @Override public void onServiceConnected() {
    store = new RideStore(this);
    settings = getSharedPreferences("auto_control", MODE_PRIVATE);
  }
  @Override public void onAccessibilityEvent(AccessibilityEvent e) {
    if (e == null || settings == null || !settings.getBoolean("enabled",false)) return;
    String pkg = String.valueOf(e.getPackageName());
    String platform = pkg.equals("com.ubercab.driver") ? "Uber" : pkg.equals("com.app99.driver") ? "99" : null;
    if (platform == null) return;
    int type=e.getEventType();
    if(type!=AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && type!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && type!=AccessibilityEvent.TYPE_VIEW_CLICKED) return;
    AccessibilityNodeInfo root=getRootInActiveWindow();
    if(root==null)return;
    StringBuilder sb=new StringBuilder();
    read(root,sb,0,new int[]{0});root.recycle();
    String screen=RideParser.normalize(sb.toString());
    StringBuilder clickBuilder=new StringBuilder();
    if(type==AccessibilityEvent.TYPE_VIEW_CLICKED) for(CharSequence s:e.getText()) if(s!=null)clickBuilder.append(s).append(' ');
    String clicked=RideParser.normalize(clickBuilder.toString());
    Ride ride=store.candidate(platform);
    if(RideParser.containsAny(screen,"usuario cancelou","passageiro cancelou","corrida cancelada","viagem cancelada","cancelar confirmado","cancelamento confirmado")) {
      store.clearCandidate(platform);return;
    }
    if(ride==null && RideParser.isOffer(screen) && RideParser.amount(screen)>0) {
      ride=new Ride();ride.id=UUID.randomUUID().toString();ride.platform=platform;ride.value=RideParser.amount(screen);
      ride.km=RideParser.km(screen);ride.tripKm=ride.km;ride.minutes=RideParser.minutes(screen);
      ride.timestamp=System.currentTimeMillis();ride.status="OFERTA";ride.estimatedKmTime=true;
      store.saveCandidate(ride);
    }
    if(ride==null)return;
    if(RideParser.isFinishClick(platform,clicked) || RideParser.containsAny(clicked,"encerrar uberx","coletar pagamento","confirmar valor","concluir")) {
      ride.status="FINALIZANDO";ride.triggerAt=System.currentTimeMillis();store.saveCandidate(ride);
    }
    boolean proof=RideParser.containsAny(screen,"como foi a viagem","avaliar usuario","enviar avaliacao","viagem concluida","corrida concluida");
    if(!proof || !ride.status.equals("FINALIZANDO") || ride.triggerAt<=0 || System.currentTimeMillis()-ride.triggerAt>1800000) return;
    // Não lançar valor da oferta como se fosse valor final. Somente salvar automaticamente
    // quando os três campos finais explícitos estão acessíveis; senão vai para pendências.
    Ride end=ride.copy();
    end.status="PENDENTE";end.note="Conclusão detectada; confira valor, km e tempo finais.";
    boolean hasFinal=RideParser.containsAny(screen,"valor final","ganhos da viagem","valor recebido");
    if(hasFinal) {double v=RideParser.amount(screen);if(v>0)end.value=v;}
    end.estimatedKmTime=true; // km e tempo da oferta são estimativas, nunca confirmar silenciosamente.
    store.setPending(end);store.clearCandidate(platform);
  }
  private void read(AccessibilityNodeInfo node,StringBuilder sb,int depth,int[] count){
    if(node==null || depth>30 || count[0]++>250 || sb.length()>9000)return;
    if(node.getText()!=null)sb.append(node.getText()).append(' ');
    if(node.getContentDescription()!=null)sb.append(node.getContentDescription()).append(' ');
    for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo c=node.getChild(i);if(c!=null){read(c,sb,depth+1,count);c.recycle();}}
  }
  @Override public void onInterrupt(){}
}