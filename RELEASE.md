# CleanEditor — Checklist de Release

## Estado
- [x] Build debug validado pelo CI
- [x] Testes unitários no CI
- [x] Limite de conteúdo do editor definido
- [x] Limite de prompts da IA definido
- [x] Histórico local limitado
- [x] Tráfego HTTP sem TLS bloqueado
- [x] Chave Gemini fora do código-fonte e configurada por secret no CI

## Antes de publicar
- [ ] Configurar uma chave de assinatura de release fora do repositório
- [ ] Gerar `assembleRelease` com a chave de assinatura configurada
- [ ] Testar o APK release em dispositivo físico
- [ ] Confirmar política de privacidade e tratamento de dados
- [ ] Revisar permissões e versão final
- [ ] Gerar checksum SHA-256 do APK final

## Versionamento Android

O `versionCode` atual é obtido de `GITHUB_RUN_NUMBER` nos builds do CI e usa
`1` fora do GitHub Actions. Antes de distribuir uma versão de release, mantenha
o `versionCode` estritamente maior que o maior APK já distribuído. Recomenda-se
centralizar esse número no processo de release (ou em uma propriedade de build)
caso sejam usados builds fora do CI, sem redefini-lo arbitrariamente nesta
correção de assinatura.

## Segurança da chave Gemini
A chave usada pelo cliente Android pode ser extraída de um APK. Para produção, prefira um backend que mantenha a credencial fora do aplicativo e encaminhe somente as solicitações necessárias para a API.

Nunca coloque uma chave real em `.env.example`, `strings.xml`, código Kotlin ou commits do Git.
