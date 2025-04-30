package com.skd.githubsearch.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.skd.githubsearch.R
import com.skd.githubsearch.adapter.RepoAdapter
import com.skd.githubsearch.constants.StringConstants
import com.skd.githubsearch.databinding.ActivityMainDashboardBinding
import com.skd.githubsearch.viewModel.MainViewModel

class MainDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainDashboardBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: RepoAdapter
    private var isFirstLaunch = true

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isFirstLaunch = savedInstanceState == null
        if (isFirstLaunch) {
            binding.activityProgressBar.isVisible = true
        }
        uiView()
        initSwitch()
        notification()
        getAdapterData()
    }

    private fun uiView(){
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            // Only use bottom inset from keyboard if keyboard is visible, otherwise use system bar
            val bottomInset = if (imeVisible) imeInsets.bottom else systemBars.bottom
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset)
            insets
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initSwitch() {
        val themeSwitch: Switch = binding.themeSwitch
        // Checking device dark mode is enabled or not
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        themeSwitch.isChecked = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Listen for changes to the switch
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun notification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun getAdapterData() {
        // Set initial visibility for ProgressBar to visible
        binding.activityProgressBar.visibility = View.VISIBLE

        adapter = RepoAdapter(
            onClick = { repo ->
                if (isNetworkAvailable(this)) {
                    val dialogView = layoutInflater.inflate(R.layout.webview_layout, null)
                    val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
                    val webView = dialogView.findViewById<WebView>(R.id.webView)
                    webView.webViewClient = WebViewClient()
                    webView.settings.javaScriptEnabled = true
                    webView.loadUrl(repo.repoURL)
                    val dialog = AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton(resources.getString(R.string.lbl_close)) { d, _ -> d.dismiss() }
                        .create()

                    dialog.setOnShowListener {
                        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        val isDarkTheme = when (AppCompatDelegate.getDefaultNightMode()) {
                            AppCompatDelegate.MODE_NIGHT_YES -> true
                            AppCompatDelegate.MODE_NIGHT_NO -> false
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                                val currentNightMode = resources.configuration.uiMode and
                                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                                currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                            }
                            else -> false
                        }

                        // Set the button text color based on dark mode
                        if (isDarkTheme) {
                            positiveButton.setTextColor(resources.getColor(android.R.color.white, theme))
                            progressBar.indeterminateDrawable.setColorFilter(
                                resources.getColor(android.R.color.white, theme),
                                android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        } else {
                            positiveButton.setTextColor(resources.getColor(android.R.color.black, theme))
                            progressBar.indeterminateDrawable.setColorFilter(
                                resources.getColor(android.R.color.black, theme),
                                android.graphics.PorterDuff.Mode.SRC_IN
                            )
                        }
                    }
                    dialog.show()

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            progressBar.isVisible = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            progressBar.isVisible = false
                        }
                    }

                    createNotification(repo.repoURL)
                } else {
                    Toast.makeText(this, resources.getString(R.string.lbl_check_internet_connection), Toast.LENGTH_SHORT).show()
                }
            },
            onDataLoaded = {
                binding.activityProgressBar.isVisible = false
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    viewModel.search(query).observe(this@MainDashboardActivity, Observer { repos ->
                        adapter.submitList(repos)
                    })
                } else {
                    viewModel.search(query).observe(this@MainDashboardActivity, Observer { repos ->
                        adapter.submitList(repos)
                    })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewModel.repos.observe(this) { repos ->
            if (repos.isEmpty()) {
                // Data is empty, so we need to fetch it from the API
                viewModel.refreshRepos()
            } else {
                // Data is available locally, hide the ProgressBar
                binding.activityProgressBar.visibility = View.GONE
                adapter.submitList(repos)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                // Show ProgressBar while data is being fetched
                binding.activityProgressBar.visibility = View.VISIBLE
            } else {
                // Hide ProgressBar once data is fetched
                binding.activityProgressBar.visibility = View.GONE
            }
        }

        if (isNetworkAvailable(this)) {
            viewModel.refreshRepos()
        } else if (isFirstLaunch) {
            Toast.makeText(this, resources.getString(R.string.lbl_show_local_data), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, resources.getString(R.string.lbl_notification_permission), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, resources.getString(R.string.lbl_notification_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun createNotification(url: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = StringConstants.CHANNEL_ID
            val channelName = StringConstants.CHANNEL_NAME
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // for build notification
        val notification = NotificationCompat.Builder(this, StringConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.github_logo)
            .setContentTitle(resources.getString(R.string.lbl_repo_url))
            .setContentText("URL: $url")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // to show the notification
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManagerCompat.notify(1, notification)
    }
}