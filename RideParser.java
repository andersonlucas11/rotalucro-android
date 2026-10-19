package br.com.rotalucro.app;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extrai dados exibidos; não presume que estimativas da oferta sejam o percurso real. */
public final class RideParser {
    private RideParser() { }
    private static final Pattern MONEY = Pattern.compile("R\\$\\s*([0-9]{1,6}(?:\\.[0-9]{3})*,[0-9]{2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern KM = Pattern.compile("(?<![\\d])([0-9]{1,4}(?:[.,][0-9]{1,2})?)\\s*km\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIN = Pattern.compile("\\b([0-9]{1,3})\\s*(?:min(?:utos?)?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HRS = Pattern.compile("\\b([0-9]{1,2})\\s*h(?:\\s*([0-9]{1,2})(?:min)?)?\\b", Pattern.CASE_INSENSITIVE);

    public static String normalize(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static boolean containsAny(String text, String... keywords) {
        for (String word : keywords) if (text.contains(word)) return true;
        return false;
    }

    public static double amount(String text) {
        Matcher m = MONEY.matcher(text);
        if (!m.find()) return 0.0;
        try { return Double.parseDouble(m.group(1).replace(".", "").replace(",", ".")); }
        catch (Exception e) { return 0.0; }
    }

    public static double km(String text) {
        Matcher m = KM.matcher(text);
        if (!m.find()) return 0.0;
        try { return Double.parseDouble(m.group(1).replace(",", ".")); }
        catch (Exception e) { return 0.0; }
    }

    public static int minutes(String text) {
        Matcher m = HRS.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)) * 60 +
                    (m.group(2) == null ? 0 : Integer.parseInt(m.group(2))); }
            catch (Exception ignored) { }
        }
        m = MIN.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); }
            catch (Exception ignored) { }
        }
        return 0;
    }

    public static boolean isOffer(String normalized) {
        return containsAny(normalized, "aceitar", "recusar", "recusar corrida", "solicitacao de viagem")
                && !containsAny(normalized, "corrida cancelada", "viagem cancelada");
    }

    public static boolean isCanceled(String normalized) {
        return containsAny(normalized,
                "corrida cancelada", "viagem cancelada", "passageiro cancelou",
                "solicitacao cancelada", "cancelamento confirmado", "viagem foi cancelada");
    }

    /** Só aceitar provas explicitas, nunca interpretar a tela de ofertas como conclusão. */
    public static boolean isFinishedScreen(String normalized) {
        return containsAny(normalized,
                "viagem concluida", "corrida concluida", "viagem finalizada",
                "corrida finalizada", "resumo da viagem", "resumo da corrida",
                "detalhes da viagem", "recibo da viagem", "ganhos desta viagem",
                "ganhos da viagem", "avaliar passageiro", "avaliar o passageiro");
    }

    public static boolean isFinishClick(String platform, String normalizedClicked) {
        if (platform.equals("Uber")) {
            return containsAny(normalizedClicked, "finalizar uberx", "finalizar viagem", "finalizar corrida");
        }
        return containsAny(normalizedClicked, "cobrar pagamento", "finalizar corrida", "finalizar viagem");
    }
}
