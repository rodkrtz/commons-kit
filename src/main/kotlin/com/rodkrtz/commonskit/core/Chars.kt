package com.rodkrtz.commonskit.core

import kotlin.text.iterator

/**
 * Utilitários de alto desempenho para manipulação de documentos e strings.
 * Focado em baixa alocação de memória e processamento linear.
 */
public object Chars {

    /**
     * Extrai apenas os dígitos de uma sequência, evitando criar uma nova String
     * se o input já for puramente numérico.
     */
    @JvmStatic
    public fun onlyDigits(input: CharSequence?): String {
        if (input.isNullOrEmpty()) return ""

        val len = input.length
        val buffer = CharArray(len)
        var count = 0

        for (i in 0 until len) {
            val c = input[i]
            if (c in '0'..'9') {
                buffer[count++] = c
            }
        }

        return if (count == len && input is String) {
            input
        } else {
            String(buffer, 0, count)
        }
    }

    /**
     * Extrai exatamente [expected] dígitos.
     * Retorna string vazia se encontrar caracteres não numéricos ou se o tamanho for diferente.
     */
    @JvmStatic
    public fun extractDigits(input: String, expected: Int): String {
        val buf = CharArray(expected)
        var pos = 0
        for (c in input) {
            if (c in '0'..'9') {
                if (pos == expected) return ""
                buf[pos++] = c
            }
        }
        return if (pos != expected) "" else String(buf)
    }

    /**
     * Extrai exatamente [expected] caracteres alfanuméricos (0-9, A-Z).
     * Útil para o novo formato de CNPJ Alfanumérico.
     */
    @JvmStatic
    public fun extractBase36(input: String, expected: Int): String {
        val buf = CharArray(expected)
        var pos = 0
        for (c0 in input) {
            val c = c0.uppercaseChar()
            if ((c in '0'..'9') || (c in 'A'..'Z')) {
                if (pos == expected) return ""
                buf[pos++] = c
            }
        }
        return if (pos != expected) "" else String(buf)
    }

    /**
     * Verifica se todos os caracteres da sequência são iguais (ex: "111.111.111-11").
     */
    @JvmStatic
    public fun isRepeated(input: CharSequence): Boolean {
        if (input.isEmpty()) return false
        val first = input[0]
        for (i in 1 until input.length) {
            if (input[i] != first) return false
        }
        return true
    }

    /**
     * Converte um caractere Base36 (0-9, A-Z) para seu valor inteiro (0-35).
     * Utiliza a função nativa do JDK para máxima performance.
     */
    @JvmStatic
    public fun base36Value(c: Char): Int {
        val digit = Character.digit(c, 36)
        if (digit == -1) throw IllegalArgumentException("Invalid base36 char: $c")
        return digit
    }

    /**
     * Função genérica de mascaramento para segurança (Modo SAFE).
     * Exemplo: mask("123456789", 3, 2) -> "123****89"
     */
    @JvmStatic
    public fun mask(input: String, keepStart: Int, keepEnd: Int, maskChar: Char = '*'): String {
        if (input.length <= keepStart + keepEnd) return input

        val chars = input.toCharArray()
        for (i in keepStart until (input.length - keepEnd)) {
            chars[i] = maskChar
        }
        return String(chars)
    }
}