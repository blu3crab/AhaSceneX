///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 28FEB2020.
//
package com.adaptivehandyapps.ahascenex.model

import android.net.Uri
import android.os.Parcelable
import android.widget.ImageView
import kotlinx.android.parcel.Parcelize
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO: re-factor StageType & PropType
enum class PropType(val value: String) {
    SCENE_TYPE("scene"),
    PROP_TYPE("prop"),
    ICON_TYPE("icon"),
    ALL_TYPE("all")
}

@Parcelize
@Entity(tableName = "prop_model_table")
data class PropModel(
    @PrimaryKey(autoGenerate = true)
    var tableId: Long = 0L,

    @ColumnInfo(name = "prop_res_id")
    var propResId: Int = 0,
    @ColumnInfo(name = "stage_id")
    var stageId: Long = 0L,

    @ColumnInfo(name = "nickname")
    var nickname: String = "nada",
    @ColumnInfo(name = "type")
    var type: String = PropType.PROP_TYPE.toString(),
    @ColumnInfo(name = "label")
    var label: String = "nada",

    @ColumnInfo(name = "prop_scale")
    var propScale: Float = 0.0F,
    @ColumnInfo(name = "prop_x")
    var propX: Float = 0.0F,
    @ColumnInfo(name = "prop_y")
    var propY: Float = 0.0F) : Parcelable {

    val isProp
        get() = type == StageType.PROP_TYPE.value
}
