///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 3/24/2020.
//
package com.adaptivehandyapps.ahascenex

import com.adaptivehandyapps.ahascenex.craft.CraftTouch
import com.adaptivehandyapps.ahascenex.model.PropModel
import com.adaptivehandyapps.ahascenex.model.StageModel

fun formatStageModel(stageModel: StageModel?, terse: Boolean = false): String {
    stageModel?.let {
        if (terse) {
            return "stageModel tableId# " + stageModel.tableId +
                    ", nickname# " + stageModel.nickname + " label " + stageModel.label
        }
        return "stageModel tableId# " + stageModel.tableId +
                ", nickname " + stageModel.nickname + " = " + stageModel.label +
                ", type " + stageModel.type + ", uri " + stageModel.sceneSrcUrl +
                "\n scene scale = " + stageModel.sceneScale + ", scene x/y = " + stageModel.sceneX + "/" + stageModel.sceneY
    }
    return "stageModel NULL... "
}

fun formatPropModel(propModel: PropModel?, terse: Boolean = false): String {
    propModel?.let {
        if (terse) {
            return "propModel tableId# " + propModel.tableId + ", nickname# " + propModel.nickname + " label " + propModel.label
        }
        return "propModel tableId# " + propModel.tableId + ", nickname# " +
                propModel.nickname + " = " + propModel.label + ", type " + propModel.type +
                "\n res id " + propModel.propResId + ", stage nickname " + propModel.stageNickname +
                "\n prop scale = " + propModel.propScale + ", prop x/y = " + propModel.propX + "/" + propModel.propY
    }
    return "propModel NULL... "
}

fun formatScalePivot(scalePivot: CraftTouch.ScalePivot?): String {
    scalePivot?.let {
        return "ScalePivot->scale ${scalePivot.scale}, x/y -> ${scalePivot.x}/${scalePivot.y}"
    }
    return "scalePivot NULL... "
}
