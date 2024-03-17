package com.jki.hananeelcinta.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentFullscreenImageViewerBinding
import com.jki.hananeelcinta.home.weeklyreflection.DetailWeeklyReflectionFragment
import com.jki.hananeelcinta.home.weeklyreflection.DetailWeeklyReflectionViewModel

class ImageViewerFragment : DialogFragment() {

    companion object {
        fun newInstance() = ImageViewerFragment()
        const val IMAGE_URL = "imageUrl"
    }

    private lateinit var binding: FragmentFullscreenImageViewerBinding

    private var imageUrl: String = ""

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.filterDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_fullscreen_image_viewer,
            container,
            false
        )

        getArgument()

        return binding.root
    }

    private fun getArgument() {
        val args = arguments
        if (args != null) {
            imageUrl = args.getString(IMAGE_URL)!!
            setupLayout()
        }
    }

    private fun setupLayout() {
        if (imageUrl.isNotEmpty()) {
            activity?.applicationContext?.let {
                Glide.with(it)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.ivImage)
            }
        }

        binding.btnClose.setOnClickListener { dismiss() }
        binding.lyImageViewer.setOnClickListener { dismiss() }
    }

}