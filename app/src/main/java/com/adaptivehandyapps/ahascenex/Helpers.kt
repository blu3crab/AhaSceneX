//
// Created by MAT on 3/24/2020.
//
package com.adaptivehandyapps.ahascenex

import com.adaptivehandyapps.ahascenex.model.StageModel

fun formatStageModel(stageModel: StageModel?, terse: Boolean = false): String {
    stageModel?.let {
        if (terse) {
            return "stageModel nickname# " + stageModel.nickname + " label " + stageModel.label
        }
        return "stageModel id# " + stageModel.nickname + " = " + stageModel.label +
                ", type " + stageModel.type + ", uri " + stageModel.sceneSrcUrl
    }
    return "stageModel NULL... "
}
