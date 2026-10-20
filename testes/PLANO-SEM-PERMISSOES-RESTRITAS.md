# RotaLucro — testes gratuitos sem permissões restritas

## Limite técnico
O serviço de acessibilidade da versão 0.4 não pode ser ativado no dispositivo enquanto o Android restringir a permissão. Não orientar a desativar Play Protect ou ignorar bloqueios. NotificationListenerService também é uma permissão sensível e NÃO é substituto garantido.

## Testes offline da lógica (sem ler Uber/99)
Implementar simulador local que injete eventos fictícios no analisador e valide as transições. Dados de exemplo nunca devem incorporar nomes/endereços de passageiros.

Cenários obrigatórios:
- Oferta Uber R$ 9,02, busca 2,0 km, viagem 1,5 km -> PROVISÓRIA; receita 0.
- Aceitou / Iniciar UberX -> continua PROVISÓRIA; receita 0.
- Usuário cancelou (sem aviso de taxa) -> CANCELADA; receita 0.
- Usuário cancelou com aviso de taxa -> CANCELADA; taxa só no histórico após crédito confirmado.
- Encerrar UberX -> FINALIZANDO; receita 0.
- Coletar pagamento -> FINALIZANDO; receita 0.
- Como foi a viagem? -> possível conclusão; não somar sem valor, km e tempo finais comprovados.
- Nova oferta enquanto a anterior está finalizando -> não presumir conclusão, colocar PENDENTE.
- 99 Dinheiro -> Finalizar corrida / Confirmar valor; confirmar conclusão e dados finais antes de salvar.
- 99 Cartão -> Finalizar corrida / Concluir; confirmar conclusão e dados finais antes de salvar.
- Mesmo identificador de corrida em eventos repetidos -> salvar no máximo uma vez.
- Meta variável por data nunca bloqueia detecção ou contabilização.
- Edição de corrida existente deve ajustar total sem duplicar dados.
- Fechamento: faturamento - despesas = resultado; faturamento - meta = diferença, meta não é despesa.

## Importação por compartilhamento
Verificar se os aplicativos oferecem ação nativa "Compartilhar" para comprovantes/histórico; se não houver, oferecer importação de arquivo selecionado explicitamente pelo usuário. Nunca automatizar cliques nem ler outros apps sem permissão.

## Critério de conclusão
- APK 0.4 não está validado para lançamento automático; não anunciar como completo.
- Não adicionar permissão de internet, notificações ou acessibilidade sem necessidade claramente explicada e opt-in do usuário.
- Preservar dados locais em novas versões e usar assinatura estável.
