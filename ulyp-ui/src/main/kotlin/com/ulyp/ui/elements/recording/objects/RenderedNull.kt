package com.ulyp.ui.elements.recording.objects

import com.ulyp.ui.elements.render.flow

class RenderedNull internal constructor() : RenderedObject() {

    init {
        children += flow {
            nul()
        }
    }
}