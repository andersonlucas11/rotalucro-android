# RotaLucro — preparação de teste pelo Google Play

Estado: documentação de preparação, **não publicado e não aprovado**.

## Requisitos pendentes
1. Criar e verificar conta Google Play Console (ação do proprietário; não fornecer credenciais).
2. Implementar na interface divulgação destacada, específica, com consentimento afirmativo **antes** da ativação de AccessibilityService.
3. Informar honestamente que a leitura observa somente telas acessíveis da Uber Motorista e da 99 Motorista; pode conter detalhes de viagem/passagens e valores; registros são locais; confirmar em testes.
4. Não marcar isAccessibilityTool=true: a finalidade é controle financeiro, não assistência a pessoas com deficiência.
5. Preparar política de privacidade pública, seção Segurança de dados e declaração AccessibilityService do Play Console, com vídeo demonstrativo.
6. Gerar AAB assinado com chave estável guardada em local seguro; não colocar keystore ou senhas no GitHub.
7. Fazer teste interno no Play Console, testar em dispositivo real, corrigir deteções e falhas antes de pleitear distribuição.
8. **O leitor 0.4 atual não registra automaticamente o faturamento final**: se não identificar valores finais comprovados, cria pendência. Não anunciar automação 100% antes de provar.
9. Não desabilitar Play Protect, não tentar contornar Configurações restritas.

Referências: https://support.google.com/googleplay/android-developer/answer/10964491?hl=pt-BR e https://support.google.com/googleplay/android-developer/answer/14151465

Para contas pessoais novas, produção pode requerer 12 testadores inscritos por 14 dias; teste interno tem regras diferentes. Aprovação não garantida.
