package com.jairaj.janglegmail.motioneye.activities.MainActivity

import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.AddDeviceDetailsActivity
import com.jairaj.janglegmail.motioneye.activities.BaseActivity
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.RecyclerViewItemTouchHelper
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.checkAndAutoOpenIfOnlyOneCam
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.fetchDataAndDisplayList
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.resetActionbarState
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.toggleListReorder
import com.jairaj.janglegmail.motioneye.activities.MainActivity.utils.itemCheckedCountInDeviceList
import com.jairaj.janglegmail.motioneye.activities.WebMotionEyeActivity
import com.jairaj.janglegmail.motioneye.databinding.ActivityMainBinding
import com.jairaj.janglegmail.motioneye.dataclass.CamDevice
import com.jairaj.janglegmail.motioneye.helpers.onOptionsItemSelectedListener
import com.jairaj.janglegmail.motioneye.utils.AppUtils.displayMainActivityTutorial
import com.jairaj.janglegmail.motioneye.utils.AppUtils.isFirstTimeAppOpened
import com.jairaj.janglegmail.motioneye.utils.AppUtils.isFirstTimeDevice
import com.jairaj.janglegmail.motioneye.utils.AppUtils.isFirstTimeDrive
import com.jairaj.janglegmail.motioneye.utils.AppUtils.runWhenReady
import com.jairaj.janglegmail.motioneye.utils.AppUtils.showRateDialog
import com.jairaj.janglegmail.motioneye.utils.Constants
import com.jairaj.janglegmail.motioneye.utils.Constants.DATA_IS_DRIVE_ADDED
import com.jairaj.janglegmail.motioneye.utils.Constants.EDIT
import com.jairaj.janglegmail.motioneye.utils.Constants.LABEL
import com.jairaj.janglegmail.motioneye.utils.Constants.ServerMode
import com.jairaj.janglegmail.motioneye.utils.DataBaseHelper

class MainActivity : BaseActivity() {
    private lateinit var instance: MainActivity
    internal val logTAG = MainActivity::class.java.name
    internal lateinit var binding: ActivityMainBinding
    internal lateinit var dataBaseHelper: DataBaseHelper
    internal var shortcutManager: ShortcutManager? = null
    private lateinit var touchHelper: ItemTouchHelper

    //Flag to store state of ListView device_list' items: checked or not checked
    internal var isListViewCheckboxEnabled = false
    internal var isReorderingEnabled = false

    // UI Elements
    internal var buttonDelete: MenuItem? = null
    internal var buttonEdit: MenuItem? = null
    internal var buttonReorderList: MenuItem? = null
    internal var buttonApplyListOrder: MenuItem? = null
    internal var buttonCancelListOrder: MenuItem? = null

    internal var actionAbout: MenuItem? = null
    internal var actionHelpFaq: MenuItem? = null
    internal var actionSettings: MenuItem? = null

    internal val deviceList: MutableList<CamDevice> = mutableListOf()

    // Target Drive Icon Res Id to display Tutorial for
    internal var tutorialTargetDriveIcon: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdgeAppBar(binding.appBarLayout2)

        instance = this

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitle(R.string.motioneye_servers)

        binding.toolbar.setOnMenuItemClickListener { item ->
            onOptionsItemSelectedListener(item)
            true
        }

        binding.deviceListRv.setHasFixedSize(true)

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.deviceListRv.layoutManager = llm

        try {
            shortcutManager = this.getSystemService(ShortcutManager::class.java)
        } catch (e: Exception) {
            Log.e(logTAG, "Exception in getting ShortcutManager service: $e")
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPress()
            }
        })

        // init DataBase object
        dataBaseHelper = DataBaseHelper(this)

        //Insert Preview status column in Data Base if the previous version of app didn't have it
        //TODO: Find if there is better way to handle SQL Table column addition over previous app version
        dataBaseHelper.insertNewColumn()

        showRateDialog(this, false)

        //If this is the first run of the app show tutorial
        if (isFirstTimeAppOpened(this)) {
            displayMainActivityTutorial(
                this,
                Constants.DisplayTutorialMode.FirstTimeAppOpened
            )
        }

        binding.fab.setOnClickListener {
            gotoAddDeviceDetail(Constants.EDIT_MODE_NEW_DEV)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                binding.fab.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
        }

        //Handler to handle data fetching from SQL in BG
        fetchDataAndDisplayList()

        // Check if there is only one camera, auto open the camera if auto open setting is enabled
        checkAndAutoOpenIfOnlyOneCam()

        val recyclerViewItemTouchCallback = RecyclerViewItemTouchHelper(binding)
        touchHelper = ItemTouchHelper(recyclerViewItemTouchCallback)

        toggleListReorder(false)
    }

    internal fun setLongTouchToReorder(enable: Boolean) {
        touchHelper.attachToRecyclerView(
            if (enable)
                binding.deviceListRv
            else
                null
        )
    }

    internal fun goToWebMotionEye(label: String, urlPort: String?, @ServerMode mode: Int) {
        Log.d(logTAG, "In goToWebMotionEye(...)")

        val bundle = Bundle()
        bundle.putString(Constants.KEY_LABEL, label)
        bundle.putString(Constants.KEY_URL_PORT, urlPort)
        bundle.putInt(Constants.KEY_MODE, mode)

        val intentWebMotionEyeActivity = Intent(this@MainActivity, WebMotionEyeActivity::class.java)
        intentWebMotionEyeActivity.putExtras(bundle)

        startActivity(intentWebMotionEyeActivity)
    }

    internal fun gotoAddDeviceDetail(editMode: Int) {
        Log.d(logTAG, "In gotoAddDeviceDetail(editMode = $editMode)")

        var deleteLabel = ""
        val bundle = Bundle()

        if (editMode == Constants.EDIT_MODE_EXIST_DEV) {
            for (deviceView in binding.deviceListRv.children) {
                val checkbox = deviceView.findViewById<CheckBox>(R.id.checkBox)

                if (checkbox.isChecked) {
                    deleteLabel = (deviceView.findViewById<View>(R.id.title_label_text)
                            as TextView).text.toString()
                    break
                }
            }

            bundle.putString(LABEL, deleteLabel)
        }

        resetActionbarState()

        bundle.putInt(EDIT, editMode)
        val intentForAddDevice = Intent(this, AddDeviceDetailsActivity::class.java)
            .apply {
                putExtras(bundle)
            }
        resultLauncher.launch(intentForAddDevice)
        Log.d(logTAG, "opening Add device!!!")
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        resetActionbarState()

        fetchDataAndDisplayList()

        binding.deviceListRv.post {
            if (
                result.resultCode == Constants.DEVICE_ADDITION_CANCELLED_RESULT_CODE
                || result.resultCode != Constants.DEVICE_ADDITION_DONE_RESULT_CODE
            ) {
                return@post
            }

            val isDriveAdded = result.data?.extras?.getBoolean(
                DATA_IS_DRIVE_ADDED, false
            ) ?: false

            val flagIsFirstDevice: Boolean = isFirstTimeDevice(this)

            var flagIsFirstDrive = false
            if (isDriveAdded) {
                flagIsFirstDrive = isFirstTimeDrive(this)
            }

            Log.d(logTAG, "flagIsFirstDevice = $flagIsFirstDevice")
            Log.d(logTAG, "flagIsFirstDrive = $flagIsFirstDrive")

            binding.deviceListRv.runWhenReady {
                Log.i(logTAG, "Device List Recycler View is Ready")
                if (flagIsFirstDevice && !flagIsFirstDrive) {
                    displayMainActivityTutorial(
                        this,
                        Constants.DisplayTutorialMode.FirstTimeDeviceAdded
                    )
                } else if (!flagIsFirstDevice && flagIsFirstDrive) {
                    displayMainActivityTutorial(
                        this,
                        Constants.DisplayTutorialMode.NotFirstTimeForDeviceAdditionButFirstTimeForDrive
                    )
                } else if (flagIsFirstDevice && flagIsFirstDrive)
                    displayMainActivityTutorial(
                        this,
                        Constants.DisplayTutorialMode.FirstTimeForDeviceAdditionAsWellAsDrive
                    )
            }
        }
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_cam, menu)
        buttonDelete = menu.findItem(R.id.delete)
        buttonEdit = menu.findItem(R.id.edit)
        buttonReorderList = menu.findItem(R.id.reorder_list)
        buttonApplyListOrder = menu.findItem(R.id.apply_list_order)
        buttonCancelListOrder = menu.findItem(R.id.cancel_list_order)

        actionAbout = menu.findItem(R.id.action_about)
        actionHelpFaq = menu.findItem(R.id.action_help)
        actionSettings = menu.findItem(R.id.action_settings)

        return true
    }

    internal fun deleteData(delLabel: String) {
        val deletedRows = dataBaseHelper.deleteData(delLabel)

        if (deletedRows <= 0) {
            Log.e(logTAG, "Failed to delete device with label = $delLabel")
            Toast.makeText(this@MainActivity, "Failed to delete", Toast.LENGTH_LONG).show()
        }
    }

    fun backPress() {
        // If neither edit/delete mode nor reordering is enabled, exit the app
        if (itemCheckedCountInDeviceList == 0 && !isReorderingEnabled) {
            finish()
            return
        }

        // Otherwise, reset the edit/delete and reordering mode
        resetActionbarState()
    }
}
