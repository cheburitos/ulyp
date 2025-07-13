package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.elements.render.Separator
import com.ulyp.ui.elements.render.flow
import com.ulyp.ui.elements.render.toSeparator
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText

class RenderedNumber(numberPrinted: String, type: Type, renderSettings: RenderSettings) : RenderedObject() {

    init {
        children += flow {
            if (renderSettings.showTypes) {
                +type
                +": ".toSeparator()
            }
            num(numberPrinted)
        }
    }
}