package com.jki.hananeelcinta.home.weeklyreflection

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentDetailWeeklyReflectionBinding
import com.jki.hananeelcinta.model.PastorMessage

class DetailWeeklyReflectionFragment : DialogFragment() {

    private lateinit var binding: FragmentDetailWeeklyReflectionBinding
    private lateinit var detailMessage: PastorMessage

    companion object {
        fun newInstance() = DetailWeeklyReflectionFragment()
        const val DETAIL_MESSAGE = "detailMessage"
    }

    private lateinit var viewModel: DetailWeeklyReflectionViewModel

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
        viewModel =
            ViewModelProvider(requireActivity())[DetailWeeklyReflectionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detail_weekly_reflection,
            container,
            false
        )

        getArgument()

        return binding.root
    }

    private fun getArgument() {
        val args = arguments
        if (args != null) {
            detailMessage = args.getParcelable(DETAIL_MESSAGE)!!
            setupLayout()
        }
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { dismiss() }
        binding.tvTitle.text = detailMessage.title
        binding.tvWriter.text = "By His Grace \n" + detailMessage.writer
        binding.tvMessage.text = detailMessage.messages
    }
}