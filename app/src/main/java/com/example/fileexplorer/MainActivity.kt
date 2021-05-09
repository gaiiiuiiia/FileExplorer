package com.example.fileexplorer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.example.fileexplorer.fragments.CardFragment
import com.example.fileexplorer.fragments.HomeFragment
import com.example.fileexplorer.fragments.InternalFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open_Drawer, R.string.Close_Drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setStartFragment(navigationView)
    }

    private fun setStartFragment(navigationView: NavigationView)
    {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .addToBackStack(null)
            .commit()
        navigationView.setCheckedItem(R.id.nav_home)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId) {
            R.id.nav_home -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .addToBackStack(null).
                    commit()
            }
            R.id.nav_internal -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, InternalFragment())
                    .addToBackStack(null).
                    commit()
            }
            R.id.nav_card -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, CardFragment())
                    .addToBackStack(null).
                    commit()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed()
    {
        supportFragmentManager.popBackStackImmediate()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}