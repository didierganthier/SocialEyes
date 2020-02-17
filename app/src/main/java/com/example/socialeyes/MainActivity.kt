package com.example.socialeyes

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.socialeyes.Fragments.HomeFragment
import com.example.socialeyes.Fragments.NotificationsFragment
import com.example.socialeyes.Fragments.ProfileFragment
import com.example.socialeyes.Fragments.SearchFragment

class MainActivity : AppCompatActivity() {

     internal var selectedFragemtnt: Fragment = HomeFragment();

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home ->
            {
                selectedFragemtnt = HomeFragment()
            }
            R.id.nav_search ->
            {
                selectedFragemtnt = SearchFragment()
            }
            R.id.nav_add_post ->
            {
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_notifications ->
            {
                selectedFragemtnt = NotificationsFragment()
            }
            R.id.nav_profile ->
            {
                selectedFragemtnt = ProfileFragment()
            }
        }

        if(selectedFragemtnt != null)
        {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragemtnt).commit();
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
    }
}
