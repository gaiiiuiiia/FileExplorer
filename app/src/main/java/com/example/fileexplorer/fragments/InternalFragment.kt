package com.example.fileexplorer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fileexplorer.FileAdapter
import com.example.fileexplorer.FileAllowManager
import com.example.fileexplorer.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File

class InternalFragment: Fragment()
{
    private lateinit var recyclerView: RecyclerView
    private lateinit var fileList: List<File>
    private lateinit var img_back: ImageView
    private lateinit var tv_pathHolder: TextView
    private lateinit var fileAdapter: FileAdapter

    lateinit var storage: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_internal, container, false)

        tv_pathHolder = view.findViewById(R.id.tv_pathHolder)
        img_back = view.findViewById(R.id.img_back)

        val internalStorage = System.getenv("EXTERNAL_STORAGE")  // внутреннее хранилище
        storage = File(internalStorage)

        runtimePermission()

        tv_pathHolder.text = storage.absolutePath

        return view
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
        recyclerView = view!!.findViewById(R.id.recycler_internal)
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
        val files: Array<File> = file.listFiles()!!

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

        return arrayList
    }
}