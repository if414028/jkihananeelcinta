package com.jki.hananeelcinta.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityMainBinding
import com.jki.hananeelcinta.databinding.ItemMenuBinding
import com.jki.hananeelcinta.home.weeklyreflection.DetailWeeklyReflectionFragment
import com.jki.hananeelcinta.profile.ProfileActivity
import com.jki.hananeelcinta.reflection.ReflectionActivity
import com.jki.hananeelcinta.services.ServicesActivity
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter
import com.jki.hananeelcinta.util.UserConfiguration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: SimpleRecyclerAdapter<ModuleView>
    private val moduleList = arrayListOf<ModuleView>()

    private lateinit var weeklyReflectionFragment: DetailWeeklyReflectionFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()

        setupLayout()
    }

    private fun setupLayout() {
        setupModuleView()
        setupMenuRecyclerView()
        setupWeeklyReflection()

        binding.ivProfile.setOnClickListener {
            val intent = Intent(applicationContext, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.tvUsername.text = UserConfiguration.getInstance().getUserData()!!.username + "!"
    }

    private fun setupMenuRecyclerView() {
        binding.rvMenu.layoutManager = GridLayoutManager(applicationContext, 3)
        adapter = SimpleRecyclerAdapter<ModuleView>(
            moduleList,
            R.layout.item_menu
        ) { holder, item ->
            val itemBinding: ItemMenuBinding = holder.layoutBinding as ItemMenuBinding
            itemBinding.icMenu.setImageDrawable(resources.getDrawable(item.moduleIcon))
            itemBinding.tvMenu.text = item.moduleName
            itemBinding.root.setOnClickListener {
                val intent: Intent = when (item.moduleName) {
                    resources.getString(R.string.services) ->
                        Intent(applicationContext, ServicesActivity::class.java)

                    else -> Intent(applicationContext, ReflectionActivity::class.java)
                }
                startActivity(intent)
            }
        }
        binding.rvMenu.adapter = adapter
        binding.rvMenu.isNestedScrollingEnabled = false
    }

    private fun setupModuleView() {
        val servicesModule =
            ModuleView(resources.getString(R.string.services), R.drawable.ic_church)
        val eventModule =
            ModuleView(resources.getString(R.string.reflection), R.drawable.ic_light)
        val churchServiceScheduleModule =
            ModuleView(resources.getString(R.string.reflection), R.drawable.ic_light)
        val givingModule =
            ModuleView(resources.getString(R.string.reflection), R.drawable.ic_light)

        moduleList.add(servicesModule)
        moduleList.add(eventModule)
        moduleList.add(churchServiceScheduleModule)
        moduleList.add(givingModule)
    }

    private fun setupWeeklyReflection() {
        binding.btnWeeklyReflectionDetail.setOnClickListener {
            weeklyReflectionFragment = DetailWeeklyReflectionFragment.newInstance()
            weeklyReflectionFragment.show(supportFragmentManager, "weekly_reflection_detail")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}