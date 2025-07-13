package com.ulyp.ui.elements.render

import com.ulyp.core.Type
import com.ulyp.ui.util.Style
import javafx.scene.Node
import javafx.scene.text.Text

@DslMarker
annotation class HtmlTagMarker

@HtmlTagMarker
abstract class Tag {

}

@JvmInline
value class Separator(val text: String)

fun String.toSeparator(): Separator {
    return Separator(this)
}

class Flow : Tag() {
    val children = arrayListOf<TextElement>()

    protected fun <T : TextElement> addTag(tag: T): T {
        children.add(tag)
        return tag
    }

    protected fun <T : TextElement> addAndInitTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    fun body(init: Body.() -> Unit): Body {
        val body = Body()
        body.init()
        return body
    }

    operator fun Type.unaryPlus() {
        children.add(TypeText(this))
    }

    operator fun Separator.unaryPlus() {
        children.add(SeparatorText(this))
    }

    fun num(num: String): NumberText = addTag(NumberText(num))

    fun num(num: Long): NumberText = addTag(NumberText(num.toString()))

    fun nul(): NullText = addTag(NullText())

    fun num(num: Long, init: NumberText.() -> Unit): NumberText = addAndInitTag(NumberText(num.toString()), init)

    fun build(): List<Node> {
        return children.map { it.render() }.toList()
    }
}

interface TextElement {
    fun render(): Node
}

abstract class AbstractBody : Tag(), TextElement {
    fun trimmed(init: TrimmedText.() -> Unit): TrimmedText {
        val node = TrimmedText()
        node.init()
        return node
    }
}

class Body : AbstractBody() {
    var node = Text()

    override fun render(): Text {
        TODO("Not yet implemented")
    }
}

class TrimmedText() : AbstractBody() {
    var node = Text()

    operator fun String.unaryPlus() {
        node = Text(this)
    }

    override fun render(): Text {
        var str = node.text
        if (str.length > 10) {
            str = str.substring(0, 10)
        }
        node.text = str
        return node
    }
}

class NumberText(numPrinted: String) : AbstractBody() {
    var node = Text(numPrinted)

    override fun render(): Text {
        node.styleClass += Style.CALL_TREE_NUMBER.cssClasses
        return node
    }
}

class SeparatorText(separator: Separator) : AbstractBody() {
    var node = Text(separator.text)

    override fun render(): Text {
        node.styleClass += Style.CALL_TREE_NODE_SEPARATOR.cssClasses
        return node
    }
}

class TypeText(type: Type) : AbstractBody() {
    var node = Text(type.name)

    override fun render(): Text {
        node.styleClass += Style.CALL_TREE_TYPE_NAME.cssClasses
        return node
    }
}

class NullText : AbstractBody() {
    var node = Text("null")

    override fun render(): Text {
        node.styleClass += Style.CALL_TREE_NULL.cssClasses
        return node
    }
}

fun flow(init: Flow.() -> Unit): List<Node> {
    val flow = Flow()
    flow.init()
    return flow.build()
}

fun main() {
    val flow = flow {
        num(5)
    }

    println(flow)
}