package com.andrew.hdss.util

import java.time.LocalDate

sealed class ExprValue {
    data class Str(val value: String) : ExprValue()
    data class Num(val value: Double) : ExprValue()
    data class Bool(val value: Boolean) : ExprValue()
    data class DateVal(val value: LocalDate) : ExprValue()

    fun asString(): String = when (this) {
        is Str -> value
        is Num -> when {
            value.isNaN() -> ""
            value == value.toLong().toDouble() -> value.toLong().toString()
            else -> value.toString()
        }
        is Bool -> value.toString()
        is DateVal -> value.toString() // ISO-8601 yyyy-MM-dd
    }

    fun asBoolean(): Boolean = when (this) {
        is Bool -> value
        is Str -> value.isNotEmpty()
        is Num -> value != 0.0
        is DateVal -> true
    }

    fun asNumber(): Double = when (this) {
        is Num -> value
        is DateVal -> value.toEpochDay().toDouble()
        is Str -> value.trim().let { s ->
            s.toDoubleOrNull()
                ?: runCatching { LocalDate.parse(s).toEpochDay().toDouble() }.getOrNull()
                ?: Double.NaN
        }
        is Bool -> if (value) 1.0 else 0.0
    }
}

class ExpressionException(message: String) : Exception(message)

// ---- AST ----

private sealed class ExprNode {
    data class Literal(val value: ExprValue) : ExprNode()
    data class VarRef(val questionName: String) : ExprNode()
    object SelfRef : ExprNode()
    data class BinaryOp(val op: String, val left: ExprNode, val right: ExprNode) : ExprNode()
    data class UnaryOp(val op: String, val operand: ExprNode) : ExprNode()
    data class Call(val fnName: String, val args: List<ExprNode>) : ExprNode()
}

// ---- Tokenizer ----

private sealed class Token {
    data class Ident(val name: String) : Token()          // today, if, and, or, not
    data class VarToken(val name: String) : Token()         // ${name}
    object SelfToken : Token()                              // .
    data class StringLit(val value: String) : Token()       // 'text'
    data class NumberLit(val value: Double) : Token()
    data class Op(val symbol: String) : Token()             // = != < <= > >= + - * /
    object LParen : Token()
    object RParen : Token()
    object Comma : Token()
    object Eof : Token()
}

private class Tokenizer(private val input: String) {
    private var pos = 0

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (true) {
            skipWhitespace()
            if (pos >= input.length) {
                tokens += Token.Eof
                break
            }
            val c = input[pos]
            when {
                c == '$' && peek(1) == '{' -> tokens += readVarRef()
                c == '.' && !peekIsDigit(1) -> { pos++; tokens += Token.SelfToken }
                c == '\'' -> tokens += readStringLiteral()
                c.isDigit() -> tokens += readNumber()
                c == '(' -> { pos++; tokens += Token.LParen }
                c == ')' -> { pos++; tokens += Token.RParen }
                c == ',' -> { pos++; tokens += Token.Comma }
                c == '=' -> { pos++; tokens += Token.Op("=") }
                c == '!' && peek(1) == '=' -> { pos += 2; tokens += Token.Op("!=") }
                c == '<' && peek(1) == '=' -> { pos += 2; tokens += Token.Op("<=") }
                c == '<' -> { pos++; tokens += Token.Op("<") }
                c == '>' && peek(1) == '=' -> { pos += 2; tokens += Token.Op(">=") }
                c == '>' -> { pos++; tokens += Token.Op(">") }
                c == '+' -> { pos++; tokens += Token.Op("+") }
                c == '-' -> { pos++; tokens += Token.Op("-") }
                c == '*' -> { pos++; tokens += Token.Op("*") }
                c == '/' -> { pos++; tokens += Token.Op("/") }
                c.isLetter() -> tokens += readIdent()
                else -> throw ExpressionException("Unexpected character '$c' at position $pos")
            }
        }
        return tokens
    }

    private fun peek(offset: Int): Char? =
        if (pos + offset < input.length) input[pos + offset] else null

    private fun peekIsDigit(offset: Int): Boolean = peek(offset)?.isDigit() == true

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) pos++
    }

    private fun readVarRef(): Token.VarToken {
        pos += 2 // skip ${
        val start = pos
        while (pos < input.length && input[pos] != '}') pos++
        val name = input.substring(start, pos)
        pos++ // skip }
        return Token.VarToken(name)
    }

    private fun readStringLiteral(): Token.StringLit {
        pos++ // skip opening '
        val start = pos
        while (pos < input.length && input[pos] != '\'') pos++
        val value = input.substring(start, pos)
        pos++ // skip closing '
        return Token.StringLit(value)
    }

    private fun readNumber(): Token.NumberLit {
        val start = pos
        while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
        return Token.NumberLit(input.substring(start, pos).toDouble())
    }

    private fun readIdent(): Token.Ident {
        val start = pos
        while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_' || input[pos] == '-')) pos++
        return Token.Ident(input.substring(start, pos))
    }
}

// ---- Parser (recursive descent, ascending precedence: or, and, not, comparison, additive, multiplicative, unary, primary) ----

private class Parser(private val tokens: List<Token>) {
    private var pos = 0
    private fun peek(): Token = tokens[pos]
    private fun advance(): Token = tokens[pos++]
    private fun expect(token: Token) {
        if (peek() != token) throw ExpressionException("Expected $token but found ${peek()}")
        advance()
    }

    fun parse(): ExprNode {
        val node = parseOr()
        if (peek() != Token.Eof) throw ExpressionException("Unexpected trailing token: ${peek()}")
        return node
    }

    private fun parseOr(): ExprNode {
        var left = parseAnd()
        while (peek().let { it is Token.Ident && it.name == "or" }) {
            advance()
            left = ExprNode.BinaryOp("or", left, parseAnd())
        }
        return left
    }

    private fun parseAnd(): ExprNode {
        var left = parseNot()
        while (peek().let { it is Token.Ident && it.name == "and" }) {
            advance()
            left = ExprNode.BinaryOp("and", left, parseNot())
        }
        return left
    }

    private fun parseNot(): ExprNode {
        val token = peek()
        if (token is Token.Ident && token.name == "not") {
            advance()
            expect(Token.LParen)
            val operand = parseOr()
            expect(Token.RParen)
            return ExprNode.UnaryOp("not", operand)
        }
        return parseComparison()
    }

    private fun parseComparison(): ExprNode {
        var left = parseAdditive()
        while (peek() is Token.Op && (peek() as Token.Op).symbol in setOf("=", "!=", "<", "<=", ">", ">=")) {
            val op = (advance() as Token.Op).symbol
            left = ExprNode.BinaryOp(op, left, parseAdditive())
        }
        return left
    }

    private fun parseAdditive(): ExprNode {
        var left = parseMultiplicative()
        while (peek() is Token.Op && (peek() as Token.Op).symbol in setOf("+", "-")) {
            val op = (advance() as Token.Op).symbol
            left = ExprNode.BinaryOp(op, left, parseMultiplicative())
        }
        return left
    }

    private fun parseMultiplicative(): ExprNode {
        var left = parseUnary()
        while (peek() is Token.Op && (peek() as Token.Op).symbol in setOf("*", "/")) {
            val op = (advance() as Token.Op).symbol
            left = ExprNode.BinaryOp(op, left, parseUnary())
        }
        return left
    }

    private fun parseUnary(): ExprNode {
        if (peek() is Token.Op && (peek() as Token.Op).symbol == "-") {
            advance()
            return ExprNode.UnaryOp("-", parseUnary())
        }
        return parsePrimary()
    }

    private fun parsePrimary(): ExprNode {
        return when (val token = advance()) {
            is Token.VarToken -> ExprNode.VarRef(token.name)
            is Token.SelfToken -> ExprNode.SelfRef
            is Token.StringLit -> ExprNode.Literal(ExprValue.Str(token.value))
            is Token.NumberLit -> ExprNode.Literal(ExprValue.Num(token.value))
            is Token.Ident -> {
                if (peek() == Token.LParen) {
                    advance()
                    val args = mutableListOf<ExprNode>()
                    if (peek() != Token.RParen) {
                        args += parseOr()
                        while (peek() == Token.Comma) {
                            advance()
                            args += parseOr()
                        }
                    }
                    expect(Token.RParen)
                    ExprNode.Call(token.name, args)
                } else {
                    throw ExpressionException("Unexpected identifier '${token.name}' — bare identifiers must be function calls")
                }
            }
            Token.LParen -> {
                val inner = parseOr()
                expect(Token.RParen)
                inner
            }
            else -> throw ExpressionException("Unexpected token: $token")
        }
    }
}

// ---- Evaluator ----

class ExpressionEvaluator {

    // Parsing is pure and side-effect-free per string, so cache by the
    // raw expression text — this runs on every answer change across a
    // whole form.
    private val parseCache = mutableMapOf<String, ExprNode>()

    private fun parseCached(expression: String): ExprNode =
        parseCache.getOrPut(expression) { Parser(Tokenizer(expression).tokenize()).parse() }

    fun evaluate(
        expression: String,
        answers: Map<String, String?>,
        selfValue: String? = null
    ): ExprValue = evalNode(parseCached(expression), answers, selfValue)

    private fun evalNode(node: ExprNode, answers: Map<String, String?>, selfValue: String?): ExprValue =
        when (node) {
            is ExprNode.Literal -> node.value
            is ExprNode.VarRef -> ExprValue.Str(answers[node.questionName] ?: "")
            ExprNode.SelfRef -> ExprValue.Str(selfValue ?: "")
            is ExprNode.UnaryOp -> when (node.op) {
                "not" -> ExprValue.Bool(!evalNode(node.operand, answers, selfValue).asBoolean())
                "-" -> ExprValue.Num(-evalNode(node.operand, answers, selfValue).asNumber())
                else -> throw ExpressionException("Unknown unary operator '${node.op}'")
            }
            is ExprNode.BinaryOp -> evalBinary(node, answers, selfValue)
            is ExprNode.Call -> evalCall(node, answers, selfValue)
        }

    private fun evalBinary(node: ExprNode.BinaryOp, answers: Map<String, String?>, selfValue: String?): ExprValue {
        val left = evalNode(node.left, answers, selfValue)
        val right = evalNode(node.right, answers, selfValue)
        return when (node.op) {
            "and" -> ExprValue.Bool(left.asBoolean() && right.asBoolean())
            "or" -> ExprValue.Bool(left.asBoolean() || right.asBoolean())
            "=" -> ExprValue.Bool(valuesEqual(left, right))
            "!=" -> ExprValue.Bool(!valuesEqual(left, right))
            "<" -> ExprValue.Bool(left.asNumber() < right.asNumber())
            "<=" -> ExprValue.Bool(left.asNumber() <= right.asNumber())
            ">" -> ExprValue.Bool(left.asNumber() > right.asNumber())
            ">=" -> ExprValue.Bool(left.asNumber() >= right.asNumber())
            "+" -> ExprValue.Num(left.asNumber() + right.asNumber())
            "-" -> ExprValue.Num(left.asNumber() - right.asNumber())
            "*" -> ExprValue.Num(left.asNumber() * right.asNumber())
            "/" -> ExprValue.Num(left.asNumber() / right.asNumber())
            else -> throw ExpressionException("Unknown binary operator '${node.op}'")
        }
    }

    // String comparison when either side is a string literal/answer (the
    // common case: ${dob_known} = 'yes'), numeric otherwise.
    private fun valuesEqual(left: ExprValue, right: ExprValue): Boolean =
        if (left is ExprValue.Str || right is ExprValue.Str) {
            left.asString() == right.asString()
        } else {
            left.asNumber() == right.asNumber()
        }

    private fun evalCall(node: ExprNode.Call, answers: Map<String, String?>, selfValue: String?): ExprValue {
        val args = node.args.map { evalNode(it, answers, selfValue) }
        return when (node.fnName) {
            "today" -> ExprValue.DateVal(LocalDate.now())
            "date" -> {
                val days = args[0].asNumber()
                if (days.isNaN()) ExprValue.Str("")
                else ExprValue.DateVal(LocalDate.ofEpochDay(days.toLong()))
            }
            "decimal-date-time" -> ExprValue.Num(
                (args[0] as? ExprValue.DateVal)?.value?.toEpochDay()?.toDouble()
                    ?: throw ExpressionException("decimal-date-time() requires a date argument")
            )
            "if" -> if (args[0].asBoolean()) args[1] else args[2]
            else -> throw ExpressionException("Unknown function '${node.fnName}'")
        }
    }
}