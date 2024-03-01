package com.jki.hananeelcinta.home.weeklyreflection

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentDetailWeeklyReflectionBinding
import com.jki.hananeelcinta.model.PastorMessage
import com.jki.hananeelcinta.model.Role
import com.jki.hananeelcinta.util.UIHelper
import com.jki.hananeelcinta.util.UserConfiguration

class DetailWeeklyReflectionFragment : DialogFragment() {

    private lateinit var binding: FragmentDetailWeeklyReflectionBinding
    private lateinit var detailMessage: PastorMessage

    private val isAdmin =
        UserConfiguration.getInstance().getUserData()?.role.equals(Role.SUPERUSER.role)
    private var databaseReference = FirebaseDatabase.getInstance().getReference("pastorMessages")

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
        if (isAdmin) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                UIHelper.getInstance().displayConfirmation(
                    resources.getString(R.string.confirm_delete_pastor_message),
                    resources.getString(R.string.confirm_delete_pastor_message_desc),
                    activity,
                    { },
                    {
                        deleteReflection()
                    },
                    resources.getString(R.string.text_confirmation_no),
                    resources.getString(R.string.text_confirmation_yes_),
                    R.drawable.question,
                    true
                )
            }
        } else {
            binding.btnDelete.visibility = View.GONE
        }
    }

    private fun deleteReflection() {
        databaseReference.child(detailMessage.date.toString()).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    UIHelper.getInstance().displaySuccessDialog(
                        resources.getString(R.string.success_delete_pastor_message),
                        null,
                        activity,
                        {
                            dismiss()
                        },
                        resources.getString(R.string.OK),
                        false,
                    )
                } else {
                    Toast.makeText(context, "Gagal menghapus renungan.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}