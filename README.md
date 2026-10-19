# RotaLucro — Protótipo Android (Uber + 99)

**Estado:** código-fonte de teste, versão `0.1-beta`, ainda não validada em um celular real nem homologada para uso diário. Não é um APK pré-compilado.

App Android em Java (Android 8.0+), com interface escura, painel de ganhos, registros locais e um serviço de acessibilidade que observa **somente** `com.ubercab.driver` (Uber Driver) e `com.app99.driver` (99 Motorista).

## Regras implementadas

1. Ao aparecer uma oferta, lê e mantém temporariamente, **sem somar**, o preço, os km e o tempo exibidos, quando identificáveis.
2. Quando o usuário toca em **“Finalizar UberX”** (Uber) ou **“Cobrar pagamento”** (99), o app gera um registro **PENDENTE**. Também admite textos equivalentes “Finalizar corrida/viagem”.
3. Só soma automaticamente após identificar, até três minutos depois, **uma tela com indicação explícita de conclusão + valor final** e já possuir km e duração (da tela final ou da oferta). Se km e duração vieram da oferta, marca ambos como **estimados**, pois podem diferir do percurso real.
4. Se não houver evidência suficiente ou se dados estiverem indisponíveis, a corrida **NÃO entra no faturamento**. Permanece pendente para conferência com o histórico oficial.
5. Detectando mensagem explícita de cancelamento, limpa a oferta e descarta a pendência recente daquela plataforma, sem registrar receita.
6. Evita somar uma mesma corrida duas vezes pelo seu ID local.
7. Mostra ganhos do dia, por Uber/99, número de corridas, km, tempo, R$/km, R$/hora, pendências e histórico.
8. Permite conferir manualmente uma pendência ou cadastrar uma corrida caso a leitura automática não funcione.

## Limitações importantes

- Uber e 99 podem mudar os textos, layouts e permissões de acessibilidade. **Não há garantia de que um clique ou todos os valores estejam expostos ao Android**. Se não estiverem, não haverá leitura automática confiável.
- O toque em “Cobrar pagamento” pode ser uma etapa anterior à confirmação de pagamento. **Não é usado, isoladamente, como prova de corrida concluída.**
- O valor da oferta não é necessariamente o valor final: a soma automática requer uma quantia lida na tela de confirmação.
- Os dados de km e tempo podem vir da **oferta** e são sinalizados como estimados; não há rastreamento GPS neste protótipo.
- Em transições muito rápidas ou telas não acessíveis, o app pode perder eventos e requerer conferência manual.
- Não houve teste de compilação com Android SDK neste ambiente. A sintaxe do extrator de texto foi verificada com `javac`, mas o projeto inteiro deve ser compilado no Android Studio ou no GitHub Actions.
- O app é **independente, não oficial e não afiliado a Uber/99**. Verifique as políticas das plataformas antes de distribuir.

## Privacidade

- Não pede permissão de `INTERNET` nem de localização.
- Não registra endereços, nomes de passageiros ou senhas; os textos de tela são interpretados na memória e descartados.
- O serviço de acessibilidade requer autorização explícita no Android. É uma permissão sensível: conceda somente se confiar no código instalado.
- Para exportar ou apagar o banco local, futuramente será necessário adicionar ferramentas; no momento, limpar os dados do app apaga o histórico.

## Como gerar o APK

### No computador (Android Studio)

1. Descompacte `RotaLucro-Android.zip`.
2. Abra a pasta descompactada em **Android Studio** com SDK 35 instalado.
3. Aguarde a sincronização Gradle.
4. No menu **Build** escolha **Build APK(s)**. Local: `app/build/outputs/apk/debug/app-debug.apk`.
5. Instale o APK **apenas no seu aparelho de testes**, habilitando a instalação da fonte confiável quando solicitado.

### Pelo GitHub Actions (sem Android Studio)

1. Crie um repositório GitHub exclusivo para o RotaLucro e envie o **conteúdo descompactado** da pasta, incluindo `.github/workflows/android.yml`.
2. Abra **Actions → Gerar APK Android → Run workflow** (ou envie para a branch `main`).
3. Se a compilação terminar com êxito, baixe o artefato `RotaLucro-APK` no job e extraia `app-debug.apk`.
4. Instale no seu Android **somente após conferir o código e a origem**. Se a compilação falhar, os logs da ação indicam o erro a corrigir.

### Depois de instalar

1. Abra **RotaLucro → Ativar leitura no Android**.
2. Conceda acessibilidade a **RotaLucro - leitura de corridas** se confiar no código.
3. Alguns Android 13+ podem bloquear essa configuração para apps externos; consulte a documentação oficial do Android sobre “configurações restritas”.
4. Em Uber/99, teste **uma oferta cancelada** e **uma corrida concluída**, comparando o painel e a lista de pendências com o histórico oficial.
5. Se não detectar o botão ou os dados, não modifique a Uber/99; ajuste os padrões de texto deste projeto a partir de observações próprias e não sensíveis.

## Arquivos principais

- `app/src/main/java/br/com/rotalucro/app/MainActivity.java`: painel e correções.
- `app/src/main/java/br/com/rotalucro/app/RideAccessibilityService.java`: leitura do Android.
- `app/src/main/java/br/com/rotalucro/app/RideParser.java`: palavras-chave e formatos numéricos.
- `app/src/main/java/br/com/rotalucro/app/RideStore.java`: persistência local e proteção contra duplicatas.
- `app/src/main/res/xml/accessibility_service_config.xml`: apps monitorados.
