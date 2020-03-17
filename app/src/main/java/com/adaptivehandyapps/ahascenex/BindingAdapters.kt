//
// Created by MAT on 3/16/2020.
//
package com.adaptivehandyapps.ahascenex

import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adaptivehandyapps.ahascenex.model.StageModel
import com.adaptivehandyapps.ahascenex.stage.SceneGridAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<StageModel>?) {
    Log.d("BindingAdapter", "scenex listData size " + data?.size)
    val adapter = recyclerView.adapter as SceneGridAdapter
    adapter.submitList(data)
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    Log.d("BindingAdapter", "scenex imageUrl...")
    imgUrl?.let {
//        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Log.d("BindingAdapter", "imgUrl->" + imgUrl)
        val imgUri = imgUrl.toUri()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}
