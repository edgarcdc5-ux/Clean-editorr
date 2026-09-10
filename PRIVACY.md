# CleanEditor — Privacidade e Segurança da IA

## Dados enviados ao Gemini

Ao usar uma ação de IA, o texto informado pelo usuário pode ser enviado ao serviço do Google Gemini para gerar a resposta solicitada. Arquivos enviados para análise seguem o mesmo princípio.

Não envie senhas, tokens, chaves de API, documentos confidenciais ou outros dados que você não queira compartilhar com um serviço externo.

## Histórico local

O histórico de respostas da IA é armazenado localmente no dispositivo para permitir consultas recentes. O aplicativo limita o histórico a 30 entradas e limita o tamanho salvo de cada entrada.

O histórico não é um mecanismo de sincronização em nuvem.

## Chave da API

A chave do Gemini não deve ser colocada no código-fonte, em `strings.xml`, nem versionada no Git. Para desenvolvimento/CI, use a configuração de ambiente/secret já prevista pelo projeto.

Para uma versão de produção distribuída a terceiros, uma chave embutida no APK não deve ser considerada secreta. O caminho recomendado é mover a chamada do Gemini para um backend sob controle do aplicativo e manter a credencial somente no servidor.

## Logs

A aplicação não deve registrar prompts, respostas ou chaves de API em logs de diagnóstico.

## Boas práticas

- mantenha a chave fora do Git;
- use secrets no CI;
- evite enviar dados pessoais ou confidenciais para a IA;
- revise permissões antes de publicar;
- para produção, prefira backend seguro em vez de chave diretamente no APK.
