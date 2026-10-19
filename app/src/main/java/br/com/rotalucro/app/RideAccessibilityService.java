package br.com.rotalucro.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Serviço somente de leitura: monitora eventos da Uber e da 99.
 * NÃO toca, automatiza, aceita, cancela ou altera qualquer operação nesses apps.
 *
 * As telas mudam com atualizações das plataformas. Por segurança, evidências
 * incompletas resultam em PENDENTE, nunca em receita contabilizada.
 */
public class RideAccessibilityService extends AccessibilityService {
    private static final long OFFER_TTL = 4 * 60 * 60 * 1000L;
    private static final long COMPLETION_TTL = 3 * 60 * 1000L;
    private static final long DUPLICATE_CLICK_MS = 12000L;
    private RideStore store;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        store = new RideStore(this);
        AccessibilityServiceInfo settings = getServiceInfo();
        if (settings != null) {
            settings.packageNames = new String[] {"com.ubercab.driver", "com.app99.driver"};
            settings.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED
                    | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
            settings.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
            settings.notificationTimeout = 100;
            setServiceInfo(settings);
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        final String platform;
        if (pkg.equals("com.ubercab.driver")) platform = "Uber";
        else if (pkg.equals("com.app99.driver")) platform = "99";
        else return;

        if (store == null) store = new RideStore(this);
        long now = System.currentTimeMillis();
        String clicked = "";
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            clicked = RideParser.normalize(clickedLabel(event));
            // Alguns botões usam texto no evento, sem nó acessível.
            if (clicked.isEmpty() && event.getText() != null) {
                StringBuilder b = new StringBuilder();
                for (CharSequence part : event.getText()) if (part != null) b.append(' ').append(part);
                clicked = RideParser.normalize(b.toString());
            }
        }

        String snapshot = readVisibleText(pkg);
        String norm = RideParser.normalize(snapshot);

        if (RideParser.isCanceled(norm)) {
            store.clearCandidate(platform);
            Ride active = store.pending(platform);
            if (active != null && active.triggerAt > 0 && now - active.triggerAt <= COMPLETION_TTL) {
                store.discardPending(active.id);
            }
            return;
        }

        // Capturar a oferta para associar seus dados à viagem, sem somá-los ainda.
        // Nunca permitir que telas de ganhos diários substituam uma oferta.
        if (RideParser.isOffer(norm)) {
            double money = RideParser.amount(snapshot);
            double km = RideParser.km(snapshot);
            int minutes = RideParser.minutes(snapshot);
            if (money > 0 && km > 0 && minutes > 0) {
                Ride candidate = new Ride();
                candidate.id = platform + "-" + now;
                candidate.platform = platform;
                candidate.timestamp = now;
                candidate.value = money;
                candidate.km = km;
                candidate.minutes = minutes;
                candidate.note = "Dados da oferta (estimativas; podem diferir do trajeto real).";
                candidate.estimatedKmTime = true;
                store.saveCandidate(candidate);
            }
        }

        // O toque é o gatilho, mas NÃO é prova de que o pagamento foi confirmado.
        if (!clicked.isEmpty() && RideParser.isFinishClick(platform, clicked)) {
            Ride prev = store.pending(platform);
            if (prev == null || now - prev.triggerAt > DUPLICATE_CLICK_MS) {
                Ride candidate = store.candidate(platform);
                Ride pending = new Ride();
                if (candidate != null && now - candidate.timestamp < OFFER_TTL) {
                    pending = candidate.copy();
                }
                // Usa a tela atual somente para completar campos ausentes.
                pending.platform = platform;
                pending.id = platform + "-" + now;
                pending.timestamp = now;
                pending.triggerAt = now;
                pending.status = "PENDENTE";
                if (pending.value == 0) pending.value = RideParser.amount(snapshot);
                if (pending.km == 0) pending.km = RideParser.km(snapshot);
                if (pending.minutes == 0) pending.minutes = RideParser.minutes(snapshot);
                pending.note = "Botão de término identificado. Aguardando comprovante de conclusão e valor final.";
                store.setPending(pending);
            }
        }

        Ride pending = store.pending(platform);
        if (pending == null || pending.triggerAt <= 0) return;
        if (now - pending.triggerAt > COMPLETION_TTL) return;

        // Finalização comprovada por tela posterior + valor monetário explícito.
        // Uma simples oferta ou o próprio botão não são evidência suficiente.
        if (RideParser.isFinishedScreen(norm) && !RideParser.isOffer(norm)) {
            double finalAmount = RideParser.amount(snapshot);
            if (finalAmount > 0) {
                pending.value = finalAmount;
                double finalKm = RideParser.km(snapshot);
                int finalMinutes = RideParser.minutes(snapshot);
                if (finalKm > 0 && finalMinutes > 0) {
                    pending.km = finalKm;
                    pending.minutes = finalMinutes;
                    pending.estimatedKmTime = false;
                }
                if (pending.km > 0 && pending.minutes > 0) {
                    pending.note = pending.estimatedKmTime
                            ? "Valor lido na conclusão; km e tempo baseados na oferta (estimados)."
                            : "Valor, km e tempo lidos na confirmação final.";
                    store.complete(pending);
                } else {
                    pending.note = "Conclusão e valor identificados; km/tempo não disponíveis. Conferir antes de somar.";
                    store.setPending(pending);
                }
            }
        }
    }

    private String clickedLabel(AccessibilityEvent event) {
        AccessibilityNodeInfo node = event.getSource();
        if (node == null) return "";
        StringBuilder b = new StringBuilder();
        if (node.getText() != null) b.append(node.getText()).append(' ');
        if (node.getContentDescription() != null) b.append(node.getContentDescription());
        return b.toString();
    }

    /** Limita leitura a elementos visíveis de Uber/99 já filtrados pelo serviço. */
    private String readVisibleText(String expectedPackage) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || root.getPackageName() == null
                || !expectedPackage.equals(root.getPackageName().toString())) return "";
        StringBuilder b = new StringBuilder();
        ArrayDeque<AccessibilityNodeInfo> q = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        q.add(root);
        int visited = 0;
        while (!q.isEmpty() && visited < 200 && b.length() < 9000) {
            AccessibilityNodeInfo node = q.removeFirst();
            visited++;
            String text = node.getText() == null ? "" : node.getText().toString().trim();
            String desc = node.getContentDescription() == null ? "" : node.getContentDescription().toString().trim();
            if (!text.isEmpty() && seen.add(text)) b.append(' ').append(text);
            if (!desc.isEmpty() && seen.add(desc)) b.append(' ').append(desc);
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) q.add(child);
            }
        }
        return b.toString();
    }

    @Override public void onInterrupt() { }
}
