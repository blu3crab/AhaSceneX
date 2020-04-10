///////////////////////////////////////////////////////////////////////////
// StageCraft: the ART of creating compelling ILLUSIONS
//
// Created by MAT on 28FEB2020.
//
package com.adaptivehandyapps.ahascenex.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StageType(val value: String) {
    SCENE_TYPE("scene"),
    PROP_TYPE("prop"),
    ICON_TYPE("icon"),
    ALL_TYPE("all")
}

@Parcelize
@Entity(tableName = "stage_model_table")
data class StageModel(
    @PrimaryKey(autoGenerate = true)
    var tableId: Long = 0L,

    @ColumnInfo(name = "nickname")
    var nickname: String = "nada",
//        @Json(name = "img_src") val imgSrcUrl: String,
//    var sceneSrcUrl: Uri? = null,
    @ColumnInfo(name = "scene_src_url")
    var sceneSrcUrl: String = "nada",
    @ColumnInfo(name = "type")
    var type: String = "nada",
    @ColumnInfo(name = "label")
    var label: String = "nada",
    @ColumnInfo(name = "scene_scale")
    var sceneScale: Float = 0.0F,
    @ColumnInfo(name = "scene_x")
    var sceneX: Float = 0.0F,
    @ColumnInfo(name = "scene_y")
    var sceneY: Float = 0.0F) : Parcelable {

    val isScene
        get() = type == StageType.SCENE_TYPE.value
        //get() = type == "scene"
}
