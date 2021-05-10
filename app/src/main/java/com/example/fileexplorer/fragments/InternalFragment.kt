package com.example.fileexplorer.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fileexplorer.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException

class InternalFragment: Fragment(), OnFileSelectedListener
{
    private lateinit var recyclerView: RecyclerView
    private lateinit var fileList: List<File>
    private lateinit var img_back: ImageView
    private lateinit var tv_pathHolder: TextView
    private lateinit var fileAdapter: FileAdapter

    private lateinit var myView: View

    lateinit var storage: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myView = inflater.inflate(R.layout.fragment_internal, container, false)

        tv_pathHolder = myView.findViewById(R.id.tv_pathHolder)
        img_back = myView.findViewById(R.id.img_back)

        storage = getStorage(arguments)

        runtimePermission()

        tv_pathHolder.text = storage.absolutePath

        return myView
    }

    private fun getStorage(arguments: Bundle?): File
    {
        var storage = File(System.getenv("EXTERNAL_STORAGE"))

        arguments?.let {
            if (arguments.getString("path") != null) {
                storage = File(arguments.getString("path"))
            }
        }

        return storage
    }

    private fun runtimePermission()
    {
        Dexter.withContext(context).withPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                displayFiles()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }
        }).check()
    }

    private fun displayFiles()
    {
        recyclerView = myView.findViewById(R.id.recycler_internal)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        fileList = ArrayList()
        (fileList as ArrayList<File>).addAll(findFiles(storage))
        fileAdapter = FileAdapter(context!!, fileList, this)
        recyclerView.adapter = fileAdapter
    }

    private fun findFiles(file: File): ArrayList<File>
    {
        val arrayList: ArrayList<File> = ArrayList()
        val files: Array<File>? = file.listFiles()
        files?.let {
            for (singleFile: File in files) {
                if (singleFile.isDirectory && !singleFile.isHidden) {
                    arrayList.add(singleFile)
                }
            }

            for (singleFile: File in files) {
                if (FileAllowManager.isAllowedImage(singleFile.name)) {
                    arrayList.add(singleFile)
                }
            }
        }

        return arrayList
    }

    override fun onFileClicked(file: File)
    {
        if (file.isDirectory) {
            val bundle = Bundle()
            bundle.putString("path", file.absolutePath)
            val internalFragment = InternalFragment()
            internalFragment.arguments = bundle
            fragmentManager!!
                .beginTransaction()
                .replace(R.id.fragment_container, internalFragment)
                .addToBackStack(null)
                .commit()
        } else {
            try {
                FileOpener.openFile(context!!, file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onFileLongClicked(file: File)
    {
        val optionDialog = Dialog(context!!)
        optionDialog.setContentView(R.layout.option_dialog)
        optionDialog.setTitle("Select Options")
        val options: ListView = optionDialog.findViewById(R.id.list)
        options.adapter = OptionListAdapter()
        optionDialog.show()
    }

    inner class OptionListAdapter: BaseAdapter()
    {
        private var options = arrayOf(
            "Details", "Rename", "Delete", "Share"
        )

        override fun getCount(): Int
        {
            return options.size
        }

        override fun getItem(position: Int): Any
        {
            return options[position]
        }

        override fun getItemId(position: Int): Long
        {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
        {
            val myView = layoutInflater.inflate(R.layout.option_layout, null)
            val txtOption: TextView = myView.findViewById(R.id.txt_option)
            val imgOption: ImageView = myView.findViewById(R.id.img_option)
            txtOption.text = options[position]
            when {
                options[position] == "Details" -> {
                    imgOption.setImageResource(R.drawable.ic_details)
                }
                options[position] == "Rename" -> {
                    imgOption.setImageResource(R.drawable.ic_edit)
                }
                options[position] == "Delete" -> {
                    imgOption.setImageResource(R.drawable.ic_delete)
                }
                options[position] == "Share" -> {
                    imgOption.setImageResource(R.drawable.ic_share)
                }
            }

            return myView
        }
    }
}