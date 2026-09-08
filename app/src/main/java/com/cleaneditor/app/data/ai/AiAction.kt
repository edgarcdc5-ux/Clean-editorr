package com.cleaneditor.app.data.ai

enum class AiAction(val instruction: String) {
    SUMMARIZE("Resuma o texto de forma clara e objetiva, preservando os pontos essenciais."),
    CORRECT("Corrija ortografia, gramática, pontuação e clareza, sem alterar o sentido do texto."),
    REWRITE("Reescreva o texto de forma mais clara, natural e profissional, preservando o significado."),
    EXPLAIN("Explique o texto de forma simples, detalhando conceitos importantes quando necessário."),
    TASK("Transforme o texto em uma tarefa executável. Retorne exatamente neste formato: # Título da tarefa; Prioridade: BAIXA, MÉDIA ou ALTA; Prazo: data no formato YYYY-MM-DD ou NÃO INFORMADO; Objetivo: uma frase; Passos: uma lista de itens iniciados por '- [ ]'. Não invente prazo ou prioridade se o texto não informar.")
}

fun buildAiPrompt(action: AiAction, text: String): String = buildString {
    append(action.instruction)
    append("\n\nTexto de referência:\n")
    append(text.trim())
}
