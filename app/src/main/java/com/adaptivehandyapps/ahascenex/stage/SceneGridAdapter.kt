/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adaptivehandyapps.ahascenex.stage

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adaptivehandyapps.ahascenex.databinding.GridViewItemBinding
import com.adaptivehandyapps.ahascenex.model.StageModel

class SceneGridAdapter (private val onClickListener: OnClickListener):
        ListAdapter<StageModel,
                SceneGridAdapter.SceneViewHolder>(DiffCallback) {

    class SceneViewHolder(private var binding: GridViewItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(stageModel: StageModel) {
            Log.d("SceneViewHolder", "bind...")
            binding.stageModel = stageModel
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StageModel>() {
        override fun areItemsTheSame(oldItem: StageModel, newItem: StageModel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: StageModel, newItem: StageModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SceneViewHolder {
        return SceneViewHolder(GridViewItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: SceneViewHolder, position: Int) {
        val stageModel = getItem(position)
        holder.itemView.setOnClickListener { onClickListener.onClick(stageModel) }
        holder.bind(stageModel)
    }

    class OnClickListener(val clickListener: (stageModel:StageModel) -> Unit) {
        fun onClick(stageModel:StageModel) = clickListener(stageModel)
    }
}




