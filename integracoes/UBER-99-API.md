# Integração oficial Uber e 99 — pesquisa 19/10/2026

## Uber: acesso restrito, candidato para automação legítima
- Introdução oficial Driver API: https://developer.uber.com/docs/drivers/tutorials/api/introduction
- Histórico: GET /v1/partners/trips com scope OAuth `partner.trips`: https://developer.uber.com/docs/drivers/references/api/v1/partners-trips-get
- Pagamentos: `partner.payments`; requer aprovação/escopos: https://developer.uber.com/docs/drivers/tutorials/api/best-practices
- O acesso é LIMITED ACCESS; deve-se inscrever no Developer Dashboard e solicitar acesso. Não inventar tokens e não armazenar segredos no repo.
- Testar inicialmente para a própria conta do desenvolvedor com autorização, após aprovação. API pode não incluir km de busca; sinalizar estimativa em vez de inventar. Não assumir webhooks/atualização em tempo real.
- Implementar OAuth seguro PKCE se suportado e consulta agendada após acesso, identificador de viagem persistente para evitar duplicados. Guardar credenciais com proteção Android (Keystore), nunca em Java hardcoded.
- Para valores definitivos consultar pagamentos e conciliar com ID da viagem antes de contabilizar. Só contabilizar corridas concluídas.
- Não usar AccessibilityService enquanto o Android impedir sua ativação.

## 99: não confirmado acesso individual
- API pública encontrada em https://github.com/99Taxis/corp-api-v2-documentation é corporativa, NÃO prova disponibilidade de dados da conta 99 Motorista.
- Investigar contato de parcerias/desenvolvedores 99, solicitar API de histórico individual. Não afirmar que integra até obter documentação e autorização.

## Bloqueio do APK lateral
https://developers.google.com/android/play-protect/warning-dev-guidance?hl=pt-BR : Play Protect pode bloquear APK sideload com AccessibilityService ou NotificationListener.

## Próximo passo de usuário
Cadastrar app no portal Uber Developers sem pagar, verificar disponibilidade Driver API, solicitar `partner.trips` e `partner.payments`. Não compartilhar token, client secret ou senhas em chat.

Estado: pesquisa, não há integração funcional nem acesso concedido.
