# RotaLucro 0.3 — Financeiro (Android)

Atualização sobre a versão 0.2 segura. **Não remova a versão 0.2 antes de instalar a atualização**: o package id permanece `br.com.rotalucro.app`, os registros antigos continuam no armazenamento local. Entretanto, APKs de compilacoes diferentes podem ter assinaturas distintas, exigindo exportação/migração antes de reinstalar. Não desinstale antes de conferir.

## Recursos implementados
- Uber e 99: registro manual de corrida concluída e histórico local, mantidos.
- Busca (km) + viagem (km) separados e soma do total; registros legados são tratados como km de viagem.
- Forma de recebimento: dinheiro versus aplicativo/cartão/voucher.
- Meta individual por data **para hoje**, sem afetar a receita e sem ser custo.
- Despesas diárias (combustível, alimentação, pedágios/estacionamento, outros).
- Fechamento salva despesas e indicador de dia fechado, com resultado após despesas.
- Contador de horas Ligar/Desligar (apenas cronômetro; não monitora Uber/99).

## Limitações importantes
- **Não há leitura automática** de Uber/99 nesta versão. A detecção por acessibilidade exige autorização, validação de telas reais, distribuição segura e não deve ser contornada.
- Meta e despesas apenas para a data atual na interface; registros de dias anteriores preservados na base local.
- Contador de horas é um cronômetro manual e não distingue busca, viagem e espera.
- Ganhos não são abatidos por taxas, e o lucro mostrado é resultado de receitas menos despesas informadas, não lucro contábil completo.
- Para reduzir risco, não solicita Internet nem Acessibilidade.

## Como gerar APK
Envie os arquivos e pastas descompactados à raiz do repositório GitHub e execute `.github/workflows/android.yml`. O APK gerado é um artefato ZIP do Actions. Para não comprometer seu histórico, confira a assinatura antes de atualizar. Não contorne alertas do Play Protect.
